#!/bin/bash

# Gemma 3N 模型設置腳本
# 自動化模型下載、轉換和部署流程

set -e

echo "🚀 Gemma 3N 模型設置工具"
echo "=========================="

# 顏色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 檢查 Python 環境
check_python() {
    echo -e "${BLUE}檢查 Python 環境...${NC}"
    
    if ! command -v python3 &> /dev/null; then
        echo -e "${RED}❌ Python 3 未安裝${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ Python 3 已安裝${NC}"
}

# 安裝依賴
install_dependencies() {
    echo -e "${BLUE}安裝 Python 依賴...${NC}"
    
    # 創建虛擬環境
    if [ ! -d "venv" ]; then
        python3 -m venv venv
        echo -e "${GREEN}✅ 虛擬環境已創建${NC}"
    fi
    
    # 激活虛擬環境
    source venv/bin/activate
    
    # 升級 pip
    pip install --upgrade pip
    
    # 安裝依賴
    pip install tensorflow transformers torch huggingface_hub optimum[tflite]
    
    echo -e "${GREEN}✅ 依賴安裝完成${NC}"
}

# 創建佔位符文件
create_placeholder() {
    echo -e "${BLUE}創建佔位符文件...${NC}"
    
    # 創建目錄
    mkdir -p ../app/src/main/assets/models
    
    # 創建佔位符模型文件
    echo "PLACEHOLDER_MODEL_FILE" > ../app/src/main/assets/models/gemma_3n_2b_int8.tflite
    
    # 創建簡化詞彙表
    cat > ../app/src/main/assets/vocab.json << 'EOF'
{
  "<pad>": 0,
  "<bos>": 1,
  "<eos>": 2,
  "<unk>": 3,
  " ": 4,
  "!": 5,
  "\"": 6,
  "#": 7,
  "$": 8,
  "%": 9,
  "&": 10,
  "'": 11,
  "(": 12,
  ")": 13,
  "*": 14,
  "+": 15,
  ",": 16,
  "-": 17,
  ".": 18,
  "/": 19,
  "0": 20,
  "1": 21,
  "2": 22,
  "3": 23,
  "4": 24,
  "5": 25,
  "6": 26,
  "7": 27,
  "8": 28,
  "9": 29,
  ":": 30,
  ";": 31,
  "<": 32,
  "=": 33,
  ">": 34,
  "?": 35,
  "@": 36,
  "hello": 100,
  "world": 101,
  "test": 102,
  "example": 103,
  "model": 104,
  "text": 105,
  "generate": 106,
  "android": 107,
  "app": 108,
  "的": 200,
  "是": 201,
  "在": 202,
  "有": 203,
  "我": 204,
  "你": 205,
  "他": 206,
  "她": 207,
  "它": 208,
  "們": 209
}
EOF
    
    echo -e "${GREEN}✅ 佔位符文件已創建${NC}"
    echo -e "${YELLOW}📝 注意：這些是佔位符文件，應用可以編譯但需要真實模型才能正常工作${NC}"
}

# 下載真實模型
download_real_model() {
    echo -e "${BLUE}準備下載真實模型...${NC}"
    echo -e "${YELLOW}⚠️  注意：下載真實模型需要：${NC}"
    echo "1. Hugging Face 賬戶"
    echo "2. Gemma 模型訪問權限"
    echo "3. 足夠的磁盤空間 (約 5GB)"
    echo "4. 穩定的網絡連接"
    echo ""
    
    read -p "您是否已經滿足上述條件並想要繼續? (y/N): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}開始下載模型...${NC}"
        
        # 激活虛擬環境
        source venv/bin/activate
        
        # 運行 Python 腳本
        python3 download_and_convert_model.py
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ 模型下載和轉換完成${NC}"
        else
            echo -e "${RED}❌ 模型下載失敗，使用佔位符文件${NC}"
            create_placeholder
        fi
    else
        echo -e "${YELLOW}跳過真實模型下載，使用佔位符文件${NC}"
        create_placeholder
    fi
}

# 驗證文件
verify_files() {
    echo -e "${BLUE}驗證文件...${NC}"
    
    MODEL_FILE="../app/src/main/assets/models/gemma_3n_2b_int8.tflite"
    VOCAB_FILE="../app/src/main/assets/vocab.json"
    
    if [ -f "$MODEL_FILE" ]; then
        echo -e "${GREEN}✅ 模型文件存在: $MODEL_FILE${NC}"
        echo "   文件大小: $(du -h "$MODEL_FILE" | cut -f1)"
    else
        echo -e "${RED}❌ 模型文件不存在${NC}"
        return 1
    fi
    
    if [ -f "$VOCAB_FILE" ]; then
        echo -e "${GREEN}✅ 詞彙表文件存在: $VOCAB_FILE${NC}"
    else
        echo -e "${RED}❌ 詞彙表文件不存在${NC}"
        return 1
    fi
    
    return 0
}

# 編譯測試
test_build() {
    echo -e "${BLUE}測試 Android 項目編譯...${NC}"
    
    cd ..
    
    if [ -f "gradlew" ]; then
        echo "運行 Gradle 構建..."
        ./gradlew assembleDebug
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ Android 項目編譯成功${NC}"
        else
            echo -e "${RED}❌ Android 項目編譯失敗${NC}"
            return 1
        fi
    else
        echo -e "${YELLOW}⚠️  未找到 gradlew，跳過編譯測試${NC}"
    fi
    
    cd scripts
}

# 主函數
main() {
    echo "選擇操作模式："
    echo "1. 快速設置（佔位符文件）"
    echo "2. 完整設置（下載真實模型）"
    echo "3. 僅安裝依賴"
    echo "4. 僅驗證文件"
    echo ""
    
    read -p "請選擇 (1-4): " -n 1 -r
    echo
    
    case $REPLY in
        1)
            echo -e "${BLUE}🚀 開始快速設置...${NC}"
            check_python
            create_placeholder
            verify_files
            test_build
            echo -e "${GREEN}🎉 快速設置完成！${NC}"
            ;;
        2)
            echo -e "${BLUE}🚀 開始完整設置...${NC}"
            check_python
            install_dependencies
            download_real_model
            verify_files
            test_build
            echo -e "${GREEN}🎉 完整設置完成！${NC}"
            ;;
        3)
            echo -e "${BLUE}🚀 安裝依賴...${NC}"
            check_python
            install_dependencies
            echo -e "${GREEN}🎉 依賴安裝完成！${NC}"
            ;;
        4)
            echo -e "${BLUE}🚀 驗證文件...${NC}"
            verify_files
            ;;
        *)
            echo -e "${RED}無效選擇${NC}"
            exit 1
            ;;
    esac
    
    echo ""
    echo -e "${GREEN}📱 下一步：${NC}"
    echo "1. 在 Android Studio 中打開項目"
    echo "2. 運行 './gradlew assembleDebug' 編譯應用"
    echo "3. 安裝到設備: './gradlew installDebug'"
    echo ""
    echo -e "${YELLOW}💡 提示：如果使用佔位符文件，應用可以運行但AI功能需要真實模型${NC}"
}

# 運行主函數
main
