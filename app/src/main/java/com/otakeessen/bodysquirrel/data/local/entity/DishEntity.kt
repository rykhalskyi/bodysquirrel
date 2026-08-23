package com.otakeessen.bodysquirrel.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "dishes")
data class DishEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val portionWeightG: Double,
    val totalKcal: Double,
    val emoji: String? = null
)
