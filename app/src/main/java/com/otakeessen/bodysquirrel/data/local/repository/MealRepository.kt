package com.otakeessen.bodysquirrel.data.local.repository

import com.otakeessen.bodysquirrel.data.local.AppDatabase
import com.otakeessen.bodysquirrel.data.local.entity.MealLogEntity

open class MealRepository(private val database: AppDatabase) {

    open fun observeLogs(date: String) = database.mealLogDao().observeLogs(date)

    open suspend fun insertLog(log: MealLogEntity) = database.mealLogDao().insert(log)
}
