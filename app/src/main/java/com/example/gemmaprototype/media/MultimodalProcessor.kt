package com.example.gemmaprototype.media

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.example.gemmaprototype.data.MediaAttachment
import com.example.gemmaprototype.data.MediaType
import com.example.gemmaprototype.document.DocumentProcessor
import com.example.gemmaprototype.document.DocumentResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 多模態處理器
 * 處理不同類型的媒體內容，為AI模型提供統一的輸入格式
 */
class MultimodalProcessor(private val context: Context) {
    
    companion object {
        private const val TAG = "MultimodalProcessor"
    }
    
    /**
     * 處理媒體附件，生成描述文本
     */
    suspend fun processMediaAttachments(attachments: List<MediaAttachment>): String = withContext(Dispatchers.IO) {
        val descriptions = mutableListOf<String>()
        
        attachments.forEach { attachment ->
            try {
                val description = when (attachment.type) {
                    MediaType.IMAGE -> processImage(Uri.parse(attachment.path))
                    MediaType.VIDEO -> processVideo(Uri.parse(attachment.path))
                    MediaType.AUDIO -> processAudio(Uri.parse(attachment.path))
                    MediaType.DOCUMENT -> processDocument(Uri.parse(attachment.path), attachment.mimeType)
                }
                descriptions.add(description)
            } catch (e: Exception) {
                Log.e(TAG, "Error processing attachment: ${attachment.path}", e)
                descriptions.add("無法處理的媒體文件: ${attachment.path}")
            }
        }
        
        descriptions.joinToString("\n\n")
    }
    
    /**
     * 處理圖片
     */
    private suspend fun processImage(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            // 獲取圖片基本信息
            val mediaInfo = MediaUtils.getMediaInfo(context, uri)
            
            // 壓縮圖片以便分析
            val bitmap = MediaUtils.compressImage(context, uri, 512, 512, 80)
            
            if (bitmap != null) {
                // 分析圖片內容（簡化版本）
                val analysis = analyzeImage(bitmap)
                
                "圖片內容: $analysis\n" +
                "尺寸: ${mediaInfo.width}x${mediaInfo.height}\n" +
                "大小: ${MediaUtils.formatFileSize(mediaInfo.size)}"
            } else {
                "無法分析的圖片文件"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image", e)
            "圖片處理失敗: ${e.message}"
        }
    }
    
    /**
     * 處理視頻
     */
    private suspend fun processVideo(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val mediaInfo = MediaUtils.getMediaInfo(context, uri)
            
            "視頻文件\n" +
            "時長: ${mediaInfo.duration?.let { MediaUtils.formatDuration(it) } ?: "未知"}\n" +
            "尺寸: ${mediaInfo.width}x${mediaInfo.height}\n" +
            "大小: ${MediaUtils.formatFileSize(mediaInfo.size)}"
        } catch (e: Exception) {
            Log.e(TAG, "Error processing video", e)
            "視頻處理失敗: ${e.message}"
        }
    }
    
    /**
     * 處理音頻
     */
    private suspend fun processAudio(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val mediaInfo = MediaUtils.getMediaInfo(context, uri)
            
            "音頻文件\n" +
            "時長: ${mediaInfo.duration?.let { MediaUtils.formatDuration(it) } ?: "未知"}\n" +
            "大小: ${MediaUtils.formatFileSize(mediaInfo.size)}\n" +
            "格式: ${mediaInfo.mimeType}"
        } catch (e: Exception) {
            Log.e(TAG, "Error processing audio", e)
            "音頻處理失敗: ${e.message}"
        }
    }
    
    /**
     * 處理文檔
     */
    private suspend fun processDocument(uri: Uri, mimeType: String): String = withContext(Dispatchers.IO) {
        try {
            when (val result = DocumentProcessor.processDocument(context, uri, mimeType)) {
                is DocumentResult.Success -> {
                    "文檔內容:\n${result.summary}\n\n" +
                    "字數: ${result.wordCount}\n" +
                    "類型: ${DocumentProcessor.getDocumentTypeDescription(mimeType)}"
                }
                is DocumentResult.Error -> {
                    "文檔處理失敗: ${result.message}"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing document", e)
            "文檔處理失敗: ${e.message}"
        }
    }
    
    /**
     * 分析圖片內容（簡化版本）
     */
    private fun analyzeImage(bitmap: Bitmap): String {
        // 這是一個簡化的圖片分析實現
        // 實際應用中可以集成圖像識別API或本地模型
        
        val width = bitmap.width
        val height = bitmap.height
        val aspectRatio = width.toFloat() / height.toFloat()
        
        // 分析主要顏色
        val dominantColor = analyzeDominantColor(bitmap)
        
        // 分析圖片特徵
        val features = mutableListOf<String>()
        
        when {
            aspectRatio > 1.5 -> features.add("橫向圖片")
            aspectRatio < 0.7 -> features.add("縱向圖片")
            else -> features.add("方形圖片")
        }
        
        when {
            width * height > 2000000 -> features.add("高解析度")
            width * height > 500000 -> features.add("中等解析度")
            else -> features.add("低解析度")
        }
        
        features.add("主要顏色: $dominantColor")
        
        return features.joinToString(", ")
    }
    
    /**
     * 分析主要顏色
     */
    private fun analyzeDominantColor(bitmap: Bitmap): String {
        // 簡化的顏色分析
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        
        var redSum = 0L
        var greenSum = 0L
        var blueSum = 0L
        
        pixels.forEach { pixel ->
            redSum += (pixel shr 16) and 0xFF
            greenSum += (pixel shr 8) and 0xFF
            blueSum += pixel and 0xFF
        }
        
        val pixelCount = pixels.size
        val avgRed = (redSum / pixelCount).toInt()
        val avgGreen = (greenSum / pixelCount).toInt()
        val avgBlue = (blueSum / pixelCount).toInt()
        
        return when {
            avgRed > avgGreen && avgRed > avgBlue -> "偏紅色"
            avgGreen > avgRed && avgGreen > avgBlue -> "偏綠色"
            avgBlue > avgRed && avgBlue > avgGreen -> "偏藍色"
            avgRed + avgGreen + avgBlue < 384 -> "偏暗色"
            avgRed + avgGreen + avgBlue > 600 -> "偏亮色"
            else -> "中性色調"
        }
    }
    
    /**
     * 生成媒體摘要
     */
    fun generateMediaSummary(attachments: List<MediaAttachment>): String {
        if (attachments.isEmpty()) return ""
        
        val typeCount = attachments.groupBy { it.type }.mapValues { it.value.size }
        val summary = mutableListOf<String>()
        
        typeCount.forEach { (type, count) ->
            val typeName = when (type) {
                MediaType.IMAGE -> "圖片"
                MediaType.VIDEO -> "視頻"
                MediaType.AUDIO -> "音頻"
                MediaType.DOCUMENT -> "文檔"
            }
            summary.add("$count 個$typeName")
        }
        
        return "包含: ${summary.joinToString(", ")}"
    }
    
    /**
     * 檢查媒體是否需要處理
     */
    fun shouldProcessMedia(attachment: MediaAttachment): Boolean {
        return when (attachment.type) {
            MediaType.IMAGE -> MediaUtils.isSupportedImageFormat(attachment.mimeType)
            MediaType.VIDEO -> MediaUtils.isSupportedVideoFormat(attachment.mimeType)
            MediaType.AUDIO -> MediaUtils.isSupportedAudioFormat(attachment.mimeType)
            MediaType.DOCUMENT -> DocumentProcessor.isSupportedDocument(attachment.mimeType)
        }
    }
}
