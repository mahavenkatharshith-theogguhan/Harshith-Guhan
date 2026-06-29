package com.example.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// Gemini Request Schemas
data class GeminiRequest(
    val contents: List<ContentItem>
)

data class ContentItem(
    val parts: List<PartItem>
)

data class PartItem(
    val text: String
)

// Gemini Response Schemas
data class GeminiResponse(
    val candidates: List<CandidateItem>?
)

data class CandidateItem(
    val content: ResponseContentItem?
)

data class ResponseContentItem(
    val parts: List<ResponsePartItem>?
)

data class ResponsePartItem(
    val text: String?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/"

        fun create(): GeminiApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(GeminiApiService::class.java)
        }
    }
}
