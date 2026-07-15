package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.flow.Flow

class NexusRepository(private val database: AppDatabase) {
    private val chatDao = database.chatDao()
    private val apiKeyDao = database.apiKeyDao()
    private val assetDao = database.assetDao()

    // --- Chat Sessions ---
    val allSessions: Flow<List<ChatSession>> = chatDao.getAllSessions()

    suspend fun getSession(id: Int): ChatSession? = chatDao.getSessionById(id)

    suspend fun createSession(title: String, provider: String): Int {
        val session = ChatSession(title = title, provider = provider)
        return chatDao.insertSession(session).toInt()
    }

    suspend fun updateSession(session: ChatSession) {
        chatDao.updateSession(session)
    }

    suspend fun deleteSession(id: Int) {
        chatDao.deleteMessagesForSession(id)
        chatDao.deleteSessionById(id)
    }

    // --- Chat Messages ---
    fun getMessagesForSession(sessionId: Int): Flow<List<ChatMessage>> =
        chatDao.getMessagesForSession(sessionId)

    suspend fun addMessage(sessionId: Int, role: String, content: String, thought: String? = null, filePath: String? = null, fileName: String? = null) {
        val msg = ChatMessage(
            sessionId = sessionId,
            role = role,
            content = content,
            thought = thought,
            filePath = filePath,
            fileName = fileName
        )
        chatDao.insertMessage(msg)
        
        // Update session's lastUpdated timestamp
        getSession(sessionId)?.let { session ->
            chatDao.updateSession(session.copy(lastUpdated = System.currentTimeMillis()))
        }
    }

    // --- API Keys ---
    val allApiKeys: Flow<List<ApiKey>> = apiKeyDao.getAllApiKeysFlow()

    suspend fun getApiKey(provider: String): String? =
        apiKeyDao.getApiKey(provider)?.key

    suspend fun setApiKey(provider: String, key: String) {
        if (key.trim().isEmpty()) {
            apiKeyDao.deleteApiKey(provider)
        } else {
            apiKeyDao.insertApiKey(ApiKey(provider, key))
        }
    }

    suspend fun deleteApiKey(provider: String) {
        apiKeyDao.deleteApiKey(provider)
    }

    // --- Generated Assets ---
    val allAssets: Flow<List<GeneratedAsset>> = assetDao.getAllAssets()

    fun getAssetsByType(type: String): Flow<List<GeneratedAsset>> =
        assetDao.getAssetsByType(type)

    fun searchAssets(query: String): Flow<List<GeneratedAsset>> =
        assetDao.searchAssets(query)

    suspend fun addAsset(type: String, title: String, prompt: String, provider: String, filePath: String? = null, content: String? = null, extraInfo: String? = null): Int {
        val asset = GeneratedAsset(
            type = type,
            title = title,
            prompt = prompt,
            provider = provider,
            filePath = filePath,
            content = content,
            extraInfo = extraInfo
        )
        return assetDao.insertAsset(asset).toInt()
    }

    suspend fun deleteAsset(id: Int) {
        assetDao.deleteAssetById(id)
    }
}
