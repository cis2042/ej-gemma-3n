package com.example.gemmaprototype.speech

import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume

/**
 * 語音處理管理器
 * 處理語音識別和語音合成
 */
class SpeechManager(
    private val context: Context,
    private val onSpeechResult: (String) -> Unit = {},
    private val onSpeechError: (String) -> Unit = {},
    private val onTtsReady: () -> Unit = {},
    private val onRecordingStateChanged: (Boolean) -> Unit = {}
) {
    
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false
    private var isTtsInitialized = false
    
    companion object {
        private const val TAG = "SpeechManager"
    }
    
    /**
     * 初始化語音服務
     */
    fun initialize() {
        initializeSpeechRecognizer()
        initializeTextToSpeech()
    }
    
    /**
     * 初始化語音識別
     */
    private fun initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "Ready for speech")
                }
                
                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "Beginning of speech")
                }
                
                override fun onRmsChanged(rmsdB: Float) {
                    // 可以用於顯示音量變化
                }
                
                override fun onBufferReceived(buffer: ByteArray?) {
                    // 音頻緩衝區接收
                }
                
                override fun onEndOfSpeech() {
                    Log.d(TAG, "End of speech")
                    onRecordingStateChanged(false)
                }
                
                override fun onError(error: Int) {
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "音頻錯誤"
                        SpeechRecognizer.ERROR_CLIENT -> "客戶端錯誤"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "權限不足"
                        SpeechRecognizer.ERROR_NETWORK -> "網絡錯誤"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "網絡超時"
                        SpeechRecognizer.ERROR_NO_MATCH -> "無匹配結果"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "識別器忙碌"
                        SpeechRecognizer.ERROR_SERVER -> "服務器錯誤"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "語音超時"
                        else -> "未知錯誤"
                    }
                    Log.e(TAG, "Speech recognition error: $errorMessage")
                    onSpeechError(errorMessage)
                    onRecordingStateChanged(false)
                }
                
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val recognizedText = matches[0]
                        Log.d(TAG, "Speech recognition result: $recognizedText")
                        onSpeechResult(recognizedText)
                    }
                    onRecordingStateChanged(false)
                }
                
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        // 可以用於實時顯示部分識別結果
                        Log.d(TAG, "Partial result: ${matches[0]}")
                    }
                }
                
                override fun onEvent(eventType: Int, params: Bundle?) {
                    // 其他事件處理
                }
            })
        } else {
            Log.e(TAG, "Speech recognition not available")
            onSpeechError("語音識別不可用")
        }
    }
    
    /**
     * 初始化文字轉語音
     */
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.CHINESE)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // 嘗試使用英語
                    textToSpeech?.setLanguage(Locale.ENGLISH)
                }
                
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        Log.d(TAG, "TTS started: $utteranceId")
                    }
                    
                    override fun onDone(utteranceId: String?) {
                        Log.d(TAG, "TTS completed: $utteranceId")
                    }
                    
                    override fun onError(utteranceId: String?) {
                        Log.e(TAG, "TTS error: $utteranceId")
                    }
                })
                
                isTtsInitialized = true
                onTtsReady()
                Log.d(TAG, "TextToSpeech initialized successfully")
            } else {
                Log.e(TAG, "TextToSpeech initialization failed")
            }
        }
    }
    
    /**
     * 開始語音識別
     */
    fun startSpeechRecognition() {
        if (speechRecognizer == null) {
            onSpeechError("語音識別器未初始化")
            return
        }
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        
        try {
            speechRecognizer?.startListening(intent)
            onRecordingStateChanged(true)
            Log.d(TAG, "Speech recognition started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition", e)
            onSpeechError("啟動語音識別失敗")
        }
    }
    
    /**
     * 停止語音識別
     */
    fun stopSpeechRecognition() {
        speechRecognizer?.stopListening()
        onRecordingStateChanged(false)
        Log.d(TAG, "Speech recognition stopped")
    }
    
    /**
     * 開始錄音
     */
    fun startRecording(): File? {
        if (isRecording) return null
        
        try {
            audioFile = createAudioFile()
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }
            
            isRecording = true
            onRecordingStateChanged(true)
            Log.d(TAG, "Recording started: ${audioFile?.absolutePath}")
            return audioFile
        } catch (e: IOException) {
            Log.e(TAG, "Error starting recording", e)
            onSpeechError("開始錄音失敗")
            return null
        }
    }
    
    /**
     * 停止錄音
     */
    fun stopRecording(): File? {
        if (!isRecording) return null
        
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            onRecordingStateChanged(false)
            Log.d(TAG, "Recording stopped: ${audioFile?.absolutePath}")
            return audioFile
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
            onSpeechError("停止錄音失敗")
            return null
        }
    }
    
    /**
     * 文字轉語音
     */
    fun speak(text: String) {
        if (!isTtsInitialized) {
            Log.w(TAG, "TTS not initialized")
            return
        }
        
        val utteranceId = UUID.randomUUID().toString()
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        Log.d(TAG, "Speaking: $text")
    }
    
    /**
     * 停止語音播放
     */
    fun stopSpeaking() {
        textToSpeech?.stop()
        Log.d(TAG, "Stopped speaking")
    }
    
    /**
     * 創建音頻文件
     */
    private fun createAudioFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val audioFileName = "AUDIO_${timeStamp}_"
        val storageDir = context.getExternalFilesDir("Audio")
        return File.createTempFile(audioFileName, ".3gp", storageDir)
    }
    
    /**
     * 檢查是否正在錄音
     */
    fun isRecording(): Boolean = isRecording
    
    /**
     * 檢查TTS是否可用
     */
    fun isTtsAvailable(): Boolean = isTtsInitialized
    
    /**
     * 釋放資源
     */
    fun release() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        
        if (isRecording) {
            stopRecording()
        }
        
        Log.d(TAG, "SpeechManager released")
    }
}
