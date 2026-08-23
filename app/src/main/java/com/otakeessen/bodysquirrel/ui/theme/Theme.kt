package com.otakeessen.bodysquirrel.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SquirrelOrange,
    onPrimary = Color.White,
    secondary = SquirrelBrown,
    onSecondary = Color.White,
    tertiary = Acorn,
    onTertiary = Color.White,
    background = Cream,
    onBackground = Cocoa,
    surface = Cream,
    onSurface = Cocoa,
    surfaceVariant = Amber.copy(alpha = 0.35f),
    onSurfaceVariant = Cocoa,
)

private val DarkColorScheme = darkColorScheme(
    primary = SquirrelOrangeDark,
    onPrimary = Color(0xFF3B1C0A),
    secondary = SquirrelBrownDark,
    onSecondary = Color(0xFF3B1C0A),
    tertiary = AcornDark,
    onTertiary = Color(0xFF241610),
    background = CreamDark,
    onBackground = CocoaDark,
    surface = CreamDark,
    onSurface = CocoaDark,
    surfaceVariant = SquirrelBrownDark.copy(alpha = 0.35f),
    onSurfaceVariant = CocoaDark,
)

@Composable
fun BodySquirrelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
