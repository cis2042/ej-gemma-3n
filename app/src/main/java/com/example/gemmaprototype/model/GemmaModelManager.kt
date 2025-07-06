package com.example.gemmaprototype.model

import android.content.Context
import android.util.Log
import com.example.gemmaprototype.utils.DeviceCapabilityChecker
import com.example.gemmaprototype.utils.ModelUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Gemma 模型管理器
 * 負責模型的加載、推理和資源管理
 */
class GemmaModelManager(private val context: Context) {
    private var interpreter: Interpreter? = null
    private var tokenizer: GemmaTokenizer? = null
    private var gpuDelegate: GpuDelegate? = null
    private var nnApiDelegate: NnApiDelegate? = null
    
    private val modelFileName = "gemma_3n_2b_int8.tflite"
    private val maxSequenceLength = 2048
    private val maxNewTokens = 128
    
    companion object {
        private const val TAG = "GemmaModelManager"
    }
    
    /**
     * 初始化模型管理器
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Initializing Gemma model manager")
            
            // 檢查設備能力
            val memoryInfo = DeviceCapabilityChecker.getDeviceMemoryInfo(context)
            Log.d(TAG, "Device memory info: $memoryInfo")
            
            if (!DeviceCapabilityChecker.isDeviceSuitableForLargeModel(context)) {
                Log.w(TAG, "Device may not be suitable for large model")
            }
            
            // 初始化分詞器
            tokenizer = GemmaTokenizer(context)
            Log.d(TAG, "Tokenizer initialized with vocab size: ${tokenizer?.getVocabSize()}")
            
            // 加載模型
            val modelLoaded = loadModel()
            if (!modelLoaded) {
                Log.e(TAG, "Failed to load model")
                return@withContext false
            }
            
            Log.d(TAG, "Model manager initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize model manager", e)
            false
        }
    }
    
    /**
     * 加載 TensorFlow Lite 模型
     */
    private fun loadModel(): Boolean {
        return try {
            val modelFile = getOrCreateModelFile()
            if (!ModelUtils.isModelFileValid(modelFile)) {
                Log.e(TAG, "Model file is not valid")
                return false
            }
            
            Log.d(TAG, "Loading model from: ${modelFile.absolutePath}")
            Log.d(TAG, "Model file size: ${ModelUtils.formatFileSize(modelFile.length())}")
            
            val options = createInterpreterOptions()
            interpreter = Interpreter(modelFile, options)
            
            // 獲取模型輸入輸出信息
            logModelInfo()
            
            Log.d(TAG, "Model loaded successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load model", e)
            cleanup()
            false
        }
    }
    
    /**
     * 獲取或創建模型文件
     */
    private fun getOrCreateModelFile(): File {
        val modelFile = ModelUtils.getModelFile(context, modelFileName)
        
        // 如果模型文件不存在，嘗試從 assets 複製
        if (!ModelUtils.isModelFileValid(modelFile)) {
            Log.d(TAG, "Model file not found, attempting to copy from assets")
            
            val copied = ModelUtils.copyModelFromAssets(context, modelFileName, modelFile)
            if (!copied) {
                Log.w(TAG, "Could not copy model from assets, using placeholder")
                // 創建一個佔位符文件用於演示
                createPlaceholderModel(modelFile)
            }
        }
        
        return modelFile
    }
    
    /**
     * 創建佔位符模型（用於演示）
     */
    private fun createPlaceholderModel(modelFile: File) {
        try {
            modelFile.parentFile?.mkdirs()
            modelFile.createNewFile()
            Log.d(TAG, "Created placeholder model file")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create placeholder model", e)
        }
    }
    
