package com.otakeessen.bodysquirrel.data.local.repository

import com.otakeessen.bodysquirrel.data.local.AppDatabase
import com.otakeessen.bodysquirrel.data.local.entity.MealLogEntity

class MealRepository(private val database: AppDatabase) {

    fun observeLogs(date: String) = database.mealLogDao().observeLogs(date)

    suspend fun insertLog(log: MealLogEntity) = database.mealLogDao().insert(log)
}
