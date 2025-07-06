package com.example.gemmaprototype.utils

import android.content.Context
import android.os.Build
import android.util.Log
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate

/**
 * 設備能力檢測工具類
 * 用於檢測設備是否支持各種加速選項
 */
object DeviceCapabilityChecker {
    private const val TAG = "DeviceCapabilityChecker"
    
    /**
     * 檢測是否支持 NNAPI 加速
     */
    fun isNNAPIAvailable(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                val nnApiDelegate = NnApiDelegate()
                nnApiDelegate.close()
                Log.d(TAG, "NNAPI is available")
                true
            } else {
                Log.d(TAG, "NNAPI not available - Android version too low")
                false
            }
        } catch (e: Exception) {
            Log.w(TAG, "NNAPI not available: ${e.message}")
            false
        }
    }
    
    /**
     * 檢測是否支持 GPU 加速
     */
    fun isGPUDelegateAvailable(): Boolean {
        return try {
            val gpuDelegate = GpuDelegate()
            gpuDelegate.close()
            Log.d(TAG, "GPU delegate is available")
            true
        } catch (e: Exception) {
            Log.w(TAG, "GPU delegate not available: ${e.message}")
            false
        }
    }
    
    /**
     * 獲取設備內存信息
     */
    fun getDeviceMemoryInfo(context: Context): DeviceMemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val memoryInfo = android.app.ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val runtime = Runtime.getRuntime()
        val maxHeapSize = runtime.maxMemory()
        val usedHeapSize = runtime.totalMemory() - runtime.freeMemory()
        val availableHeapSize = maxHeapSize - usedHeapSize
        
        return DeviceMemoryInfo(
            totalRAM = memoryInfo.totalMem,
            availableRAM = memoryInfo.availMem,
            maxHeapSize = maxHeapSize,
            usedHeapSize = usedHeapSize,
            availableHeapSize = availableHeapSize,
            isLowMemory = memoryInfo.lowMemory
        )
    }
    
    /**
     * 檢測設備是否適合運行大型模型
     */
    fun isDeviceSuitableForLargeModel(context: Context): Boolean {
        val memoryInfo = getDeviceMemoryInfo(context)
        
        // 至少需要 3GB RAM 和 1GB 可用堆內存
        val minRAM = 3L * 1024 * 1024 * 1024 // 3GB
        val minHeap = 1L * 1024 * 1024 * 1024 // 1GB
        
        val suitable = memoryInfo.totalRAM >= minRAM && 
                      memoryInfo.availableHeapSize >= minHeap &&
                      !memoryInfo.isLowMemory
        
        Log.d(TAG, "Device suitable for large model: $suitable")
        Log.d(TAG, "Total RAM: ${memoryInfo.totalRAM / (1024 * 1024)}MB")
        Log.d(TAG, "Available heap: ${memoryInfo.availableHeapSize / (1024 * 1024)}MB")
        
        return suitable
    }
    
    /**
     * 獲取推薦的線程數
     */
    fun getRecommendedThreadCount(): Int {
        val cores = Runtime.getRuntime().availableProcessors()
        // 使用核心數的一半到全部，最少2個，最多8個
        return (cores / 2).coerceAtLeast(2).coerceAtMost(8)
    }
}

/**
 * 設備內存信息數據類
 */
data class DeviceMemoryInfo(
    val totalRAM: Long,
    val availableRAM: Long,
    val maxHeapSize: Long,
    val usedHeapSize: Long,
    val availableHeapSize: Long,
    val isLowMemory: Boolean
)
