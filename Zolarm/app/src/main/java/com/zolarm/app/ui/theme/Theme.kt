package com.zolarm.app.ui.theme

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ZolarmDarkColors = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Ink900,
    primaryContainer = NeonCyanDim,
    onPrimaryContainer = Ink900,
    secondary = NeonViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF241B4A),
    onSecondaryContainer = Color(0xFFD9CCFF),
    tertiary = NeonMagenta,
    onTertiary = Color.White,
    background = Ink900,
    onBackground = TextPrimaryDark,
    surface = Ink850,
    onSurface = TextPrimaryDark,
    surfaceVariant = Ink800,
    onSurfaceVariant = TextMutedDark,
    surfaceContainer = Ink800,
    surfaceContainerHigh = Ink700,
    surfaceContainerHighest = Ink600,
    outline = Ink700,
    outlineVariant = Ink600,
    error = DangerRed,
    onError = Color.White
)

private val ZolarmLightColors = lightColorScheme(
    primary = Color(0xFF007A8A),
    onPrimary = Color.White,
    primaryContainer = NeonCyan,
    onPrimaryContainer = Ink900,
    secondary = Color(0xFF5A2BE0),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE4DAFF),
    onSecondaryContainer = Color(0xFF1B0B45),
    tertiary = Color(0xFFC1186E),
    onTertiary = Color.White,
    background = Paper100,
    onBackground = TextPrimaryLight,
    surface = Color.White,
    onSurface = TextPrimaryLight,
    surfaceVariant = Paper200,
    onSurfaceVariant = TextMutedLight,
    surfaceContainer = Paper200,
    surfaceContainerHigh = Paper300,
    surfaceContainerHighest = Paper300,
    outline = Paper300,
    outlineVariant = Paper200,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun ZolarmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> ZolarmDarkColors
        else -> ZolarmLightColors
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = colorScheme, typography = ZolarmTypography, content = content)
}
