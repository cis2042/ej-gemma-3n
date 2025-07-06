package com.example.gemmaprototype.data

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room 數據庫類型轉換器
 */
class Converters {
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromMessageType(value: MessageType): String {
        return value.name
    }
    
    @TypeConverter
    fun toMessageType(value: String): MessageType {
        return MessageType.valueOf(value)
    }
}
