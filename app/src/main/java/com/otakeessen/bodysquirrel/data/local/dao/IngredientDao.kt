package com.otakeessen.bodysquirrel.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.otakeessen.bodysquirrel.data.local.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ingredients: List<IngredientEntity>)

    @Query("SELECT * FROM ingredients ORDER BY name")
    fun observeAll(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE name LIKE '%' || :query || '%' ORDER BY name")
    fun search(query: String): Flow<List<IngredientEntity>>

    @Query("SELECT COUNT(*) FROM ingredients")
    suspend fun count(): Int
}
