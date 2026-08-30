package com.otakeessen.bodysquirrel.ui.addmeal

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.otakeessen.bodysquirrel.R
import com.otakeessen.bodysquirrel.data.MealType
import com.otakeessen.bodysquirrel.data.local.entity.DishEntity
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealSheet(
    onDismissRequest: () -> Unit,
    viewModel: AddMealViewModel = viewModel(factory = AddMealViewModel.Factory),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val state by viewModel.uiState.collectAsState()

    val handleDismiss = {
        viewModel.resetState()
        onDismissRequest()
    }

    LaunchedEffect(Unit) {
        if (viewModel.uiState.value.step is AddMealStep.Done) {
            viewModel.resetState()
        }
    }

    LaunchedEffect(state.step) {
        if (state.step is AddMealStep.Done) {
            handleDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = handleDismiss,
        sheetState = sheetState,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            SheetHeader(
                step = state.step,
                onBack = {
                    when (state.step) {
                        AddMealStep.ChooseMethod -> handleDismiss()
                        else -> viewModel.navigateTo(AddMealStep.ChooseMethod)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Crossfade(targetState = state.step, label = "AddMealStep") { currentStep ->
                when (currentStep) {
                    AddMealStep.ChooseMethod -> ChooseMethodContent(
                        state = state,
                        onScanClick = { viewModel.navigateTo(AddMealStep.PhotoCapture) },
                        onManualPickClick = { viewModel.navigateTo(AddMealStep.ManualPick) },
                        onManualNewClick = { viewModel.navigateTo(AddMealStep.ManualNew) },
                        onQuickDishSelect = { dish ->
                            viewModel.selectDish(dish)
                            viewModel.navigateTo(AddMealStep.ManualPick)
                        }
                    )

                    AddMealStep.ManualPick -> ManualPickContent(
                        state = state,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onDishSelect = viewModel::selectDish,
                        onMealTypeSelect = viewModel::updateSelectedMealType,
                        onWeightChange = viewModel::updatePortionWeight,
                        onConfirmLog = viewModel::logSelectedDish
                    )

                    AddMealStep.ManualNew -> ManualNewDishContent(
                        state = state,
                        onNameChange = viewModel::updateNewDishName,
                        onMealTypeSelect = viewModel::updateSelectedMealType,
                        onWeightChange = viewModel::updatePortionWeight,
                        onKcalChange = viewModel::updateTotalKcal,
                        onConfirmSave = viewModel::saveAndLogNewDish
                    )

                    AddMealStep.PhotoCapture -> PhotoCaptureContent(
                        state = state,
                        onPhotoSelected = { uri, bytes ->
                            viewModel.setCapturedPhoto(uri, bytes)
                        },
                        onAnalyze = { viewModel.analyzeCapturedPhoto() }
                    )

                    AddMealStep.ScanDraft -> ScanDraftContent(
                        state = state,
                        onNameChange = viewModel::updateDraftDishName,
                        onMealTypeSelect = viewModel::updateDraftMealType,
                        onWeightChange = viewModel::updateDraftPortionWeight,
                        onKcalChange = viewModel::updateDraftTotalKcal,
                        onSaveToCatalogChange = viewModel::updateSaveToCatalog,
                        onClarificationSelect = viewModel::selectClarificationOption,
                        onConfirmLog = viewModel::saveScanDraft,
                        onFallbackManual = { viewModel.navigateTo(AddMealStep.ManualNew) },
                        onRetryScan = { viewModel.analyzeCapturedPhoto() }
                    )

                    AddMealStep.Done -> {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetHeader(
    step: AddMealStep,
    onBack: () -> Unit
) {
    val title = when (step) {
        AddMealStep.ChooseMethod -> stringResource(R.string.add_meal)
        AddMealStep.ManualPick -> "Select Dish from Catalog"
        AddMealStep.ManualNew -> "Enter New Dish"
        AddMealStep.PhotoCapture -> "Take or Pick Photo"
        AddMealStep.ScanDraft -> "Review Scanned Meal"
        AddMealStep.Done -> stringResource(R.string.add_meal)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ChooseMethodContent(
    state: AddMealUiState,
    onScanClick: () -> Unit,
    onManualPickClick: () -> Unit,
    onManualNewClick: () -> Unit,
    onQuickDishSelect: (DishEntity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                onClick = onScanClick,
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Scan Photo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "AI meal scanner",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Card(
                onClick = onManualPickClick,
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.RestaurantMenu,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "From Catalog",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Pick saved dishes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onManualNewClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Enter Custom Dish Manually")
        }

        if (state.catalogDishes.isNotEmpty()) {
            Text(
                text = "Recent / Popular Dishes",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyColumn(
                modifier = Modifier.height(180.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.catalogDishes.take(5)) { dish ->
                    Surface(
                        onClick = { onQuickDishSelect(dish) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dish.emoji ?: "\uD83C\uDF72",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dish.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${dish.portionWeightG.roundToInt()}g • ${dish.totalKcal.roundToInt()} kcal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Select"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualPickContent(
    state: AddMealUiState,
    onSearchQueryChange: (String) -> Unit,
    onDishSelect: (DishEntity) -> Unit,
    onMealTypeSelect: (MealType) -> Unit,
    onWeightChange: (String) -> Unit,
    onConfirmLog: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Search Catalog") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        MealTypeSelector(
            selectedType = state.selectedMealType,
            onSelect = onMealTypeSelect
        )

        Text(
            text = "Select Dish",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier.height(160.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.filteredDishes) { dish ->
                val isSelected = state.selectedDish?.id == dish.id
                Surface(
                    onClick = { onDishSelect(dish) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = dish.emoji ?: "\uD83C\uDF72", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = dish.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Base: ${dish.portionWeightG.roundToInt()}g (${dish.totalKcal.roundToInt()} kcal)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isSelected) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        if (state.selectedDish != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.portionWeightG,
                    onValueChange = onWeightChange,
                    label = { Text("Portion (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Total Energy", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = "${state.totalKcal} kcal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Button(
                onClick = onConfirmLog,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Log ${state.selectedDish.name}")
            }
        }
    }
}

@Composable
private fun ManualNewDishContent(
    state: AddMealUiState,
    onNameChange: (String) -> Unit,
    onMealTypeSelect: (MealType) -> Unit,
    onWeightChange: (String) -> Unit,
    onKcalChange: (String) -> Unit,
    onConfirmSave: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = state.newDishName,
            onValueChange = onNameChange,
            label = { Text("Dish Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        MealTypeSelector(
            selectedType = state.selectedMealType,
            onSelect = onMealTypeSelect
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.portionWeightG,
                onValueChange = onWeightChange,
                label = { Text("Weight (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            OutlinedTextField(
                value = state.totalKcal,
                onValueChange = onKcalChange,
                label = { Text("Calories (kcal)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Button(
            onClick = onConfirmSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.newDishName.isNotBlank()
        ) {
            Text(text = "Save Dish & Log Meal")
        }
    }
}

@Composable
private fun PhotoCaptureContent(
    state: AddMealUiState,
    onPhotoSelected: (Uri?, ByteArray?) -> Unit,
    onAnalyze: () -> Unit
) {
    val context = LocalContext.current
    var tempUri = remember { createTempImageUri(context) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = TakePictureWithUriGrant()
    ) { success ->
        if (success && tempUri != null) {
            val bytes = context.contentResolver.openInputStream(tempUri!!)?.use { it.readBytes() }
            onPhotoSelected(tempUri, bytes)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            onPhotoSelected(uri, bytes)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (state.capturedImageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(state.capturedImageUri),
                contentDescription = "Meal photo preview",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        tempUri = createTempImageUri(context)
                        tempUri?.let { cameraLauncher.launch(it) }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Retake")
                }

                Button(
                    onClick = onAnalyze,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.FilterVintage, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Analyze with AI")
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "No photo selected yet", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        tempUri = createTempImageUri(context)
                        tempUri?.let { cameraLauncher.launch(it) }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Take Photo")
                }

                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("From Gallery")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScanDraftContent(
    state: AddMealUiState,
    onNameChange: (String) -> Unit,
    onMealTypeSelect: (MealType) -> Unit,
    onWeightChange: (String) -> Unit,
    onKcalChange: (String) -> Unit,
    onSaveToCatalogChange: (Boolean) -> Unit,
    onClarificationSelect: (String) -> Unit,
    onConfirmLog: () -> Unit,
    onFallbackManual: () -> Unit,
    onRetryScan: () -> Unit
) {
    if (state.isAnalyzing) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Squirrel AI is scanning your meal...",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
        return
    }

    if (state.scanErrorMessage != null) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Scan Error: ${state.scanErrorMessage}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRetryScan) { Text("Retry Scan") }
                OutlinedButton(onClick = onFallbackManual) { Text("Enter Manually") }
            }
        }
        return
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (state.needsClarification && state.clarificationQuestion != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Disambiguation: ${state.clarificationQuestion}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.clarificationOptions.forEach { option ->
                            AssistChip(
                                onClick = { onClarificationSelect(option) },
                                label = { Text(option) }
                            )
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = state.draftDishName,
            onValueChange = onNameChange,
            label = { Text("Dish Name (Guessed)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        MealTypeSelector(
            selectedType = state.draftMealType,
            onSelect = onMealTypeSelect
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.draftPortionWeightG,
                onValueChange = onWeightChange,
                label = { Text("Weight (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            OutlinedTextField(
                value = state.draftTotalKcal,
                onValueChange = onKcalChange,
                label = { Text("Calories (kcal)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        if (state.draftItems.isNotEmpty()) {
            Text(
                text = "Detected Ingredients",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.draftItems.forEach { item ->
                    AssistChip(
                        onClick = {},
                        label = { Text("${item.name} (${item.weightG.roundToInt()}g)") }
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onSaveToCatalogChange(!state.saveToCatalog) }
        ) {
            Checkbox(
                checked = state.saveToCatalog,
                onCheckedChange = onSaveToCatalogChange
            )
            Text(
                text = "Save to Catalog as a reusable dish",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = onConfirmLog,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirm & Log Meal")
        }
    }
}

@Composable
private fun MealTypeSelector(
    selectedType: MealType,
    onSelect: (MealType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        MealType.entries.filter { it != MealType.WATER }.forEach { type ->
            val selected = selectedType == type
            FilterChip(
                selected = selected,
                onClick = { onSelect(type) },
                label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun createTempImageUri(context: Context): Uri? {
    return try {
        val file = File.createTempFile("meal_photo_", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    } catch (e: Exception) {
        null
    }
}

class TakePictureWithUriGrant : ActivityResultContract<Uri, Boolean>() {
    override fun createIntent(context: Context, input: Uri): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, input)
            clipData = ClipData.newRawUri("output", input)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return resultCode == Activity.RESULT_OK
    }
}
