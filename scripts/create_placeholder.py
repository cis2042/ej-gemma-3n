#!/usr/bin/env python3
"""
創建佔位符文件腳本
用於快速設置開發環境
"""

import os
import json
from pathlib import Path

def create_placeholder_model():
    """創建佔位符模型文件"""
    models_dir = Path("../app/src/main/assets/models")
    models_dir.mkdir(parents=True, exist_ok=True)
    
    # 創建佔位符 TFLite 文件
    placeholder_content = b"PLACEHOLDER_TFLITE_MODEL_FILE_FOR_DEVELOPMENT"
    model_file = models_dir / "gemma_3n_2b_int8.tflite"
    
    with open(model_file, 'wb') as f:
        f.write(placeholder_content)
    
    print(f"✅ 佔位符模型文件已創建: {model_file}")
    return model_file

def create_enhanced_vocab():
    """創建增強的詞彙表文件"""
    assets_dir = Path("../app/src/main/assets")
    assets_dir.mkdir(parents=True, exist_ok=True)
    
    # 創建更完整的詞彙表
    vocab = {
        # 特殊標記
        "<pad>": 0,
        "<bos>": 1,
        "<eos>": 2,
        "<unk>": 3,
        
        # 基本字符
        " ": 4,
        "\n": 5,
        "\t": 6,
        
        # 標點符號
        "!": 10, "\"": 11, "#": 12, "$": 13, "%": 14,
        "&": 15, "'": 16, "(": 17, ")": 18, "*": 19,
        "+": 20, ",": 21, "-": 22, ".": 23, "/": 24,
        ":": 25, ";": 26, "<": 27, "=": 28, ">": 29,
        "?": 30, "@": 31, "[": 32, "\\": 33, "]": 34,
        "^": 35, "_": 36, "`": 37, "{": 38, "|": 39,
        "}": 40, "~": 41,
        
        # 數字
        "0": 50, "1": 51, "2": 52, "3": 53, "4": 54,
        "5": 55, "6": 56, "7": 57, "8": 58, "9": 59,
        
        # 英文字母（小寫）
        "a": 100, "b": 101, "c": 102, "d": 103, "e": 104,
        "f": 105, "g": 106, "h": 107, "i": 108, "j": 109,
        "k": 110, "l": 111, "m": 112, "n": 113, "o": 114,
        "p": 115, "q": 116, "r": 117, "s": 118, "t": 119,
        "u": 120, "v": 121, "w": 122, "x": 123, "y": 124,
        "z": 125,
        
        # 英文字母（大寫）
        "A": 150, "B": 151, "C": 152, "D": 153, "E": 154,
        "F": 155, "G": 156, "H": 157, "I": 158, "J": 159,
        "K": 160, "L": 161, "M": 162, "N": 163, "O": 164,
        "P": 165, "Q": 166, "R": 167, "S": 168, "T": 169,
        "U": 170, "V": 171, "W": 172, "X": 173, "Y": 174,
        "Z": 175,
        
        # 常用英文單詞
        "the": 200, "be": 201, "to": 202, "of": 203, "and": 204,
        "a": 205, "in": 206, "that": 207, "have": 208, "i": 209,
        "it": 210, "for": 211, "not": 212, "on": 213, "with": 214,
        "he": 215, "as": 216, "you": 217, "do": 218, "at": 219,
        "this": 220, "but": 221, "his": 222, "by": 223, "from": 224,
        "they": 225, "we": 226, "say": 227, "her": 228, "she": 229,
        "or": 230, "an": 231, "will": 232, "my": 233, "one": 234,
        "all": 235, "would": 236, "there": 237, "their": 238,
        "what": 239, "so": 240, "up": 241, "out": 242, "if": 243,
        "about": 244, "who": 245, "get": 246, "which": 247, "go": 248,
        "me": 249, "when": 250, "make": 251, "can": 252, "like": 253,
        "time": 254, "no": 255, "just": 256, "him": 257, "know": 258,
        "take": 259, "people": 260, "into": 261, "year": 262, "your": 263,
        "good": 264, "some": 265, "could": 266, "them": 267, "see": 268,
        "other": 269, "than": 270, "then": 271, "now": 272, "look": 273,
        "only": 274, "come": 275, "its": 276, "over": 277, "think": 278,
        "also": 279, "back": 280, "after": 281, "use": 282, "two": 283,
        "how": 284, "our": 285, "work": 286, "first": 287, "well": 288,
        "way": 289, "even": 290, "new": 291, "want": 292, "because": 293,
        "any": 294, "these": 295, "give": 296, "day": 297, "most": 298,
        "us": 299,
        
        # 常用中文字符
        "的": 1000, "是": 1001, "在": 1002, "有": 1003, "我": 1004,
        "你": 1005, "他": 1006, "她": 1007, "它": 1008, "們": 1009,
        "了": 1010, "著": 1011, "過": 1012, "來": 1013, "去": 1014,
        "說": 1015, "看": 1016, "想": 1017, "知": 1018, "道": 1019,
        "會": 1020, "能": 1021, "可": 1022, "以": 1023, "要": 1024,
        "不": 1025, "沒": 1026, "很": 1027, "好": 1028, "大": 1029,
        "小": 1030, "多": 1031, "少": 1032, "新": 1033, "舊": 1034,
        "高": 1035, "低": 1036, "長": 1037, "短": 1038, "快": 1039,
        "慢": 1040, "早": 1041, "晚": 1042, "上": 1043, "下": 1044,
        "前": 1045, "後": 1046, "左": 1047, "右": 1048, "中": 1049,
        "內": 1050, "外": 1051, "東": 1052, "西": 1053, "南": 1054,
        "北": 1055, "今": 1056, "明": 1057, "昨": 1058, "天": 1059,
        "年": 1060, "月": 1061, "日": 1062, "時": 1063, "分": 1064,
        "秒": 1065, "人": 1066, "家": 1067, "國": 1068, "地": 1069,
        "方": 1070, "事": 1071, "物": 1072, "生": 1073, "活": 1074,
        "工": 1075, "作": 1076, "學": 1077, "習": 1078, "問": 1079,
        "題": 1080, "答": 1081, "案": 1082, "開": 1083, "始": 1084,
        "結": 1085, "束": 1086, "進": 1087, "出": 1088, "入": 1089,
        "做": 1090, "用": 1091, "給": 1092, "拿": 1093, "放": 1094,
        "買": 1095, "賣": 1096, "吃": 1097, "喝": 1098, "睡": 1099,
        
        # 技術相關詞彙
        "hello": 2000, "world": 2001, "test": 2002, "example": 2003,
        "model": 2004, "text": 2005, "generate": 2006, "android": 2007,
        "app": 2008, "application": 2009, "mobile": 2010, "phone": 2011,
        "computer": 2012, "software": 2013, "hardware": 2014, "code": 2015,
        "program": 2016, "data": 2017, "file": 2018, "system": 2019,
        "network": 2020, "internet": 2021, "web": 2022, "site": 2023,
        "page": 2024, "user": 2025, "interface": 2026, "design": 2027,
        "development": 2028, "programming": 2029, "language": 2030,
        "artificial": 2031, "intelligence": 2032, "machine": 2033,
        "learning": 2034, "deep": 2035, "neural": 2036, "network": 2037,
        "algorithm": 2038, "database": 2039, "server": 2040, "client": 2041,
        "api": 2042, "json": 2043, "xml": 2044, "html": 2045, "css": 2046,
        "javascript": 2047, "python": 2048, "java": 2049, "kotlin": 2050
    }
    
    vocab_file = assets_dir / "vocab.json"
    with open(vocab_file, 'w', encoding='utf-8') as f:
        json.dump(vocab, f, ensure_ascii=False, indent=2)
    
    print(f"✅ 增強詞彙表已創建: {vocab_file}")
    print(f"   詞彙表大小: {len(vocab)} 個詞彙")
    return vocab_file

