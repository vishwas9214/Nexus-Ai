package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val provider: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val role: String, // "user" or "model"
    val content: String,
    val thought: String? = null, // for thinking reasoning
    val timestamp: Long = System.currentTimeMillis(),
    val filePath: String? = null,
    val fileName: String? = null
)

@Entity(tableName = "api_keys")
data class ApiKey(
    @PrimaryKey val provider: String, // "OpenAI", "Google Gemini", "Grok", "Perplexity", "ElevenLabs", "Suno", "Runway"
    val key: String
)

@Entity(tableName = "generated_assets")
data class GeneratedAsset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "image", "video", "music", "app", "document"
    val title: String,
    val prompt: String,
    val provider: String,
    val filePath: String? = null, // local path, or URI
    val content: String? = null, // code, lyrics, markdown text, etc.
    val timestamp: Long = System.currentTimeMillis(),
    val extraInfo: String? = null // generic metadata e.g. genre, runtime, file extension
)
