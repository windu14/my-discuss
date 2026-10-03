package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.DiscussionEntity
import com.example.data.model.MessageSender
import com.example.data.repository.DiscussionRepository
import com.example.data.repository.GeminiRepository
import com.example.data.repository.GeminiRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class DiscussionViewModel(application: Application) : AndroidViewModel(application) {

    private val discussionRepository: DiscussionRepository
    val geminiRepository: GeminiRepository

    init {
        val db = AppDatabase.getDatabase(application)
        discussionRepository = DiscussionRepository(db.discussionDao())
        geminiRepository = GeminiRepositoryImpl(application)
    }

    // Search and Filter State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val categories = listOf("Semua", "UI/UX", "Android", "AI & ML", "Ide Produk")

    // Reactive discussions from Room based on query and category (real data only, no dummy data)
    val discussions: StateFlow<List<DiscussionEntity>> = combine(
        _searchQuery,
        _selectedCategory
    ) { query, category ->
        Pair(query, category)
    }.flatMapLatest { (query, category) ->
        discussionRepository.searchDiscussions(query, category)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Bookmarked discussions
    val bookmarkedDiscussions: StateFlow<List<DiscussionEntity>> = discussionRepository.getBookmarkedDiscussions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Active Discussion for Detail Dialog
    private val _activeDiscussion = MutableStateFlow<DiscussionEntity?>(null)
    val activeDiscussion: StateFlow<DiscussionEntity?> = _activeDiscussion.asStateFlow()

    // Live AI Discussion Screen State (Powered by Gemini API)
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "welcome_ai",
                sender = MessageSender.AI,
                text = "Selamat datang di ruang diskusi pribadi Anda. Ajukan pertanyaan teknis, ide arsitektur, atau topik riset. Respon dijawab langsung oleh Gemini 3.5 Flash dan dapat disimpan ke basis data lokal Anda.",
                timestamp = System.currentTimeMillis()
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun openDiscussionDetail(discussion: DiscussionEntity) {
        _activeDiscussion.value = discussion
    }

    fun closeDiscussionDetail() {
        _activeDiscussion.value = null
    }

    fun toggleBookmark(id: Long) {
        viewModelScope.launch {
            discussionRepository.toggleBookmark(id)
            val current = _activeDiscussion.value
            if (current != null && current.id == id) {
                _activeDiscussion.value = current.copy(isBookmarked = !current.isBookmarked)
            }
        }
    }

    fun deleteDiscussion(discussion: DiscussionEntity) {
        viewModelScope.launch {
            discussionRepository.deleteDiscussion(discussion)
            if (_activeDiscussion.value?.id == discussion.id) {
                _activeDiscussion.value = null
            }
            showFeedback("Diskusi berhasil dihapus dari database")
        }
    }

    fun sendUserMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.USER,
            text = text.trim()
        )
        val updatedHistory = _chatMessages.value + userMsg
        _chatMessages.value = updatedHistory

        viewModelScope.launch {
            _isAiThinking.value = true
            val result = geminiRepository.generateDiscussionResponse(
                prompt = text.trim(),
                history = updatedHistory
            )

            result.fold(
                onSuccess = { aiResponse ->
                    val aiMsg = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        sender = MessageSender.AI,
                        text = aiResponse
                    )
                    _chatMessages.value = _chatMessages.value + aiMsg
                },
                onFailure = { error ->
                    val errorMsg = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        sender = MessageSender.AI,
                        text = "Gagal memuat respon dari Gemini API: ${error.message ?: "Terjadi kesalahan jaringan"}.\n\nPastikan GEMINI_API_KEY telah diatur di Secrets Panel AI Studio."
                    )
                    _chatMessages.value = _chatMessages.value + errorMsg
                    showFeedback("Koneksi Gemini API gagal")
                }
            )
            _isAiThinking.value = false
        }
    }

    fun saveCurrentDiscussionToDatabase() {
        viewModelScope.launch {
            val messages = _chatMessages.value
            val userQueries = messages.filter { it.sender == MessageSender.USER }
            if (userQueries.isEmpty()) {
                showFeedback("Belum ada dialog untuk disimpan")
                return@launch
            }

            val transcript = messages.joinToString("\n\n") { msg ->
                val role = if (msg.sender == MessageSender.USER) "User" else "AI"
                "$role: ${msg.text}"
            }

            // Synthesize discussion using Gemini Repository
            _isAiThinking.value = true
            val synthesisResult = geminiRepository.synthesizeDiscussion(transcript)
            _isAiThinking.value = false

            val synthesis = synthesisResult.getOrDefault(
                com.example.data.remote.gemini.DiscussionSynthesisResult(
                    title = userQueries.last().text.take(50),
                    summary = messages.lastOrNull { it.sender == MessageSender.AI }?.text?.take(200) ?: "Sintesis diskusi AI",
                    category = "AI & ML",
                    tags = "AI, Diskusi"
                )
            )

            val entity = DiscussionEntity(
                title = synthesis.title,
                query = userQueries.last().text,
                summary = synthesis.summary,
                fullTranscript = transcript,
                category = synthesis.category,
                tags = synthesis.tags,
                isBookmarked = true,
                timestamp = System.currentTimeMillis(),
                aiModel = "Gemini 3.5 Flash"
            )

            discussionRepository.insertDiscussion(entity)
            showFeedback("Diskusi berhasil disintesis & disimpan ke database!")
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = MessageSender.AI,
                text = "Sesi baru dimulai. Silakan ajukan topik riset atau pertanyaan baru.",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    private fun showFeedback(message: String) {
        viewModelScope.launch {
            _userFeedbackMessage.value = message
            delay(3000)
            if (_userFeedbackMessage.value == message) {
                _userFeedbackMessage.value = null
            }
        }
    }
}
