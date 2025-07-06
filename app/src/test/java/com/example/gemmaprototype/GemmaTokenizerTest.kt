package com.example.gemmaprototype

import android.content.Context
import com.example.gemmaprototype.model.GemmaTokenizer
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.io.ByteArrayInputStream

/**
 * GemmaTokenizer 單元測試
 */
class GemmaTokenizerTest {
    
    @Mock
    private lateinit var mockContext: Context
    
    @Mock
    private lateinit var mockAssets: android.content.res.AssetManager
    
    private lateinit var tokenizer: GemmaTokenizer
    
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        
        // 模擬 assets 管理器
        `when`(mockContext.assets).thenReturn(mockAssets)
        
        // 模擬詞彙表文件
        val vocabJson = """
        {
            "<pad>": 0,
            "<bos>": 1,
            "<eos>": 2,
            "<unk>": 3,
            "hello": 4,
            "world": 5,
            " ": 6,
            "test": 7
        }
        """.trimIndent()
        
        val inputStream = ByteArrayInputStream(vocabJson.toByteArray())
        `when`(mockAssets.open("vocab.json")).thenReturn(inputStream)
        
        tokenizer = GemmaTokenizer(mockContext)
    }
    
    @Test
    fun testEncodeSimpleText() {
        val text = "hello world"
        val tokens = tokenizer.encode(text)
        
        // 應該包含 BOS, hello, world, EOS
        assertTrue("Tokens should contain BOS token", tokens.contains(1))
        assertTrue("Tokens should contain EOS token", tokens.contains(2))
        assertTrue("Tokens should contain hello token", tokens.contains(4))
        assertTrue("Tokens should contain world token", tokens.contains(5))
    }
    
    @Test
    fun testEncodeEmptyText() {
        val text = ""
        val tokens = tokenizer.encode(text)
        
        // 空文本應該只包含 BOS 和 EOS
        assertEquals("Empty text should have 2 tokens", 2, tokens.size)
        assertEquals("First token should be BOS", 1, tokens[0])
        assertEquals("Second token should be EOS", 2, tokens[1])
    }
    
    @Test
    fun testDecodeTokens() {
        val tokens = listOf(1, 4, 6, 5, 2) // BOS, hello, space, world, EOS
        val text = tokenizer.decode(tokens)
        
        // 解碼後應該得到原始文本（去除特殊標記）
        assertTrue("Decoded text should contain 'hello'", text.contains("hello"))
        assertTrue("Decoded text should contain 'world'", text.contains("world"))
    }
    
    @Test
    fun testSpecialTokens() {
        assertEquals("BOS token should be 1", 1, tokenizer.getBosToken())
        assertEquals("EOS token should be 2", 2, tokenizer.getEosToken())
        assertEquals("UNK token should be 3", 3, tokenizer.getUnkToken())
        assertEquals("PAD token should be 0", 0, tokenizer.getPadToken())
    }
    
    @Test
    fun testVocabSize() {
        val vocabSize = tokenizer.getVocabSize()
        assertTrue("Vocab size should be greater than 0", vocabSize > 0)
    }
    
    @Test
    fun testValidTokenId() {
        assertTrue("Token ID 1 should be valid", tokenizer.isValidTokenId(1))
        assertTrue("Token ID 4 should be valid", tokenizer.isValidTokenId(4))
        assertFalse("Token ID 999 should be invalid", tokenizer.isValidTokenId(999))
    }
    
    @Test
    fun testUnknownWord() {
        val text = "unknownword"
        val tokens = tokenizer.encode(text)
        
        // 未知詞應該被分解為字符或使用 UNK token
        assertTrue("Should contain some tokens", tokens.size > 2) // 至少 BOS + content + EOS
    }
}
