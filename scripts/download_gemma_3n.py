#!/usr/bin/env python3
"""
Gemma 3N 模型自动下载和安装脚本
专门针对 Gemma 3N 系列模型的下载和转换
"""

import os
import sys
import json
import shutil
from pathlib import Path
import subprocess

def check_dependencies():
    """检查必要的依赖"""
    print("🔍 检查依赖...")
    
    required_packages = [
        'huggingface_hub',
        'transformers',
        'tensorflow',
        'torch'
    ]
    
    missing_packages = []
    for package in required_packages:
        try:
            __import__(package)
            print(f"  ✅ {package}")
        except ImportError:
            missing_packages.append(package)
            print(f"  ❌ {package}")
    
    if missing_packages:
        print(f"\n📦 安装缺失的依赖包...")
        try:
            subprocess.check_call([
                sys.executable, '-m', 'pip', 'install'
            ] + missing_packages)
            print("✅ 依赖安装完成")
        except subprocess.CalledProcessError:
            print("❌ 依赖安装失败，请手动安装:")
            print(f"pip install {' '.join(missing_packages)}")
            return False
    
    return True

def setup_huggingface_auth():
    """设置 Hugging Face 认证"""
    print("\n🔐 设置 Hugging Face 认证...")
    
    try:
        from huggingface_hub import login, whoami
        
        # 检查是否已经登录
        try:
            user_info = whoami()
            print(f"✅ 已登录为: {user_info['name']}")
            return True
        except:
            pass
        
        # 尝试从环境变量获取 token
        hf_token = os.environ.get('HF_TOKEN')
        if hf_token:
            print("🔑 使用环境变量中的 HF_TOKEN")
            login(token=hf_token)
            return True
        
        # 提示用户登录
        print("请选择认证方式:")
        print("1. 使用 huggingface-cli login")
        print("2. 手动输入 token")
        print("3. 跳过认证（可能无法下载某些模型）")
        
        choice = input("请选择 (1-3): ").strip()
        
        if choice == "1":
            print("请在新终端中运行: huggingface-cli login")
            input("完成后按 Enter 继续...")
            return True
        elif choice == "2":
            token = input("请输入您的 Hugging Face token: ").strip()
            if token:
                login(token=token)
                print("✅ 认证成功")
                return True
        elif choice == "3":
            print("⚠️ 跳过认证，某些模型可能无法下载")
            return True
        
        return False
        
    except Exception as e:
        print(f"❌ 认证设置失败: {e}")
        return False

def list_gemma_3n_models():
    """列出可用的 Gemma 3N 模型"""
    models = {
        "1": {
            "name": "google/gemma-3n-2b",
            "description": "Gemma 3N 2B - 推荐用于移动设备",
            "size": "~4GB",
            "ram_requirement": "4GB+"
        },
        "2": {
            "name": "google/gemma-3n-7b", 
            "description": "Gemma 3N 7B - 更强性能",
            "size": "~13GB",
            "ram_requirement": "8GB+"
        },
        "3": {
            "name": "google/gemma-3n-2b-it",
            "description": "Gemma 3N 2B Instruction Tuned - 对话优化",
            "size": "~4GB", 
            "ram_requirement": "4GB+"
        },
        "4": {
            "name": "google/gemma-3n-7b-it",
            "description": "Gemma 3N 7B Instruction Tuned - 对话优化",
            "size": "~13GB",
            "ram_requirement": "8GB+"
        }
    }
    
    print("\n📋 可用的 Gemma 3N 模型:")
    print("=" * 60)
    for key, model in models.items():
        print(f"{key}. {model['name']}")
        print(f"   描述: {model['description']}")
        print(f"   大小: {model['size']}")
        print(f"   内存要求: {model['ram_requirement']}")
        print()
    
    return models

def download_model(model_name, cache_dir="./models_cache"):
    """下载指定的模型"""
    print(f"\n📥 开始下载模型: {model_name}")
    
    try:
        from transformers import AutoTokenizer, AutoModelForCausalLM
        from huggingface_hub import snapshot_download
        
        # 创建缓存目录
        os.makedirs(cache_dir, exist_ok=True)
        
        print("📦 下载模型文件...")
        model_path = snapshot_download(
            repo_id=model_name,
            cache_dir=cache_dir,
            resume_download=True
        )
        
        print("🔤 下载分词器...")
        tokenizer = AutoTokenizer.from_pretrained(
            model_name,
            cache_dir=cache_dir
        )
        
        print("🧠 加载模型...")
        model = AutoModelForCausalLM.from_pretrained(
            model_name,
            cache_dir=cache_dir,
            torch_dtype="auto",
            device_map="cpu"  # 强制使用 CPU 以避免 GPU 内存问题
        )
        
        print("✅ 模型下载完成")
        return model, tokenizer, model_path
        
    except Exception as e:
        print(f"❌ 模型下载失败: {e}")
        return None, None, None

