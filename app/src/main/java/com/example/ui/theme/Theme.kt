package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
    themeData: AppThemeData = AppThemes.Cyberpunk,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (themeData.isDark) {
        darkColorScheme(
            primary = themeData.primary,
            onPrimary = Color.White,
            primaryContainer = themeData.cardSecondary,
            onPrimaryContainer = themeData.textMain,
            secondary = themeData.accent,
            onSecondary = Color.Black,
            background = themeData.canvas,
            onBackground = themeData.textMain,
            surface = themeData.card,
            onSurface = themeData.textMain,
            surfaceVariant = themeData.cardSecondary,
            onSurfaceVariant = themeData.textMuted,
            outline = themeData.cardBorder,
            error = themeData.error
        )
    } else {
        lightColorScheme(
            primary = themeData.primary,
            onPrimary = Color.White,
            primaryContainer = themeData.cardSecondary,
            onPrimaryContainer = themeData.textMain,
            secondary = themeData.accent,
            onSecondary = Color.White,
            background = themeData.canvas,
            onBackground = themeData.textMain,
            surface = themeData.card,
            onSurface = themeData.textMain,
            surfaceVariant = themeData.cardSecondary,
            onSurfaceVariant = themeData.textMuted,
            outline = themeData.cardBorder,
            error = themeData.error
        )
    }

    CompositionLocalProvider(LocalAppTheme provides themeData) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
