package com.otakeessen.bodysquirrel.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "dish_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = DishEntity::class,
            parentColumns = ["id"],
            childColumns = ["dishId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dishId"), Index("ingredientId")]
)
data class DishIngredientEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val dishId: String,
    val ingredientId: String,
    val weightG: Double
)
