package com.example.gemmaprototype.camera

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 相機管理器
 * 處理相機拍照和錄影功能
 */
class CameraManager(
    private val activity: AppCompatActivity,
    private val onImageCaptured: (Uri) -> Unit = {},
    private val onVideoCaptured: (Uri) -> Unit = {},
    private val onError: (String) -> Unit = {}
) {
    
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    
    private lateinit var cameraExecutor: ExecutorService
    
    // 系統相機啟動器
    private val systemCameraLauncher = activity.registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            onImageCaptured(currentPhotoUri!!)
        }
    }
    
    private val systemVideoLauncher = activity.registerForActivityResult(
        ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success && currentVideoUri != null) {
            onVideoCaptured(currentVideoUri!!)
        }
    }
    
    private var currentPhotoUri: Uri? = null
    private var currentVideoUri: Uri? = null
    
    companion object {
        private const val TAG = "CameraManager"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
    
    /**
     * 初始化相機
     */
    fun initialize() {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }
    
    /**
     * 啟動系統相機拍照
     */
    fun launchSystemCamera() {
        val photoFile = createImageFile()
        currentPhotoUri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            photoFile
        )
        systemCameraLauncher.launch(currentPhotoUri)
    }
    
    /**
     * 啟動系統相機錄影
     */
    fun launchSystemVideoRecorder() {
        val videoFile = createVideoFile()
        currentVideoUri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            videoFile
        )
        systemVideoLauncher.launch(currentVideoUri)
    }
    
    /**
     * 啟動自定義相機界面
     */
    fun startCustomCamera(previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(previewView)
            } catch (exc: Exception) {
                onError("相機初始化失敗: ${exc.message}")
            }
        }, ContextCompat.getMainExecutor(activity))
    }
    
    /**
     * 綁定相機用例
     */
    private fun bindCameraUseCases(previewView: PreviewView) {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")
        
        // 預覽用例
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        
        // 圖像捕獲用例
        imageCapture = ImageCapture.Builder().build()
        
        // 視頻捕獲用例
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
            .build()
        videoCapture = VideoCapture.withOutput(recorder)
        
        // 選擇後置相機
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        
        try {
            // 解綁所有用例
            cameraProvider.unbindAll()
            
            // 綁定用例到相機
            camera = cameraProvider.bindToLifecycle(
                activity, cameraSelector, preview, imageCapture, videoCapture
            )
        } catch (exc: Exception) {
            onError("相機綁定失敗: ${exc.message}")
        }
    }
    
    /**
     * 拍照
     */
    fun takePhoto() {
        val imageCapture = imageCapture ?: return
        
        val photoFile = createImageFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(activity),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    onError("拍照失敗: ${exception.message}")
                }
                
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    onImageCaptured(savedUri)
                }
            }
        )
    }
    
    /**
     * 開始錄影
     */
    fun startRecording() {
        val videoCapture = this.videoCapture ?: return
        
        val videoFile = createVideoFile()
        val outputOptions = FileOutputOptions.Builder(videoFile).build()
        
        recording = videoCapture.output
            .prepareRecording(activity, outputOptions)
            .apply {
                if (ContextCompat.checkSelfPermission(
                        activity,
                        android.Manifest.permission.RECORD_AUDIO
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(activity)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        // 錄影開始
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val savedUri = Uri.fromFile(videoFile)
                            onVideoCaptured(savedUri)
                        } else {
                            onError("錄影失敗: ${recordEvent.error}")
                        }
                    }
                }
            }
    }
    
    /**
     * 停止錄影
     */
    fun stopRecording() {
        recording?.stop()
        recording = null
    }
    
    /**
     * 檢查是否正在錄影
     */
    fun isRecording(): Boolean {
        return recording != null
    }
    
    /**
     * 切換相機（前置/後置）
     */
    fun switchCamera(previewView: PreviewView) {
        // 實現相機切換邏輯
        bindCameraUseCases(previewView)
    }
    
    /**
     * 創建圖片文件
     */
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = activity.getExternalFilesDir("Pictures")
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
    
    /**
     * 創建視頻文件
     */
    private fun createVideoFile(): File {
        val timeStamp = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val videoFileName = "MP4_${timeStamp}_"
        val storageDir = activity.getExternalFilesDir("Movies")
        return File.createTempFile(videoFileName, ".mp4", storageDir)
    }
    
    /**
     * 釋放資源
     */
    fun release() {
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
    }
}
