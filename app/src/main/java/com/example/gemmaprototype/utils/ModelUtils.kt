package com.example.gemmaprototype.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 模型相關工具類
 */
object ModelUtils {
    private const val TAG = "ModelUtils"
    
    /**
     * 從 assets 複製模型文件到外部存儲
     */
    fun copyModelFromAssets(context: Context, assetFileName: String, destFile: File): Boolean {
        return try {
            Log.d(TAG, "Copying model from assets: $assetFileName to ${destFile.absolutePath}")
            
            // 確保目標目錄存在
            destFile.parentFile?.mkdirs()
            
            context.assets.open("models/$assetFileName").use { input ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytes = 0L
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytes += bytesRead
                    }
                    
                    Log.d(TAG, "Successfully copied $totalBytes bytes")
                }
            }
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to copy model from assets", e)
            false
        }
    }
    
    /**
     * 檢查模型文件是否存在且有效
     */
    fun isModelFileValid(file: File, expectedMinSize: Long = 100 * 1024 * 1024): Boolean {
        return file.exists() && file.isFile && file.length() >= expectedMinSize
    }
    
    /**
     * 獲取模型文件路徑
     */
    fun getModelFile(context: Context, modelFileName: String): File {
        return File(context.getExternalFilesDir("models"), modelFileName)
    }
    
    /**
     * 清理舊的模型文件
     */
    fun cleanupOldModels(context: Context, keepFiles: Set<String> = emptySet()) {
        val modelsDir = File(context.getExternalFilesDir("models"), "")
        if (modelsDir.exists() && modelsDir.isDirectory) {
            modelsDir.listFiles()?.forEach { file ->
                if (file.isFile && !keepFiles.contains(file.name)) {
                    Log.d(TAG, "Deleting old model file: ${file.name}")
                    file.delete()
                }
            }
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
}
