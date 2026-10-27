package com.example.clutch_ai.network

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

data class HealthResponse(
    val status: String,
    val app: String,
    val version: String
)

data class UserRegisterRequest(
    val email: String,
    val password: String,
    @SerializedName("full_name") val fullName: String
)

data class UserLoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("user_id") val userId: String,
    val email: String,
    @SerializedName("full_name") val fullName: String?
)

data class BackendChatSession(
    val id: String,
    val title: String,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class BackendChatMessage(
    val id: String,
    @SerializedName("session_id") val sessionId: String,
    val role: String,
    val content: String,
    @SerializedName("created_at") val createdAt: String? = null
)

data class BackendSessionDetail(
    val id: String,
    val title: String,
    val messages: List<BackendChatMessage> = emptyList()
)

data class ChatStreamRequest(
    val content: String,
    @SerializedName("session_id") val sessionId: String? = null,
    val provider: String? = null,
    val model: String? = null,
    @SerializedName("use_rag") val useRag: Boolean = true,
    val timezone: String? = null,
    @SerializedName("client_time") val clientTime: String? = null,
    @SerializedName("attached_doc_title") val attachedDocTitle: String? = null,
    @SerializedName("attached_doc_content") val attachedDocContent: String? = null
)

data class DocumentExtractResponse(
    val filename: String,
    @SerializedName("file_type") val fileType: String,
    @SerializedName("page_count") val pageCount: Int,
    val text: String,
    @SerializedName("word_count") val wordCount: Int
)

data class SearchRequest(
    val query: String,
    val limit: Int = 5
)

data class SearchItem(
    @SerializedName("chunk_id") val chunkId: String,
    @SerializedName("document_id") val documentId: String,
    @SerializedName("document_title") val documentTitle: String,
    @SerializedName("page_number") val pageNumber: Int,
    val content: String,
    @SerializedName("similarity_score") val similarityScore: Float
)

data class BackendDocument(
    val id: String,
    val title: String,
    @SerializedName("original_filename") val originalFilename: String,
    @SerializedName("file_type") val fileType: String,
    @SerializedName("file_size") val fileSize: Long,
    val status: String,
    @SerializedName("chunk_count") val chunkCount: Int,
    val summary: String? = null
)

interface ClutchApiService {
    @GET("health")
    suspend fun checkHealth(): Response<HealthResponse>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: UserRegisterRequest): Response<AuthResponse>

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: UserLoginRequest): Response<AuthResponse>

    @GET("api/v1/auth/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<AuthResponse>

    @GET("api/v1/chat/sessions")
    suspend fun listChatSessions(@Header("Authorization") token: String): Response<List<BackendChatSession>>

    @GET("api/v1/chat/sessions/{session_id}")
    suspend fun getChatSessionDetail(
        @Path("session_id") sessionId: String,
        @Header("Authorization") token: String
    ): Response<BackendSessionDetail>

    @POST("api/v1/chat/stream")
    @Streaming
    suspend fun streamChat(
        @Body request: ChatStreamRequest,
        @Header("Authorization") token: String? = null,
        @Header("X-Timezone") timezone: String? = null
    ): Response<ResponseBody>

    @Multipart
    @POST("api/v1/documents/extract")
    suspend fun extractDocument(
        @Part file: okhttp3.MultipartBody.Part,
        @Header("Authorization") token: String? = null
    ): Response<DocumentExtractResponse>

    @Multipart
    @POST("api/v1/documents/upload")
    suspend fun uploadDocument(
        @Part file: okhttp3.MultipartBody.Part,
        @Part("title") title: okhttp3.RequestBody? = null,
        @Header("Authorization") token: String? = null
    ): Response<BackendDocument>

    @GET("api/v1/documents/")
    suspend fun listDocuments(
        @Header("Authorization") token: String? = null
    ): Response<List<BackendDocument>>

    @POST("api/v1/documents/search")
    suspend fun searchDocuments(
        @Body request: SearchRequest,
        @Header("Authorization") token: String? = null
    ): Response<List<SearchItem>>
}
