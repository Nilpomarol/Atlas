package com.atlas.ui.components.geo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.atlas.ui.theme.AtlasOnSurfaceFaint
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasOutlineStrong
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasSurfaceSubtle

@Composable
fun AtlasGeoCanvas(
    modifier: Modifier = Modifier,
    viewport: GeoViewport = GeoViewport.World,
    routeSegments: List<GeoRouteSegment> = emptyList(),
    markers: List<GeoMarker> = emptyList(),
    highlightedIso2: Set<String> = emptySet(),
) {
    val context = LocalContext.current
    var features by remember { mutableStateOf<GeoFeatureCollection?>(null) }

    LaunchedEffect(context) {
        features = AtlasGeoAssetLoader.loadCountries(context.applicationContext)
    }

    Box(
        modifier = modifier
            .background(AtlasSurface),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val collection = features
            drawRect(AtlasSurfaceSubtle.copy(alpha = 0.72f))

            val paddingPx = 18.dp.toPx()
            val projection = buildProjection(viewport, size, paddingPx)

            drawGraticule(
                projection = projection,
                drawSize = size,
                color = AtlasOutlineStrong.copy(alpha = 0.28f),
            )

            if (collection != null) {
                collection.countries.forEach { country ->
                    val isHighlighted = country.iso2 != null && country.iso2 in highlightedIso2
                    val fill = if (isHighlighted) {
                        AtlasSurface.copy(alpha = 0.96f)
                    } else {
                        AtlasSurface.copy(alpha = 0.76f)
                    }
                    val stroke = if (isHighlighted) {
                        AtlasOutlineStrong.copy(alpha = 0.72f)
                    } else {
                        AtlasOutline.copy(alpha = 0.45f)
                    }
                    country.polygons.forEach { polygon ->
                        val path = polygon.toPath(projection)
                        drawPath(path = path, color = fill)
                        drawPath(
                            path = path,
                            color = stroke,
                            style = Stroke(width = if (isHighlighted) 1.2.dp.toPx() else 0.65.dp.toPx()),
                        )
                    }
                }
            }

            routeSegments.forEach { segment ->
                drawRouteSegment(segment, projection)
            }

            markers.forEach { marker ->
                val center = projection.project(marker.coordinate)
                val radius = 6.5.dp.toPx() * marker.radiusMultiplier
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f * marker.alpha),
                    radius = radius + 3.dp.toPx(),
                    center = center,
                )
                drawCircle(
                    color = marker.color.copy(alpha = marker.alpha),
                    radius = radius,
                    center = center,
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.92f * marker.alpha),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 1.4.dp.toPx()),
                )
            }
        }
    }
}

private fun GeoPolygon.toPath(projection: GeoProjection): Path =
    Path().apply {
        fillType = PathFillType.EvenOdd
        rings.forEach { ring ->
            var hasStarted = false
            var previous: GeoCoordinate? = null
            ring.points.forEach { point ->
                val previousPoint = previous
                if (previousPoint != null && isAntimeridianJump(previousPoint, point, projection.centerLongitude)) {
                    close()
                    hasStarted = false
                }
                val offset = projection.project(point)
                if (!hasStarted) {
                    moveTo(offset.x, offset.y)
                    hasStarted = true
                } else {
                    lineTo(offset.x, offset.y)
                }
                previous = point
            }
            if (hasStarted) close()
        }
    }

private fun DrawScope.drawGraticule(
    projection: GeoProjection,
    drawSize: Size,
    color: Color,
) {
    val stroke = Stroke(width = 0.6.dp.toPx())
    for (longitude in -180..180 step 30) {
        val path = Path()
        (-80..80 step 8).forEachIndexed { index, latitude ->
            val point = projection.project(GeoCoordinate(latitude = latitude.toDouble(), longitude = longitude.toDouble()))
            if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
        }
        drawPath(path = path, color = color, style = stroke)
    }
    for (latitude in -60..75 step 15) {
        val path = Path()
        (-180..180 step 8).forEachIndexed { index, longitude ->
            val point = projection.project(GeoCoordinate(latitude = latitude.toDouble(), longitude = longitude.toDouble()))
            if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
        }
        drawPath(path = path, color = color, style = stroke)
    }

    drawRect(
        color = AtlasOnSurfaceFaint.copy(alpha = 0.05f),
        size = drawSize,
    )
}

private fun DrawScope.drawRouteSegment(
    segment: GeoRouteSegment,
    projection: GeoProjection,
) {
    val path = Path()
    val points = flightRouteCurvePoints(segment.from, segment.to)
    var hasStarted = false
    var previous: GeoCoordinate? = null
    points.forEach { point ->
        val previousPoint = previous
        if (previousPoint != null && isAntimeridianJump(previousPoint, point, projection.centerLongitude)) {
            hasStarted = false
        }
        val offset = projection.project(point)
        if (!hasStarted) {
            path.moveTo(offset.x, offset.y)
            hasStarted = true
        } else {
            path.lineTo(offset.x, offset.y)
        }
        previous = point
    }

    drawPath(
        path = path,
        color = segment.color.copy(alpha = 0.22f * segment.alpha),
        style = Stroke(
            width = if (segment.isDashed) 4.dp.toPx() else 6.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = if (segment.isDashed) PathEffect.dashPathEffect(floatArrayOf(12.dp.toPx(), 9.dp.toPx())) else null,
        ),
    )
    drawPath(
        path = path,
        color = segment.color.copy(alpha = segment.alpha),
        style = Stroke(
            width = if (segment.isDashed) 1.8.dp.toPx() else 2.8.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = if (segment.isDashed) PathEffect.dashPathEffect(floatArrayOf(12.dp.toPx(), 9.dp.toPx())) else null,
        ),
    )
}
