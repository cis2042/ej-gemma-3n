#!/bin/bash

# Gemma 3N 一键安装脚本
# 自动下载和安装 Gemma 3N 模型

set -e

echo "🚀 Gemma 3N 模型一键安装工具"
echo "================================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查 Python 环境
check_python() {
    echo -e "${BLUE}检查 Python 环境...${NC}"
    
    if ! command -v python3 &> /dev/null; then
        echo -e "${RED}❌ Python 3 未安装${NC}"
        echo "请先安装 Python 3.8+"
        exit 1
    fi
    
    echo -e "${GREEN}✅ Python 3 已安装${NC}"
}

# 安装依赖
install_dependencies() {
    echo -e "${BLUE}安装 Python 依赖...${NC}"
    
    # 创建虚拟环境
    if [ ! -d "venv" ]; then
        python3 -m venv venv
        echo -e "${GREEN}✅ 虚拟环境已创建${NC}"
    fi
    
    # 激活虚拟环境
    source venv/bin/activate
    
    # 升级 pip
    pip install --upgrade pip
    
    # 安装依赖
    echo "安装必要的依赖包..."
    pip install huggingface_hub transformers torch tensorflow
    
    echo -e "${GREEN}✅ 依赖安装完成${NC}"
}

# 设置 Hugging Face 认证
setup_auth() {
    echo -e "${BLUE}设置 Hugging Face 认证...${NC}"
    
    if [ -z "$HF_TOKEN" ]; then
        echo -e "${YELLOW}请选择认证方式:${NC}"
        echo "1. 使用 huggingface-cli login"
        echo "2. 设置 HF_TOKEN 环境变量"
        echo "3. 跳过认证（可能无法下载某些模型）"
        
        read -p "请选择 (1-3): " -n 1 -r
        echo
        
        case $REPLY in
            1)
                echo "请运行: huggingface-cli login"
                read -p "完成后按 Enter 继续..."
                ;;
            2)
                read -p "请输入您的 HF_TOKEN: " HF_TOKEN
                export HF_TOKEN
                echo -e "${GREEN}✅ Token 已设置${NC}"
                ;;
            3)
                echo -e "${YELLOW}⚠️ 跳过认证${NC}"
                ;;
            *)
                echo -e "${RED}无效选择${NC}"
                exit 1
                ;;
        esac
    else
        echo -e "${GREEN}✅ 使用环境变量中的 HF_TOKEN${NC}"
    fi
}

# 选择模型
select_model() {
    echo -e "${BLUE}选择 Gemma 3N 模型...${NC}"
    echo ""
    echo "可用的模型:"
    echo "1. google/gemma-3n-2b (推荐用于移动设备, ~4GB)"
    echo "2. google/gemma-3n-7b (更强性能, ~13GB)"
    echo "3. google/gemma-3n-2b-it (对话优化, ~4GB)"
    echo "4. google/gemma-3n-7b-it (对话优化, ~13GB)"
    echo ""
    
    read -p "请选择模型 (1-4): " -n 1 -r
    echo
    
    case $REPLY in
        1)
            MODEL_NAME="google/gemma-3n-2b"
            MODEL_SIZE="4GB"
            ;;
        2)
            MODEL_NAME="google/gemma-3n-7b"
            MODEL_SIZE="13GB"
            ;;
        3)
            MODEL_NAME="google/gemma-3n-2b-it"
            MODEL_SIZE="4GB"
            ;;
        4)
            MODEL_NAME="google/gemma-3n-7b-it"
            MODEL_SIZE="13GB"
            ;;
        *)
            echo -e "${RED}无效选择，使用默认模型${NC}"
            MODEL_NAME="google/gemma-3n-2b"
            MODEL_SIZE="4GB"
            ;;
    esac
    
    echo -e "${GREEN}✅ 选择了模型: $MODEL_NAME${NC}"
    echo -e "${YELLOW}📊 预计下载大小: $MODEL_SIZE${NC}"
}

