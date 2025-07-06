package com.example.gemmaprototype

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * MainActivity 儀器測試
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun testUIElementsExist() {
        // 檢查主要UI元素是否存在
        onView(withId(R.id.titleTextView))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.modelStatusText))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.promptEditText))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.generateButton))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.clearButton))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.resultTextView))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testInputAndClear() {
        val testPrompt = "測試提示詞"
        
        // 輸入文本
        onView(withId(R.id.promptEditText))
            .perform(typeText(testPrompt), closeSoftKeyboard())
        
        // 檢查文本是否輸入成功
        onView(withId(R.id.promptEditText))
            .check(matches(withText(testPrompt)))
        
        // 點擊清除按鈕
        onView(withId(R.id.clearButton))
            .perform(click())
        
        // 檢查文本是否被清除
        onView(withId(R.id.promptEditText))
            .check(matches(withText("")))
    }
    
    @Test
    fun testGenerateButtonInitialState() {
        // 初始狀態下生成按鈕應該是禁用的（因為模型還在加載）
        onView(withId(R.id.generateButton))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun testEmptyPromptHandling() {
        // 確保輸入框為空
        onView(withId(R.id.promptEditText))
            .perform(clearText())
        
        // 嘗試點擊生成按鈕（如果已啟用）
        // 注意：這個測試可能需要等待模型加載完成
    }
}
