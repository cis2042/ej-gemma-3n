package com.example.gemmaprototype.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.IOException

/**
 * 媒體處理工具類
 */
object MediaUtils {
    private const val TAG = "MediaUtils"
    
    /**
     * 獲取媒體文件信息
     */
    fun getMediaInfo(context: Context, uri: Uri): MediaInfo {
        val contentResolver = context.contentResolver
        var mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
        var size = 0L
        var duration: Long? = null
        var width: Int? = null
        var height: Int? = null
        
        // 獲取文件大小和名稱
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }
        
        // 根據MIME類型獲取特定信息
        when {
            mimeType.startsWith("image/") -> {
                val imageInfo = getImageInfo(context, uri)
                width = imageInfo.first
                height = imageInfo.second
            }
            mimeType.startsWith("video/") -> {
                val videoInfo = getVideoInfo(context, uri)
                duration = videoInfo.first
                width = videoInfo.second
                height = videoInfo.third
            }
            mimeType.startsWith("audio/") -> {
                duration = getAudioDuration(context, uri)
            }
        }
        
        return MediaInfo(mimeType, size, duration, width, height)
    }
    
    /**
     * 獲取圖片信息
     */
    private fun getImageInfo(context: Context, uri: Uri): Pair<Int, Int> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
            
            Pair(options.outWidth, options.outHeight)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting image info", e)
            Pair(0, 0)
        }
    }
    
    /**
     * 獲取視頻信息
     */
    private fun getVideoInfo(context: Context, uri: Uri): Triple<Long, Int, Int> {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            
            Triple(duration, width, height)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting video info", e)
            Triple(0L, 0, 0)
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing MediaMetadataRetriever", e)
            }
        }
    }
    
    /**
     * 獲取音頻時長
     */
    private fun getAudioDuration(context: Context, uri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error getting audio duration", e)
            0L
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing MediaMetadataRetriever", e)
            }
        }
    }
    
    /**
     * 壓縮圖片
     */
    fun compressImage(context: Context, uri: Uri, maxWidth: Int = 1024, maxHeight: Int = 1024, quality: Int = 80): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (originalBitmap == null) return null
            
            val ratio = Math.min(
                maxWidth.toFloat() / originalBitmap.width,
                maxHeight.toFloat() / originalBitmap.height
            )
            
            if (ratio >= 1) return originalBitmap
            
            val newWidth = (originalBitmap.width * ratio).toInt()
            val newHeight = (originalBitmap.height * ratio).toInt()
            
            Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image", e)
            null
        }
    }
    
    /**
     * 獲取圖片旋轉角度
     */
    fun getImageRotation(context: Context, uri: Uri): Int {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val exif = ExifInterface(inputStream!!)
            inputStream.close()
            
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting image rotation", e)
            0
        }
    }
    
    /**
     * 格式化文件大小
     */
    fun formatFileSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return "%.1f %s".format(size, units[unitIndex])
    }
    
    /**
     * 格式化時長
     */
    fun formatDuration(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        
        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes % 60, seconds % 60)
            else -> String.format("%d:%02d", minutes, seconds % 60)
        }
    }
    
    /**
     * 檢查是否為支持的圖片格式
     */
    fun isSupportedImageFormat(mimeType: String): Boolean {
        return mimeType in listOf(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/bmp"
        )
    }
    
    /**
     * 檢查是否為支持的視頻格式
     */
    fun isSupportedVideoFormat(mimeType: String): Boolean {
        return mimeType in listOf(
            "video/mp4",
            "video/3gpp",
            "video/webm",
            "video/mkv",
            "video/avi"
        )
    }
    
    /**
     * 檢查是否為支持的音頻格式
     */
    fun isSupportedAudioFormat(mimeType: String): Boolean {
        return mimeType in listOf(
            "audio/mpeg",
            "audio/mp4",
            "audio/wav",
            "audio/ogg",
            "audio/3gpp",
            "audio/amr"
        )
    }
}
