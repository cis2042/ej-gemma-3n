# 模型文件目錄

## 📁 文件說明

此目錄用於存放 TensorFlow Lite 模型文件。

### 當前狀態
- ✅ 目錄已創建
- ⚠️ 需要添加真實的模型文件

### 所需文件
- `gemma_3n_2b_int8.tflite` - Gemma 3N 模型文件（量化版本）

### 獲取模型文件

請按照以下步驟獲取模型文件：

1. **運行自動化腳本**：
   ```bash
   cd scripts
   ./setup_model.sh
   ```

2. **或手動下載**：
   - 參考 `MODEL_SETUP_GUIDE.md` 中的詳細說明
   - 使用 `scripts/download_and_convert_model.py` 腳本

3. **或使用佔位符**：
   - 應用可以使用佔位符文件進行編譯測試
   - 但需要真實模型才能正常工作

### 文件大小參考
- Gemma 2B (INT8): ~500MB
- Gemma 7B (INT8): ~1.5GB

### 注意事項
- 模型文件較大，請確保有足夠的存儲空間
- 首次加載可能需要較長時間
- 建議在 WiFi 環境下下載
