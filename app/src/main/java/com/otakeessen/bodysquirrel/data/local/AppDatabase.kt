package com.otakeessen.bodysquirrel.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.otakeessen.bodysquirrel.data.local.dao.CategoryDao
import com.otakeessen.bodysquirrel.data.local.dao.DishDao
import com.otakeessen.bodysquirrel.data.local.dao.DishIngredientDao
import com.otakeessen.bodysquirrel.data.local.dao.IngredientDao
import com.otakeessen.bodysquirrel.data.local.dao.MealLogDao
import com.otakeessen.bodysquirrel.data.local.entity.CategoryEntity
import com.otakeessen.bodysquirrel.data.local.entity.DishEntity
import com.otakeessen.bodysquirrel.data.local.entity.DishIngredientEntity
import com.otakeessen.bodysquirrel.data.local.entity.IngredientEntity
import com.otakeessen.bodysquirrel.data.local.entity.MealLogEntity

@Database(
    entities = [
        CategoryEntity::class,
        IngredientEntity::class,
        DishEntity::class,
        DishIngredientEntity::class,
        MealLogEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun dishDao(): DishDao
    abstract fun dishIngredientDao(): DishIngredientDao
    abstract fun mealLogDao(): MealLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "body_squirrel_database"
                ).build().also { INSTANCE = it }
            }
    }
}
