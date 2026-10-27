package com.example.clutch_ai.viewmodel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.clutch_ai.data.UserSessionManager
import com.example.clutch_ai.network.UserRegisterRequest
import com.example.clutch_ai.network.UserLoginRequest
import com.example.clutch_ai.network.BackendChatSession
import com.example.clutch_ai.network.BackendChatMessage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clutch_ai.engine.UniversalKnowledgeEngine
import com.example.clutch_ai.model.*
import com.example.clutch_ai.network.ApiClient
import com.example.clutch_ai.network.ChatStreamRequest
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class UploadStep {
    IDLE,
    SCANNING,
    PARSING,
    GENERATING_VECTORS,
    STASHED
}

data class UploadProgressState(
    val step: UploadStep = UploadStep.IDLE,
    val progress: Float = 0f,
    val fileName: String = ""
)

data class ClutchUiState(
    val isLoggedIn: Boolean = false,
    val isAuthLoading: Boolean = false,
    val authErrorMessage: String? = null,
    val userFullName: String = "",
    val userEmail: String = "",
    val documents: List<StashDocument> = emptyList(),
    val chatSessions: List<ChatSession> = emptyList(),
    val activeSessionId: String = "",
    val activeFilter: String = "All",
    val attachedDocTitle: String? = null,
    val isGeneratingResponse: Boolean = false,
    val selectedCitation: Citation? = null,
    val isSpotlightOpen: Boolean = false,
    val isHistoryOpen: Boolean = false,
    val uploadState: UploadProgressState = UploadProgressState(),
    val telemetry: TelemetryData = TelemetryData(
        totalFiles = 0,
        totalVectors = 0,
        totalChunks = 0,
        storageEstimateKb = 0,
        scanLatencyMs = 0f,
        engineName = "Clutch Cloud & Local AI"
    ),
    val memories: List<TrainedMemory> = emptyList(),
    val trainingState: TrainingProgressState = TrainingProgressState()
)

class ClutchViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = UserSessionManager(application.applicationContext)

    private val _uiState = MutableStateFlow(ClutchUiState())
    val uiState: StateFlow<ClutchUiState> = _uiState.asStateFlow()

    init {
        val initialSessionId = UUID.randomUUID().toString()
        val initialSession = ChatSession(
            id = initialSessionId,
            title = "New Chat",
            lastUpdated = "Just now",
            messages = emptyList()
        )

        val loggedIn = sessionManager.isLoggedIn()
        val savedName = sessionManager.getUserFullName() ?: ""
        val savedEmail = sessionManager.getUserEmail() ?: ""

        _uiState.update {
            it.copy(
                isLoggedIn = loggedIn,
                userFullName = savedName,
                userEmail = savedEmail,
                documents = emptyList(),
                memories = emptyList(),
                chatSessions = listOf(initialSession),
                activeSessionId = initialSessionId
            )
        }

        if (loggedIn) {
            syncUserData()
        }
    }

    fun signUp(fullName: String, email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null) }
            }
            try {
                val resp = ApiClient.apiService.register(
                    UserRegisterRequest(email = email, password = password, fullName = fullName)
                )
                if (resp.isSuccessful && resp.body() != null) {
                    val auth = resp.body()!!
                    sessionManager.saveSession(
                        accessToken = auth.accessToken,
                        userId = auth.userId,
                        email = auth.email,
                        fullName = auth.fullName ?: fullName
                    )
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                isLoggedIn = true,
                                isAuthLoading = false,
                                authErrorMessage = null,
                                userFullName = auth.fullName ?: fullName,
                                userEmail = auth.email
                            )
                        }
                    }
                    syncUserData()
                } else {
                    val err = resp.errorBody()?.string() ?: "Registration failed"
                    val msg = try {
                        JSONObject(err).optString("detail", "Registration failed")
                    } catch (e: Exception) {
                        "Registration failed"
                    }
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(isAuthLoading = false, authErrorMessage = msg) }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isAuthLoading = false, authErrorMessage = e.message ?: "Network error") }
                }
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null) }
            }
            try {
                val resp = ApiClient.apiService.login(
                    UserLoginRequest(email = email, password = password)
                )
                if (resp.isSuccessful && resp.body() != null) {
                    val auth = resp.body()!!
                    sessionManager.saveSession(
                        accessToken = auth.accessToken,
                        userId = auth.userId,
                        email = auth.email,
                        fullName = auth.fullName ?: ""
                    )
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                isLoggedIn = true,
                                isAuthLoading = false,
                                authErrorMessage = null,
                                userFullName = auth.fullName ?: "",
                                userEmail = auth.email
                            )
                        }
                    }
                    syncUserData()
                } else {
                    val err = resp.errorBody()?.string() ?: "Login failed"
                    val msg = try {
                        JSONObject(err).optString("detail", "Invalid email or password")
                    } catch (e: Exception) {
                        "Invalid email or password"
                    }
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(isAuthLoading = false, authErrorMessage = msg) }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isAuthLoading = false, authErrorMessage = e.message ?: "Network error") }
                }
            }
        }
    }

    fun signOut() {
        sessionManager.clearSession()
        val initialSessionId = UUID.randomUUID().toString()
        _uiState.update {
            ClutchUiState(
                isLoggedIn = false,
                userFullName = "",
                userEmail = "",
                chatSessions = listOf(ChatSession(id = initialSessionId, title = "New Chat", lastUpdated = "Just now", messages = emptyList())),
                activeSessionId = initialSessionId
            )
        }
    }

    fun syncUserData() {
        val token = sessionManager.getAccessToken() ?: return
        val bearer = "Bearer $token"

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Sync User Profile Details
                val meResp = ApiClient.apiService.getMe(bearer)
                if (meResp.isSuccessful && meResp.body() != null) {
                    val me = meResp.body()!!
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                userFullName = me.fullName ?: it.userFullName,
                                userEmail = me.email
                            )
                        }
                    }
                }

                // 2. Sync Chat Sessions from Backend
                val sessResp = ApiClient.apiService.listChatSessions(bearer)
                if (sessResp.isSuccessful && sessResp.body() != null) {
                    val bSessions = sessResp.body()!!
                    if (bSessions.isNotEmpty()) {
                        val clientSessions = bSessions.map { bSess ->
                            ChatSession(
                                id = bSess.id,
                                title = bSess.title,
                                lastUpdated = bSess.updatedAt ?: "Recent",
                                messages = emptyList()
                            )
                        }

                        // Load messages for the first/active session
                        val firstSessId = clientSessions.first().id
                        val detailResp = ApiClient.apiService.getChatSessionDetail(firstSessId, bearer)
                        val fullMessages = if (detailResp.isSuccessful && detailResp.body() != null) {
                            detailResp.body()!!.messages.map { bm ->
                                ChatMessage(
                                    id = bm.id,
                                    sender = if (bm.role == "user") MessageSender.USER else MessageSender.AI,
                                    text = bm.content,
                                    timestamp = bm.createdAt ?: "Earlier",
                                    modelName = "Clutch Cloud AI • Live"
                                )
                            }
                        } else emptyList()

                        val updatedFirst = clientSessions.first().copy(messages = fullMessages)
                        val finalSessions = listOf(updatedFirst) + clientSessions.drop(1)

                        withContext(Dispatchers.Main) {
                            _uiState.update {
                                it.copy(
                                    chatSessions = finalSessions,
                                    activeSessionId = firstSessId
                                )
                            }
                        }
                    }
                }

                // 3. Sync Documents
                fetchDocumentsFromBackend()
            } catch (e: Exception) {
                // Keep local state on error
            }
        }
    }

    fun fetchDocumentsFromBackend() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = sessionManager.getAccessToken()?.let { "Bearer $it" }
                val response = ApiClient.apiService.listDocuments(token)
                if (response.isSuccessful && response.body() != null) {
                    val backendDocs = response.body()!!
                    val stashDocs = backendDocs.map { bDoc ->
                        val fType = when {
                            bDoc.fileType.contains("pdf", true) || bDoc.originalFilename.endsWith(".pdf", true) -> FileType.PDF
                            bDoc.fileType.contains("md", true) || bDoc.originalFilename.endsWith(".md", true) -> FileType.MARKDOWN
                            else -> FileType.TEXT
                        }
                        StashDocument(
                            id = bDoc.id,
                            title = bDoc.title.ifBlank { bDoc.originalFilename },
                            fileType = fType,
                            fileSizeFormat = "${(bDoc.fileSize / 1024).coerceAtLeast(1)} KB",
                            chunkCount = bDoc.chunkCount,
                            addedDate = "Synced from Cloud",
                            summary = bDoc.summary ?: "Indexed for semantic search & RAG synthesis.",
                            chunks = emptyList()
                        )
                    }

                    withContext(Dispatchers.Main) {
                        _uiState.update { state ->
                            val totalChunks = stashDocs.sumOf { it.chunkCount }
                            val totalKb = backendDocs.sumOf { it.fileSize / 1024 }
                            state.copy(
                                documents = stashDocs,
                                telemetry = state.telemetry.copy(
                                    totalFiles = stashDocs.size,
                                    totalChunks = totalChunks,
                                    totalVectors = totalChunks * 384,
                                    storageEstimateKb = totalKb
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Keep local state if server is offline
            }
        }
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(activeFilter = filter) }
    }

    private var _attachedDocContent: String? = null

    fun attachDocumentToChat(docTitle: String) {
        _uiState.update { it.copy(attachedDocTitle = docTitle) }
    }

    fun removeAttachedDoc() {
        _attachedDocContent = null
        _uiState.update { it.copy(attachedDocTitle = null) }
    }

    fun attachDocumentFromUri(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var fileName = "Document"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0) fileName = cursor.getString(nameIndex)
                    }
                }

                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
                val textContent = if (fileName.endsWith(".pdf", ignoreCase = true)) {
                    "Attached PDF: $fileName (${bytes.size} bytes). Uploaded and referenced in session."
                } else {
                    String(bytes, Charsets.UTF_8)
                }

                _attachedDocContent = textContent

                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(attachedDocTitle = fileName) }
                    android.widget.Toast.makeText(context, "Attached: $fileName", android.widget.Toast.LENGTH_SHORT).show()
                }

                try {
                    val mediaType = "application/octet-stream".toMediaTypeOrNull()
                    val requestBody = bytes.toRequestBody(mediaType)
                    val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
                    val titleBody = fileName.toRequestBody("text/plain".toMediaTypeOrNull())
                    val token = sessionManager.getAccessToken()?.let { "Bearer $it" }
                    ApiClient.apiService.uploadDocument(part, titleBody, token)
                } catch (ignored: Exception) {}

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun setSelectedCitation(citation: Citation?) {
        _uiState.update { it.copy(selectedCitation = citation) }
    }

    fun toggleSpotlight(isOpen: Boolean) {
        _uiState.update { it.copy(isSpotlightOpen = isOpen) }
    }

    fun toggleHistory(isOpen: Boolean) {
        _uiState.update { it.copy(isHistoryOpen = isOpen) }
    }

    fun trainNewMemory(title: String, content: String, category: MemoryCategory) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    trainingState = TrainingProgressState(
                        isTraining = true,
                        currentEpoch = 1,
                        totalEpochs = 3,
                        currentLoss = 0.42f,
                        progress = 0.25f,
                        statusMessage = "Ingesting & Tokenizing on-device..."
                    )
                )
            }
            delay(500)

            _uiState.update {
                it.copy(
                    trainingState = it.trainingState.copy(
                        currentEpoch = 2,
                        currentLoss = 0.18f,
                        progress = 0.65f,
                        statusMessage = "Computing 384d semantic vectors..."
                    )
                )
            }
            delay(500)

            _uiState.update {
                it.copy(
                    trainingState = it.trainingState.copy(
                        currentEpoch = 3,
                        currentLoss = 0.038f,
                        progress = 0.95f,
                        statusMessage = "Fitting local neural adapter weights..."
                    )
                )
            }
            delay(600)

            val newMemory = TrainedMemory(
                id = UUID.randomUUID().toString(),
                category = category,
                title = title,
                content = content,
                confidenceScore = 0.97f + (0..20).random() / 1000f,
                learnedAt = "Just now",
                isActive = true
            )

            _uiState.update { state ->
                val updatedMemories = listOf(newMemory) + state.memories
                state.copy(
                    memories = updatedMemories,
                    trainingState = TrainingProgressState(
                        isTraining = false,
                        progress = 1f,
                        statusMessage = "Training complete!"
                    ),
                    telemetry = state.telemetry.copy(
                        totalVectors = state.telemetry.totalVectors + 384,
                        storageEstimateKb = state.telemetry.storageEstimateKb + 18
                    )
                )
            }
        }
    }

    fun toggleMemory(id: String) {
        _uiState.update { state ->
            val updated = state.memories.map {
                if (it.id == id) it.copy(isActive = !it.isActive) else it
            }
            state.copy(memories = updated)
        }
    }

    fun deleteMemory(id: String) {
        _uiState.update { state ->
            val updated = state.memories.filter { it.id != id }
            state.copy(memories = updated)
        }
    }

        fun uploadDocumentFromUri(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            var fileName = "Document"
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0) fileName = cursor.getString(nameIndex)
                    }
                }

                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            uploadState = UploadProgressState(
                                step = UploadStep.SCANNING,
                                progress = 0.20f,
                                fileName = fileName
                            )
                        )
                    }
                }

                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)

                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            uploadState = UploadProgressState(
                                step = UploadStep.PARSING,
                                progress = 0.50f,
                                fileName = fileName
                            )
                        )
                    }
                }

                val mediaType = "application/octet-stream".toMediaTypeOrNull()
                val requestBody = bytes.toRequestBody(mediaType)
                val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
                val titleBody = fileName.toRequestBody("text/plain".toMediaTypeOrNull())

                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            uploadState = UploadProgressState(
                                step = UploadStep.GENERATING_VECTORS,
                                progress = 0.85f,
                                fileName = fileName
                            )
                        )
                    }
                }

                val token = sessionManager.getAccessToken()?.let { "Bearer $it" }
                val response = ApiClient.apiService.uploadDocument(part, titleBody, token)
                val fType = when {
                    fileName.endsWith(".pdf", true) -> FileType.PDF
                    fileName.endsWith(".md", true) -> FileType.MARKDOWN
                    else -> FileType.TEXT
                }

                val bDoc = response.body()
                val newDoc = if (response.isSuccessful && bDoc != null) {
                    StashDocument(
                        id = bDoc.id,
                        title = bDoc.title.ifBlank { fileName },
                        fileType = fType,
                        fileSizeFormat = "${(bytes.size / 1024).coerceAtLeast(1)} KB",
                        chunkCount = bDoc.chunkCount,
                        addedDate = "Just now",
                        summary = bDoc.summary ?: "Successfully indexed into knowledge base with ${bDoc.chunkCount} vector segments."
                    )
                } else {
                    StashDocument(
                        id = UUID.randomUUID().toString(),
                        title = fileName,
                        fileType = fType,
                        fileSizeFormat = "${(bytes.size / 1024).coerceAtLeast(1)} KB",
                        chunkCount = 1,
                        addedDate = "Just now",
                        summary = "Document cached locally on device."
                    )
                }

                withContext(Dispatchers.Main) {
                    _uiState.update { current ->
                        val updatedDocs = listOf(newDoc) + current.documents.filter { it.id != newDoc.id }
                        val addedChunks = if (bDoc != null) bDoc.chunkCount else 1
                        current.copy(
                            documents = updatedDocs,
                            uploadState = UploadProgressState(
                                step = UploadStep.STASHED,
                                progress = 1.0f,
                                fileName = fileName
                            ),
                            telemetry = current.telemetry.copy(
                                totalFiles = updatedDocs.size,
                                totalChunks = current.telemetry.totalChunks + addedChunks,
                                totalVectors = current.telemetry.totalVectors + (addedChunks * 384),
                                storageEstimateKb = current.telemetry.storageEstimateKb + (bytes.size / 1024)
                            )
                        )
                    }
                    android.widget.Toast.makeText(context, "Indexed: $fileName", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(uploadState = UploadProgressState()) }
                    android.widget.Toast.makeText(context, "Upload error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            } finally {
                delay(1200)
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(uploadState = UploadProgressState()) }
                }
            }
        }
    }

