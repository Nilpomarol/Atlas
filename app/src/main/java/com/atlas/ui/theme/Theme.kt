package com.atlas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AtlasPrimary,
    secondary = AtlasSecondary,
    tertiary = AtlasTertiary,
    background = AtlasBackground,
    surface = AtlasSurface,
    surfaceVariant = AtlasSurfaceSubtle,
    outline = AtlasOutline,
    outlineVariant = AtlasOutlineStrong,
    onPrimary = AtlasSurface,
    onSecondary = AtlasSurface,
    onTertiary = AtlasSurface,
    onBackground = AtlasOnSurfaceStrong,
    onSurface = AtlasOnSurfaceStrong,
    onSurfaceVariant = AtlasOnSurfaceMuted,
    error = AtlasError,
)

@Composable
fun AtlasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AtlasTypography,
        content = content,
    )
}
