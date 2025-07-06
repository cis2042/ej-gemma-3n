#!/usr/bin/env python3
"""
Gemma 3N 模型下載和轉換腳本
自動下載 Gemma 模型並轉換為 TensorFlow Lite 格式
"""

import os
import sys
import subprocess
import requests
from pathlib import Path
import tempfile
import shutil

def check_dependencies():
    """檢查必要的依賴"""
    required_packages = [
        'tensorflow',
        'transformers',
        'torch',
        'huggingface_hub'
    ]
    
    missing_packages = []
    for package in required_packages:
        try:
            __import__(package)
        except ImportError:
            missing_packages.append(package)
    
    if missing_packages:
        print(f"缺少依賴包: {', '.join(missing_packages)}")
        print("請運行: pip install " + " ".join(missing_packages))
        return False
    
    return True

def download_gemma_model(model_name="google/gemma-2b", cache_dir="./models"):
    """下載 Gemma 模型"""
    print(f"正在下載模型: {model_name}")
    
    try:
        from transformers import AutoTokenizer, AutoModelForCausalLM
        from huggingface_hub import login
        
        # 注意：需要 Hugging Face 訪問令牌
        print("請確保您已經:")
        print("1. 在 Hugging Face 上申請了 Gemma 模型的訪問權限")
        print("2. 設置了 HF_TOKEN 環境變量或運行 huggingface-cli login")
        
        # 下載模型和分詞器
        tokenizer = AutoTokenizer.from_pretrained(
            model_name,
            cache_dir=cache_dir,
            trust_remote_code=True
        )
        
        model = AutoModelForCausalLM.from_pretrained(
            model_name,
            cache_dir=cache_dir,
            trust_remote_code=True,
            torch_dtype="auto"
        )
        
        print("模型下載完成")
        return model, tokenizer, cache_dir
        
    except Exception as e:
        print(f"模型下載失敗: {e}")
        return None, None, None

def convert_to_tflite(model, tokenizer, output_dir="./converted_models"):
    """將模型轉換為 TensorFlow Lite 格式"""
    print("開始轉換模型為 TensorFlow Lite 格式...")
    
    try:
        import tensorflow as tf
        from transformers import TFAutoModelForCausalLM
        
        # 創建輸出目錄
        os.makedirs(output_dir, exist_ok=True)
        
        # 轉換為 TensorFlow 格式
        print("轉換為 TensorFlow 格式...")
        tf_model = TFAutoModelForCausalLM.from_pretrained(
            model.config.name_or_path,
            from_tf=False,
            from_pytorch=True
        )
        
        # 保存 TensorFlow 模型
        tf_model_path = os.path.join(output_dir, "tf_model")
        tf_model.save_pretrained(tf_model_path, saved_model=True)
        
        # 轉換為 TFLite
        print("轉換為 TensorFlow Lite 格式...")
        converter = tf.lite.TFLiteConverter.from_saved_model(tf_model_path)
        
        # 應用量化
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        converter.target_spec.supported_types = [tf.int8]
        
        # 轉換
        tflite_model = converter.convert()
        
        # 保存 TFLite 模型
        tflite_path = os.path.join(output_dir, "gemma_3n_2b_int8.tflite")
        with open(tflite_path, 'wb') as f:
            f.write(tflite_model)
        
        print(f"TFLite 模型已保存到: {tflite_path}")
        
        # 保存分詞器詞彙表
        vocab_path = os.path.join(output_dir, "vocab.json")
        tokenizer.save_vocabulary(output_dir)
        
        print(f"詞彙表已保存到: {vocab_path}")
        
        return tflite_path, vocab_path
        
    except Exception as e:
        print(f"模型轉換失敗: {e}")
        print("嘗試使用替代方法...")
        return convert_with_optimum(model, tokenizer, output_dir)

