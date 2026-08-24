package com.otakeessen.bodysquirrel

import android.app.Application
import com.otakeessen.bodysquirrel.data.local.AppDatabase
import com.otakeessen.bodysquirrel.data.local.repository.CatalogRepository
import com.otakeessen.bodysquirrel.data.local.repository.MealRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

import com.otakeessen.bodysquirrel.data.llm.MealAnalyzer
import com.otakeessen.bodysquirrel.data.llm.SiliconFlowMealAnalyzer

class BodySquirrelApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val catalogRepository by lazy { CatalogRepository(database) }
    val mealRepository by lazy { MealRepository(database) }
    val mealAnalyzer: MealAnalyzer by lazy {
        SiliconFlowMealAnalyzer(apiKey = BuildConfig.SILICON_FLOW_KEY)
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch { catalogRepository.seedIfEmpty() }
    }
}
