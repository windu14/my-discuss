package com.example.data.remote.openrouter

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenRouterMessage(
    @field:Json(name = "role") val role: String,
    @field:Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class OpenRouterChatRequest(
    @field:Json(name = "model") val model: String,
    @field:Json(name = "messages") val messages: List<OpenRouterMessage>,
    @field:Json(name = "temperature") val temperature: Float? = 0.7f,
    @field:Json(name = "max_tokens") val maxTokens: Int? = 2048
)

@JsonClass(generateAdapter = true)
data class OpenRouterChoice(
    @field:Json(name = "message") val message: OpenRouterMessage?,
    @field:Json(name = "finish_reason") val finishReason: String?
)

@JsonClass(generateAdapter = true)
data class OpenRouterError(
    @field:Json(name = "message") val message: String?,
    @field:Json(name = "code") val code: Any?
)

@JsonClass(generateAdapter = true)
data class OpenRouterChatResponse(
    @field:Json(name = "id") val id: String? = null,
    @field:Json(name = "model") val model: String? = null,
    @field:Json(name = "choices") val choices: List<OpenRouterChoice>? = null,
    @field:Json(name = "error") val error: OpenRouterError? = null
)
