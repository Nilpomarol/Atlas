package com.atlas.ui.rework.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.atlas.ui.rework.foundation.AtlasReworkTheme

/**
 * App-wide styled dropdown menu for the rework UI. Wraps the Material3 popup with
 * the paper/ink surface, rounded card radius, hairline border, and soft shadow so
 * menus stop reading as stock Material. Pair with [ReworkDropdownItem] for content.
 */
@Composable
fun ReworkDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 6.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = AtlasReworkTheme.colors
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        offset = offset,
        shape = RoundedCornerShape(AtlasReworkTheme.dimensions.controlRadius),
        containerColor = colors.surfaceStrong,
        tonalElevation = 0.dp,
        shadowElevation = 14.dp,
        border = BorderStroke(1.dp, colors.border),
        content = content,
    )
}

/**
 * A single row inside a [ReworkDropdownMenu]. [selected] highlights the active choice
 * with an accent wash, accent label, and trailing check. [trailing] shows optional
 * monospace metadata (e.g. a direction glyph) before the check.
 */
@Composable
fun ReworkDropdownItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    trailing: String? = null,
) {
    val colors = AtlasReworkTheme.colors
    Row(
        modifier
            .widthIn(min = 200.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = label,
            style = AtlasReworkTheme.typography.body.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = if (selected) colors.accent else colors.ink,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Text(trailing, style = AtlasReworkTheme.typography.data, color = colors.inkMuted)
        }
        if (selected) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
        }
    }
}

/** Hairline separator for grouping items within a [ReworkDropdownMenu]. */
@Composable
fun ReworkDropdownDivider() {
    val colors = AtlasReworkTheme.colors
    androidx.compose.foundation.layout.Box(
        Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.border),
    )
}
