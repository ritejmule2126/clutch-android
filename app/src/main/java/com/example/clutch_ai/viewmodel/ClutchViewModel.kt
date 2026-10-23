package com.example.clutch_ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clutch_ai.engine.UniversalKnowledgeEngine
import com.example.clutch_ai.model.*
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
        totalFiles = 3,
        totalVectors = 4992,
        totalChunks = 13,
        storageEstimateKb = 1420,
        scanLatencyMs = 6.2f,
        engineName = "Local On-Device Engine"
    ),
    val memories: List<TrainedMemory> = emptyList(),
    val trainingState: TrainingProgressState = TrainingProgressState()
)

class ClutchViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ClutchUiState())
    val uiState: StateFlow<ClutchUiState> = _uiState.asStateFlow()

    init {
        seedInitialState()
    }

    private fun seedInitialState() {
        val initialDocs = listOf(
            StashDocument(
                id = "doc-1",
                title = "Paylogic_System_Architecture.pdf",
                fileType = FileType.PDF,
                fileSizeFormat = "420 KB",
                chunkCount = 6,
                addedDate = "Today, 10:14 AM",
                summary = "Full microservices overview: Docker orchestration, Redis L2 caching, RabbitMQ message queues, and zero-downtime deployment specs.",
                chunks = listOf(
                    DocumentChunk(
                        id = "c-1",
                        docId = "doc-1",
                        docTitle = "Paylogic_System_Architecture.pdf",
                        chunkIndex = 1,
                        text = "The ecollection-api listens on port 8443 and connects to redis:7-alpine on port 6379 for fast session verification.",
                        vectorPreview = listOf(0.12f, -0.45f, 0.78f, -0.09f, 0.33f),
                        cosineScore = 0.962f
                    ),
                    DocumentChunk(
                        id = "c-2",
                        docId = "doc-1",
                        docTitle = "Paylogic_System_Architecture.pdf",
                        chunkIndex = 2,
                        text = "RabbitMQ staging broker routes asynchronous ledger events across worker nodes with AMQP 0-9-1 protocols.",
                        vectorPreview = listOf(-0.22f, 0.61f, -0.15f, 0.88f, -0.04f),
                        cosineScore = 0.941f
                    )
                )
            ),
            StashDocument(
                id = "doc-2",
                title = "Vedic_Cosmic_Cycles_and_Dharma.md",
                fileType = FileType.MARKDOWN,
                fileSizeFormat = "180 KB",
                chunkCount = 4,
                addedDate = "Yesterday",
                summary = "Detailed analysis of the 4 Yugas (Satya, Treta, Dvapara, Kali) and the Bhagavad Gita's Nishkama Karma philosophy.",
                chunks = listOf(
                    DocumentChunk(
                        id = "c-3",
                        docId = "doc-2",
                        docTitle = "Vedic_Cosmic_Cycles_and_Dharma.md",
                        chunkIndex = 1,
                        text = "Satya Yuga represents 100% truth and alignment with cosmic order; Dvapara brings dualism and the discourse of Krishna on duty.",
                        vectorPreview = listOf(0.33f, 0.14f, 0.85f, -0.21f, 0.44f),
                        cosineScore = 0.978f
                    )
                )
            ),
            StashDocument(
                id = "doc-3",
                title = "Terraform_DevOps_State_Matrix.txt",
                fileType = FileType.TEXT,
                fileSizeFormat = "820 KB",
                chunkCount = 3,
                addedDate = "3 days ago",
                summary = "AWS Amplify and core Postgres bootstrapping scripts for staging and development infrastructure.",
                chunks = emptyList()
            )
        )

        val initialMemories = listOf(
            TrainedMemory(
                id = "mem-1",
                category = MemoryCategory.WORK_HABIT,
                title = "Lead Infrastructure Engineer",
                content = "User leads high-performance cloud engineering, Terraform infrastructure, Docker containerization, and Postgres databases.",
                confidenceScore = 0.985f,
                learnedAt = "System Inception"
            ),
            TrainedMemory(
                id = "mem-2",
                category = MemoryCategory.UNIVERSAL_WISDOM,
                title = "Nishkama Karma Discipline",
                content = "Focus 100% on the execution and duty of current actions, maintaining emotional equanimity regardless of outcome fluctuations.",
                confidenceScore = 0.972f,
                learnedAt = "System Inception"
            ),
            TrainedMemory(
                id = "mem-3",
                category = MemoryCategory.CORE_RULE,
                title = "Zero-Cloud Local Privacy",
                content = "All queries and personal facts must strictly remain on-device. Never transmit user data to external third-party servers.",
                confidenceScore = 0.999f,
                learnedAt = "System Inception"
            )
        )

        val initialSessionId = UUID.randomUUID().toString()
        val initialSession = ChatSession(
            id = initialSessionId,
            title = "New Chat",
            lastUpdated = "Just now",
            messages = emptyList()
        )

        _uiState.update {
            it.copy(
                documents = initialDocs,
                memories = initialMemories,
                chatSessions = listOf(initialSession),
                activeSessionId = initialSessionId
            )
        }
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(activeFilter = filter) }
    }

    fun attachDocumentToChat(docTitle: String) {
        _uiState.update { it.copy(attachedDocTitle = docTitle) }
    }

    fun removeAttachedDoc() {
        _uiState.update { it.copy(attachedDocTitle = null) }
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

        // Dual-Brain Synthesis: Universal Foundation + Local Trained Memories
        viewModelScope.launch {
            delay(650)

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
