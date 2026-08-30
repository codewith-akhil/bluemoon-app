package com.example.telegram.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = TelegramBlue,
    onPrimary = Color.White,
    primaryContainer = TelegramSky,
    onPrimaryContainer = TelegramBlue,
    secondary = TelegramLightBlue,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = TelegramTextPrimary,
    surface = Color.White,
    onSurface = TelegramTextPrimary,
    surfaceVariant = TelegramLightBackground,
    onSurfaceVariant = TelegramTextSecondary,
    outline = TelegramDivider
)

private val DarkColorScheme = darkColorScheme(
    primary = TelegramDarkPrimary,
    onPrimary = Color.White,
    primaryContainer = TelegramDarkSurfaceElevated,
    onPrimaryContainer = Color.White,
    secondary = TelegramLightBlue,
    onSecondary = Color.White,
    background = TelegramDarkBg,
    onBackground = TelegramDarkTextPrimary,
    surface = TelegramDarkSurface,
    onSurface = TelegramDarkTextPrimary,
    surfaceVariant = TelegramDarkSurfaceElevated,
    onSurfaceVariant = TelegramDarkTextSecondary,
    outline = TelegramDarkDivider
)

@Composable
fun TelegramTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
