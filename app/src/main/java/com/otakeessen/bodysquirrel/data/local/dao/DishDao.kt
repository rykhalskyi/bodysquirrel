package com.otakeessen.bodysquirrel.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.otakeessen.bodysquirrel.data.local.entity.DishEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DishDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dishes: List<DishEntity>)

    @Query("SELECT * FROM dishes ORDER BY name")
    fun observeAll(): Flow<List<DishEntity>>

    @Query("SELECT * FROM dishes WHERE name LIKE '%' || :query || '%' ORDER BY name")
    fun search(query: String): Flow<List<DishEntity>>

    @Query("SELECT COUNT(*) FROM dishes")
    suspend fun count(): Int
}
