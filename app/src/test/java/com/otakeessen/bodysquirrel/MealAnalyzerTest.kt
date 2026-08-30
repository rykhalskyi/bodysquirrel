package com.otakeessen.bodysquirrel

import com.otakeessen.bodysquirrel.data.llm.MealScanResult
import com.otakeessen.bodysquirrel.data.llm.network.ChatChoice
import com.otakeessen.bodysquirrel.data.llm.network.ChatCompletionResponse
import com.otakeessen.bodysquirrel.data.llm.network.ChatResponseMessage
import com.otakeessen.bodysquirrel.data.llm.network.SiliconFlowApi
import com.otakeessen.bodysquirrel.data.llm.SiliconFlowMealAnalyzer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MealAnalyzerTest {

    @Test
    fun parsesStructuredJsonResultCorrectly() = runBlocking {
        val fakeJson = """
            {
              "dishName": "Oatmeal with Berries",
              "guessedMealType": "BREAKFAST",
              "items": [
                {"name": "Oats", "weightG": 100.0, "confidence": 0.95},
                {"name": "Blueberries", "weightG": 50.0, "confidence": 0.90}
              ],
              "totalKcal": 350.0,
              "portionWeightG": 150.0,
              "confidence": 0.92,
              "needsClarification": false,
              "question": null,
              "options": []
            }
        """.trimIndent()

        val mockApi = object : SiliconFlowApi {
            override suspend fun createChatCompletion(
                authorization: String,
                request: com.otakeessen.bodysquirrel.data.llm.network.ChatCompletionRequest
            ): ChatCompletionResponse {
                return ChatCompletionResponse(
                    choices = listOf(
                        ChatChoice(
                            message = ChatResponseMessage(role = "assistant", content = fakeJson)
                        )
                    )
                )
            }
        }

        val analyzer = SiliconFlowMealAnalyzer(apiKey = "fake_key", apiService = mockApi)
        val result = analyzer.analyze("fake_bytes".toByteArray())

        assertEquals("Oatmeal with Berries", result.dishName)
        assertEquals("BREAKFAST", result.guessedMealType)
        assertEquals(350.0, result.totalKcal, 0.01)
        assertEquals(2, result.items.size)
        assertFalse(result.needsClarification)
    }
}
