package com.otakeessen.bodysquirrel.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.otakeessen.bodysquirrel.BodySquirrelApplication
import com.otakeessen.bodysquirrel.data.DateUtils
import com.otakeessen.bodysquirrel.data.MealType
import com.otakeessen.bodysquirrel.data.MealTypeTotal
import com.otakeessen.bodysquirrel.data.local.repository.MealRepository
import com.otakeessen.bodysquirrel.data.toMealTypeOrNull
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val totalsByType: Map<MealType, MealTypeTotal> = emptyMap(),
    val totalKcal: Double = 0.0,
    val waterMl: Double = 0.0,
    val budgetKcal: Double = DEFAULT_BUDGET_KCAL,
)

const val DEFAULT_BUDGET_KCAL = 2000.0

class HomeViewModel(
    private val mealRepository: MealRepository,
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras,
            ): T {
                val application = checkNotNull(
                    extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                ) as BodySquirrelApplication
                return HomeViewModel(application.mealRepository) as T
            }
        }
    }

    val uiState: StateFlow<HomeUiState> = mealRepository
        .observeLogs(DateUtils.todayIsoDate())
        .map { logs ->
            val totalsByType = logs
                .groupBy { it.mealType }
                .mapNotNull { (name, entries) ->
                    name.toMealTypeOrNull()?.let { type ->
                        type to MealTypeTotal(
                            mealType = type,
                            kcal = entries.sumOf { it.kcal },
                            waterMl = entries.sumOf { it.waterMl },
                        )
                    }
                }
                .toMap()

            HomeUiState(
                totalsByType = totalsByType,
                totalKcal = logs.sumOf { it.kcal },
                waterMl = logs.sumOf { it.waterMl },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )
}
