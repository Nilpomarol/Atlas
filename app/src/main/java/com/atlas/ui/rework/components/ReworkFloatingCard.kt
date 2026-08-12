package com.atlas.ui.rework.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.atlas.ui.rework.foundation.AtlasReworkTheme

@Composable
fun ReworkFloatingCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(AtlasReworkTheme.dimensions.cardPadding),
    content: @Composable () -> Unit,
) {
    val colors = AtlasReworkTheme.colors
    val shape = RoundedCornerShape(AtlasReworkTheme.dimensions.cardRadius)
    Surface(
        modifier = modifier.shadow(12.dp, shape, ambientColor = colors.shadow, spotColor = colors.shadow),
        shape = shape,
        color = colors.surface,
        contentColor = colors.ink,
        border = BorderStroke(1.dp, colors.border),
    ) {
        Box(
            Modifier
                .background(Brush.verticalGradient(listOf(colors.surfaceStrong, colors.surface)))
                .padding(contentPadding),
        ) { content() }
    }
}
