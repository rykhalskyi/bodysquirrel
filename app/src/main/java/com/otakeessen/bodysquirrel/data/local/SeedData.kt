package com.otakeessen.bodysquirrel.data.local

import com.otakeessen.bodysquirrel.data.local.entity.CategoryEntity
import com.otakeessen.bodysquirrel.data.local.entity.DishEntity
import com.otakeessen.bodysquirrel.data.local.entity.DishIngredientEntity
import com.otakeessen.bodysquirrel.data.local.entity.IngredientEntity

object SeedData {

    val categories: List<CategoryEntity> = listOf(
        CategoryEntity(id = "cat_breakfast", name = "Breakfast", emoji = "🍳"),
        CategoryEntity(id = "cat_lunch", name = "Lunch", emoji = "🥪"),
        CategoryEntity(id = "cat_dinner", name = "Dinner", emoji = "🍽️"),
        CategoryEntity(id = "cat_snack", name = "Snack", emoji = "🍿"),
        CategoryEntity(id = "cat_protein", name = "Protein", emoji = "🥩"),
        CategoryEntity(id = "cat_carbs", name = "Carbs", emoji = "🍚"),
        CategoryEntity(id = "cat_vegetables", name = "Vegetables", emoji = "🥦"),
        CategoryEntity(id = "cat_dairy", name = "Dairy", emoji = "🥛"),
    )

    val ingredients: List<IngredientEntity> = listOf(
        IngredientEntity(id = "ing_oats", name = "Oats", kcalPer100g = 389.0, emoji = "🌾"),
        IngredientEntity(id = "ing_milk", name = "Milk", kcalPer100g = 61.0, emoji = "🥛"),
        IngredientEntity(id = "ing_banana", name = "Banana", kcalPer100g = 89.0, emoji = "🍌"),
        IngredientEntity(id = "ing_apple", name = "Apple", kcalPer100g = 52.0, emoji = "🍎"),
        IngredientEntity(id = "ing_chicken", name = "Chicken breast", kcalPer100g = 165.0, emoji = "🍗"),
        IngredientEntity(id = "ing_rice", name = "Rice (cooked)", kcalPer100g = 130.0, emoji = "🍚"),
        IngredientEntity(id = "ing_egg", name = "Egg", kcalPer100g = 155.0, emoji = "🥚"),
        IngredientEntity(id = "ing_bread", name = "Bread", kcalPer100g = 265.0, emoji = "🍞"),
        IngredientEntity(id = "ing_butter", name = "Butter", kcalPer100g = 717.0, emoji = "🧈"),
        IngredientEntity(id = "ing_cheese", name = "Cheese", kcalPer100g = 402.0, emoji = "🧀"),
        IngredientEntity(id = "ing_potato", name = "Potato", kcalPer100g = 77.0, emoji = "🥔"),
        IngredientEntity(id = "ing_tomato", name = "Tomato", kcalPer100g = 18.0, emoji = "🍅"),
        IngredientEntity(id = "ing_cucumber", name = "Cucumber", kcalPer100g = 15.0, emoji = "🥒"),
        IngredientEntity(id = "ing_salmon", name = "Salmon", kcalPer100g = 208.0, emoji = "🐟"),
        IngredientEntity(id = "ing_yogurt", name = "Yogurt", kcalPer100g = 59.0, emoji = "🥛"),
        IngredientEntity(id = "ing_oil", name = "Olive oil", kcalPer100g = 884.0, emoji = "🫒"),
        IngredientEntity(id = "ing_strawberry", name = "Strawberry", kcalPer100g = 32.0, emoji = "🍓"),
    )

    val dishes: List<DishEntity> = listOf(
        DishEntity(id = "dish_oatmeal", name = "Oatmeal with banana", portionWeightG = 340.0, totalKcal = 426.0, emoji = "🥣"),
        DishEntity(id = "dish_eggs", name = "Scrambled eggs", portionWeightG = 140.0, totalKcal = 245.0, emoji = "🍳"),
        DishEntity(id = "dish_chicken_rice", name = "Chicken with rice", portionWeightG = 400.0, totalKcal = 480.0, emoji = "🍛"),
        DishEntity(id = "dish_salad", name = "Greek salad", portionWeightG = 300.0, totalKcal = 250.0, emoji = "🥗"),
        DishEntity(id = "dish_sandwich", name = "Cheese sandwich", portionWeightG = 150.0, totalKcal = 380.0, emoji = "🥪"),
        DishEntity(id = "dish_salmon", name = "Salmon with potatoes", portionWeightG = 350.0, totalKcal = 420.0, emoji = "🐟"),
        DishEntity(id = "dish_yogurt", name = "Yogurt with berries", portionWeightG = 200.0, totalKcal = 150.0, emoji = "🍓"),
    )

    val dishIngredients: List<DishIngredientEntity> = listOf(
        DishIngredientEntity(dishId = "dish_oatmeal", ingredientId = "ing_oats", weightG = 60.0),
        DishIngredientEntity(dishId = "dish_oatmeal", ingredientId = "ing_milk", weightG = 200.0),
        DishIngredientEntity(dishId = "dish_oatmeal", ingredientId = "ing_banana", weightG = 80.0),
        DishIngredientEntity(dishId = "dish_eggs", ingredientId = "ing_egg", weightG = 100.0),
        DishIngredientEntity(dishId = "dish_eggs", ingredientId = "ing_butter", weightG = 10.0),
        DishIngredientEntity(dishId = "dish_eggs", ingredientId = "ing_milk", weightG = 30.0),
        DishIngredientEntity(dishId = "dish_chicken_rice", ingredientId = "ing_chicken", weightG = 150.0),
        DishIngredientEntity(dishId = "dish_chicken_rice", ingredientId = "ing_rice", weightG = 200.0),
        DishIngredientEntity(dishId = "dish_chicken_rice", ingredientId = "ing_oil", weightG = 10.0),
        DishIngredientEntity(dishId = "dish_salad", ingredientId = "ing_tomato", weightG = 120.0),
        DishIngredientEntity(dishId = "dish_salad", ingredientId = "ing_cucumber", weightG = 100.0),
        DishIngredientEntity(dishId = "dish_salad", ingredientId = "ing_cheese", weightG = 50.0),
        DishIngredientEntity(dishId = "dish_salad", ingredientId = "ing_oil", weightG = 15.0),
        DishIngredientEntity(dishId = "dish_sandwich", ingredientId = "ing_bread", weightG = 80.0),
        DishIngredientEntity(dishId = "dish_sandwich", ingredientId = "ing_cheese", weightG = 40.0),
        DishIngredientEntity(dishId = "dish_sandwich", ingredientId = "ing_butter", weightG = 10.0),
        DishIngredientEntity(dishId = "dish_salmon", ingredientId = "ing_salmon", weightG = 150.0),
        DishIngredientEntity(dishId = "dish_salmon", ingredientId = "ing_potato", weightG = 200.0),
        DishIngredientEntity(dishId = "dish_salmon", ingredientId = "ing_butter", weightG = 10.0),
        DishIngredientEntity(dishId = "dish_yogurt", ingredientId = "ing_yogurt", weightG = 150.0),
        DishIngredientEntity(dishId = "dish_yogurt", ingredientId = "ing_strawberry", weightG = 50.0),
    )
}
