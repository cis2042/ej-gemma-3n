package com.example.gemmaprototype.media

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.gemmaprototype.data.MediaAttachment
import com.example.gemmaprototype.data.MediaType
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 多媒體輸入管理器
 * 處理圖片、音頻、視頻、文檔的選擇和拍攝
 */
class MediaInputManager(
    private val activity: AppCompatActivity,
    private val onMediaSelected: (List<MediaAttachment>) -> Unit
) {
    
    private var currentPhotoUri: Uri? = null
    private var currentVideoUri: Uri? = null
    
    // 圖片選擇器
    private val imagePickerLauncher = activity.registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        handleSelectedMedia(uris, MediaType.IMAGE)
    }
    
    // 相機拍照
    private val cameraLauncher = activity.registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            handleSelectedMedia(listOf(currentPhotoUri!!), MediaType.IMAGE)
        }
    }
    
    // 視頻選擇器
    private val videoPickerLauncher = activity.registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { handleSelectedMedia(listOf(it), MediaType.VIDEO) }
    }
    
    // 視頻錄製
    private val videoRecorderLauncher = activity.registerForActivityResult(
        ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success && currentVideoUri != null) {
            handleSelectedMedia(listOf(currentVideoUri!!), MediaType.VIDEO)
        }
    }
    
    // 音頻選擇器
    private val audioPickerLauncher = activity.registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { handleSelectedMedia(listOf(it), MediaType.AUDIO) }
    }
    
    // 文檔選擇器
    private val documentPickerLauncher = activity.registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        handleSelectedMedia(uris, MediaType.DOCUMENT)
    }
    
    /**
     * 顯示媒體選擇選項
     */
    fun showMediaOptions() {
        val options = arrayOf(
            "拍照",
            "從相冊選擇圖片",
            "錄製視頻",
            "選擇視頻",
            "選擇音頻",
            "選擇文檔"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle("選擇媒體類型")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePicture()
                    1 -> selectImages()
                    2 -> recordVideo()
                    3 -> selectVideo()
                    4 -> selectAudio()
                    5 -> selectDocuments()
                }
            }
            .show()
    }
    
    /**
     * 拍照
     */
    private fun takePicture() {
        val photoFile = createImageFile()
        currentPhotoUri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            photoFile
        )
        cameraLauncher.launch(currentPhotoUri)
    }
    
    /**
     * 選擇圖片
     */
    private fun selectImages() {
        imagePickerLauncher.launch("image/*")
    }
    
    /**
     * 錄製視頻
     */
    private fun recordVideo() {
        val videoFile = createVideoFile()
        currentVideoUri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            videoFile
        )
        videoRecorderLauncher.launch(currentVideoUri)
    }
    
    /**
     * 選擇視頻
     */
    private fun selectVideo() {
        videoPickerLauncher.launch("video/*")
    }
    
    /**
     * 選擇音頻
     */
    private fun selectAudio() {
        audioPickerLauncher.launch("audio/*")
    }
    
    /**
     * 選擇文檔
     */
    private fun selectDocuments() {
        documentPickerLauncher.launch("*/*")
    }
    
    /**
     * 處理選中的媒體
     */
    private fun handleSelectedMedia(uris: List<Uri>, mediaType: MediaType) {
        val attachments = uris.mapNotNull { uri ->
            try {
                val mediaInfo = MediaUtils.getMediaInfo(activity, uri)
                MediaAttachment(
                    path = uri.toString(),
                    type = mediaType,
                    mimeType = mediaInfo.mimeType,
                    size = mediaInfo.size,
                    duration = mediaInfo.duration,
                    width = mediaInfo.width,
                    height = mediaInfo.height
                )
            } catch (e: Exception) {
                null
            }
        }
        
        if (attachments.isNotEmpty()) {
            onMediaSelected(attachments)
        }
    }
    
    /**
     * 創建圖片文件
     */
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = activity.getExternalFilesDir("Pictures")
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
    
    /**
     * 創建視頻文件
     */
    private fun createVideoFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val videoFileName = "MP4_${timeStamp}_"
        val storageDir = activity.getExternalFilesDir("Movies")
        return File.createTempFile(videoFileName, ".mp4", storageDir)
    }
}

/**
 * 媒體信息數據類
 */
data class MediaInfo(
    val mimeType: String,
    val size: Long,
    val duration: Long? = null,
    val width: Int? = null,
    val height: Int? = null
)
