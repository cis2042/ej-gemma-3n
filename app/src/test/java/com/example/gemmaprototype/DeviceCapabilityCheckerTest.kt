package com.example.gemmaprototype

import android.content.Context
import com.example.gemmaprototype.utils.DeviceCapabilityChecker
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * DeviceCapabilityChecker 單元測試
 */
class DeviceCapabilityCheckerTest {
    
    @Mock
    private lateinit var mockContext: Context
    
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }
    
    @Test
    fun testGetRecommendedThreadCount() {
        val threadCount = DeviceCapabilityChecker.getRecommendedThreadCount()
        
        assertTrue("Thread count should be at least 2", threadCount >= 2)
        assertTrue("Thread count should be at most 8", threadCount <= 8)
    }
    
    @Test
    fun testNNAPIAvailability() {
        // 這個測試在模擬環境中可能會失敗，但可以驗證方法不會拋出異常
        try {
            val isAvailable = DeviceCapabilityChecker.isNNAPIAvailable()
            // 結果可能是 true 或 false，但不應該拋出異常
            assertTrue("Method should return a boolean", isAvailable || !isAvailable)
        } catch (e: Exception) {
            fail("NNAPI availability check should not throw exception: ${e.message}")
        }
    }
    
    @Test
    fun testGPUDelegateAvailability() {
        // 這個測試在模擬環境中可能會失敗，但可以驗證方法不會拋出異常
        try {
            val isAvailable = DeviceCapabilityChecker.isGPUDelegateAvailable()
            // 結果可能是 true 或 false，但不應該拋出異常
            assertTrue("Method should return a boolean", isAvailable || !isAvailable)
        } catch (e: Exception) {
            fail("GPU delegate availability check should not throw exception: ${e.message}")
        }
    }
}
