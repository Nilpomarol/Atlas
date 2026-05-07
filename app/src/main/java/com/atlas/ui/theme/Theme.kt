package com.atlas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AtlasBlue,
    onPrimary = AtlasSand,
    primaryContainer = AtlasBlueLight,
    onPrimaryContainer = AtlasBlue,
    secondary = AtlasOrange,
    onSecondary = AtlasSand,
    secondaryContainer = AtlasOrangeLight,
    onSecondaryContainer = AtlasBlue,
    background = AtlasSand,
    onBackground = AtlasBlue,
    surface = AtlasSand,
    onSurface = AtlasSlate,
    surfaceVariant = AtlasSandDark,
    onSurfaceVariant = AtlasSlate,
    outline = AtlasSlateLight
)

private val DarkColorScheme = darkColorScheme(
    primary = AtlasBlueLight,
    onPrimary = AtlasDarkBackground,
    primaryContainer = AtlasBlueMid,
    onPrimaryContainer = AtlasSand,
    secondary = AtlasOrange,
    onSecondary = AtlasDarkBackground,
    secondaryContainer = AtlasOrange,
    onSecondaryContainer = AtlasSand,
    background = AtlasDarkBackground,
    onBackground = AtlasSand,
    surface = AtlasDarkSurface,
    onSurface = AtlasSand,
    surfaceVariant = AtlasDarkSurface,
    onSurfaceVariant = AtlasSlateLight,
    outline = AtlasSlate
)

@Composable
fun AtlasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AtlasTypography,
        content = content
    )
}