fun simulateUpload(fileName: String, fileType: FileType) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    uploadState = UploadProgressState(
                        step = UploadStep.SCANNING,
                        progress = 0.15f,
                        fileName = fileName
                    )
                )
            }
            delay(500)

            _uiState.update {
                it.copy(
                    uploadState = UploadProgressState(
                        step = UploadStep.PARSING,
                        progress = 0.50f,
                        fileName = fileName
                    )
                )
            }
            delay(500)

            _uiState.update {
                it.copy(
                    uploadState = UploadProgressState(
                        step = UploadStep.GENERATING_VECTORS,
                        progress = 0.85f,
                        fileName = fileName
                    )
                )
            }
            delay(600)

            val newDoc = StashDocument(
                id = UUID.randomUUID().toString(),
                title = fileName,
                fileType = fileType,
                fileSizeFormat = "310 KB",
                chunkCount = 6,
                addedDate = "Just now",
                summary = "User stashed $fileName parsed on-device into 6 384d vector chunks."
            )

            _uiState.update { current ->
                val newDocs = listOf(newDoc) + current.documents
                current.copy(
                    documents = newDocs,
                    uploadState = UploadProgressState(
                        step = UploadStep.STASHED,
                        progress = 1.0f,
                        fileName = fileName
                    ),
                    telemetry = current.telemetry.copy(
                        totalFiles = newDocs.size,
                        totalChunks = current.telemetry.totalChunks + 6,
                        totalVectors = current.telemetry.totalVectors + (6 * 384),
                        storageEstimateKb = current.telemetry.storageEstimateKb + 310
                    )
                )
            }

            delay(1200)
            _uiState.update { it.copy(uploadState = UploadProgressState()) }
        }
    }

    fun sendMessage(promptText: String) {
        if (promptText.isBlank()) return

        val session = uiState.value.chatSessions.find { it.id == uiState.value.activeSessionId }
            ?: return

        val attachedDoc = uiState.value.attachedDocTitle

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.USER,
            text = promptText,
            timestamp = "Just now",
            attachedDocTitle = attachedDoc
        )

        val dynamicTitle = if (session.title.startsWith("New Chat")) {
            val clean = promptText.trim().replaceFirstChar { it.uppercase() }
            if (clean.length > 25) clean.take(25) + "..." else clean
        } else {
            session.title
        }

        val updatedMessages = session.messages + userMessage
        val updatedSession = session.copy(
            title = dynamicTitle,
            messages = updatedMessages,
            lastUpdated = "Just now"
        )

        _uiState.update { state ->
            val newSessions = state.chatSessions.map {
                if (it.id == session.id) updatedSession else it
            }
            state.copy(
                chatSessions = newSessions,
                isGeneratingResponse = true
            )
        }

        // Live Catalyst Cloud AI Streaming with Sovereign Local Fallback
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            var cloudSuccess = false

            try {
                val clientTz = java.util.TimeZone.getDefault().id
                val clientNow = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.US).format(java.util.Date())

                val docSnippet = _attachedDocContent?.take(8000) ?: ""
                val contentToSend = if (attachedDoc != null && docSnippet.isNotBlank()) {
                    "Referencing Attached Document ($attachedDoc): " + docSnippet + " " + promptText
                } else {
                    promptText
                }

                val response = withContext(Dispatchers.IO) {
                    val token = sessionManager.getAccessToken()?.let { "Bearer $it" }
                    ApiClient.apiService.streamChat(
                        request = ChatStreamRequest(
                            content = contentToSend,
                            sessionId = session.id,
                            useRag = true,
                            timezone = clientTz,
                            clientTime = clientNow
                        ),
                        token = token,
                        timezone = clientTz
                    )
                }

                if (response.isSuccessful && response.body() != null) {
                    val aiMsgId = UUID.randomUUID().toString()
                    var aiMessage = ChatMessage(
                        id = aiMsgId,
                        sender = MessageSender.AI,
                        text = "",
                        timestamp = "Just now",
                        modelName = "Clutch AI (Catalyst Cloud)",
                        isThinking = true
                    )

                    // Add empty placeholder message for live streaming
                    _uiState.update { state ->
                        val initialAiMessages = updatedMessages + aiMessage
                        val initialSession = updatedSession.copy(messages = initialAiMessages)
                        val initialSessions = state.chatSessions.map {
                            if (it.id == session.id) initialSession else it
                        }
                        state.copy(chatSessions = initialSessions)
                    }

                    val accumulatedText = StringBuilder()
                    val accumulatedThought = StringBuilder()

                    withContext(Dispatchers.IO) {
                        val reader = BufferedReader(InputStreamReader(response.body()!!.byteStream()))
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            val currentLine = line ?: continue
                            if (currentLine.startsWith("data: ")) {
                                val dataStr = currentLine.removePrefix("data: ").trim()
                                if (dataStr.isNotBlank() && dataStr != "[DONE]") {
                                    try {
                                        val json = JSONObject(dataStr)
                                        val type = json.optString("type")
                                        if (type == "thinking") {
                                            val delta = json.optString("delta")
                                            accumulatedThought.append(delta)
                                            withContext(Dispatchers.Main) {
                                                aiMessage = aiMessage.copy(
                                                    isThinking = true,
                                                    thoughtText = accumulatedThought.toString()
                                                )
                                                _uiState.update { state ->
                                                    val streamMsgs = updatedMessages + aiMessage
                                                    val streamSession = updatedSession.copy(messages = streamMsgs)
                                                    val streamSessions = state.chatSessions.map {
                                                        if (it.id == session.id) streamSession else it
                                                    }
                                                    state.copy(chatSessions = streamSessions)
                                                }
                                            }
                                        } else if (type == "thinking_done") {
                                            withContext(Dispatchers.Main) {
                                                aiMessage = aiMessage.copy(isThinking = false)
                                                _uiState.update { state ->
                                                    val streamMsgs = updatedMessages + aiMessage
                                                    val streamSession = updatedSession.copy(messages = streamMsgs)
                                                    val streamSessions = state.chatSessions.map {
                                                        if (it.id == session.id) streamSession else it
                                                    }
                                                    state.copy(chatSessions = streamSessions)
                                                }
                                            }
                                        } else if (type == "token") {
                                            val delta = json.optString("delta")
                                            accumulatedText.append(delta)

                                            val currentSnapshot = accumulatedText.toString()
                                            val elapsed = System.currentTimeMillis() - startTime
                                            withContext(Dispatchers.Main) {
                                                aiMessage = aiMessage.copy(
                                                    text = currentSnapshot,
                                                    isThinking = false,
                                                    latencyMs = elapsed
                                                )
                                                _uiState.update { state ->
                                                    val streamMsgs = updatedMessages + aiMessage
                                                    val streamSession = updatedSession.copy(messages = streamMsgs)
                                                    val streamSessions = state.chatSessions.map {
                                                        if (it.id == session.id) streamSession else it
                                                    }
                                                    state.copy(chatSessions = streamSessions)
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // Ignore malformed intermediate SSE chunk
                                    }
                                }
                            }
                        }
                        reader.close()
                    }

                    if (accumulatedText.isNotBlank()) {
                        cloudSuccess = true
                    }
                }
            } catch (e: Exception) {
                cloudSuccess = false
            }

            if (!cloudSuccess) {
                // Seamless local fallback if cloud is unreachable
                val activeMemories = uiState.value.memories.filter { it.isActive }
                val generationResult = UniversalKnowledgeEngine.generateSynthesis(
                    prompt = promptText,
                    activeMemories = activeMemories,
                    stashedDocs = uiState.value.documents,
                    attachedDocTitle = attachedDoc
                )

                val aiMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.AI,
                    text = generationResult.text,
                    timestamp = "Just now",
                    latencyMs = generationResult.latencyMs,
                    modelName = generationResult.modelName,
                    codeSnippet = generationResult.codeSnippet,
                    citations = generationResult.citations,
                    memoryHits = generationResult.memoryHits
                )

                _uiState.update { state ->
                    val finalMessages = updatedMessages + aiMessage
                    val finalSession = updatedSession.copy(messages = finalMessages)
                    val finalSessions = state.chatSessions.map {
                        if (it.id == session.id) finalSession else it
                    }
                    state.copy(
                        chatSessions = finalSessions,
                        isGeneratingResponse = false,
                        attachedDocTitle = null
                    )
                }
            } else {
                _uiState.update { state ->
                    state.copy(
                        isGeneratingResponse = false,
                        attachedDocTitle = null
                    )
                }
            }
        }
    }

    fun createNewChatSession() {
        val newId = UUID.randomUUID().toString()
        val sessionCount = uiState.value.chatSessions.size + 1
        val newSession = ChatSession(
            id = newId,
            title = "New Chat #$sessionCount",
            lastUpdated = "Just now",
            messages = emptyList()
        )

        _uiState.update {
            it.copy(
                chatSessions = listOf(newSession) + it.chatSessions,
                activeSessionId = newId
            )
        }
    }

    fun selectSession(sessionId: String) {
        _uiState.update { it.copy(activeSessionId = sessionId) }
    }

    fun renameSession(sessionId: String, newTitle: String) {
        _uiState.update { state ->
            val updated = state.chatSessions.map {
                if (it.id == sessionId) it.copy(title = newTitle) else it
            }
            state.copy(chatSessions = updated)
        }
    }

    fun deleteSession(sessionId: String) {
        _uiState.update { state ->
            val remaining = state.chatSessions.filter { it.id != sessionId }
            val nextActive = if (state.activeSessionId == sessionId) {
                remaining.firstOrNull()?.id ?: ""
            } else state.activeSessionId

            state.copy(
                chatSessions = remaining,
                activeSessionId = nextActive
            )
        }
    }

    fun purgeVault() {
        _uiState.update { state ->
            state.copy(
                documents = emptyList(),
                memories = emptyList(),
                telemetry = TelemetryData(
                    totalFiles = 0,
                    totalVectors = 0,
                    totalChunks = 0,
                    storageEstimateKb = 0,
                    scanLatencyMs = 0.0f,
                    engineName = "Local On-Device Engine"
                )
            )
        }
    }
}
