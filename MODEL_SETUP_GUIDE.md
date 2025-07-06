# Gemma 3N 模型設置指南

## 🎯 概述

本指南將幫助您下載 Gemma 3N 模型並轉換為 TensorFlow Lite 格式，以便在 Android 應用中使用。

## 🚀 快速開始（推薦）

### 方法一：自動化腳本

1. **運行設置腳本**：
   ```bash
   cd scripts
   chmod +x setup_model.sh
   ./setup_model.sh
   ```

2. **選擇設置模式**：
   - 選擇 `1` 進行快速設置（使用佔位符文件）
   - 選擇 `2` 進行完整設置（下載真實模型）

3. **編譯和運行**：
   ```bash
   cd ..
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

### 方法二：Python 腳本

```bash
cd scripts
python3 download_and_convert_model.py
```

## 📋 詳細步驟

### 步驟 1：準備環境

#### 安裝 Python 依賴
```bash
pip install tensorflow transformers torch huggingface_hub optimum[tflite]
```

#### 設置 Hugging Face 訪問
```bash
# 方法 1：使用 CLI
huggingface-cli login

# 方法 2：設置環境變量
export HF_TOKEN="your_huggingface_token"
```

### 步驟 2：獲取 Gemma 模型訪問權限

1. **訪問 Hugging Face**：
   - 前往 [Gemma 模型頁面](https://huggingface.co/google/gemma-2b)
   - 點擊 "Request Access"
   - 填寫申請表格並等待批准

2. **生成訪問令牌**：
   - 前往 [Hugging Face Settings](https://huggingface.co/settings/tokens)
   - 創建新的訪問令牌
   - 選擇 "Read" 權限

### 步驟 3：下載和轉換模型

#### 自動轉換（推薦）
```python
# 使用提供的 Python 腳本
python3 scripts/download_and_convert_model.py
```

#### 手動轉換
```python
from transformers import AutoTokenizer, AutoModelForCausalLM
import tensorflow as tf

# 下載模型
model_name = "google/gemma-2b"
tokenizer = AutoTokenizer.from_pretrained(model_name)
model = AutoModelForCausalLM.from_pretrained(model_name)

# 轉換為 TensorFlow Lite
converter = tf.lite.TFLiteConverter.from_saved_model(saved_model_dir)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
converter.target_spec.supported_types = [tf.int8]

tflite_model = converter.convert()

# 保存模型
with open('gemma_3n_2b_int8.tflite', 'wb') as f:
    f.write(tflite_model)
```

### 步驟 4：部署到 Android 項目

1. **複製模型文件**：
   ```bash
   cp gemma_3n_2b_int8.tflite app/src/main/assets/models/
   ```

2. **複製詞彙表**：
   ```bash
   cp vocab.json app/src/main/assets/
   ```

3. **驗證文件結構**：
   ```
   app/src/main/assets/
   ├── models/
   │   └── gemma_3n_2b_int8.tflite
   └── vocab.json
   ```

## 🔧 替代方案

### 使用佔位符文件（測試用）

如果無法獲取真實模型，可以使用佔位符文件進行開發和測試：

```bash
# 創建佔位符模型文件
mkdir -p app/src/main/assets/models
echo "PLACEHOLDER_MODEL_FILE" > app/src/main/assets/models/gemma_3n_2b_int8.tflite

# 創建簡化詞彙表
cat > app/src/main/assets/vocab.json << 'EOF'
{
  "<pad>": 0,
  "<bos>": 1,
  "<eos>": 2,
  "<unk>": 3,
  " ": 4,
  "hello": 5,
  "world": 6,
  "test": 7
}
EOF
```

### 使用其他模型

如果 Gemma 模型不可用，可以嘗試其他開源模型：

1. **DistilBERT**（較小）：
   ```python
   model_name = "distilbert-base-uncased"
   ```

2. **GPT-2**（中等大小）：
   ```python
   model_name = "gpt2"
   ```

3. **TinyLlama**（專為移動設備優化）：
   ```python
   model_name = "TinyLlama/TinyLlama-1.1B-Chat-v1.0"
   ```

## 🛠️ 故障排除

### 常見問題

#### 1. 模型下載失敗
```
Error: Repository not found or access denied
```
**解決方案**：
- 確認已申請 Gemma 模型訪問權限
- 檢查 Hugging Face 令牌是否正確設置
- 嘗試重新登錄：`huggingface-cli login`

#### 2. 內存不足
```
OutOfMemoryError during model conversion
```
**解決方案**：
- 增加系統內存或使用更小的模型
- 使用模型分片：`device_map="auto"`
- 嘗試量化：`load_in_8bit=True`

#### 3. 轉換失敗
```
TensorFlow Lite conversion failed
```
**解決方案**：
- 更新 TensorFlow 版本：`pip install --upgrade tensorflow`
- 嘗試使用 Optimum：`pip install optimum[tflite]`
- 使用不同的量化策略

#### 4. Android 編譯錯誤
```
Asset file not found: models/gemma_3n_2b_int8.tflite
```
**解決方案**：
- 確認文件路徑正確
- 檢查文件權限
- 重新同步項目：`./gradlew clean build`

### 性能優化

#### 模型大小優化
```python
# 更激進的量化
converter.optimizations = [tf.lite.Optimize.DEFAULT]
converter.target_spec.supported_types = [tf.int8]
converter.inference_input_type = tf.int8
converter.inference_output_type = tf.int8
```

#### 推理速度優化
```python
# 啟用 GPU 代理
converter.target_spec.supported_ops = [
    tf.lite.OpsSet.TFLITE_BUILTINS,
    tf.lite.OpsSet.SELECT_TF_OPS
]
```

## 📊 模型規格

| 模型 | 大小 | 參數 | 量化後大小 | 推薦設備 |
|------|------|------|------------|----------|
| Gemma 2B | ~5GB | 2B | ~500MB | 4GB+ RAM |
| Gemma 7B | ~14GB | 7B | ~1.5GB | 8GB+ RAM |
| DistilBERT | ~250MB | 66M | ~80MB | 2GB+ RAM |
| GPT-2 | ~500MB | 124M | ~150MB | 2GB+ RAM |

## 🔗 相關資源

- [Gemma 官方文檔](https://ai.google.dev/gemma)
- [TensorFlow Lite 指南](https://www.tensorflow.org/lite)
- [Hugging Face Transformers](https://huggingface.co/docs/transformers)
- [Android ML Kit](https://developers.google.com/ml-kit)

## 📞 技術支持

如果遇到問題，請：

1. 檢查日誌輸出獲取詳細錯誤信息
2. 確認所有依賴都已正確安裝
3. 嘗試使用佔位符文件進行測試
4. 查看項目的 Issues 頁面

---

**注意**：模型文件較大，請確保有足夠的存儲空間和穩定的網絡連接。首次設置可能需要較長時間。
