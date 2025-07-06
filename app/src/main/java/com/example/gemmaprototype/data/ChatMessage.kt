package com.example.gemmaprototype.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 聊天消息數據類
 */
@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long = 1, // 默認會話ID
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Date = Date(),
    val messageType: MessageType = MessageType.TEXT,
    val mediaPath: String? = null,
    val mediaType: String? = null,
    val isProcessing: Boolean = false
)

/**
 * 消息類型枚舉
 */
enum class MessageType {
    TEXT,           // 純文字消息
    IMAGE,          // 圖片消息
    AUDIO,          // 音頻消息
    VIDEO,          // 視頻消息
    DOCUMENT,       // 文檔消息
    MIXED           // 混合類型消息
}

/**
 * 媒體附件數據類
 */
data class MediaAttachment(
    val path: String,
    val type: MediaType,
    val mimeType: String,
    val size: Long,
    val duration: Long? = null, // 音頻/視頻時長（毫秒）
    val width: Int? = null,     // 圖片/視頻寬度
    val height: Int? = null     // 圖片/視頻高度
)

/**
 * 媒體類型枚舉
 */
enum class MediaType {
    IMAGE,
    AUDIO,
    VIDEO,
    DOCUMENT
}

/**
 * 對話會話數據類
 */
@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val createdAt: Date = Date(),
    val lastMessageAt: Date = Date(),
    val messageCount: Int = 0
)
