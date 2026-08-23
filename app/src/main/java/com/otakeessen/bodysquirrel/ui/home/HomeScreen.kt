package com.otakeessen.bodysquirrel.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.otakeessen.bodysquirrel.R
import com.otakeessen.bodysquirrel.data.MealType
import com.otakeessen.bodysquirrel.data.MealTypeTotal
import com.otakeessen.bodysquirrel.ui.theme.BodySquirrelTheme
import java.util.Calendar
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    onAddMeal: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsState()
    HomeScreenContent(
        state = state,
        onAddMeal = onAddMeal,
        modifier = modifier
    )
}

@Composable
private fun HomeScreenContent(
    state: HomeUiState,
    onAddMeal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 1. Fixed Background Hero Section
        HeroSection(modifier = Modifier.fillMaxSize())

        // 2. Scrollable Content on top
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 0.dp), // Padding handled by Surface
        ) {
            // Spacer for the fixed header
            item {
                Spacer(modifier = Modifier.statusBarsPadding().height(64.dp))
            }

            // 3. Transparent space to see the squirrel
            item {
                Spacer(modifier = Modifier.height(300.dp))
            }

            // 4. Content Cards onto a semi-transparent background
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                    color = Color.White.copy(alpha = 0.4f),
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                            .padding(bottom = 48.dp), // Extra bottom space for scrolling
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SquirrelEnergyCard(state)
                        TodayProgressSection(state = state, onAddMeal = onAddMeal)
                        DailyTipCard()
                    }
                }
            }
        }

        // 5. Fixed Header at the top
        Header(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun Header(modifier: Modifier = Modifier) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetingRes = when (hour) {
        in 5..11 -> R.string.greeting_morning
        in 12..17 -> R.string.greeting_afternoon
        else -> R.string.greeting_evening
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(greetingRes),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = stringResource(R.string.subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
            )
        }
        IconButton(onClick = { /* TODO: gifts */ }) {
            Icon(
                imageVector = Icons.Filled.Redeem,
                contentDescription = stringResource(R.string.gift_button_desc),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun HeroSection(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.bs_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 0.dp, y = 0.dp),
        )
        Image(
            painter = painterResource(R.drawable.hero),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(1f)
                .offset(x = 0.dp, y = 0.dp),
        )/*
        SpeechBubble(
            text = stringResource(R.string.speech_bubble),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
                .offset(y = 0.dp),
        )*/
    }
}

@Composable
private fun SpeechBubble(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shadowElevation = 2.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SquirrelEnergyCard(state: HomeUiState) {
    val consumed = state.totalKcal
    val budget = state.budgetKcal
    val remaining = (budget - consumed).roundToInt()
    val progress = if (budget <= 0.0) 0f else (consumed / budget).toFloat().coerceIn(0f, 1f)
    val description = if (remaining >= 0) {
        stringResource(R.string.energy_kcal_left, remaining)
    } else {
        stringResource(R.string.energy_kcal_over, -remaining)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.energy_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TodayProgressSection(state: HomeUiState, onAddMeal: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.today_progress_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MealType.entries.forEach { type ->
                MealTypeCard(
                    type = type,
                    total = state.totalsByType[type],
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Button(onClick = onAddMeal, modifier = Modifier.fillMaxWidth()) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.add_meal))
        }
    }
}

@Composable
private fun MealTypeCard(
    type: MealType,
    total: MealTypeTotal?,
    modifier: Modifier = Modifier,
) {
    val labelRes = when (type) {
        MealType.BREAKFAST -> R.string.meal_breakfast
        MealType.LUNCH -> R.string.meal_lunch
        MealType.DINNER -> R.string.meal_dinner
        MealType.SNACKS -> R.string.meal_snacks
        MealType.WATER -> R.string.meal_water
    }
    val emoji = when (type) {
        MealType.BREAKFAST -> "\uD83C\uDF73"
        MealType.LUNCH -> "\uD83E\uDD6A"
        MealType.DINNER -> "\uD83C\uDF7D\uFE0F"
        MealType.SNACKS -> "\uD83C\uDF7F"
        MealType.WATER -> "\uD83D\uDCA7"
    }
    val value = if (type == MealType.WATER) {
        "${(total?.waterMl ?: 0.0).roundToInt()} ${stringResource(R.string.ml_short)}"
    } else {
        "${(total?.kcal ?: 0.0).roundToInt()} ${stringResource(R.string.kcal_short)}"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = emoji, style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun DailyTipCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.daily_tip_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.daily_tip_text),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    BodySquirrelTheme {
        HomeScreenContent(
            state = HomeUiState(),
            onAddMeal = {}
        )
    }
}
