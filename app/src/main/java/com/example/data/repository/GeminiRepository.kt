package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.data.remote.gemini.DiscussionSynthesisResult
import com.example.data.remote.gemini.GeminiContent
import com.example.data.remote.gemini.GeminiGenerationConfig
import com.example.data.remote.gemini.GeminiPart
import com.example.data.remote.gemini.GeminiRequest
import com.example.data.remote.gemini.GeminiRetrofitClient
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface GeminiRepository {
    suspend fun generateDiscussionResponse(
        prompt: String,
        history: List<ChatMessage> = emptyList()
    ): Result<String>

    suspend fun synthesizeDiscussion(
        fullTranscript: String
    ): Result<DiscussionSynthesisResult>

    fun isApiKeyConfigured(): Boolean

    fun saveCustomApiKey(key: String)
}

class GeminiRepositoryImpl(
    private val context: Context
) : GeminiRepository {

    private val tag = "GeminiRepository"
    private val defaultModelName = "gemini-3.5-flash"
    private val prefs = context.getSharedPreferences("diskusiku_prototype_prefs", Context.MODE_PRIVATE)

    /**
     * Securely checks if the Gemini API Key is configured via BuildConfig or stored prototype key.
     */
    override fun isApiKeyConfigured(): Boolean {
        val key = getSecureApiKey()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    override fun saveCustomApiKey(key: String) {
        prefs.edit().putString("custom_gemini_key", key.trim()).apply()
    }

    private fun getSecureApiKey(): String {
        val customKey = prefs.getString("custom_gemini_key", "") ?: ""
        if (customKey.isNotBlank()) return customKey.trim()
        return BuildConfig.GEMINI_API_KEY.trim()
    }

    override suspend fun generateDiscussionResponse(
        prompt: String,
        history: List<ChatMessage>
    ): Result<String> = withContext(Dispatchers.IO) {
        // Attempt Firebase AI SDK if FirebaseApp is available
        val firebaseResult = tryFirebaseAi(prompt)
        if (firebaseResult.isSuccess) {
            return@withContext firebaseResult
        }

        // Fallback to Retrofit client with secured BuildConfig key
        if (!isApiKeyConfigured()) {
            return@withContext Result.failure(
                IllegalStateException("Kunci Gemini API belum dikonfigurasi. Harap tambahkan GEMINI_API_KEY pada panel Secrets AI Studio.")
            )
        }

        try {
            val apiKey = getSecureApiKey()
            val contentsList = mutableListOf<GeminiContent>()

            // Map conversation history
            history.takeLast(10).forEach { msg ->
                val role = if (msg.sender == MessageSender.USER) "user" else "model"
                contentsList.add(
                    GeminiContent(
                        role = role,
                        parts = listOf(GeminiPart(text = msg.text))
                    )
                )
            }

            // Append current prompt
            contentsList.add(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = prompt))
                )
            )

            val request = GeminiRequest(
                contents = contentsList,
                systemInstruction = GeminiContent(
                    parts = listOf(
                        GeminiPart(
                            text = "Anda adalah asisten sintesis diskusi dan arsitek pengetahuan cerdas. Berikan analisis mendalam, terstruktur, berbasis poin-poin yang mudah dipahami dalam Bahasa Indonesia, siap diarsipkan ke basis data pengetahuan pengguna."
                        )
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.7f,
                    topP = 0.95f,
                    maxOutputTokens = 2048
                )
            )

            val response = GeminiRetrofitClient.apiService.generateContent(
                apiKey = apiKey,
                request = request
            )

            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                val blockReason = response.promptFeedback?.blockReason
                Result.failure(Exception("Tidak ada teks respon dari Gemini${if (blockReason != null) " (Diblokir: $blockReason)" else ""}"))
            }
        } catch (e: Exception) {
            Log.e(tag, "Retrofit Gemini call failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun synthesizeDiscussion(
        fullTranscript: String
    ): Result<DiscussionSynthesisResult> = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            // Provide fallback local extraction if API key is not yet configured
            val firstLine = fullTranscript.lines().firstOrNull { it.isNotBlank() } ?: "Diskusi AI"
            val title = if (firstLine.length > 50) firstLine.take(47) + "..." else firstLine
            val summary = fullTranscript.take(250) + "..."
            return@withContext Result.success(
                DiscussionSynthesisResult(
                    title = title,
                    summary = summary,
                    category = "AI & ML",
                    tags = "AI, Diskusi, Arsip"
                )
            )
        }

        try {
            val apiKey = getSecureApiKey()
            val prompt = """
                Berdasarkan transkrip percakapan berikut, buatkan metadata ringkasan untuk disimpan ke database:
                Format output yang WAJIB dipatuhi:
                TITLE: [Judul singkat menarik maksimal 60 karakter]
                CATEGORY: [Pilih satu yang paling tepat: UI/UX, Android, AI & ML, atau Ide Produk]
                TAGS: [3-5 kata kunci dipisahkan koma]
                SUMMARY: [Rangkuman sintesis 2-3 kalimat mengenai poin kunci yang disepakati]

                Transkrip:
                $fullTranscript
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = prompt))
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.3f,
                    maxOutputTokens = 512
                )
            )

            val response = GeminiRetrofitClient.apiService.generateContent(
                apiKey = apiKey,
                request = request
            )

            val raw = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            val title = raw.lines().find { it.startsWith("TITLE:", ignoreCase = true) }?.substringAfter(":")?.trim()
                ?: fullTranscript.lines().firstOrNull()?.take(50) ?: "Diskusi AI"
            val category = raw.lines().find { it.startsWith("CATEGORY:", ignoreCase = true) }?.substringAfter(":")?.trim()
                ?: "AI & ML"
            val tags = raw.lines().find { it.startsWith("TAGS:", ignoreCase = true) }?.substringAfter(":")?.trim()
                ?: "AI, Diskusi"
            val summary = raw.lines().find { it.startsWith("SUMMARY:", ignoreCase = true) }?.substringAfter(":")?.trim()
                ?: raw.take(250)

            Result.success(
                DiscussionSynthesisResult(
                    title = title,
                    summary = summary,
                    category = category,
                    tags = tags
                )
            )
        } catch (e: Exception) {
            Log.e(tag, "Failed to synthesize discussion: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun tryFirebaseAi(prompt: String): Result<String> {
        return try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isEmpty()) {
                return Result.failure(IllegalStateException("FirebaseApp not initialized"))
            }
            val model: GenerativeModel = Firebase.ai.generativeModel(defaultModelName)
            val response = model.generateContent(prompt)
            val text = response.text
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.failure(Exception("Empty Firebase AI response"))
            }
        } catch (e: Throwable) {
            Result.failure(Exception(e.message ?: "Firebase AI error"))
        }
    }
}
