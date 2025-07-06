package com.example.gemmaprototype.model

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.IOException

/**
 * Gemma 模型分詞器
 * 負責文本的編碼和解碼
 */
class GemmaTokenizer(private val context: Context) {
    private val vocabulary: Map<String, Int> by lazy { loadVocabulary() }
    private val idToToken: Map<Int, String> by lazy { 
        vocabulary.entries.associate { (token, id) -> id to token }
    }
    
    // 特殊標記
    private val bosToken: Int by lazy { vocabulary["<bos>"] ?: 1 }
    private val eosToken: Int by lazy { vocabulary["<eos>"] ?: 2 }
    private val unkToken: Int by lazy { vocabulary["<unk>"] ?: 3 }
    private val padToken: Int by lazy { vocabulary["<pad>"] ?: 0 }
    
    companion object {
        private const val TAG = "GemmaTokenizer"
        private const val VOCAB_FILE = "vocab.json"
        private const val MAX_SEQUENCE_LENGTH = 2048
    }
    
    /**
     * 從 assets 加載詞彙表
     */
    private fun loadVocabulary(): Map<String, Int> {
        return try {
            Log.d(TAG, "Loading vocabulary from assets")
            
            // 嘗試從 assets 加載詞彙表
            val vocabMap = mutableMapOf<String, Int>()
            
            try {
                context.assets.open(VOCAB_FILE).use { input ->
                    val jsonString = input.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(jsonString)
                    
                    jsonObject.keys().forEach { token ->
                        vocabMap[token] = jsonObject.getInt(token)
                    }
                }
                Log.d(TAG, "Loaded ${vocabMap.size} tokens from vocabulary")
            } catch (e: IOException) {
                Log.w(TAG, "Could not load vocabulary from assets, using fallback")
                // 如果無法加載詞彙表，使用簡化的詞彙表
                createFallbackVocabulary(vocabMap)
            }
            
            vocabMap
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load vocabulary", e)
            // 返回最小詞彙表
            createMinimalVocabulary()
        }
    }
    
    /**
     * 創建備用詞彙表
     */
    private fun createFallbackVocabulary(vocabMap: MutableMap<String, Int>) {
        // 特殊標記
        vocabMap["<pad>"] = 0
        vocabMap["<bos>"] = 1
        vocabMap["<eos>"] = 2
        vocabMap["<unk>"] = 3
        
        // 常用字符
        var id = 4
        
        // 數字
        for (i in 0..9) {
            vocabMap[i.toString()] = id++
        }
        
        // 英文字母
        for (c in 'a'..'z') {
            vocabMap[c.toString()] = id++
        }
        for (c in 'A'..'Z') {
            vocabMap[c.toString()] = id++
        }
        
        // 常用標點符號
        val punctuation = listOf(" ", ".", ",", "!", "?", ":", ";", "'", "\"", "-", "(", ")", "[", "]", "{", "}")
        punctuation.forEach { punct ->
            vocabMap[punct] = id++
        }
        
        // 常用中文字符（簡化版）
        val commonChinese = listOf("的", "是", "在", "有", "我", "你", "他", "她", "它", "們", "了", "著", "過", "來", "去", "說", "看", "想", "知", "道")
        commonChinese.forEach { char ->
            vocabMap[char] = id++
        }
        
        Log.d(TAG, "Created fallback vocabulary with ${vocabMap.size} tokens")
    }
    
    /**
     * 創建最小詞彙表
     */
    private fun createMinimalVocabulary(): Map<String, Int> {
        return mapOf(
            "<pad>" to 0,
            "<bos>" to 1,
            "<eos>" to 2,
            "<unk>" to 3
        )
    }
    
    /**
     * 編碼文本為 token IDs
     */
    fun encode(text: String): List<Int> {
        if (text.isEmpty()) return listOf(bosToken, eosToken)
        
        val tokens = mutableListOf<Int>()
        tokens.add(bosToken)
        
        // 簡化的分詞邏輯 - 實際應用中需要使用更複雜的 BPE 或 SentencePiece
        val words = text.trim().split(Regex("\\s+"))
        
        for (word in words) {
            val wordTokens = encodeWord(word)
            tokens.addAll(wordTokens)
            
            // 檢查序列長度限制
            if (tokens.size >= MAX_SEQUENCE_LENGTH - 1) {
                break
            }
        }
        
        tokens.add(eosToken)
        
        // 截斷到最大長度
        return tokens.take(MAX_SEQUENCE_LENGTH)
    }
    
    /**
     * 編碼單個詞
     */
    private fun encodeWord(word: String): List<Int> {
        val tokens = mutableListOf<Int>()
        
        // 首先嘗試整個詞
        if (vocabulary.containsKey(word)) {
            tokens.add(vocabulary[word]!!)
            return tokens
        }
        
        // 如果詞不在詞彙表中，按字符分解
        for (char in word) {
            val charStr = char.toString()
            val tokenId = vocabulary[charStr] ?: unkToken
            tokens.add(tokenId)
        }
        
        return tokens
    }
    
    /**
     * 解碼 token IDs 為文本
     */
    fun decode(ids: List<Int>): String {
        val tokens = mutableListOf<String>()
        
        for (id in ids) {
            // 跳過特殊標記
            if (id == bosToken || id == eosToken || id == padToken) {
                continue
            }
            
            val token = idToToken[id] ?: "<unk>"
            tokens.add(token)
        }
        
        // 簡單的拼接邏輯 - 實際應用中需要更複雜的處理
        return tokens.joinToString("").replace("▁", " ").trim()
    }
    
    /**
     * 獲取特殊標記
     */
    fun getBosToken(): Int = bosToken
    fun getEosToken(): Int = eosToken
    fun getUnkToken(): Int = unkToken
    fun getPadToken(): Int = padToken
    
    /**
     * 獲取詞彙表大小
     */
    fun getVocabSize(): Int = vocabulary.size
    
    /**
     * 檢查是否為有效的 token ID
     */
    fun isValidTokenId(id: Int): Boolean = idToToken.containsKey(id)
}
