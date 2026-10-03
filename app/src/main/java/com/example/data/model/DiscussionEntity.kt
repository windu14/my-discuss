package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "discussions")
data class DiscussionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val query: String,
    val summary: String,
    val fullTranscript: String,
    val category: String,
    val tags: String, // comma separated tags
    val isBookmarked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val aiModel: String = "Gemini Expressive 2026"
)

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false
)

enum class MessageSender {
    USER,
    AI
}
