package com.example.snaprecipe.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Amber700,
    onPrimary = Cream,
    primaryContainer = Amber200,
    onPrimaryContainer = Charcoal,
    secondary = Amber500,
    onSecondary = Cream,
    background = Cream,
    onBackground = Charcoal,
    surface = Cream,
    onSurface = Charcoal,
    surfaceVariant = CreamDeep,
    onSurfaceVariant = WarmGray
)

private val DarkColors = darkColorScheme(
    primary = DarkAmber,
    onPrimary = DarkBrown,
    primaryContainer = Amber700,
    onPrimaryContainer = Cream,
    secondary = Amber400,
    onSecondary = DarkBrown,
    background = DarkBrown,
    onBackground = Cream,
    surface = DarkSurface,
    onSurface = Cream,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = Amber200
)

@Composable
fun SnapRecipeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Material You dynamic color is available on Android 12+.
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
