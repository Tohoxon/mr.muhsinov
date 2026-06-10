package com.muhsinov.budget.ui.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Teal60,
    onPrimary = Color(0xFF003731),
    primaryContainer = Teal20,
    onPrimaryContainer = Teal80,
    secondary = Green80,
    onSecondary = Color(0xFF1B3B1C),
    secondaryContainer = Green20,
    onSecondaryContainer = Green80,
    tertiary = Color(0xFF80DEEA),
    onTertiary = Color(0xFF00363C),
    tertiaryContainer = Color(0xFF004F57),
    onTertiaryContainer = Color(0xFFB2EBF2),
    background = SurfaceDark,
    onBackground = Neutral90,
    surface = SurfaceDark,
    onSurface = Neutral90,
    surfaceVariant = CardDark,
    onSurfaceVariant = Neutral80,
    outline = Color(0xFF4A6466),
    outlineVariant = Color(0xFF253D42),
    error = Color(0xFFCF6679),
    onError = Color(0xFF680020),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Neutral90,
    inverseOnSurface = Neutral20,
    inversePrimary = Teal40
)

private val LightColorScheme = lightColorScheme(
    primary = Teal40,
    onPrimary = Color.White,
    primaryContainer = Teal80,
    onPrimaryContainer = Teal20,
    secondary = Green40,
    onSecondary = Color.White,
    secondaryContainer = Green80,
    onSecondaryContainer = Green20,
    tertiary = Color(0xFF006874),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF97F0FF),
    onTertiaryContainer = Color(0xFF001F24),
    background = SurfaceLight,
    onBackground = Neutral10,
    surface = SurfaceLight,
    onSurface = Neutral10,
    surfaceVariant = Neutral95,
    onSurfaceVariant = Neutral30,
    outline = Color(0xFF5F7C7F),
    outlineVariant = Neutral90,
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    inverseSurface = Neutral20,
    inverseOnSurface = Neutral95,
    inversePrimary = Teal80
)

@Composable
fun BudgetAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
