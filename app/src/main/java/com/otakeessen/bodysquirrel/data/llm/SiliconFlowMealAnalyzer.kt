package com.otakeessen.bodysquirrel.data.llm

import android.util.Base64
import com.otakeessen.bodysquirrel.data.llm.network.ChatChoice
import com.otakeessen.bodysquirrel.data.llm.network.ChatCompletionRequest
import com.otakeessen.bodysquirrel.data.llm.network.ChatContentPart
import com.otakeessen.bodysquirrel.data.llm.network.ChatMessage
import com.otakeessen.bodysquirrel.data.llm.network.ImageUrlPart
import com.otakeessen.bodysquirrel.data.llm.network.ResponseFormat
import com.otakeessen.bodysquirrel.data.llm.network.SiliconFlowApi
import kotlinx.serialization.json.Json
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class SiliconFlowMealAnalyzer(
    private val apiKey: String,
    private val baseUrl: String = "https://api.siliconflow.com/",
    private val model: String = "Qwen/Qwen3-VL-8B-Instruct",
    private val apiService: SiliconFlowApi? = null
) : MealAnalyzer {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val service: SiliconFlowApi by lazy {
        apiService ?: run {
            val contentType = "application/json".toMediaType()
            val loggingInterceptor = HttpLoggingInterceptor { message ->
                Log.d("SiliconFlowMealAnalyzer", message)
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build()

            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()
                .create(SiliconFlowApi::class.java)
        }
    }

    override suspend fun analyze(imageBytes: ByteArray, promptHint: String?): MealScanResult {
        if (apiKey.isBlank()) {
            throw IllegalStateException("Silicon Flow API key is missing")
        }

        val processedBytes = compressImageIfNeeded(imageBytes)
        val base64Image = encodeBase64(processedBytes)
        val dataUrl = "data:image/jpeg;base64,$base64Image"

        val promptText = buildString {
            append("Analyze this meal image and return a JSON object describing the food.")
            append(" Strict JSON schema:\n")
            append("{\n")
            append("  \"dishName\": \"Short name of main dish (string)\",\n")
            append("  \"guessedMealType\": \"BREAKFAST\" | \"LUNCH\" | \"DINNER\" | \"SNACKS\",\n")
            append("  \"items\": [{\"name\": \"item name\", \"weightG\": 150.0, \"confidence\": 0.9}],\n")
            append("  \"totalKcal\": 450.0,\n")
            append("  \"portionWeightG\": 300.0,\n")
            append("  \"confidence\": 0.85,\n")
            append("  \"needsClarification\": false,\n")
            append("  \"question\": \"Optional question if ambiguous\",\n")
            append("  \"options\": [\"Option 1\", \"Option 2\"]\n")
            append("}\n")
            if (!promptHint.isNullOrBlank()) {
                append("User hint / clarification: ").append(promptHint)
            }
        }

        val request = ChatCompletionRequest(
            model = model,
            messages = listOf(
                ChatMessage(
                    role = "user",
                    content = listOf(
                        ChatContentPart(type = "image_url", image_url = ImageUrlPart(url = dataUrl)),
                        ChatContentPart(type = "text", text = promptText)
                    )
                )
            ),
            response_format = ResponseFormat(type = "json_object"),
            max_tokens = 2048
        )

        val response = try {
            service.createChatCompletion("Bearer $apiKey", request)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: ""
            Log.e("SiliconFlowMealAnalyzer", "HTTP ${e.code()} Error: $errorBody", e)
            throw IllegalStateException("API Error (${e.code()}): $errorBody")
        } catch (e: Exception) {
            Log.e("SiliconFlowMealAnalyzer", "Network Error: ${e.message}", e)
            throw e
        }
        val responseText = response.choices.firstOrNull()?.message?.content
            ?: throw IllegalStateException("Empty response from AI vision model")

        val cleanJsonText = sanitizeJson(responseText)
        return json.decodeFromString<MealScanResult>(cleanJsonText)
    }

    private fun sanitizeJson(rawText: String): String {
        var text = rawText.trim()
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json")
        } else if (text.startsWith("```")) {
            text = text.removePrefix("```")
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```")
        }
        return text.trim()
    }

    private fun encodeBase64(bytes: ByteArray): String {
        return try {
            java.util.Base64.getEncoder().encodeToString(bytes)
        } catch (e: Throwable) {
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        }
    }

    private fun compressImageIfNeeded(imageBytes: ByteArray): ByteArray {
        return try {
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ?: return imageBytes
            val maxDim = 1024
            val scale = Math.min(maxDim.toFloat() / bitmap.width, maxDim.toFloat() / bitmap.height)
            val scaledBitmap = if (scale < 1.0f) {
                android.graphics.Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
            } else {
                bitmap
            }
            val outputStream = java.io.ByteArrayOutputStream()
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
            val bytes = outputStream.toByteArray()
            if (scaledBitmap != bitmap) scaledBitmap.recycle()
            bitmap.recycle()
            bytes
        } catch (e: Throwable) {
            imageBytes
        }
    }
}
