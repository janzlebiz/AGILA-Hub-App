package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.Typography

// Original Rich Brand Light Color Scheme
private val OriginalLightColorScheme = lightColorScheme(
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

// Original Rich Brand Dark Color Scheme
private val OriginalDarkColorScheme = darkColorScheme(
    primary = EagleBlueLight,
    onPrimary = Color.White,
    primaryContainer = EagleBlueDark,
    onPrimaryContainer = EagleBlueContainer,
    secondary = ForestGreenSecondary,
    onSecondary = Color.White,
    secondaryContainer = EagleBlueDark,
    onSecondaryContainer = ForestGreenContainer,
    tertiary = BumblebeeYellowTertiary,
    onTertiary = TextPrimaryDark,
    tertiaryContainer = EagleBlueDark,
    onTertiaryContainer = BumblebeeYellowContainer,
    background = EagleBlueDark,
    onBackground = Color.White,
    surface = Color(0xFF132338),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1C2E46),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF233852),
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun AgilaHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) OriginalDarkColorScheme else OriginalLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
