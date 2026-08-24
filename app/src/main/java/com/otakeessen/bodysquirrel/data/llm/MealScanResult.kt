package com.otakeessen.bodysquirrel.data.llm

import kotlinx.serialization.Serializable

@Serializable
data class MealScanItem(
    val name: String,
    val weightG: Double = 0.0,
    val confidence: Double = 1.0
)

@Serializable
data class MealScanResult(
    val dishName: String,
    val guessedMealType: String = "LUNCH",
    val items: List<MealScanItem> = emptyList(),
    val totalKcal: Double = 0.0,
    val portionWeightG: Double = 0.0,
    val confidence: Double = 1.0,
    val needsClarification: Boolean = false,
    val question: String? = null,
    val options: List<String> = emptyList()
)
