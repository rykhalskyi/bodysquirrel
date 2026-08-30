package com.otakeessen.bodysquirrel.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.otakeessen.bodysquirrel.data.local.entity.DishIngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DishIngredientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DishIngredientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DishIngredientEntity>)

    @Query("SELECT * FROM dish_ingredients WHERE dishId = :dishId")
    fun observeByDish(dishId: String): Flow<List<DishIngredientEntity>>
}
