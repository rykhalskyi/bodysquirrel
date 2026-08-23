package com.otakeessen.bodysquirrel

import android.app.Application
import com.otakeessen.bodysquirrel.data.local.AppDatabase
import com.otakeessen.bodysquirrel.data.local.repository.CatalogRepository
import com.otakeessen.bodysquirrel.data.local.repository.MealRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BodySquirrelApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val catalogRepository by lazy { CatalogRepository(database) }
    val mealRepository by lazy { MealRepository(database) }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch { catalogRepository.seedIfEmpty() }
    }
}
