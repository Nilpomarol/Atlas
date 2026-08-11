package com.atlas.ui.rework.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.atlas.ui.rework.foundation.AtlasReworkTheme
import com.atlas.ui.rework.navigation.ReworkDestination

@Composable
fun ReworkNavigationBar(
    selected: ReworkDestination,
    onSelect: (ReworkDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AtlasReworkTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(AtlasReworkTheme.dimensions.navigationHeight)
            .shadow(14.dp, shape, ambientColor = colors.shadow, spotColor = colors.shadow),
        shape = shape,
        color = colors.surfaceStrong,
        border = BorderStroke(1.dp, colors.border),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            ReworkDestination.entries.forEach { destination ->
                val active = destination == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(destination) }
                        .padding(vertical = 9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                        tint = if (active) colors.accent else colors.inkMuted,
                    )
                    Text(
                        text = destination.label,
                        style = AtlasReworkTheme.typography.label,
                        color = if (active) colors.accent else colors.inkMuted,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
