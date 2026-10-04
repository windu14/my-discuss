package com.example.data.remote.openrouter

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApiService {
    @POST("api/v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Header("HTTP-Referer") referer: String = "https://github.com/windu14/my-discuss",
        @Header("X-Title") title: String = "Diskusiku",
        @Body request: OpenRouterChatRequest
    ): OpenRouterChatResponse
}
