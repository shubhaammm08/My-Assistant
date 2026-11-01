package com.jarvis.assistant.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

data class RemoteChatRequest(
    val message: String,
    @SerializedName("session_id") val sessionId: String = "android-device",
    @SerializedName("user_locale") val userLocale: String = "en-US",
    @SerializedName("voice_profile") val voiceProfile: String = "japanese_female",
    @SerializedName("client_capabilities") val capabilities: List<String> = listOf(
        "open_app", "set_alarm", "set_timer", "create_task", "make_phone_call", "draft_sms"
    )
)

data class RemoteVoiceSettings(
    val pitch: Float,
    val speed: Float,
    val volume: Float,
    val locale: String,
    @SerializedName("voice_name") val voiceName: String,
    @SerializedName("accent_influence") val accentInfluence: String
)

data class RemoteToolCall(
    @SerializedName("call_id") val callId: String?,
    val tool: String,
    val arguments: Map<String, Any>
)

data class RemoteChatResponse(
    @SerializedName("reply_text") val replyText: String,
    @SerializedName("detected_language") val detectedLanguage: String,
    @SerializedName("voice_settings") val voiceSettings: RemoteVoiceSettings,
    @SerializedName("tool_calls") val toolCalls: List<RemoteToolCall>,
    @SerializedName("requires_confirmation") val requiresConfirmation: Boolean,
    @SerializedName("confirmation_action") val confirmationAction: String?,
    @SerializedName("session_id") val sessionId: String
)

data class RemoteTaskItem(
    val id: String,
    val title: String,
    val description: String?,
    @SerializedName("due_date") val dueDate: String?,
    val priority: String,
    val status: String,
    val recurrence: String?,
    val category: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class RemoteTaskSyncRequest(
    @SerializedName("client_tasks") val clientTasks: List<RemoteTaskItem>,
    @SerializedName("last_sync_timestamp") val lastSyncTimestamp: String?
)

data class RemoteTaskSyncResponse(
    @SerializedName("synced_tasks") val syncedTasks: List<RemoteTaskItem>,
    @SerializedName("server_timestamp") val serverTimestamp: String
)

interface JarvisApiService {
    @GET("api/health")
    suspend fun checkHealth(): Map<String, Any>

    @POST("api/chat")
    suspend fun sendChatMessage(@Body request: RemoteChatRequest): RemoteChatResponse

    @GET("api/tasks")
    suspend fun getTasks(): List<RemoteTaskItem>

    @POST("api/tasks")
    suspend fun createTask(@Body task: Map<String, Any?>): RemoteTaskItem

    @POST("api/tasks/sync")
    suspend fun syncTasks(@Body request: RemoteTaskSyncRequest): RemoteTaskSyncResponse
}

object JarvisApiClient {
    // Default emulator or local LAN endpoint (configurable in Settings)
    var baseUrl: String = "http://10.0.2.2:8000/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    fun getService(): JarvisApiService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JarvisApiService::class.java)
    }
}

