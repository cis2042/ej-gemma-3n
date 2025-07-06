package com.example.gemmaprototype.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 聊天消息數據訪問對象
 */
@Dao
interface ChatDao {
    
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessage>>
    
    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<ChatMessage>>
    
    @Insert
    suspend fun insertMessage(message: ChatMessage): Long
    
    @Update
    suspend fun updateMessage(message: ChatMessage)
    
    @Delete
    suspend fun deleteMessage(message: ChatMessage)
    
    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: Long)
    
    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()
    
    @Query("SELECT * FROM chat_messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: Long): ChatMessage?
}

/**
 * 聊天會話數據訪問對象
 */
@Dao
interface ChatSessionDao {
    
    @Query("SELECT * FROM chat_sessions ORDER BY lastMessageAt DESC")
    fun getAllSessions(): Flow<List<ChatSession>>
    
    @Insert
    suspend fun insertSession(session: ChatSession): Long
    
    @Update
    suspend fun updateSession(session: ChatSession)
    
    @Delete
    suspend fun deleteSession(session: ChatSession)
    
    @Query("DELETE FROM chat_sessions")
    suspend fun deleteAllSessions()
    
    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): ChatSession?
}
