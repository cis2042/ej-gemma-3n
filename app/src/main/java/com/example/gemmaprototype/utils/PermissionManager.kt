package com.example.gemmaprototype.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * 權限管理器
 * 處理應用所需的各種權限請求
 */
class PermissionManager(
    private val activity: AppCompatActivity,
    private val onPermissionResult: (String, Boolean) -> Unit
) {
    
    // 權限請求啟動器
    private val permissionLauncher: ActivityResultLauncher<Array<String>> = 
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach { entry ->
                onPermissionResult(entry.key, entry.value)
            }
        }
    
    companion object {
        // 相機相關權限
        val CAMERA_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )
        
        // 音頻相關權限
        val AUDIO_PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO
        )
        
        // 存儲相關權限
        val STORAGE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        
        // 所有權限
        val ALL_PERMISSIONS = CAMERA_PERMISSIONS + AUDIO_PERMISSIONS + STORAGE_PERMISSIONS
    }
    
    /**
     * 檢查單個權限是否已授予
     */
    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 檢查多個權限是否都已授予
     */
    fun arePermissionsGranted(permissions: Array<String>): Boolean {
        return permissions.all { isPermissionGranted(it) }
    }
    
    /**
     * 檢查相機權限
     */
    fun isCameraPermissionGranted(): Boolean {
        return arePermissionsGranted(CAMERA_PERMISSIONS)
    }
    
    /**
     * 檢查音頻權限
     */
    fun isAudioPermissionGranted(): Boolean {
        return arePermissionsGranted(AUDIO_PERMISSIONS)
    }
    
    /**
     * 檢查存儲權限
     */
    fun isStoragePermissionGranted(): Boolean {
        return arePermissionsGranted(STORAGE_PERMISSIONS)
    }
    
    /**
     * 請求相機權限
     */
    fun requestCameraPermission() {
        requestPermissions(CAMERA_PERMISSIONS)
    }
    
    /**
     * 請求音頻權限
     */
    fun requestAudioPermission() {
        requestPermissions(AUDIO_PERMISSIONS)
    }
    
    /**
     * 請求存儲權限
     */
    fun requestStoragePermission() {
        requestPermissions(STORAGE_PERMISSIONS)
    }
    
    /**
     * 請求所有權限
     */
    fun requestAllPermissions() {
        val deniedPermissions = ALL_PERMISSIONS.filter { !isPermissionGranted(it) }
        if (deniedPermissions.isNotEmpty()) {
            requestPermissions(deniedPermissions.toTypedArray())
        }
    }
    
    /**
     * 請求指定權限
     */
    fun requestPermissions(permissions: Array<String>) {
        val deniedPermissions = permissions.filter { !isPermissionGranted(it) }
        if (deniedPermissions.isNotEmpty()) {
            permissionLauncher.launch(deniedPermissions.toTypedArray())
        } else {
            // 所有權限都已授予
            permissions.forEach { permission ->
                onPermissionResult(permission, true)
            }
        }
    }
    
    /**
     * 檢查是否需要顯示權限說明
     */
    fun shouldShowRequestPermissionRationale(permission: String): Boolean {
        return activity.shouldShowRequestPermissionRationale(permission)
    }
    
    /**
     * 顯示權限說明對話框
     */
    fun showPermissionRationale(
        permission: String,
        title: String,
        message: String,
        onPositive: () -> Unit,
        onNegative: () -> Unit = {}
    ) {
        androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("授予權限") { _, _ -> onPositive() }
            .setNegativeButton("取消") { _, _ -> onNegative() }
            .setCancelable(false)
            .show()
    }
    
    /**
     * 顯示相機權限說明
     */
    fun showCameraPermissionRationale(onPositive: () -> Unit) {
        showPermissionRationale(
            Manifest.permission.CAMERA,
            "需要相機權限",
            "此功能需要使用相機來拍攝照片和錄製視頻，請授予相機權限。",
            onPositive
        )
    }
    
    /**
     * 顯示音頻權限說明
     */
    fun showAudioPermissionRationale(onPositive: () -> Unit) {
        showPermissionRationale(
            Manifest.permission.RECORD_AUDIO,
            "需要麥克風權限",
            "此功能需要使用麥克風來錄製音頻和語音識別，請授予麥克風權限。",
            onPositive
        )
    }
    
    /**
     * 顯示存儲權限說明
     */
    fun showStoragePermissionRationale(onPositive: () -> Unit) {
        showPermissionRationale(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            },
            "需要存儲權限",
            "此功能需要訪問您的照片、視頻和音頻文件，請授予存儲權限。",
            onPositive
        )
    }
    
    /**
     * 獲取權限的友好名稱
     */
    fun getPermissionName(permission: String): String {
        return when (permission) {
            Manifest.permission.CAMERA -> "相機"
            Manifest.permission.RECORD_AUDIO -> "麥克風"
            Manifest.permission.READ_EXTERNAL_STORAGE -> "存儲"
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> "存儲"
            Manifest.permission.READ_MEDIA_IMAGES -> "圖片"
            Manifest.permission.READ_MEDIA_VIDEO -> "視頻"
            Manifest.permission.READ_MEDIA_AUDIO -> "音頻"
            else -> "未知權限"
        }
    }
}
