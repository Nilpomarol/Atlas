package com.atlas.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.atlas.presentation.trip.TripStopMapPoint
import com.atlas.ui.components.geo.AtlasGeoCanvas
import com.atlas.ui.components.geo.GeoCoordinate
import com.atlas.ui.components.geo.GeoMarker
import com.atlas.ui.components.geo.GeoRouteSegment
import com.atlas.ui.components.geo.GeoViewport
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasPrimary

@Composable
internal fun TripCardMap(mapPoints: List<TripStopMapPoint>, stopCount: Int, routeColor: Color) {
    val coordinates = mapPoints.map { GeoCoordinate(it.latitude, it.longitude) }
    if (coordinates.isEmpty()) {
        TripMapTexture()
        SchematicRouteCanvas(stopCount)
        return
    }
    AtlasGeoCanvas(
        modifier = Modifier.fillMaxSize(),
        viewport = GeoViewport.FitPoints(
            points = coordinates,
            minLongitudeSpanDegrees = 4.8,
            minLatitudeSpanDegrees = 3.2,
        ),
        routeSegments = coordinates.zipWithNext { from, to ->
            GeoRouteSegment(from = from, to = to, color = routeColor, alpha = 0.9f)
        },
        markers = coordinates.mapIndexed { index, coord ->
            GeoMarker(
                coordinate = coord,
                color = routeColor,
                radiusMultiplier = if (index == 0 || index == coordinates.lastIndex) 0.58f else 0.46f,
                isHollow = index == coordinates.lastIndex && coordinates.size > 1,
            )
        },
    )
}

@Composable
private fun TripMapTexture() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val spacing = 24.dp.toPx()
        var x = 0f
        while (x <= size.width) {
            drawLine(AtlasNavy.copy(alpha = 0.08f), Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
            x += spacing
        }
        var y = 0f
        while (y <= size.height) {
            drawLine(AtlasNavy.copy(alpha = 0.08f), Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
            y += spacing
        }
    }
}

@Composable
private fun SchematicRouteCanvas(stopCount: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val pts = listOf(
            Offset(size.width * 0.10f, size.height * 0.62f),
            Offset(size.width * 0.32f, size.height * 0.36f),
            Offset(size.width * 0.55f, size.height * 0.48f),
            Offset(size.width * 0.74f, size.height * 0.30f),
            Offset(size.width * 0.92f, size.height * 0.52f),
        ).take(stopCount.coerceIn(2, 5))
        val path = Path().apply {
            pts.firstOrNull()?.let { moveTo(it.x, it.y) }
            pts.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(path, Color.White.copy(alpha = 0.82f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        pts.forEachIndexed { i, pt ->
            if (i == pts.lastIndex) drawCircle(Color.White, 5.dp.toPx(), pt, style = Stroke(2.dp.toPx()))
            else drawCircle(Color.White, 5.dp.toPx(), pt)
        }
    }
}

@Composable
internal fun TripStatePill(label: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(color, RoundedCornerShape(999.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(modifier = Modifier.size(6.dp).background(Color.White, CircleShape))
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
internal fun SeeAllLink(label: String, onClick: () -> Unit) {
    Text(
        text = "$label →",
        modifier = Modifier.clickable(onClick = onClick),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        color = AtlasPrimary,
    )
}

internal fun Double.toCompactKm(): String {
    val rounded = kotlin.math.round(this).toLong()
    return if (rounded >= 10_000) "${rounded / 1_000} k" else rounded.toString()
}

// unused — kept as pre-existing dead code
internal fun Double.toHoursText(): String {
    val hours = kotlin.math.round(this).toInt()
    return if (hours >= 1000) "${hours / 1000} k h" else "$hours h"
}
