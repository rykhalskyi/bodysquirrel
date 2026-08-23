package com.otakeessen.bodysquirrel.data.local.repository

import com.otakeessen.bodysquirrel.data.local.AppDatabase
import com.otakeessen.bodysquirrel.data.local.SeedData

class CatalogRepository(private val database: AppDatabase) {

    suspend fun seedIfEmpty() {
        if (database.categoryDao().count() == 0) {
            database.categoryDao().insertAll(SeedData.categories)
        }
        if (database.ingredientDao().count() == 0) {
            database.ingredientDao().insertAll(SeedData.ingredients)
        }
        if (database.dishDao().count() == 0) {
            database.dishDao().insertAll(SeedData.dishes)
            database.dishIngredientDao().insertAll(SeedData.dishIngredients)
        }
    }

    fun observeCategories() = database.categoryDao().observeAll()

    fun observeIngredients() = database.ingredientDao().observeAll()

    fun observeDishes() = database.dishDao().observeAll()
}