    /**
     * 創建解釋器選項
     */
    private fun createInterpreterOptions(): Interpreter.Options {
        val options = Interpreter.Options()
        
        // 設置線程數
        val threadCount = DeviceCapabilityChecker.getRecommendedThreadCount()
        options.setNumThreads(threadCount)
        Log.d(TAG, "Using $threadCount threads")
        
        // 嘗試使用硬件加速
        if (DeviceCapabilityChecker.isNNAPIAvailable()) {
            try {
                nnApiDelegate = NnApiDelegate()
                options.addDelegate(nnApiDelegate!!)
                Log.d(TAG, "Using NNAPI acceleration")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to use NNAPI", e)
            }
        } else if (DeviceCapabilityChecker.isGPUDelegateAvailable()) {
            try {
                gpuDelegate = GpuDelegate()
                options.addDelegate(gpuDelegate!!)
                Log.d(TAG, "Using GPU acceleration")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to use GPU delegate", e)
            }
        }
        
        return options
    }
    
    /**
     * 記錄模型信息
     */
    private fun logModelInfo() {
        interpreter?.let { interp ->
            try {
                val inputCount = interp.inputTensorCount
                val outputCount = interp.outputTensorCount
                
                Log.d(TAG, "Model has $inputCount input(s) and $outputCount output(s)")
                
                for (i in 0 until inputCount) {
                    val shape = interp.getInputTensor(i).shape()
                    Log.d(TAG, "Input $i shape: ${shape.contentToString()}")
                }
                
                for (i in 0 until outputCount) {
                    val shape = interp.getOutputTensor(i).shape()
                    Log.d(TAG, "Output $i shape: ${shape.contentToString()}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not get model info", e)
            }
        }
    }
    
    /**
     * 生成文本
     */
    suspend fun generateText(
        prompt: String, 
        maxTokens: Int = maxNewTokens,
        temperature: Float = 0.7f,
        onProgress: ((String) -> Unit)? = null
    ): String = withContext(Dispatchers.IO) {
        
        if (interpreter == null || tokenizer == null) {
            return@withContext "模型未正確加載"
        }
        
        try {
            Log.d(TAG, "Generating text for prompt: $prompt")
            
            // 編碼輸入
            val inputIds = tokenizer!!.encode(prompt)
            Log.d(TAG, "Input tokens: ${inputIds.size}")
            
            // 模擬文本生成（實際實現需要根據具體模型調整）
            val result = simulateTextGeneration(prompt, maxTokens, onProgress)
            
            Log.d(TAG, "Generated text: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Text generation failed", e)
            "生成失敗: ${e.message}"
        }
    }
    
    /**
     * 模擬文本生成（佔位符實現）
     */
    private suspend fun simulateTextGeneration(
        prompt: String, 
        maxTokens: Int,
        onProgress: ((String) -> Unit)?
    ): String = withContext(Dispatchers.IO) {
        
        // 這是一個模擬實現，實際應用中需要實現真正的自回歸生成
        val responses = listOf(
            "這是一個基於 Gemma 3N 模型的演示回應。",
            "模型正在處理您的請求並生成相關內容。",
            "感謝您使用離線 AI 文本生成功能。",
            "這個原型展示了在 Android 設備上運行大型語言模型的可能性。"
        )
        
        val selectedResponse = responses.random()
        val words = selectedResponse.split(" ")
        val result = StringBuilder()
        
        // 模擬逐詞生成
        for (i in words.indices) {
            if (i > 0) result.append(" ")
            result.append(words[i])
            
            // 通知進度
            onProgress?.invoke(result.toString())
            
            // 模擬生成延遲
            kotlinx.coroutines.delay(200)
        }
        
        result.toString()
    }
    
    /**
     * 檢查模型是否已加載
     */
    fun isModelLoaded(): Boolean = interpreter != null && tokenizer != null
    
    /**
     * 清理資源
     */
    fun cleanup() {
        interpreter?.close()
        interpreter = null
        
        gpuDelegate?.close()
        gpuDelegate = null
        
        nnApiDelegate?.close()
        nnApiDelegate = null
        
        tokenizer = null
        
        Log.d(TAG, "Resources cleaned up")
    }
}
