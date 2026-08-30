package com.otakeessen.bodysquirrel.ui.addmeal

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.otakeessen.bodysquirrel.BodySquirrelApplication
import com.otakeessen.bodysquirrel.data.DateUtils
import com.otakeessen.bodysquirrel.data.MealType
import com.otakeessen.bodysquirrel.data.PortionCalculator
import com.otakeessen.bodysquirrel.data.local.entity.DishEntity
import com.otakeessen.bodysquirrel.data.local.entity.MealLogEntity
import com.otakeessen.bodysquirrel.data.local.repository.CatalogRepository
import com.otakeessen.bodysquirrel.data.local.repository.MealRepository
import com.otakeessen.bodysquirrel.data.llm.MealAnalyzer
import com.otakeessen.bodysquirrel.data.llm.MealScanItem
import com.otakeessen.bodysquirrel.data.toMealTypeOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface AddMealStep {
    data object ChooseMethod : AddMealStep
    data object ManualPick : AddMealStep
    data object ManualNew : AddMealStep
    data object PhotoCapture : AddMealStep
    data object ScanDraft : AddMealStep
    data object Done : AddMealStep
}

data class AddMealUiState(
    val step: AddMealStep = AddMealStep.ChooseMethod,
    val catalogDishes: List<DishEntity> = emptyList(),
    val searchQuery: String = "",
    val filteredDishes: List<DishEntity> = emptyList(),
    val selectedDish: DishEntity? = null,
    val selectedMealType: MealType = MealType.LUNCH,
    val portionWeightG: String = "200",
    val totalKcal: String = "400",
    // Manual New Dish
    val newDishName: String = "",
    // Photo / Scan Draft
    val capturedImageUri: Uri? = null,
    val capturedImageBytes: ByteArray? = null,
    val isAnalyzing: Boolean = false,
    val scanErrorMessage: String? = null,
    val draftDishName: String = "",
    val draftMealType: MealType = MealType.LUNCH,
    val draftPortionWeightG: String = "200",
    val draftTotalKcal: String = "400",
    val draftItems: List<MealScanItem> = emptyList(),
    val saveToCatalog: Boolean = true,
    val needsClarification: Boolean = false,
    val clarificationQuestion: String? = null,
    val clarificationOptions: List<String> = emptyList(),
    val selectedClarification: String? = null,
)

