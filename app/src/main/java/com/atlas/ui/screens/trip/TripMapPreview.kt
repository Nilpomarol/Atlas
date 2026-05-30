package com.atlas.ui.screens.trip

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.TripStop

// Palette — mirrors TripDetailScreen / CountryDetailScreen
private val MapCard        = Color(0xFFFFFFFF)
private val MapBorder      = Color(0xFFE4E8EF)
private val MapBg          = Color(0xFFF1F3F7)
private val MapInk         = Color(0xFF111827)
private val MapMuted       = Color(0xFF6B7280)
private val MapAccent      = Color(0xFF024E82)   // trip blue
private val MapAccentLight = Color(0xFFC2D9F0)

// Hero background colours — same dark satellite feel as CountryDetailScreen hero
private val HeroDark1 = Color(0xFF07101C)
private val HeroDark2 = Color(0xFF08251B)
private val HeroDark3 = Color(0xFF0B1727)

// Route colours
private val RouteStroke = Color(0xFF6EE7B7)     // mint
private val RouteGlow   = MapAccent             // blue glow under stroke

@Composable
fun TripMapPreview(
    stops: List<TripStop>,
    modifier: Modifier = Modifier,
) {
    val coordinateStops = stops.filter { it.latitude != null && it.longitude != null }
    val manualStopCount = stops.size - coordinateStops.size

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MapCard)
            .border(1.dp, MapBorder, RoundedCornerShape(22.dp)),
    ) {
        // ── Map canvas ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(HeroDark1, HeroDark2, HeroDark3),
                    ),
                ),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawTripGrid()
                if (coordinateStops.isNotEmpty()) {
                    val projected = coordinateStops.mapIndexed { i, stop ->
                        ProjectedStop(
                            index = i,
                            title = stop.locationName,
                            point = projectStop(stop),
                        )
                    }
                    drawTripRoute(projected)
                }
            }

            // Empty state
            if (coordinateStops.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = Color.White.copy(alpha = 0.75f),
                    )
                    Text(
                        text = "Cap parada amb coordenades",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                    )
                    Text(
                        text = "Cerca llocs o afegeix latitud i longitud per veure-les aquí.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f),
                    )
                }
            }

            // Provisional badge — frosted glass pill
            Text(
                text = "MAPA PROVISIONAL",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.White.copy(alpha = 0.14f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
        }

        // ── Footer row ──
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MapAccentLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MapAccent,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "${coordinateStops.size} amb coordenades",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MapInk,
                )
                Text(
                    text = if (manualStopCount == 0) {
                        "Totes les parades es poden dibuixar al mapa."
                    } else {
                        "$manualStopCount manuals sense coordenades."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MapMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Canvas helpers — logic unchanged, colours explicit
// ─────────────────────────────────────────────
private data class ProjectedStop(
    val index: Int,
    val title: String,
    val point: Offset,
)

private fun DrawScope.drawTripGrid() {
    val lineColor = Color.White.copy(alpha = 0.055f)
    repeat(5) { i ->
        val x = size.width * (i + 1) / 6f
        drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
    }
    repeat(4) { i ->
        val y = size.height * (i + 1) / 5f
        drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
    }
}

private fun DrawScope.drawTripRoute(stops: List<ProjectedStop>) {
    if (stops.size > 1) {
        val path = Path().apply {
            stops.forEachIndexed { i, s ->
                if (i == 0) moveTo(s.point.x, s.point.y)
                else lineTo(s.point.x, s.point.y)
            }
        }
        // Glow pass
        drawPath(path, color = RouteGlow.copy(alpha = 0.45f), style = Stroke(width = 10f, cap = StrokeCap.Round))
        // Main stroke
        drawPath(path, color = RouteStroke.copy(alpha = 0.9f),  style = Stroke(width = 4f,  cap = StrokeCap.Round))
    }

    stops.forEach { stop ->
        // Halo
        drawCircle(Color.White.copy(alpha = 0.18f), radius = 16f, center = stop.point)
        // Fill
        drawCircle(RouteGlow, radius = 9f, center = stop.point)
        // Centre dot
        drawCircle(Color.White, radius = 4f, center = stop.point)
    }
}

private fun DrawScope.projectStop(stop: TripStop): Offset {
    val lon = requireNotNull(stop.longitude)
    val lat = requireNotNull(stop.latitude)
    val x = (((lon + 180.0) / 360.0).coerceIn(0.04, 0.96) * size.width).toFloat()
    val y = (((90.0 - lat)  / 180.0).coerceIn(0.08, 0.92) * size.height).toFloat()
    return Offset(x, y)
}