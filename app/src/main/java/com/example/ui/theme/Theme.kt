package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkDeepTealPrimary,
    primaryContainer = DarkTealContainer,
    secondary = DarkRescueRedAlert,
    tertiary = AlertAmber,
    background = DarkSlateBackground,
    surface = DarkSlateSurface,
    onPrimary = DarkSlateBackground,
    onSecondary = DarkSlateBackground,
    onBackground = DarkSlateTextLight,
    onSurface = DarkSlateTextLight
)

private val LightColorScheme = lightColorScheme(
    primary = DeepTealPrimary,
    primaryContainer = LightTealContainer,
    secondary = RescueRedAlert,
    tertiary = AlertAmber,
    background = SlateBackground,
    surface = SlateSurface,
    onPrimary = SlateSurface,
    onSecondary = SlateSurface,
    onBackground = SlateTextDark,
    onSurface = SlateTextDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors so our high-contrast healthcare branding is consistently preserved
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
