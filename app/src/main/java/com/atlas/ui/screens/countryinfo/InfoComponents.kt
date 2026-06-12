package com.atlas.ui.screens.countryinfo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.atlas.presentation.country.BreakdownSlice
import com.atlas.presentation.country.Membership
import com.atlas.ui.theme.AtlasOnSurfaceFaint
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasSurfaceSubtle

@Composable
internal fun PositionBar(rank: Int, rankTotal: Int, color: Color) {
    val fraction = (1f - (rank - 1).toFloat() / (rankTotal - 1).toFloat()).coerceIn(0f, 1f)
    TrackBar(fraction, color)
}

@Composable
internal fun TrackBar(fraction: Float, color: Color) {
    Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(5.dp)).background(AtlasSurfaceSubtle)) {
        Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().clip(RoundedCornerShape(5.dp)).background(color))
    }
}

@Composable
internal fun TierChip(tier: String) {
    val c = tierColor(tier)
    Box(Modifier.clip(RoundedCornerShape(20.dp)).background(c.copy(alpha = 0.15f)).padding(horizontal = 9.dp, vertical = 3.dp)) {
        Text(tier, style = MaterialTheme.typography.labelSmall, color = c, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun StatusBadge(value: String) {
    val c = statusColor(value)
    Box(Modifier.clip(RoundedCornerShape(20.dp)).background(c.copy(alpha = 0.15f)).padding(horizontal = 12.dp, vertical = 5.dp)) {
        Text(value, style = MaterialTheme.typography.labelMedium, color = c, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun MembershipPill(m: Membership, accent: Color) {
    val bg = if (m.isMember) accent.copy(alpha = 0.16f) else AtlasSurfaceSubtle
    val fg = if (m.isMember) accent else AtlasOnSurfaceFaint
    Box(Modifier.clip(RoundedCornerShape(20.dp)).background(bg).padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(
            (if (m.isMember) "✓ " else "· ") + m.label,
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            fontWeight = if (m.isMember) FontWeight.Medium else FontWeight.Normal,
        )
    }
}

@Composable
internal fun Chip(text: String) {
    Box(Modifier.clip(RoundedCornerShape(9.dp)).background(AtlasSurfaceSubtle).padding(horizontal = 10.dp, vertical = 5.dp)) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = AtlasOnSurfaceStrong)
    }
}

@Composable
internal fun Dot(color: Color) {
    Box(Modifier.size(9.dp).clip(RoundedCornerShape(50)).background(color))
}

@Composable
internal fun Donut(slices: List<BreakdownSlice>, modifier: Modifier = Modifier) {
    val total = slices.sumOf { it.pct }.coerceAtLeast(0.0001)
    Canvas(modifier) {
        val stroke = 17.dp.toPx()
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(stroke / 2f, stroke / 2f)
        var start = -90f
        slices.forEachIndexed { i, s ->
            val sweep = (s.pct / total * 360f).toFloat()
            drawArc(
                color = BREAKDOWN_COLORS[i % BREAKDOWN_COLORS.size],
                startAngle = start,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Butt),
            )
            start += sweep
        }
    }
}
