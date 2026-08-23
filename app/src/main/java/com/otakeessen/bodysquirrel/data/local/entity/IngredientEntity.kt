package com.otakeessen.bodysquirrel.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val kcalPer100g: Double,
    val emoji: String? = null
)
