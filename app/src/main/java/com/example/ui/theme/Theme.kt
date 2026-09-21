package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = AppleAccentBlueDark,
    onPrimary = Color.White,
    primaryContainer = AppleAccentMutedDark,
    onPrimaryContainer = AppleAccentBlueDark,
    secondary = AppleAccentBlueDark,
    onSecondary = Color.White,
    background = AppleSystemBgDark,
    onBackground = AppleTextPrimaryDark,
    surface = AppleSurfaceDark,
    onSurface = AppleTextPrimaryDark,
    surfaceVariant = AppleSurfaceElevatedDark,
    onSurfaceVariant = AppleTextSecondaryDark,
    outline = AppleSeparatorDark,
    outlineVariant = AppleSeparatorDark
)

private val LightColorScheme = lightColorScheme(
    primary = AppleAccentBlueLight,
    onPrimary = Color.White,
    primaryContainer = AppleAccentMutedLight,
    onPrimaryContainer = AppleAccentBlueLight,
    secondary = AppleAccentBlueLight,
    onSecondary = Color.White,
    background = AppleSystemBgLight,
    onBackground = AppleTextPrimaryLight,
    surface = AppleSurfaceLight,
    onSurface = AppleTextPrimaryLight,
    surfaceVariant = AppleSeparatorLight,
    onSurfaceVariant = AppleTextSecondaryLight,
    outline = AppleSeparatorLight,
    outlineVariant = AppleSeparatorLight
)

val SalimShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)

@Composable
fun SalimTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = SalimShapes,
        content = content
    )
}
