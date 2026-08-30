package com.otakeessen.bodysquirrel

import com.otakeessen.bodysquirrel.data.MealType
import com.otakeessen.bodysquirrel.data.local.entity.DishEntity
import com.otakeessen.bodysquirrel.data.local.entity.MealLogEntity
import com.otakeessen.bodysquirrel.data.local.repository.CatalogRepository
import com.otakeessen.bodysquirrel.data.local.repository.MealRepository
import com.otakeessen.bodysquirrel.data.llm.MealAnalyzer
import com.otakeessen.bodysquirrel.data.llm.MealScanResult
import com.otakeessen.bodysquirrel.ui.addmeal.AddMealStep
import com.otakeessen.bodysquirrel.ui.addmeal.AddMealViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddMealViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val mockDishes = MutableStateFlow<List<DishEntity>>(emptyList())
    private val insertedLogs = mutableListOf<MealLogEntity>()
    private val insertedDishes = mutableListOf<DishEntity>()

    private val catalogRepo = object : CatalogRepository(FakeAppDatabase()) {
        override fun observeDishes(): Flow<List<DishEntity>> = mockDishes
        override suspend fun insertDish(dish: DishEntity) {
            insertedDishes.add(dish)
        }
    }

    private val mealRepo = object : MealRepository(FakeAppDatabase()) {
        override suspend fun insertLog(log: MealLogEntity) {
            insertedLogs.add(log)
        }
    }

    private val fakeAnalyzer = object : MealAnalyzer {
        override suspend fun analyze(imageBytes: ByteArray, promptHint: String?): MealScanResult {
            return MealScanResult(
                dishName = "Scanned Chicken Rice",
                guessedMealType = "LUNCH",
                totalKcal = 500.0,
                portionWeightG = 250.0
            )
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsChooseMethod() = runTest {
        val viewModel = AddMealViewModel(catalogRepo, mealRepo, fakeAnalyzer)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        assertEquals(AddMealStep.ChooseMethod, viewModel.uiState.value.step)
    }

    @Test
    fun portionWeightChangeRecalculatesKcalForSelectedDish() = runTest {
        val viewModel = AddMealViewModel(catalogRepo, mealRepo, fakeAnalyzer)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        val dish = DishEntity(id = "1", name = "Pasta", portionWeightG = 200.0, totalKcal = 400.0)

        viewModel.selectDish(dish)
        testScheduler.advanceUntilIdle()
        assertEquals("400", viewModel.uiState.value.totalKcal)

        viewModel.updatePortionWeight("100")
        testScheduler.advanceUntilIdle()
        assertEquals("200", viewModel.uiState.value.totalKcal)
    }

    @Test
    fun saveAndLogNewDishInsertsDishAndLog() = runTest {
        val viewModel = AddMealViewModel(catalogRepo, mealRepo, fakeAnalyzer)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.updateNewDishName("Salad Bowl")
        viewModel.updatePortionWeight("150")
        viewModel.updateTotalKcal("250")
        viewModel.updateSelectedMealType(MealType.DINNER)

        viewModel.saveAndLogNewDish()
        testScheduler.advanceUntilIdle()

        assertEquals(1, insertedDishes.size)
        assertEquals("Salad Bowl", insertedDishes.first().name)
        assertEquals(1, insertedLogs.size)
        assertEquals("DINNER", insertedLogs.first().mealType)
        assertEquals(AddMealStep.Done, viewModel.uiState.value.step)
    }

    @Test
    fun analyzePhotoPopulatesScanDraft() = runTest {
        val viewModel = AddMealViewModel(catalogRepo, mealRepo, fakeAnalyzer)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.setCapturedPhoto(null, "fake_image".toByteArray())
        viewModel.analyzeCapturedPhoto()
        testScheduler.advanceUntilIdle()

        assertEquals("Scanned Chicken Rice", viewModel.uiState.value.draftDishName)
        assertEquals("500", viewModel.uiState.value.draftTotalKcal)
        assertEquals("250", viewModel.uiState.value.draftPortionWeightG)
        assertEquals(AddMealStep.ScanDraft, viewModel.uiState.value.step)
    }

    @Test
    fun resetStateResetsStepToChooseMethod() = runTest {
        val viewModel = AddMealViewModel(catalogRepo, mealRepo, fakeAnalyzer)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.navigateTo(AddMealStep.Done)
        testScheduler.advanceUntilIdle()
        assertEquals(AddMealStep.Done, viewModel.uiState.value.step)

        viewModel.resetState()
        testScheduler.advanceUntilIdle()
        assertEquals(AddMealStep.ChooseMethod, viewModel.uiState.value.step)
    }
}

// Minimal dummy AppDatabase to satisfy constructor
private class FakeAppDatabase : com.otakeessen.bodysquirrel.data.local.AppDatabase() {
    @Suppress("OVERRIDE_DEPRECATION")
    override fun createOpenHelper(config: androidx.room.DatabaseConfiguration): androidx.sqlite.db.SupportSQLiteOpenHelper {
        throw UnsupportedOperationException()
    }
    override fun createInvalidationTracker(): androidx.room.InvalidationTracker {
        throw UnsupportedOperationException()
    }
    override fun clearAllTables() {}
    override fun categoryDao() = throw UnsupportedOperationException()
    override fun dishDao() = throw UnsupportedOperationException()
    override fun ingredientDao() = throw UnsupportedOperationException()
    override fun dishIngredientDao() = throw UnsupportedOperationException()
    override fun mealLogDao() = throw UnsupportedOperationException()
}
