package com.otakeessen.bodysquirrel.data.llm

interface MealAnalyzer {
    suspend fun analyze(imageBytes: ByteArray, promptHint: String? = null): MealScanResult
}
