package com.example.gemmaprototype.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * 聊天數據庫
 */
@Database(
    entities = [ChatMessage::class, ChatSession::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ChatDatabase : RoomDatabase() {
    
    abstract fun chatDao(): ChatDao
    abstract fun sessionDao(): ChatSessionDao
    
    companion object {
        @Volatile
        private var INSTANCE: ChatDatabase? = null
        
        fun getDatabase(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    "chat_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
