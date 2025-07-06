
# 🤖 Gemma 3N Multimodal Android Assistant / 多模態 Android 助手

[English](#english) | [中文](#中文)

---

## English

A feature-rich Android chat application supporting intelligent conversations with text, images, audio, video, and documents. Based on Google's Gemma 3N model, providing a completely offline multimodal AI experience.

### 🌟 Key Features

#### Core Capabilities
- 🚀 **Completely Offline**: No network required, privacy protected
- 📱 **Native Android App**: Supports Android 8.0+
- 🧠 **Gemma 3N Integration**: 2B parameter model (INT8 quantized)
- ⚡ **Hardware Acceleration**: NNAPI, GPU auto-optimization
- 🎯 **Smart Device Detection**: Auto-adapts to device performance

#### Multimodal Interaction
- 💬 **Conversational Interface**: Modern chat app user experience
- 🖼️ **Image Processing**: Camera, gallery selection, image analysis
- 🎥 **Video Support**: Video recording, video file upload
- 🎤 **Voice Features**: Speech recognition, text-to-speech, audio recording
- 📄 **Document Processing**: PDF, text file reading and analysis

### 🚀 Quick Start

#### Prerequisites
- Android Studio Flamingo or newer
- JDK 11+
- Android SDK API 26+

#### Installation
```bash
# Clone the repository
git clone https://github.com/cis2042/ej-gemma-3n.git
cd ej-gemma-3n

# Build the project
./gradlew assembleDebug

# Install to device
./gradlew installDebug
```

#### Get Gemma 3N Model
```bash
cd scripts
./install_gemma_3n.sh
```

### 📚 Documentation

- [**Handover Guide**](HANDOVER_GUIDE_EN.md) - Complete engineer onboarding guide
- [**Testing Checklist**](TESTING_CHECKLIST_EN.md) - Comprehensive testing procedures
- [**Demo Script**](DEMO_SCRIPT_EN.md) - Project demonstration guide
- [**Quick Start**](QUICK_START.md) - Fast setup guide
- [**Model Setup**](MODEL_SETUP_GUIDE.md) - AI model configuration

### 🛠️ Technical Stack

- **Language**: Kotlin
- **Architecture**: MVVM + Component-based
- **UI**: Material Design 3
- **Database**: Room
- **AI Framework**: TensorFlow Lite
- **Camera**: CameraX
- **Image Loading**: Glide

### 📱 Screenshots

*Coming soon - screenshots of the chat interface, multimodal features, and settings*

### 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 中文

這是一個功能豐富的 Android 應用，集成 Google Gemma 3N 模型，支持多模態交互，包括文字、圖片、音頻、視頻和文檔處理，實現完全離線的智能對話功能。

## 🌟 功能特點

### 核心功能
- 🚀 **完全離線運行**：無需網絡連接，保護隱私
- 📱 **原生 Android 應用**：支持 Android 8.0+
- 🧠 **Gemma 3N 模型集成**：2B 參數模型（INT8 量化）
- ⚡ **硬件加速支持**：NNAPI、GPU 自動優化
- 🎯 **智能設備檢測**：自動適配設備性能

### 多模態交互
- 💬 **對話式界面**：類似現代聊天應用的用戶體驗
- 🖼️ **圖片處理**：拍照、相冊選擇、圖像分析
- 🎥 **視頻支持**：錄製視頻、視頻文件上傳
- 🎤 **語音功能**：語音識別、語音合成、音頻錄製
- 📄 **文檔處理**：PDF、文本文件讀取和分析
- 🗂️ **對話歷史**：自動保存和管理聊天記錄

## 系統要求

### 最低要求
- Android 8.0 (API 級別 26) 或更高版本
- 2GB RAM
- 1GB 可用存儲空間

### 推薦配置
- Android 10.0 或更高版本
- 4GB RAM 或更多
- 支持 NNAPI 或 GPU 加速的設備

## 📁 項目結構

```
app/
├── src/main/java/com/example/gemmaprototype/
│   ├── MainActivity.kt                    # 主活動（對話界面）
│   ├── data/                             # 數據層
│   │   ├── ChatMessage.kt                # 聊天消息數據模型
│   │   ├── ChatDao.kt                    # 數據庫訪問對象
│   │   ├── ChatDatabase.kt               # Room 數據庫
│   │   └── Converters.kt                 # 類型轉換器
│   ├── ui/                               # UI 組件
│   │   ├── ChatAdapter.kt                # 聊天消息適配器
│   │   └── MediaPreviewAdapter.kt        # 媒體預覽適配器
│   ├── model/                            # AI 模型
│   │   ├── GemmaModelManager.kt          # 模型管理器
│   │   └── GemmaTokenizer.kt             # 分詞器
│   ├── media/                            # 媒體處理
│   │   ├── MediaInputManager.kt          # 媒體輸入管理
│   │   ├── MediaUtils.kt                 # 媒體工具類
│   │   └── MultimodalProcessor.kt        # 多模態處理器
│   ├── speech/                           # 語音處理
│   │   └── SpeechManager.kt              # 語音識別和合成
│   ├── camera/                           # 相機功能
│   │   └── CameraManager.kt              # 相機管理器
│   ├── document/                         # 文檔處理
│   │   └── DocumentProcessor.kt          # 文檔解析器
│   └── utils/                            # 工具類
│       ├── DeviceCapabilityChecker.kt    # 設備能力檢測
│       ├── ModelUtils.kt                 # 模型工具類
│       └── PermissionManager.kt          # 權限管理器
├── src/main/res/
│   ├── layout/                           # 界面佈局
│   │   ├── activity_main.xml             # 主界面（對話式）
│   │   ├── item_message_*.xml            # 消息項目佈局
│   │   └── item_media_preview.xml        # 媒體預覽佈局
│   ├── drawable/                         # 圖標和背景
│   └── values/                           # 資源值
└── src/main/assets/
    ├── models/                           # 模型文件目錄
    │   └── gemma_3n_2b_int8.tflite      # Gemma 模型文件
    └── vocab.json                        # 詞彙表文件
```

## 安裝和使用

### 1. 克隆項目
```bash
git clone <repository-url>
cd EJ_Android
```

### 2. 準備模型文件
由於模型文件較大，需要單獨下載：

1. 從 [Google AI Studio](https://aistudio.google.com/) 或 [Hugging Face](https://huggingface.co/google/gemma-2b) 下載 Gemma 3N 2B 模型
2. 使用 TensorFlow Lite 轉換工具將模型轉換為 `.tflite` 格式
3. 應用 INT8 量化以減小模型大小
4. 將轉換後的模型文件放入 `app/src/main/assets/models/` 目錄
5. 將對應的詞彙表文件放入 `app/src/main/assets/` 目錄

### 3. 構建應用
```bash
./gradlew assembleDebug
```

### 4. 安裝到設備
```bash
./gradlew installDebug
```

## 🚀 使用說明

### 基本對話
1. **啟動應用**：打開應用後，系統會自動檢測設備能力並加載模型
2. **文字對話**：在底部輸入框中輸入消息，點擊發送按鈕
3. **查看回應**：AI 會生成回應並顯示在聊天界面中

### 多媒體功能
4. **添加圖片**：點擊附件按鈕，選擇"拍照"或"從相冊選擇"
5. **錄製視頻**：選擇"錄製視頻"或"選擇視頻"
6. **語音輸入**：長按發送按鈕進行語音輸入
7. **文檔上傳**：選擇"選擇文檔"上傳 PDF 或文本文件
8. **語音播放**：AI 回應會自動朗讀（如果啟用）

### 高級功能
9. **對話歷史**：所有對話會自動保存，重啟應用後可繼續查看
10. **媒體預覽**：選中的媒體會在輸入框上方顯示預覽
11. **權限管理**：首次使用時會請求必要的權限

## 技術實現

### 核心組件

1. **GemmaModelManager**：負責模型的加載、推理和資源管理
2. **GemmaTokenizer**：處理文本的編碼和解碼
3. **DeviceCapabilityChecker**：檢測設備硬件能力
4. **ModelUtils**：提供模型文件管理工具

### 性能優化

- 自動檢測並使用最佳硬件加速選項
- 智能線程數配置
- 內存使用監控
- 模型文件緩存管理

### 錯誤處理

- 完善的異常捕獲和處理
- 用戶友好的錯誤提示
- 自動資源清理

## 開發指南

### 添加新功能

1. 在相應的包中創建新類
2. 更新 UI 佈局（如需要）
3. 在 MainActivity 中集成新功能
4. 添加相應的測試用例

### 調試技巧

- 使用 `adb logcat` 查看詳細日誌
- 檢查 `GemmaModelManager` 標籤的日誌輸出
- 監控內存使用情況

## 已知限制

1. **模型大小**：當前使用簡化的詞彙表，實際部署需要完整詞彙表
2. **生成質量**：演示版本使用模擬生成，實際需要完整的自回歸實現
3. **性能**：在低端設備上可能運行較慢
4. **電池消耗**：長時間推理會顯著消耗電池

## 故障排除

### 常見問題

**Q: 應用啟動後顯示"模型加載失敗"**
A: 檢查模型文件是否正確放置在 assets/models/ 目錄中

**Q: 生成速度很慢**
A: 這是正常現象，大型模型在移動設備上運行需要時間

**Q: 應用崩潰或內存不足**
A: 設備內存可能不足，嘗試關閉其他應用或使用更小的模型

### 日誌分析

關鍵日誌標籤：
- `GemmaModelManager`：模型相關操作
- `DeviceCapabilityChecker`：設備能力檢測
- `GemmaTokenizer`：文本處理

## 貢獻指南

1. Fork 項目
2. 創建功能分支
3. 提交更改
4. 創建 Pull Request

## 許可證

本項目遵循 Apache 2.0 許可證。

## 聯繫方式

如有問題或建議，請創建 Issue 或聯繫開發團隊。

---

**🚀 開始您的多模態 AI 助手之旅吧！**
