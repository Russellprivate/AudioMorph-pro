package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.model.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryVioletLight,
    onPrimary = Color.White,
    primaryContainer = PrimaryVioletDark,
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = SecondaryTeal,
    onSecondary = Color.Black,
    secondaryContainer = SecondaryTealDark,
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = TertiaryMagenta,
    background = DarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceContainer = DarkSurfaceElevated,
    outline = DarkBorder
)

private val AmoledColorScheme = darkColorScheme(
    primary = PrimaryVioletLight,
    onPrimary = Color.White,
    primaryContainer = PrimaryVioletDark,
    secondary = SecondaryTealLight,
    background = AmoledBackground,
    onBackground = Color(0xFFF8FAFC),
    surface = AmoledSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = AmoledSurfaceElevated,
    surfaceContainer = AmoledSurfaceElevated,
    outline = Color(0xFF262626)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryViolet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = PrimaryVioletDark,
    secondary = SecondaryTealDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = SecondaryTealDark,
    tertiary = TertiaryMagentaDark,
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = Color(0xFF64748B),
    surfaceContainer = LightSurfaceElevated,
    outline = LightBorder
)

@Composable
fun AudioMorphTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.DARK -> true
        ThemeMode.AMOLED -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = when {
        themeMode == ThemeMode.AMOLED -> AmoledColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
