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

data class ChatStreamRequest(
    val content: String,
    @SerializedName("session_id") val sessionId: String? = null,
    val provider: String? = null,
    val model: String? = null,
    @SerializedName("use_rag") val useRag: Boolean = true,
    val timezone: String? = null,
    @SerializedName("client_time") val clientTime: String? = null
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

    @POST("api/v1/chat/stream")
    @Streaming
    suspend fun streamChat(
        @Body request: ChatStreamRequest,
        @Header("Authorization") token: String? = null,
        @Header("X-Timezone") timezone: String? = null
    ): Response<ResponseBody>

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
