package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY isPinned DESC, lastUpdated DESC")
    fun getAllSessions(): Flow<List<ChatSession>>

    @Query("SELECT * FROM chat_sessions WHERE id = :id")
    suspend fun getSessionById(id: Int): ChatSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSession): Long

    @Update
    suspend fun updateSession(session: ChatSession)

    @Delete
    suspend fun deleteSession(session: ChatSession)

    @Query("DELETE FROM chat_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Int)

    // Message Queries
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: Int): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: Int)
}

@Dao
interface ApiKeyDao {
    @Query("SELECT * FROM api_keys")
    fun getAllApiKeysFlow(): Flow<List<ApiKey>>

    @Query("SELECT * FROM api_keys")
    suspend fun getAllApiKeys(): List<ApiKey>

    @Query("SELECT * FROM api_keys WHERE provider = :provider")
    suspend fun getApiKey(provider: String): ApiKey?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiKey(apiKey: ApiKey)

    @Query("DELETE FROM api_keys WHERE provider = :provider")
    suspend fun deleteApiKey(provider: String)
}

@Dao
interface AssetDao {
    @Query("SELECT * FROM generated_assets ORDER BY timestamp DESC")
    fun getAllAssets(): Flow<List<GeneratedAsset>>

    @Query("SELECT * FROM generated_assets WHERE type = :type ORDER BY timestamp DESC")
    fun getAssetsByType(type: String): Flow<List<GeneratedAsset>>

    @Query("SELECT * FROM generated_assets WHERE title LIKE '%' || :query || '%' OR prompt LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchAssets(query: String): Flow<List<GeneratedAsset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: GeneratedAsset): Long

    @Delete
    suspend fun deleteAsset(asset: GeneratedAsset)

    @Query("DELETE FROM generated_assets WHERE id = :id")
    suspend fun deleteAssetById(id: Int)
}
