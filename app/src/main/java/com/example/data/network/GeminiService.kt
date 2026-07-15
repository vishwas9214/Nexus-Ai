package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null,
    @Json(name = "inlineData") val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    val mimeType: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>,
    val role: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val maxOutputTokens: Int? = null,
    val responseModalities: List<String>? = null,
    val speechConfig: SpeechConfig? = null
)

@JsonClass(generateAdapter = true)
data class SpeechConfig(
    val voiceConfig: VoiceConfig
)

@JsonClass(generateAdapter = true)
data class VoiceConfig(
    val prebuiltVoiceConfig: PrebuiltVoiceConfig
)

@JsonClass(generateAdapter = true)
data class PrebuiltVoiceConfig(
    val voiceName: String
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null
)

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    @POST("v1beta/models/{model}:generateImages")
    suspend fun generateImages(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: ImagenRequest
    ): ImagenResponse

    @POST("v1beta/models/{model}:generateVideos")
    suspend fun generateVideos(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: VeoRequest
    ): OperationResponse

    @GET("v1beta/{name}")
    suspend fun getOperation(
        @Path(value = "name", encoded = true) name: String,
        @Query("key") apiKey: String
    ): OperationResponse
}

@JsonClass(generateAdapter = true)
data class ImagenRequest(
    val prompt: String,
    val numberOfImages: Int? = 1,
    val outputMimeType: String? = "image/jpeg",
    val aspectRatio: String? = "1:1"
)

@JsonClass(generateAdapter = true)
data class ImagenResponse(
    val generatedImages: List<GeneratedImage>? = null
)

@JsonClass(generateAdapter = true)
data class GeneratedImage(
    val image: ImageBytes? = null
)

@JsonClass(generateAdapter = true)
data class ImageBytes(
    val imageBytes: String? = null
)

@JsonClass(generateAdapter = true)
data class VeoRequest(
    val prompt: String,
    val aspectRatio: String? = "16:9",
    val durationSeconds: Int? = 5
)

@JsonClass(generateAdapter = true)
data class OperationResponse(
    val name: String,
    val done: Boolean? = false,
    val metadata: Map<String, Any>? = null,
    val response: VeoResponse? = null,
    val error: OperationError? = null
)

@JsonClass(generateAdapter = true)
data class OperationError(
    val code: Int?,
    val message: String?
)

@JsonClass(generateAdapter = true)
data class VeoResponse(
    val generatedVideos: List<GeneratedVideo>? = null
)

@JsonClass(generateAdapter = true)
data class GeneratedVideo(
    val video: VideoFile? = null
)

@JsonClass(generateAdapter = true)
data class VideoFile(
    val uri: String? = null
)

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}