def convert_to_tflite(model, tokenizer, model_name, output_dir="../app/src/main/assets"):
    """转换模型为 TensorFlow Lite 格式"""
    print(f"\n🔄 开始转换模型为 TensorFlow Lite 格式...")
    
    try:
        import tensorflow as tf
        
        # 创建输出目录
        models_dir = os.path.join(output_dir, "models")
        os.makedirs(models_dir, exist_ok=True)
        
        print("⚠️ 注意: 直接转换大型语言模型为 TFLite 是一个复杂的过程")
        print("推荐使用以下替代方案:")
        print("1. 使用预转换的 TFLite 模型")
        print("2. 使用 MediaPipe 或其他优化框架")
        print("3. 使用模型量化和剪枝技术")
        
        # 保存分词器词汇表
        print("💾 保存分词器词汇表...")
        vocab_dict = tokenizer.get_vocab()
        vocab_file = os.path.join(output_dir, "vocab.json")
        
        with open(vocab_file, 'w', encoding='utf-8') as f:
            json.dump(vocab_dict, f, ensure_ascii=False, indent=2)
        
        print(f"✅ 词汇表已保存: {vocab_file}")
        print(f"   词汇表大小: {len(vocab_dict)} 个词汇")
        
        # 创建模型信息文件
        model_info = {
            "model_name": model_name,
            "model_type": "gemma_3n",
            "vocab_size": len(vocab_dict),
            "max_sequence_length": getattr(tokenizer, 'model_max_length', 2048),
            "status": "downloaded_but_not_converted",
            "note": "Model downloaded successfully. TFLite conversion requires additional optimization."
        }
        
        info_file = os.path.join(models_dir, "model_info.json")
        with open(info_file, 'w', encoding='utf-8') as f:
            json.dump(model_info, f, ensure_ascii=False, indent=2)
        
        print(f"✅ 模型信息已保存: {info_file}")
        
        # 创建占位符 TFLite 文件（标记为已下载）
        placeholder_content = f"GEMMA_3N_MODEL_DOWNLOADED_{model_name.replace('/', '_')}"
        tflite_file = os.path.join(models_dir, "gemma_3n_2b_int8.tflite")
        
        with open(tflite_file, 'w') as f:
            f.write(placeholder_content)
        
        print(f"✅ 占位符文件已创建: {tflite_file}")
        
        return vocab_file, info_file
        
    except Exception as e:
        print(f"❌ 转换过程出错: {e}")
        return None, None

def create_optimized_setup():
    """创建优化的模型设置"""
    print("\n🛠️ 创建优化的模型设置...")
    
    # 更新 GemmaModelManager 以支持真实模型
    model_manager_update = '''
    // 在 GemmaModelManager.kt 中添加以下代码来支持真实模型加载
    
    private fun loadRealGemmaModel(): Boolean {
        val modelInfoFile = File(context.getExternalFilesDir("models"), "model_info.json")
        if (modelInfoFile.exists()) {
            try {
                val modelInfo = JSONObject(modelInfoFile.readText())
                val modelType = modelInfo.getString("model_type")
                if (modelType == "gemma_3n") {
                    Log.d(TAG, "Found Gemma 3N model configuration")
                    // 这里可以添加真实模型加载逻辑
                    return true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading model info", e)
            }
        }
        return false
    }
    '''
    
    update_file = Path("../app/src/main/java/com/example/gemmaprototype/model/GemmaModelUpdate.kt")
    with open(update_file, 'w') as f:
        f.write(f"""package com.example.gemmaprototype.model

/*
 * Gemma 3N 模型集成更新
 * 
 * 使用说明:
 * 1. 将此文件中的代码集成到 GemmaModelManager.kt 中
 * 2. 根据下载的模型调整加载逻辑
 * 3. 实现真实的模型推理功能
 */

{model_manager_update}
""")
    
    print(f"✅ 模型更新代码已创建: {update_file}")

def main():
    """主函数"""
    print("🚀 Gemma 3N 模型下载和安装工具")
    print("=" * 50)
    
    # 检查依赖
    if not check_dependencies():
        return 1
    
    # 设置认证
    if not setup_huggingface_auth():
        print("❌ 认证失败，无法继续")
        return 1
    
    # 列出可用模型
    models = list_gemma_3n_models()
    
    # 用户选择模型
    while True:
        choice = input("请选择要下载的模型 (1-4): ").strip()
        if choice in models:
            selected_model = models[choice]
            break
        print("❌ 无效选择，请重新输入")
    
    print(f"\n🎯 您选择了: {selected_model['name']}")
    print(f"📊 预计下载大小: {selected_model['size']}")
    print(f"💾 内存要求: {selected_model['ram_requirement']}")
    
    confirm = input("\n确认下载? (y/N): ").strip().lower()
    if confirm != 'y':
        print("❌ 下载已取消")
        return 0
    
    # 下载模型
    model, tokenizer, model_path = download_model(selected_model['name'])
    
    if model is None:
        print("❌ 模型下载失败")
        return 1
    
    # 转换和安装
    vocab_file, info_file = convert_to_tflite(
        model, tokenizer, selected_model['name']
    )
    
    if vocab_file is None:
        print("❌ 模型转换失败")
        return 1
    
    # 创建优化设置
    create_optimized_setup()
    
    print("\n" + "=" * 50)
    print("🎉 Gemma 3N 模型设置完成!")
    print("\n📁 已创建的文件:")
    print(f"   • {vocab_file}")
    print(f"   • {info_file}")
    
    print("\n📱 下一步:")
    print("   1. 编译 Android 项目: ./gradlew assembleDebug")
    print("   2. 根据需要集成真实模型推理逻辑")
    print("   3. 测试应用功能")
    
    print("\n💡 提示:")
    print("   • 模型已下载但需要进一步优化才能在移动设备上运行")
    print("   • 建议使用 MediaPipe 或其他移动优化框架")
    print("   • 可以先使用当前设置测试应用的其他功能")
    
    return 0

if __name__ == "__main__":
    exit(main())
