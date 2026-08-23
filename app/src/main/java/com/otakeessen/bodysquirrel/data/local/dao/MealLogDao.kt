package com.otakeessen.bodysquirrel.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.otakeessen.bodysquirrel.data.local.entity.MealLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: MealLogEntity)

    @Query("SELECT * FROM meal_logs WHERE date = :date ORDER BY loggedAt DESC")
    fun observeLogs(date: String): Flow<List<MealLogEntity>>
}