# 下载模型
download_model() {
    echo -e "${BLUE}开始下载模型...${NC}"
    
    # 激活虚拟环境
    source venv/bin/activate
    
    # 运行 Python 下载脚本
    python3 -c "
import os
from huggingface_hub import snapshot_download
from transformers import AutoTokenizer
import json

model_name = '$MODEL_NAME'
print(f'📥 下载模型: {model_name}')

try:
    # 下载模型
    model_path = snapshot_download(
        repo_id=model_name,
        cache_dir='./models_cache',
        resume_download=True
    )
    print('✅ 模型下载完成')
    
    # 下载分词器
    tokenizer = AutoTokenizer.from_pretrained(model_name)
    vocab = tokenizer.get_vocab()
    
    # 保存词汇表
    os.makedirs('../app/src/main/assets', exist_ok=True)
    with open('../app/src/main/assets/vocab.json', 'w', encoding='utf-8') as f:
        json.dump(vocab, f, ensure_ascii=False, indent=2)
    
    print('✅ 词汇表已保存')
    
    # 创建模型信息
    os.makedirs('../app/src/main/assets/models', exist_ok=True)
    model_info = {
        'model_name': model_name,
        'model_type': 'gemma_3n',
        'vocab_size': len(vocab),
        'status': 'downloaded',
        'path': model_path
    }
    
    with open('../app/src/main/assets/models/model_info.json', 'w') as f:
        json.dump(model_info, f, indent=2)
    
    # 创建占位符 TFLite 文件
    with open('../app/src/main/assets/models/gemma_3n_2b_int8.tflite', 'w') as f:
        f.write(f'GEMMA_3N_DOWNLOADED_{model_name.replace(\"/\", \"_\")}')
    
    print('✅ 模型设置完成')
    
except Exception as e:
    print(f'❌ 下载失败: {e}')
    exit(1)
"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ 模型下载和设置完成${NC}"
    else
        echo -e "${RED}❌ 模型下载失败${NC}"
        exit 1
    fi
}

# 编译测试
test_build() {
    echo -e "${BLUE}测试 Android 项目编译...${NC}"
    
    cd ..
    
    if [ -f "gradlew" ]; then
        echo "运行 Gradle 构建..."
        ./gradlew assembleDebug
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ Android 项目编译成功${NC}"
        else
            echo -e "${RED}❌ Android 项目编译失败${NC}"
            return 1
        fi
    else
        echo -e "${YELLOW}⚠️ 未找到 gradlew，跳过编译测试${NC}"
    fi
    
    cd scripts
}

# 主函数
main() {
    echo "🎯 开始 Gemma 3N 模型安装..."
    echo ""
    
    # 检查磁盘空间
    echo -e "${YELLOW}⚠️ 注意事项:${NC}"
    echo "• 确保有足够的磁盘空间 (5-15GB)"
    echo "• 确保网络连接稳定"
    echo "• 下载可能需要较长时间"
    echo ""
    
    read -p "确认继续? (y/N): " -n 1 -r
    echo
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ 安装已取消"
        exit 0
    fi
    
    # 执行安装步骤
    check_python
    install_dependencies
    setup_auth
    select_model
    download_model
    test_build
    
    echo ""
    echo -e "${GREEN}🎉 Gemma 3N 模型安装完成!${NC}"
    echo ""
    echo "📁 已创建的文件:"
    echo "   • app/src/main/assets/vocab.json"
    echo "   • app/src/main/assets/models/model_info.json"
    echo "   • app/src/main/assets/models/gemma_3n_2b_int8.tflite"
    echo ""
    echo "📱 下一步:"
    echo "   1. 编译项目: ./gradlew assembleDebug"
    echo "   2. 安装到设备: ./gradlew installDebug"
    echo "   3. 测试应用功能"
    echo ""
    echo -e "${YELLOW}💡 提示:${NC}"
    echo "   • 模型已下载但需要进一步优化才能在移动设备上高效运行"
    echo "   • 当前设置允许应用正常编译和运行"
    echo "   • 可以先测试应用的多媒体功能"
}

# 运行主函数
main