def convert_with_optimum(model, tokenizer, output_dir):
    """使用 Optimum 進行轉換"""
    try:
        from optimum.tflite import TFLiteConfig, export_tflite
        
        print("使用 Optimum 進行轉換...")
        
        # 配置量化
        config = TFLiteConfig(quantization_approach="static")
        
        # 轉換
        tflite_path = os.path.join(output_dir, "gemma_3n_2b_int8.tflite")
        export_tflite(
            model=model,
            config=config,
            output=tflite_path
        )
        
        # 保存詞彙表
        vocab_path = os.path.join(output_dir, "vocab.json")
        tokenizer.save_vocabulary(output_dir)
        
        return tflite_path, vocab_path
        
    except Exception as e:
        print(f"Optimum 轉換也失敗: {e}")
        return None, None

def copy_to_android_assets(tflite_path, vocab_path, android_project_root):
    """將轉換後的文件複製到 Android 項目"""
    assets_dir = os.path.join(android_project_root, "app", "src", "main", "assets")
    models_dir = os.path.join(assets_dir, "models")
    
    # 創建目錄
    os.makedirs(models_dir, exist_ok=True)
    
    try:
        # 複製模型文件
        if tflite_path and os.path.exists(tflite_path):
            dest_model = os.path.join(models_dir, "gemma_3n_2b_int8.tflite")
            shutil.copy2(tflite_path, dest_model)
            print(f"模型文件已複製到: {dest_model}")
        
        # 複製詞彙表
        if vocab_path and os.path.exists(vocab_path):
            dest_vocab = os.path.join(assets_dir, "vocab.json")
            shutil.copy2(vocab_path, dest_vocab)
            print(f"詞彙表已複製到: {dest_vocab}")
        
        return True
        
    except Exception as e:
        print(f"文件複製失敗: {e}")
        return False

def create_placeholder_files(android_project_root):
    """創建佔位符文件用於測試"""
    assets_dir = os.path.join(android_project_root, "app", "src", "main", "assets")
    models_dir = os.path.join(assets_dir, "models")
    
    os.makedirs(models_dir, exist_ok=True)
    
    # 創建佔位符模型文件
    placeholder_model = os.path.join(models_dir, "gemma_3n_2b_int8.tflite")
    with open(placeholder_model, 'wb') as f:
        f.write(b'PLACEHOLDER_MODEL_FILE')
    
    # 創建簡化的詞彙表
    import json
    vocab = {
        "<pad>": 0, "<bos>": 1, "<eos>": 2, "<unk>": 3,
        " ": 4, "hello": 5, "world": 6, "test": 7
    }
    
    vocab_file = os.path.join(assets_dir, "vocab.json")
    with open(vocab_file, 'w', encoding='utf-8') as f:
        json.dump(vocab, f, ensure_ascii=False, indent=2)
    
    print("已創建佔位符文件，應用可以正常編譯和運行")
    print("請稍後替換為真實的模型文件")

def main():
    """主函數"""
    print("Gemma 3N 模型下載和轉換工具")
    print("=" * 50)
    
    # 檢查依賴
    if not check_dependencies():
        sys.exit(1)
    
    # 獲取 Android 項目根目錄
    android_project_root = input("請輸入 Android 項目根目錄路徑 (默認: ../): ").strip()
    if not android_project_root:
        android_project_root = "../"
    
    # 檢查是否要下載真實模型
    download_real = input("是否下載真實的 Gemma 模型? (需要 HF 訪問權限) [y/N]: ").strip().lower()
    
    if download_real == 'y':
        # 下載模型
        model, tokenizer, cache_dir = download_gemma_model()
        
        if model is None:
            print("模型下載失敗，創建佔位符文件...")
            create_placeholder_files(android_project_root)
            return
        
        # 轉換模型
        tflite_path, vocab_path = convert_to_tflite(model, tokenizer)
        
        if tflite_path is None:
            print("模型轉換失敗，創建佔位符文件...")
            create_placeholder_files(android_project_root)
            return
        
        # 複製到 Android 項目
        success = copy_to_android_assets(tflite_path, vocab_path, android_project_root)
        
        if success:
            print("\n✅ 模型轉換和部署完成!")
            print("現在可以編譯和運行 Android 應用了")
        else:
            print("文件複製失敗，請手動複製文件")
    else:
        # 創建佔位符文件
        create_placeholder_files(android_project_root)
        print("\n✅ 佔位符文件創建完成!")
        print("應用可以編譯運行，但需要真實模型才能正常工作")

if __name__ == "__main__":
    main()
