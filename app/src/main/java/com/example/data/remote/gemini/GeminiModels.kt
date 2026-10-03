package com.example.data.remote.gemini

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @field:Json(name = "contents")
    val contents: List<GeminiContent>,
    @field:Json(name = "systemInstruction")
    val systemInstruction: GeminiContent? = null,
    @field:Json(name = "generationConfig")
    val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @field:Json(name = "role")
    val role: String? = null,
    @field:Json(name = "parts")
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @field:Json(name = "text")
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @field:Json(name = "temperature")
    val temperature: Float? = null,
    @field:Json(name = "topP")
    val topP: Float? = null,
    @field:Json(name = "topK")
    val topK: Int? = null,
    @field:Json(name = "maxOutputTokens")
    val maxOutputTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @field:Json(name = "candidates")
    val candidates: List<GeminiCandidate>? = null,
    @field:Json(name = "promptFeedback")
    val promptFeedback: GeminiPromptFeedback? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @field:Json(name = "content")
    val content: GeminiContent? = null,
    @field:Json(name = "finishReason")
    val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPromptFeedback(
    @field:Json(name = "blockReason")
    val blockReason: String? = null
)

data class DiscussionSynthesisResult(
    val title: String,
    val summary: String,
    val category: String,
    val tags: String
)
