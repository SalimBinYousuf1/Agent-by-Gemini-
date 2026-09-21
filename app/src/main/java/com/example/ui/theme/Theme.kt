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
import com.example.data.local.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = AppleAccentBlueDark,
    onPrimary = Color.White,
    primaryContainer = AppleAccentMutedDark,
    onPrimaryContainer = AppleAccentBlueDark,
    secondary = AppleGreenDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF132B1C),
    onSecondaryContainer = AppleGreenDark,
    tertiary = AppleTealDark,
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
    secondary = AppleGreenLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8F7EC),
    onSecondaryContainer = AppleGreenLight,
    tertiary = AppleTealLight,
    background = AppleSystemBgLight,
    onBackground = AppleTextPrimaryLight,
    surface = AppleSurfaceLight,
    onSurface = AppleTextPrimaryLight,
    surfaceVariant = AppleSurfaceSecondaryLight,
    onSurfaceVariant = AppleTextSecondaryLight,
    outline = AppleSeparatorLight,
    outlineVariant = AppleSeparatorLight
)

val SalimShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(22.dp)
)

@Composable
fun SalimTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemInDark
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = SalimShapes,
        content = content
    )
}

