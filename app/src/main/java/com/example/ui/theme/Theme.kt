package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = EagleBluePrimary,
    onPrimary = Color.White,
    primaryContainer = EagleBlueContainer,
    onPrimaryContainer = EagleBlueDark,
    secondary = ForestGreenSecondary,
    onSecondary = Color.White,
    secondaryContainer = ForestGreenContainer,
    onSecondaryContainer = ForestGreenSecondary,
    tertiary = BumblebeeYellowTertiary,
    onTertiary = TextPrimaryDark,
    tertiaryContainer = BumblebeeYellowContainer,
    onTertiaryContainer = TextPrimaryDark,
    background = GlassSurfaceLight,
    onBackground = TextPrimaryDark,
    surface = GlassCardBackground,
    onSurface = TextPrimaryDark,
    surfaceVariant = GlassCardBackground,
    onSurfaceVariant = TextSecondaryDark,
    outline = GlassCardBorder,
    error = ErrorRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = EagleBlueLight,
    onPrimary = Color.White,
    primaryContainer = EagleBlueDark,
    onPrimaryContainer = EagleBlueContainer,
    secondary = ForestGreenSecondary,
    onSecondary = Color.White,
    tertiary = BumblebeeYellowTertiary,
    background = EagleBlueDark,
    onBackground = Color.White,
    surface = Color(0xFF132338),
    onSurface = Color.White,
    outline = Color(0xFF233852)
)

@Composable
fun AgilaHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