class AddMealViewModel(
    private val catalogRepository: CatalogRepository,
    private val mealRepository: MealRepository,
    private val mealAnalyzer: MealAnalyzer,
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
                return AddMealViewModel(
                    catalogRepository = application.catalogRepository,
                    mealRepository = application.mealRepository,
                    mealAnalyzer = application.mealAnalyzer,
                ) as T
            }
        }
    }

    private val _uiState = MutableStateFlow(AddMealUiState())

    val uiState: StateFlow<AddMealUiState> = combine(
        _uiState,
        catalogRepository.observeDishes()
    ) { state, dishes ->
        val filtered = if (state.searchQuery.isBlank()) {
            dishes
        } else {
            dishes.filter { it.name.contains(state.searchQuery, ignoreCase = true) }
        }
        state.copy(
            catalogDishes = dishes,
            filteredDishes = filtered
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AddMealUiState()
    )

    fun resetState() {
        _uiState.value = AddMealUiState()
    }

    fun navigateTo(step: AddMealStep) {
        _uiState.update { it.copy(step = step, scanErrorMessage = null) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectDish(dish: DishEntity) {
        _uiState.update {
            it.copy(
                selectedDish = dish,
                portionWeightG = dish.portionWeightG.toInt().toString(),
                totalKcal = dish.totalKcal.toInt().toString()
            )
        }
    }

    fun updateSelectedMealType(type: MealType) {
        _uiState.update { it.copy(selectedMealType = type) }
    }

    fun updatePortionWeight(weight: String) {
        _uiState.update { state ->
            val dish = state.selectedDish
            val newWeight = weight.toDoubleOrNull() ?: 0.0
            val recalculatedKcal = if (dish != null && newWeight > 0) {
                PortionCalculator.scaleCalories(dish.totalKcal, dish.portionWeightG, newWeight)
                    .toInt()
                    .toString()
            } else {
                state.totalKcal
            }
            state.copy(portionWeightG = weight, totalKcal = recalculatedKcal)
        }
    }

    fun updateNewDishName(name: String) {
        _uiState.update { it.copy(newDishName = name) }
    }

    fun updateTotalKcal(kcal: String) {
        _uiState.update { it.copy(totalKcal = kcal) }
    }

    fun logSelectedDish() {
        val state = _uiState.value
        val dish = state.selectedDish ?: return
        val weight = state.portionWeightG.toDoubleOrNull() ?: dish.portionWeightG
        val kcal = PortionCalculator.scaleCalories(dish.totalKcal, dish.portionWeightG, weight)

        viewModelScope.launch {
            mealRepository.insertLog(
                MealLogEntity(
                    date = DateUtils.todayIsoDate(),
                    mealType = state.selectedMealType.name,
                    dishId = dish.id,
                    portionWeightG = weight,
                    kcal = kcal
                )
            )
            _uiState.update { it.copy(step = AddMealStep.Done) }
        }
    }

    fun saveAndLogNewDish() {
        val state = _uiState.value
        val name = state.newDishName.trim().ifEmpty { "New Dish" }
        val weight = state.portionWeightG.toDoubleOrNull() ?: 200.0
        val kcal = state.totalKcal.toDoubleOrNull() ?: 300.0

        val dish = DishEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            portionWeightG = weight,
            totalKcal = kcal,
            emoji = "\uD83C\uDF72"
        )

        viewModelScope.launch {
            catalogRepository.insertDish(dish)
            mealRepository.insertLog(
                MealLogEntity(
                    date = DateUtils.todayIsoDate(),
                    mealType = state.selectedMealType.name,
                    dishId = dish.id,
                    portionWeightG = weight,
                    kcal = kcal
                )
            )
            _uiState.update { it.copy(step = AddMealStep.Done) }
        }
    }

    fun setCapturedPhoto(uri: Uri?, bytes: ByteArray?) {
        _uiState.update {
            it.copy(
                capturedImageUri = uri,
                capturedImageBytes = bytes,
                step = AddMealStep.PhotoCapture
            )
        }
    }

    fun analyzeCapturedPhoto(promptHint: String? = null) {
        val bytes = _uiState.value.capturedImageBytes ?: return
        _uiState.update {
            it.copy(
                step = AddMealStep.ScanDraft,
                isAnalyzing = true,
                scanErrorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val result = mealAnalyzer.analyze(bytes, promptHint)
                val guessedType = result.guessedMealType.toMealTypeOrNull() ?: MealType.LUNCH
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        draftDishName = result.dishName,
                        draftMealType = guessedType,
                        draftPortionWeightG = (if (result.portionWeightG > 0) result.portionWeightG else 200.0).toInt().toString(),
                        draftTotalKcal = (if (result.totalKcal > 0) result.totalKcal else 350.0).toInt().toString(),
                        draftItems = result.items,
                        needsClarification = result.needsClarification,
                        clarificationQuestion = result.question,
                        clarificationOptions = result.options,
                        selectedClarification = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        scanErrorMessage = e.localizedMessage ?: "Failed to analyze photo"
                    )
                }
            }
        }
    }

    fun selectClarificationOption(option: String) {
        _uiState.update {
            it.copy(
                selectedClarification = option,
                draftDishName = "${it.draftDishName} ($option)"
            )
        }
    }

    fun updateDraftDishName(name: String) {
        _uiState.update { it.copy(draftDishName = name) }
    }

    fun updateDraftMealType(type: MealType) {
        _uiState.update { it.copy(draftMealType = type) }
    }

    fun updateDraftPortionWeight(weight: String) {
        _uiState.update { it.copy(draftPortionWeightG = weight) }
    }

    fun updateDraftTotalKcal(kcal: String) {
        _uiState.update { it.copy(draftTotalKcal = kcal) }
    }

    fun updateSaveToCatalog(save: Boolean) {
        _uiState.update { it.copy(saveToCatalog = save) }
    }

    fun saveScanDraft() {
        val state = _uiState.value
        val dishName = state.draftDishName.trim().ifEmpty { "Scanned Meal" }
        val weight = state.draftPortionWeightG.toDoubleOrNull() ?: 200.0
        val kcal = state.draftTotalKcal.toDoubleOrNull() ?: 350.0

        viewModelScope.launch {
            val dishId = if (state.saveToCatalog) {
                val dish = DishEntity(
                    id = UUID.randomUUID().toString(),
                    name = dishName,
                    portionWeightG = weight,
                    totalKcal = kcal,
                    emoji = "\uD83D\uDCF7"
                )
                catalogRepository.insertDish(dish)
                dish.id
            } else {
                null
            }

            mealRepository.insertLog(
                MealLogEntity(
                    date = DateUtils.todayIsoDate(),
                    mealType = state.draftMealType.name,
                    dishId = dishId,
                    portionWeightG = weight,
                    kcal = kcal
                )
            )

            _uiState.update { it.copy(step = AddMealStep.Done) }
        }
    }
}
