package com.otakeessen.bodysquirrel.data.local.repository

import com.otakeessen.bodysquirrel.data.local.AppDatabase
import com.otakeessen.bodysquirrel.data.local.SeedData

open class CatalogRepository(private val database: AppDatabase) {

    open suspend fun seedIfEmpty() {
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

    open fun observeCategories() = database.categoryDao().observeAll()

    open fun observeIngredients() = database.ingredientDao().observeAll()

    open fun observeDishes() = database.dishDao().observeAll()

    open fun searchDishes(query: String) = database.dishDao().search(query)

    open suspend fun getDishById(id: String) = database.dishDao().getById(id)

    open suspend fun insertDish(dish: com.otakeessen.bodysquirrel.data.local.entity.DishEntity) = database.dishDao().insert(dish)

    open suspend fun insertIngredient(ingredient: com.otakeessen.bodysquirrel.data.local.entity.IngredientEntity) = database.ingredientDao().insert(ingredient)

    open suspend fun insertDishIngredient(item: com.otakeessen.bodysquirrel.data.local.entity.DishIngredientEntity) = database.dishIngredientDao().insert(item)
}
