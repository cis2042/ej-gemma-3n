package com.example.gemmaprototype.document

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 文檔處理器
 * 處理各種文檔格式的讀取和解析
 */
object DocumentProcessor {
    private const val TAG = "DocumentProcessor"
    
    /**
     * 處理文檔
     */
    fun processDocument(context: Context, uri: Uri, mimeType: String): DocumentResult {
        return try {
            when {
                mimeType.startsWith("text/") -> processTextFile(context, uri)
                mimeType == "application/pdf" -> processPdfFile(context, uri)
                mimeType.contains("word") -> processWordFile(context, uri)
                else -> DocumentResult.Error("不支持的文檔格式: $mimeType")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing document", e)
            DocumentResult.Error("文檔處理失敗: ${e.message}")
        }
    }
    
    /**
     * 處理文本文件
     */
    private fun processTextFile(context: Context, uri: Uri): DocumentResult {
        return try {
            val content = StringBuilder()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        content.appendLine(line)
                    }
                }
            }
            
            DocumentResult.Success(
                content = content.toString(),
                summary = generateSummary(content.toString()),
                wordCount = content.toString().split("\\s+".toRegex()).size
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error processing text file", e)
            DocumentResult.Error("文本文件處理失敗: ${e.message}")
        }
    }
    
    /**
     * 處理PDF文件（簡化版本）
     */
    private fun processPdfFile(context: Context, uri: Uri): DocumentResult {
        // 注意：這是一個簡化的實現
        // 實際應用中需要使用PDF處理庫如iText或PDFBox
        return try {
            // 這裡只是示例，實際需要PDF解析庫
            val content = "PDF文檔內容（需要PDF解析庫支持）"
            
            DocumentResult.Success(
                content = content,
                summary = "PDF文檔摘要",
                wordCount = content.split("\\s+".toRegex()).size
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error processing PDF file", e)
            DocumentResult.Error("PDF文件處理失敗: ${e.message}")
        }
    }
    
    /**
     * 處理Word文件（簡化版本）
     */
    private fun processWordFile(context: Context, uri: Uri): DocumentResult {
        // 注意：這是一個簡化的實現
        // 實際應用中需要使用Apache POI等庫
        return try {
            val content = "Word文檔內容（需要Apache POI庫支持）"
            
            DocumentResult.Success(
                content = content,
                summary = "Word文檔摘要",
                wordCount = content.split("\\s+".toRegex()).size
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Word file", e)
            DocumentResult.Error("Word文件處理失敗: ${e.message}")
        }
    }
    
    /**
     * 生成文檔摘要
     */
    private fun generateSummary(content: String): String {
        val lines = content.lines().filter { it.trim().isNotEmpty() }
        return when {
            lines.isEmpty() -> "空文檔"
            lines.size <= 3 -> content.take(200)
            else -> {
                val firstLines = lines.take(2).joinToString("\n")
                val lastLine = lines.last()
                "$firstLines\n...\n$lastLine"
            }
        }.take(300)
    }
    
    /**
     * 檢查是否為支持的文檔格式
     */
    fun isSupportedDocument(mimeType: String): Boolean {
        return when {
            mimeType.startsWith("text/") -> true
            mimeType == "application/pdf" -> true
            mimeType.contains("word") -> true
            mimeType.contains("document") -> true
            else -> false
        }
    }
    
    /**
     * 獲取文檔類型描述
     */
    fun getDocumentTypeDescription(mimeType: String): String {
        return when {
            mimeType.startsWith("text/plain") -> "純文本文件"
            mimeType.startsWith("text/") -> "文本文件"
            mimeType == "application/pdf" -> "PDF文檔"
            mimeType.contains("word") -> "Word文檔"
            mimeType.contains("document") -> "文檔文件"
            else -> "未知文檔類型"
        }
    }
}

/**
 * 文檔處理結果
 */
sealed class DocumentResult {
    data class Success(
        val content: String,
        val summary: String,
        val wordCount: Int
    ) : DocumentResult()
    
    data class Error(val message: String) : DocumentResult()
}

/**
 * 文檔信息
 */
data class DocumentInfo(
    val title: String,
    val type: String,
    val size: Long,
    val wordCount: Int,
    val summary: String
)
