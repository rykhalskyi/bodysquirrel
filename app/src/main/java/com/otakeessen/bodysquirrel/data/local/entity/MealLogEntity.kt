package com.otakeessen.bodysquirrel.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "meal_logs", indices = [Index("date")])
data class MealLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: String,
    val mealType: String,
    val dishId: String? = null,
    val portionWeightG: Double? = null,
    val kcal: Double = 0.0,
    val waterMl: Double = 0.0,
    val loggedAt: Long = System.currentTimeMillis()
)