def create_model_info():
    """創建模型信息文件"""
    models_dir = Path("../app/src/main/assets/models")
    
    model_info = {
        "model_name": "gemma_3n_2b_int8",
        "model_type": "placeholder",
        "version": "1.0.0",
        "description": "Placeholder model file for development",
        "file_size": "placeholder",
        "vocab_size": 3051,
        "max_sequence_length": 2048,
        "quantization": "int8",
        "created_by": "setup_script",
        "note": "This is a placeholder file. Replace with real Gemma model for production use."
    }
    
    info_file = models_dir / "model_info.json"
    with open(info_file, 'w', encoding='utf-8') as f:
        json.dump(model_info, f, ensure_ascii=False, indent=2)
    
    print(f"✅ 模型信息文件已創建: {info_file}")
    return info_file

def main():
    """主函數"""
    print("🚀 創建佔位符文件...")
    print("=" * 40)
    
    try:
        # 創建佔位符模型
        model_file = create_placeholder_model()
        
        # 創建增強詞彙表
        vocab_file = create_enhanced_vocab()
        
        # 創建模型信息
        info_file = create_model_info()
        
        print("\n" + "=" * 40)
        print("🎉 佔位符文件創建完成！")
        print("\n📁 創建的文件:")
        print(f"   • {model_file}")
        print(f"   • {vocab_file}")
        print(f"   • {info_file}")
        
        print("\n📱 下一步:")
        print("   1. 編譯 Android 項目: ./gradlew assembleDebug")
        print("   2. 安裝到設備: ./gradlew installDebug")
        print("   3. 測試應用功能")
        
        print("\n⚠️  注意:")
        print("   • 這些是佔位符文件，應用可以編譯運行")
        print("   • AI 功能需要真實的 Gemma 模型才能正常工作")
        print("   • 請參考 MODEL_SETUP_GUIDE.md 獲取真實模型")
        
    except Exception as e:
        print(f"❌ 創建佔位符文件失敗: {e}")
        return 1
    
    return 0

if __name__ == "__main__":
    exit(main())
