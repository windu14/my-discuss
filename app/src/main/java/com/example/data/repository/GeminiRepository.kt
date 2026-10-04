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
import com.example.data.remote.openrouter.OpenRouterChatRequest
import com.example.data.remote.openrouter.OpenRouterMessage
import com.example.data.remote.openrouter.OpenRouterRetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class OpenRouterModelOption(
    val id: String,
    val displayName: String,
    val badge: String,
    val description: String,
    val isFree: Boolean
)

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

    fun getActiveApiKey(): String

    fun getSelectedModel(): String

    fun setSelectedModel(modelId: String)

    fun getAvailableModels(): List<OpenRouterModelOption>
}

class GeminiRepositoryImpl(
    private val context: Context
) : GeminiRepository {

    private val tag = "AiRepository"
    private val prefs = context.getSharedPreferences("diskusiku_prototype_prefs", Context.MODE_PRIVATE)

    companion object {
        const val PREF_KEY_API_KEY = "custom_ai_key"
        const val PREF_KEY_MODEL = "selected_openrouter_model"
        const val DEFAULT_MODEL = "meta-llama/llama-3.3-70b-instruct:free"

        val AVAILABLE_MODELS = listOf(
            OpenRouterModelOption(
                id = "meta-llama/llama-3.3-70b-instruct:free",
                displayName = "Llama 3.3 70B",
                badge = "Free • 70B",
                description = "Sangat cerdas, serbaguna, penalaran mendalam & gratis tanpa batas.",
                isFree = true
            ),
            OpenRouterModelOption(
                id = "deepseek/deepseek-r1:free",
                displayName = "DeepSeek R1",
                badge = "Free • Reasoning",
                description = "Model penalaran tingkat tinggi dengan analisis logis mendalam.",
                isFree = true
            ),
            OpenRouterModelOption(
                id = "google/gemini-2.0-flash-lite-preview-02-05:free",
                displayName = "Gemini 2.0 Flash Lite",
                badge = "Free • Ultra Cepat",
                description = "Respon super responsif & cepat dari Google via OpenRouter.",
                isFree = true
            ),
            OpenRouterModelOption(
                id = "qwen/qwen-2.5-coder-32b-instruct:free",
                displayName = "Qwen 2.5 Coder 32B",
                badge = "Free • Coding/Teknis",
                description = "Optimal untuk riset teknis, arsitektur sistem, dan logika kode.",
                isFree = true
            ),
            OpenRouterModelOption(
                id = "mistralai/mistral-7b-instruct:free",
                displayName = "Mistral 7B Instruct",
                badge = "Free • Ringkas",
                description = "Model ringan dan efisien untuk diskusi to-the-point.",
                isFree = true
            ),
            OpenRouterModelOption(
                id = "google/gemini-2.5-flash",
                displayName = "Gemini 2.5 Flash",
                badge = "$100 Tier",
                description = "Model multimodal mutakhir Google dengan penalaran komprehensif.",
                isFree = false
            ),
            OpenRouterModelOption(
                id = "openai/gpt-4o-mini",
                displayName = "GPT-4o Mini",
                badge = "$100 Tier",
                description = "Model OpenAI hemat biaya, cerdas, dan cepat.",
                isFree = false
            ),
            OpenRouterModelOption(
                id = "anthropic/claude-3.5-sonnet",
                displayName = "Claude 3.5 Sonnet",
                badge = "$100 Tier",
                description = "Sintesis bahasa & pemikiran konseptual terbaik di kelasnya.",
                isFree = false
            )
        )
    }

    override fun isApiKeyConfigured(): Boolean {
        val key = getActiveApiKey()
        return key.isNotBlank() &&
                key != "MY_OPENROUTER_API_KEY" &&
                key != "MY_GEMINI_API_KEY"
    }

    override fun saveCustomApiKey(key: String) {
        prefs.edit().putString(PREF_KEY_API_KEY, key.trim()).apply()
    }

    override fun getActiveApiKey(): String {
        val savedKey = prefs.getString(PREF_KEY_API_KEY, "") ?: ""
        if (savedKey.isNotBlank()) return savedKey.trim()

        // Check BuildConfig for OPENROUTER_API_KEY or GEMINI_API_KEY
        val openRouterKey = runCatching {
            BuildConfig::class.java.getField("OPENROUTER_API_KEY").get(null) as? String
        }.getOrNull() ?: ""
        if (openRouterKey.isNotBlank() && openRouterKey != "MY_OPENROUTER_API_KEY") {
            return openRouterKey.trim()
        }

        val geminiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (geminiKey.isNotBlank() && geminiKey != "MY_GEMINI_API_KEY") {
            return geminiKey
        }

        return ""
    }

    override fun getSelectedModel(): String {
        return prefs.getString(PREF_KEY_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
    }

    override fun setSelectedModel(modelId: String) {
        prefs.edit().putString(PREF_KEY_MODEL, modelId.trim()).apply()
    }

    override fun getAvailableModels(): List<OpenRouterModelOption> = AVAILABLE_MODELS

    override suspend fun generateDiscussionResponse(
        prompt: String,
        history: List<ChatMessage>
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext Result.failure(
                IllegalStateException("API Key OpenRouter belum dikonfigurasi. Harap masukkan kunci OpenRouter (diawali sk-or-v1-...) melalui dialog Aktivasi AI.")
            )
        }

        val apiKey = getActiveApiKey()
        val currentModel = getSelectedModel()

        // If key is an OpenRouter key or user configured a general key
        if (apiKey.startsWith("sk-or-") || !apiKey.startsWith("AIzaSy")) {
            return@withContext callOpenRouter(apiKey, currentModel, prompt, history)
        } else {
            // Fallback for native Gemini key
            return@withContext callGemini(apiKey, prompt, history)
        }
    }

    private suspend fun callOpenRouter(
        apiKey: String,
        model: String,
        prompt: String,
        history: List<ChatMessage>
    ): Result<String> {
        try {
            val messages = mutableListOf<OpenRouterMessage>()

            // System prompt
            messages.add(
                OpenRouterMessage(
                    role = "system",
                    content = "Anda adalah asisten riset dan diskusi cerdas. Berikan analisis mendalam, terstruktur, berbasis poin-poin yang mudah dipahami dalam Bahasa Indonesia, siap diarsipkan ke basis data pengetahuan pengguna."
                )
            )

            // Conversation history
            history.takeLast(10).forEach { msg ->
                val role = if (msg.sender == MessageSender.USER) "user" else "assistant"
                messages.add(OpenRouterMessage(role = role, content = msg.text))
            }

            // Current prompt
            messages.add(OpenRouterMessage(role = "user", content = prompt))

            val request = OpenRouterChatRequest(
                model = model,
                messages = messages,
                temperature = 0.7f,
                maxTokens = 2048
            )

            val authHeader = if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"
            val response = OpenRouterRetrofitClient.apiService.createChatCompletion(
                authorization = authHeader,
                request = request
            )

            val content = response.choices?.firstOrNull()?.message?.content
            if (!content.isNullOrBlank()) {
                return Result.success(content)
            }

            val errorMessage = response.error?.message
            return Result.failure(Exception(errorMessage ?: "OpenRouter tidak mengembalikan konten teks."))
        } catch (e: Exception) {
            Log.e(tag, "OpenRouter call failed: ${e.message}", e)
            return Result.failure(e)
        }
    }

    private suspend fun callGemini(
        apiKey: String,
        prompt: String,
        history: List<ChatMessage>
    ): Result<String> {
        try {
            val contentsList = mutableListOf<GeminiContent>()

            history.takeLast(10).forEach { msg ->
                val role = if (msg.sender == MessageSender.USER) "user" else "model"
                contentsList.add(GeminiContent(role = role, parts = listOf(GeminiPart(text = msg.text))))
            }

            contentsList.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt))))

            val request = GeminiRequest(
                contents = contentsList,
                systemInstruction = GeminiContent(
                    parts = listOf(
                        GeminiPart(text = "Anda adalah asisten riset dan diskusi cerdas. Berikan analisis mendalam dalam Bahasa Indonesia.")
                    )
                ),
                generationConfig = GeminiGenerationConfig(temperature = 0.7f, maxOutputTokens = 2048)
            )

            val response = GeminiRetrofitClient.apiService.generateContent(apiKey = apiKey, request = request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                return Result.success(text)
            }
            return Result.failure(Exception("Tidak ada respon dari Gemini"))
        } catch (e: Exception) {
            Log.e(tag, "Gemini call failed: ${e.message}", e)
            return Result.failure(e)
        }
    }

    override suspend fun synthesizeDiscussion(
        fullTranscript: String
    ): Result<DiscussionSynthesisResult> = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
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
            val apiKey = getActiveApiKey()
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

            val rawResponse: String = if (apiKey.startsWith("sk-or-") || !apiKey.startsWith("AIzaSy")) {
                val request = OpenRouterChatRequest(
                    model = getSelectedModel(),
                    messages = listOf(
                        OpenRouterMessage(role = "user", content = prompt)
                    ),
                    temperature = 0.3f,
                    maxTokens = 512
                )
                val authHeader = if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"
                val res = OpenRouterRetrofitClient.apiService.createChatCompletion(
                    authorization = authHeader,
                    request = request
                )
                res.choices?.firstOrNull()?.message?.content ?: ""
            } else {
                val request = GeminiRequest(
                    contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt)))),
                    generationConfig = GeminiGenerationConfig(temperature = 0.3f, maxOutputTokens = 512)
                )
                val res = GeminiRetrofitClient.apiService.generateContent(apiKey = apiKey, request = request)
                res.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            }

            val title = rawResponse.lines().find { it.startsWith("TITLE:", ignoreCase = true) }?.substringAfter(":")?.trim()
                ?: fullTranscript.lines().firstOrNull()?.take(50) ?: "Diskusi AI"
            val category = rawResponse.lines().find { it.startsWith("CATEGORY:", ignoreCase = true) }?.substringAfter(":")?.trim()
                ?: "AI & ML"
            val tags = rawResponse.lines().find { it.startsWith("TAGS:", ignoreCase = true) }?.substringAfter(":")?.trim()
                ?: "AI, OpenRouter"
            val summary = rawResponse.lines().find { it.startsWith("SUMMARY:", ignoreCase = true) }?.substringAfter(":")?.trim()
                ?: rawResponse.take(250)

            Result.success(
                DiscussionSynthesisResult(
                    title = title,
                    summary = summary,
                    category = category,
                    tags = tags
                )
            )
        } catch (e: Exception) {
            Log.e(tag, "Failed to synthesize: ${e.message}", e)
            Result.failure(e)
        }
    }
}
