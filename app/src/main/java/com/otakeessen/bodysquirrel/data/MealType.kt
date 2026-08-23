package com.otakeessen.bodysquirrel.data

enum class MealType { BREAKFAST, LUNCH, DINNER, SNACKS, WATER }

fun String.toMealTypeOrNull(): MealType? = MealType.entries.firstOrNull { it.name == this }
