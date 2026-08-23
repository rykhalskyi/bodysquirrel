package com.otakeessen.bodysquirrel.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.ui.graphics.vector.ImageVector
import com.otakeessen.bodysquirrel.R

sealed class Destination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
) {
    object Home : Destination("home", R.string.nav_home, Icons.Filled.Home)
    object Meals : Destination("meals", R.string.nav_meals, Icons.Filled.RestaurantMenu)
    object Progress : Destination("progress", R.string.nav_progress, Icons.Filled.BarChart)
    object Profile : Destination("profile", R.string.nav_profile, Icons.Filled.Person)
}

val bottomNavDestinations = listOf(
    Destination.Home,
    Destination.Meals,
    Destination.Progress,
    Destination.Profile,
)
