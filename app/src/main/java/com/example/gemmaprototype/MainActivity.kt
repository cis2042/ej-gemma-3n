package com.example.gemmaprototype

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gemmaprototype.data.*
import com.example.gemmaprototype.databinding.ActivityMainBinding
import com.example.gemmaprototype.media.MediaInputManager
import com.example.gemmaprototype.model.GemmaModelManager
import com.example.gemmaprototype.speech.SpeechManager
import com.example.gemmaprototype.ui.ChatAdapter
import com.example.gemmaprototype.ui.MediaPreviewAdapter
import com.example.gemmaprototype.utils.DeviceCapabilityChecker
import com.example.gemmaprototype.utils.PermissionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * 主活動
 * 處理用戶界面和模型交互
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var gemmaManager: GemmaModelManager? = null
    private var isGenerating = false

    // 數據庫和適配器
    private lateinit var database: ChatDatabase
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var mediaPreviewAdapter: MediaPreviewAdapter

    // 管理器
    private lateinit var permissionManager: PermissionManager
    private lateinit var mediaInputManager: MediaInputManager
    private lateinit var speechManager: SpeechManager

    // 當前會話和媒體附件
    private var currentSessionId = 1L
    private val currentMediaAttachments = mutableListOf<MediaAttachment>()
    private var isVoiceMode = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeDatabase()
        initializeManagers()
        setupUI()
        setupRecyclerViews()
        checkDeviceCapability()
        initializeModel()
        requestPermissions()
    }
    
    /**
     * 初始化數據庫
     */
    private fun initializeDatabase() {
        database = ChatDatabase.getDatabase(this)
    }

    /**
     * 初始化管理器
     */
    private fun initializeManagers() {
        // 權限管理器
        permissionManager = PermissionManager(this) { permission, granted ->
            handlePermissionResult(permission, granted)
        }

        // 媒體輸入管理器
        mediaInputManager = MediaInputManager(this) { attachments ->
            handleMediaSelected(attachments)
        }

        // 語音管理器
        speechManager = SpeechManager(
            context = this,
            onSpeechResult = { text -> handleSpeechResult(text) },
            onSpeechError = { error -> handleSpeechError(error) },
            onTtsReady = { /* TTS ready */ },
            onRecordingStateChanged = { isRecording -> handleRecordingStateChanged(isRecording) }
        )
        speechManager.initialize()
    }

    /**
     * 設置用戶界面
     */
    private fun setupUI() {
        // 設置工具欄
        setSupportActionBar(binding.toolbar)

        // 設置輸入框
        binding.messageEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }

        // 設置按鈕點擊事件
        binding.attachButton.setOnClickListener {
            if (permissionManager.isStoragePermissionGranted()) {
                mediaInputManager.showMediaOptions()
            } else {
                permissionManager.requestStoragePermission()
            }
        }

        binding.sendButton.setOnClickListener {
            if (isVoiceMode) {
                handleVoiceInput()
            } else {
                sendMessage()
            }
        }

        // 長按發送按鈕進入語音模式
        binding.sendButton.setOnLongClickListener {
            startVoiceInput()
            true
        }
    }

    /**
     * 設置RecyclerView
     */
    private fun setupRecyclerViews() {
        // 聊天消息列表
        chatAdapter = ChatAdapter { mediaPath ->
            // 處理媒體點擊
            handleMediaClick(mediaPath)
        }

        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = chatAdapter
        }

        // 媒體預覽列表
        mediaPreviewAdapter = MediaPreviewAdapter { attachment ->
            removeMediaAttachment(attachment)
        }

        binding.mediaPreviewRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = mediaPreviewAdapter
        }

        // 觀察聊天消息
        lifecycleScope.launch {
            database.chatDao().getMessagesForSession(currentSessionId).collectLatest { messages ->
                chatAdapter.submitList(messages)
                if (messages.isNotEmpty()) {
                    binding.chatRecyclerView.scrollToPosition(messages.size - 1)
                }
            }
        }
    }
    
    /**
     * 請求權限
     */
    private fun requestPermissions() {
        permissionManager.requestAllPermissions()
    }

    /**
     * 檢查設備能力
     */
    private fun checkDeviceCapability() {
        lifecycleScope.launch(Dispatchers.IO) {
            val memoryInfo = DeviceCapabilityChecker.getDeviceMemoryInfo(this@MainActivity)
            val isSuitable = DeviceCapabilityChecker.isDeviceSuitableForLargeModel(this@MainActivity)

            withContext(Dispatchers.Main) {
                if (!isSuitable) {
                    Toast.makeText(
                        this@MainActivity,
                        "設備內存可能不足，運行可能較慢",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    /**
     * 初始化模型
     */
    private fun initializeModel() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    binding.modelStatusText.text = getString(R.string.model_loading)
                }

                gemmaManager = GemmaModelManager(this@MainActivity)
                val initialized = gemmaManager!!.initialize()

                withContext(Dispatchers.Main) {
                    if (initialized) {
                        binding.modelStatusText.text = getString(R.string.model_loaded)
                        Toast.makeText(
                            this@MainActivity,
                            "模型加載成功",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        binding.modelStatusText.text = getString(R.string.model_load_failed)
                        Toast.makeText(
                            this@MainActivity,
                            "模型加載失敗，請檢查設備存儲空間",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.modelStatusText.text = getString(R.string.model_load_failed)
                    Toast.makeText(
                        this@MainActivity,
                        "初始化失敗: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    /**
     * 發送消息
     */
    private fun sendMessage() {
        val messageText = binding.messageEditText.text.toString().trim()
        if (messageText.isEmpty() && currentMediaAttachments.isEmpty()) {
            Toast.makeText(this, "請輸入消息或選擇媒體", Toast.LENGTH_SHORT).show()
            return
        }

        if (isGenerating) {
            Toast.makeText(this, "正在生成中，請稍候", Toast.LENGTH_SHORT).show()
            return
        }

        // 創建用戶消息
        val userMessage = ChatMessage(
            sessionId = currentSessionId,
            content = messageText,
            isFromUser = true,
            timestamp = Date(),
            messageType = if (currentMediaAttachments.isNotEmpty()) MessageType.MIXED else MessageType.TEXT,
            mediaPath = currentMediaAttachments.firstOrNull()?.path
        )

        // 保存用戶消息到數據庫
        lifecycleScope.launch(Dispatchers.IO) {
            database.chatDao().insertMessage(userMessage)

            // 清空輸入
            withContext(Dispatchers.Main) {
                binding.messageEditText.text?.clear()
                clearMediaAttachments()
            }

            // 生成AI回應
            generateAIResponse(messageText, currentMediaAttachments.toList())
        }
    }

    /**
     * 生成AI回應
     */
    private suspend fun generateAIResponse(prompt: String, attachments: List<MediaAttachment>) {
        if (gemmaManager?.isModelLoaded() != true) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "模型未加載", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // 創建AI消息（處理中狀態）
        val aiMessage = ChatMessage(
            sessionId = currentSessionId,
            content = "正在思考...",
            isFromUser = false,
            timestamp = Date(),
            messageType = MessageType.TEXT,
            isProcessing = true
        )

        val messageId = database.chatDao().insertMessage(aiMessage)
        isGenerating = true

        try {
            // 構建完整的提示詞
            val fullPrompt = buildPromptWithMedia(prompt, attachments)

            val result = gemmaManager!!.generateText(
                prompt = fullPrompt,
                maxTokens = 128,
                onProgress = { partialResult ->
                    // 更新消息內容
                    lifecycleScope.launch(Dispatchers.IO) {
                        val updatedMessage = aiMessage.copy(
                            id = messageId,
                            content = partialResult,
                            isProcessing = true
                        )
                        database.chatDao().updateMessage(updatedMessage)
                    }
                }
            )

            // 更新最終結果
            val finalMessage = aiMessage.copy(
                id = messageId,
                content = result,
                isProcessing = false
            )
            database.chatDao().updateMessage(finalMessage)

            // 如果啟用了語音輸出，朗讀結果
            withContext(Dispatchers.Main) {
                if (speechManager.isTtsAvailable()) {
                    speechManager.speak(result)
                }
            }

        } catch (e: Exception) {
            val errorMessage = aiMessage.copy(
                id = messageId,
                content = "生成失敗: ${e.message}",
                isProcessing = false
            )
            database.chatDao().updateMessage(errorMessage)

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@MainActivity,
                    "生成失敗: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } finally {
            isGenerating = false
        }
    }
    
    /**
     * 構建包含媒體信息的提示詞
     */
    private fun buildPromptWithMedia(prompt: String, attachments: List<MediaAttachment>): String {
        if (attachments.isEmpty()) return prompt

        val mediaDescriptions = attachments.map { attachment ->
            when (attachment.type) {
                MediaType.IMAGE -> "圖片: ${attachment.path}"
                MediaType.VIDEO -> "視頻: ${attachment.path}"
                MediaType.AUDIO -> "音頻: ${attachment.path}"
                MediaType.DOCUMENT -> "文檔: ${attachment.path}"
            }
        }

        return "$prompt\n\n附件:\n${mediaDescriptions.joinToString("\n")}"
    }

    /**
     * 處理媒體選擇
     */
    private fun handleMediaSelected(attachments: List<MediaAttachment>) {
        currentMediaAttachments.addAll(attachments)
        updateMediaPreview()
    }

    /**
     * 移除媒體附件
     */
    private fun removeMediaAttachment(attachment: MediaAttachment) {
        currentMediaAttachments.remove(attachment)
        updateMediaPreview()
    }

    /**
     * 清空媒體附件
     */
    private fun clearMediaAttachments() {
        currentMediaAttachments.clear()
        updateMediaPreview()
    }

    /**
     * 更新媒體預覽
     */
    private fun updateMediaPreview() {
        mediaPreviewAdapter.submitList(currentMediaAttachments.toList())
        binding.mediaPreviewRecyclerView.visibility =
            if (currentMediaAttachments.isNotEmpty()) View.VISIBLE else View.GONE
    }

    /**
     * 處理媒體點擊
     */
    private fun handleMediaClick(mediaPath: String) {
        // 這裡可以實現媒體查看功能
        Toast.makeText(this, "點擊了媒體: $mediaPath", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 開始語音輸入
     */
    private fun startVoiceInput() {
        if (!permissionManager.isAudioPermissionGranted()) {
            permissionManager.requestAudioPermission()
            return
        }

        isVoiceMode = true
        binding.voiceRecordingOverlay.visibility = View.VISIBLE
        binding.sendButton.setImageResource(R.drawable.ic_mic)

        speechManager.startSpeechRecognition()
    }

    /**
     * 處理語音輸入
     */
    private fun handleVoiceInput() {
        if (isVoiceMode) {
            stopVoiceInput()
        } else {
            startVoiceInput()
        }
    }

    /**
     * 停止語音輸入
     */
    private fun stopVoiceInput() {
        isVoiceMode = false
        binding.voiceRecordingOverlay.visibility = View.GONE
        binding.sendButton.setImageResource(R.drawable.ic_send)

        speechManager.stopSpeechRecognition()
    }

    /**
     * 處理語音識別結果
     */
    private fun handleSpeechResult(text: String) {
        binding.messageEditText.setText(text)
        stopVoiceInput()
        // 自動發送語音識別的文本
        sendMessage()
    }

    /**
     * 處理語音識別錯誤
     */
    private fun handleSpeechError(error: String) {
        stopVoiceInput()
        Toast.makeText(this, "語音識別錯誤: $error", Toast.LENGTH_SHORT).show()
    }

    /**
     * 處理錄音狀態變化
     */
    private fun handleRecordingStateChanged(isRecording: Boolean) {
        // 可以在這裡更新錄音UI狀態
        if (isRecording) {
            // 開始錄音動畫或指示
        } else {
            // 停止錄音動畫或指示
        }
    }
    
    /**
     * 處理權限結果
     */
    private fun handlePermissionResult(permission: String, granted: Boolean) {
        val permissionName = permissionManager.getPermissionName(permission)
        if (granted) {
            Toast.makeText(this, "${permissionName}權限已授予", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "${permissionName}權限被拒絕", Toast.LENGTH_SHORT).show()

            // 顯示權限說明
            when (permission) {
                android.Manifest.permission.CAMERA -> {
                    if (permissionManager.shouldShowRequestPermissionRationale(permission)) {
                        permissionManager.showCameraPermissionRationale {
                            permissionManager.requestCameraPermission()
                        }
                    }
                }
                android.Manifest.permission.RECORD_AUDIO -> {
                    if (permissionManager.shouldShowRequestPermissionRationale(permission)) {
                        permissionManager.showAudioPermissionRationale {
                            permissionManager.requestAudioPermission()
                        }
                    }
                }
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_MEDIA_IMAGES -> {
                    if (permissionManager.shouldShowRequestPermissionRationale(permission)) {
                        permissionManager.showStoragePermissionRationale {
                            permissionManager.requestStoragePermission()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gemmaManager?.cleanup()
        speechManager.release()
    }

    override fun onPause() {
        super.onPause()
        // 停止語音相關操作
        if (isVoiceMode) {
            stopVoiceInput()
        }
        speechManager.stopSpeaking()
    }

    override fun onResume() {
        super.onResume()
        // 恢復時檢查狀態
    }
}
