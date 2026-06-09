package com.atlas.ui.screens.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.TravelStatus
import com.atlas.presentation.stats.StatsFlightMapRoute
import com.atlas.presentation.stats.StatsMapMarker
import com.atlas.presentation.stats.StatsUiState
import com.atlas.ui.components.geo.AtlasGeoAssetLoader
import com.atlas.ui.components.geo.GeoCoordinate
import com.atlas.ui.components.geo.GeoFeatureCollection
import com.atlas.ui.components.geo.GeoPolygon
import com.atlas.ui.components.geo.GeoRing
import com.atlas.ui.components.geo.GeoViewport
import com.atlas.ui.components.geo.buildProjection
import com.atlas.ui.components.geo.flightRouteCurvePoints
import com.atlas.ui.components.geo.isAntimeridianJump
import com.atlas.ui.components.geo.normalizeLongitudeAround
import com.atlas.ui.theme.AtlasInProgress
import com.atlas.ui.theme.AtlasLived
import com.atlas.ui.theme.AtlasLiving
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceSoft
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasOutlineStrong
import com.atlas.ui.theme.AtlasPlanned
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasSurfaceSubtle
import com.atlas.ui.theme.AtlasVisited
import com.atlas.ui.theme.AtlasWished
import kotlin.math.roundToInt

data class MapLayerFilters(
    val completedFlights: Boolean = true,
    val plannedFlights: Boolean = true,
    val tripStops: Boolean = true,
    val excursionStops: Boolean = true,
    val countriesVisited: Boolean = true,
    val countriesPlanned: Boolean = true,
)

private val MapViewport = GeoViewport.World(minLatitudeDeg = -57.0, maxLatitudeDeg = 76.0)

@Composable
fun StatsMapCanvas(
    uiState: StatsUiState,
    filters: MapLayerFilters,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    var countries by remember { mutableStateOf<GeoFeatureCollection?>(null) }
    LaunchedEffect(Unit) {
        countries = AtlasGeoAssetLoader.loadCountries50m(context.applicationContext)
    }

    var userScale by remember { mutableFloatStateOf(1f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }
    var tooltip by remember { mutableStateOf<Pair<String, Offset>?>(null) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        tooltip = null
        val nextScale = (userScale * zoomChange).coerceIn(1f, 20f)
        userScale = nextScale
        val maxPan = 4000f * nextScale
        panX = (panX + panChange.x).coerceIn(-maxPan, maxPan)
        panY = (panY + panChange.y).coerceIn(-maxPan, maxPan)
    }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    val mapPaddingPx = with(density) { 8.dp.toPx() }
    val projection = remember(canvasSize, mapPaddingPx) {
        if (canvasSize.width > 0f && canvasSize.height > 0f)
            buildProjection(MapViewport, canvasSize, mapPaddingPx)
        else null
    }

    val airportLabels: Map<Pair<Double, Double>, String> = remember(uiState.flightMapRoutes) {
        val map = mutableMapOf<Pair<Double, Double>, String>()
        uiState.flightMapRoutes.forEach { r ->
            if (r.fromCode.isNotEmpty()) map[r.fromLatitude to r.fromLongitude] = r.fromCode
            if (r.toCode.isNotEmpty()) map[r.toLatitude to r.toLongitude] = r.toCode
        }
        map
    }

    var hasSetInitialPosition by remember { mutableStateOf(false) }
    var initialScale by remember { mutableFloatStateOf(1f) }
    var initialPanX by remember { mutableFloatStateOf(0f) }
    var initialPanY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(countries, projection) {
        if (hasSetInitialPosition || countries == null || projection == null) return@LaunchedEffect
        val livingCountry = countries!!.countries.firstOrNull { it.iso2 in uiState.livingIso2s }
        if (livingCountry == null) { hasSetInitialPosition = true; return@LaunchedEffect }
        val mainRing = livingCountry.polygons
            .maxByOrNull { it.rings.firstOrNull()?.points?.size ?: 0 }
            ?.rings?.firstOrNull()
        if (mainRing == null) { hasSetInitialPosition = true; return@LaunchedEffect }
        val lats = mainRing.points.map { it.latitude }
        val lons = mainRing.points.map { it.longitude }
        val centerLat = (lats.min() + lats.max()) / 2.0
        val centerLon = (lons.min() + lons.max()) / 2.0
        val basePos = projection!!.projectZoomed(GeoCoordinate(centerLat, centerLon), 1f, 0f, 0f)
        val s = 5f
        val targetPanX = (canvasSize.width / 2f - basePos.x) * s
        val targetPanY = (canvasSize.height / 2f - basePos.y) * s
        initialScale = s; initialPanX = targetPanX; initialPanY = targetPanY
        userScale = s; panX = targetPanX; panY = targetPanY
        hasSetInitialPosition = true
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformState)
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val proj = projection ?: return@detectTapGestures
                        val scale = userScale
                        val px = panX
                        val py = panY
                        val hitRadiusPx = with(density) { 28.dp.toPx() }

                        if (tooltip != null) {
                            tooltip = null
                            return@detectTapGestures
                        }

                        // 1. Trip stop markers
                        if (filters.tripStops && uiState.tripStopMapMarkers.isNotEmpty()) {
                            val hit = uiState.tripStopMapMarkers.minByOrNull { m ->
                                (proj.projectZoomed(GeoCoordinate(m.latitude, m.longitude), scale, px, py) - tapOffset).getDistance()
                            }
                            if (hit != null) {
                                val pos = proj.projectZoomed(GeoCoordinate(hit.latitude, hit.longitude), scale, px, py)
                                if ((pos - tapOffset).getDistance() < hitRadiusPx) {
                                    tooltip = hit.label to tapOffset
                                    return@detectTapGestures
                                }
                            }
                        }

                        // 2. Excursion stop markers
                        if (filters.excursionStops && uiState.excursionStopMapMarkers.isNotEmpty()) {
                            val hit = uiState.excursionStopMapMarkers.minByOrNull { m ->
                                (proj.projectZoomed(GeoCoordinate(m.latitude, m.longitude), scale, px, py) - tapOffset).getDistance()
                            }
                            if (hit != null) {
                                val pos = proj.projectZoomed(GeoCoordinate(hit.latitude, hit.longitude), scale, px, py)
                                if ((pos - tapOffset).getDistance() < hitRadiusPx) {
                                    tooltip = hit.label to tapOffset
                                    return@detectTapGestures
                                }
                            }
                        }

                        // 3. Airport markers
                        if (airportLabels.isNotEmpty()) {
                            val airportHitRadiusPx = with(density) { 16.dp.toPx() }
                            val visibleAirports = mutableSetOf<Pair<Double, Double>>()
                            if (filters.completedFlights) {
                                uiState.flightMapRoutes.filter { !it.isPlanned }.forEach { r ->
                                    visibleAirports += r.fromLatitude to r.fromLongitude
                                    visibleAirports += r.toLatitude to r.toLongitude
                                }
                            }
                            if (filters.plannedFlights) {
                                uiState.flightMapRoutes.filter { it.isPlanned }.forEach { r ->
                                    visibleAirports += r.fromLatitude to r.fromLongitude
                                    visibleAirports += r.toLatitude to r.toLongitude
                                }
                            }
                            val hit = visibleAirports.minByOrNull { coords ->
                                (proj.projectZoomed(GeoCoordinate(coords.first, coords.second), scale, px, py) - tapOffset).getDistance()
                            }
                            if (hit != null) {
                                val pos = proj.projectZoomed(GeoCoordinate(hit.first, hit.second), scale, px, py)
                                if ((pos - tapOffset).getDistance() < airportHitRadiusPx) {
                                    val code = airportLabels[hit]
                                    if (!code.isNullOrEmpty()) {
                                        tooltip = code to tapOffset
                                        return@detectTapGestures
                                    }
                                }
                            }
                        }

                        // 4. Flight route midpoints
                        val routesToCheck = buildList {
                            if (filters.completedFlights) addAll(uiState.flightMapRoutes.filter { !it.isPlanned })
                            if (filters.plannedFlights) addAll(uiState.flightMapRoutes.filter { it.isPlanned })
                        }
                        if (routesToCheck.isNotEmpty()) {
                            val routeHitRadiusPx = with(density) { 20.dp.toPx() }
                            val hit = routesToCheck.minByOrNull { r ->
                                val mid = routeMidpoint(r, proj, scale, px, py)
                                (mid - tapOffset).getDistance()
                            }
                            if (hit != null) {
                                val mid = routeMidpoint(hit, proj, scale, px, py)
                                if ((mid - tapOffset).getDistance() < routeHitRadiusPx) {
                                    tooltip = "${hit.fromCode} → ${hit.toCode}" to tapOffset
                                    return@detectTapGestures
                                }
                            }
                        }

                        // 5. Country polygon hit-test
                        val tapCoord = proj.unproject(tapOffset, scale, px, py)
                        val hitCountry = countries?.countries?.firstOrNull { feature ->
                            feature.polygons.any { polygon ->
                                pointInPolygon(tapCoord, polygon, proj.centerLongitude)
                            }
                        }
                        if (hitCountry != null) {
                            val stateLabel = when (hitCountry.iso2) {
                                in uiState.livingIso2s -> " · Vivint-hi"
                                in uiState.livedIso2s -> " · Viscut"
                                in uiState.visitedIso2s -> " · Visitat"
                                in uiState.plannedIso2s -> " · Planificat"
                                in uiState.wishedIso2s -> " · Desig"
                                else -> ""
                            }
                            tooltip = "${hitCountry.name}$stateLabel" to tapOffset
                        }
                    }
                }
        ) {
            val proj = projection ?: return@Canvas

            drawRect(AtlasSurfaceSubtle.copy(alpha = 0.72f))
            drawZoomedGraticule(proj, userScale, panX, panY, AtlasOutlineStrong.copy(alpha = 0.22f))

            // Country polygons
            val collection = countries
            if (collection != null) {
                collection.countries.forEach { feature ->
                    val highlightColor = feature.iso2?.let { iso2 ->
                        when {
                            iso2 in uiState.livingIso2s && filters.countriesVisited -> AtlasLiving
                            iso2 in uiState.livedIso2s && filters.countriesVisited -> AtlasLived
                            iso2 in uiState.visitedIso2s && filters.countriesVisited -> AtlasVisited
                            iso2 in uiState.plannedIso2s && filters.countriesPlanned -> AtlasPlanned
                            iso2 in uiState.wishedIso2s && filters.countriesPlanned -> AtlasWished
                            else -> null
                        }
                    }
                    val fill = if (highlightColor != null) highlightColor.copy(alpha = 0.72f) else AtlasSurface.copy(alpha = 0.76f)
                    val stroke = if (highlightColor != null) AtlasOutlineStrong.copy(alpha = 0.78f) else AtlasOutline.copy(alpha = 0.45f)
                    val strokeWidth = if (highlightColor != null) 0.9.dp.toPx() else 0.65.dp.toPx()
                    feature.polygons.forEach { polygon ->
                        val path = polygon.toZoomedPath(proj, userScale, panX, panY)
                        drawPath(path = path, color = fill)
                        drawPath(path = path, color = stroke, style = Stroke(width = strokeWidth))
                    }
                }
            }

            // Completed flight routes (solid)
            if (filters.completedFlights) {
                uiState.flightMapRoutes.filter { !it.isPlanned }.forEach { route ->
                    drawZoomedRouteArc(
                        from = GeoCoordinate(route.fromLatitude, route.fromLongitude),
                        to = GeoCoordinate(route.toLatitude, route.toLongitude),
                        color = AtlasPrimary,
                        alpha = 1.0f,
                        isDashed = false,
                        proj = proj, userScale = userScale, panX = panX, panY = panY,
                    )
                }
            }

            // Planned flight routes (dashed)
            if (filters.plannedFlights) {
                uiState.flightMapRoutes.filter { it.isPlanned }.forEach { route ->
                    drawZoomedRouteArc(
                        from = GeoCoordinate(route.fromLatitude, route.fromLongitude),
                        to = GeoCoordinate(route.toLatitude, route.toLongitude),
                        color = AtlasPlanned,
                        alpha = 0.90f,
                        isDashed = true,
                        proj = proj, userScale = userScale, panX = panX, panY = panY,
                    )
                }
            }

            // Airport markers
            val visibleAirportRoutes = buildList {
                if (filters.completedFlights) addAll(uiState.flightMapRoutes.filter { !it.isPlanned })
                if (filters.plannedFlights) addAll(uiState.flightMapRoutes.filter { it.isPlanned })
            }
            if (visibleAirportRoutes.isNotEmpty()) {
                val airportCoords = visibleAirportRoutes.flatMap { r ->
                    listOf(r.fromLatitude to r.fromLongitude, r.toLatitude to r.toLongitude)
                }.toSet()
                val airportRadius = 2.5.dp.toPx()
                airportCoords.forEach { (lat, lon) ->
                    val center = proj.projectZoomed(GeoCoordinate(lat, lon), userScale, panX, panY)
                    drawCircle(Color.White.copy(alpha = 0.90f), airportRadius + 1.5.dp.toPx(), center)
                    drawCircle(AtlasPrimary.copy(alpha = 0.85f), airportRadius, center)
                }
            }

            // Excursion stop markers (hollow, smaller)
            if (filters.excursionStops) {
                uiState.excursionStopMapMarkers.forEach { marker ->
                    val center = proj.projectZoomed(GeoCoordinate(marker.latitude, marker.longitude), userScale, panX, panY)
                    val radius = 4.5.dp.toPx()
                    drawCircle(Color.White.copy(alpha = 0.88f), radius + 2.dp.toPx(), center)
                    drawCircle(AtlasLived.copy(alpha = 0.85f), radius, center, style = Stroke(width = 1.8.dp.toPx()))
                }
            }

            // Trip stop markers (solid, status-colored)
            if (filters.tripStops) {
                uiState.tripStopMapMarkers.forEach { marker ->
                    val center = proj.projectZoomed(GeoCoordinate(marker.latitude, marker.longitude), userScale, panX, panY)
                    val color = marker.statusColor()
                    val radius = 5.5.dp.toPx()
                    drawCircle(Color.White.copy(alpha = 0.88f), radius + 2.dp.toPx(), center)
                    drawCircle(color, radius, center)
                    drawCircle(Color.White.copy(alpha = 0.88f), radius, center, style = Stroke(width = 1.4.dp.toPx()))
                }
            }
        }

        // Reset button
        Surface(
            onClick = {
                userScale = initialScale
                panX = initialPanX
                panY = initialPanY
                tooltip = null
            },
            shape = RoundedCornerShape(12.dp),
            color = AtlasSurface.copy(alpha = 0.97f),
            border = BorderStroke(1.dp, AtlasOutline),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Reseteja mapa",
                tint = AtlasOnSurfaceSoft,
                modifier = Modifier
                    .padding(8.dp)
                    .size(16.dp),
            )
        }

        // Tooltip
        tooltip?.let { (label, tapOffset) ->
            val tooltipX = (tapOffset.x - with(density) { 60.dp.toPx() })
                .coerceIn(with(density) { 8.dp.toPx() }, canvasSize.width - with(density) { 200.dp.toPx() })
            val tooltipY = (tapOffset.y - with(density) { 52.dp.toPx() })
                .coerceAtLeast(with(density) { 8.dp.toPx() })
            Box(
                modifier = Modifier
                    .offset { IntOffset(tooltipX.roundToInt(), tooltipY.roundToInt()) }
                    .background(AtlasSurface.copy(alpha = 0.97f), RoundedCornerShape(12.dp))
                    .border(1.dp, AtlasOutline, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = AtlasOnSurfaceStrong,
                )
            }
        }
    }
}

// ── Drawing helpers ───────────────────────────────────────────────────────────

private fun DrawScope.drawZoomedGraticule(
    proj: com.atlas.ui.components.geo.GeoProjection,
    userScale: Float,
    panX: Float,
    panY: Float,
    color: Color,
) {
    val stroke = Stroke(width = 0.6.dp.toPx())
    for (longitude in -180..180 step 30) {
        val path = Path()
        (-80..80 step 8).forEachIndexed { index, latitude ->
            val pt = proj.projectZoomed(GeoCoordinate(latitude.toDouble(), longitude.toDouble()), userScale, panX, panY)
            if (index == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
        }
        drawPath(path, color, style = stroke)
    }
    for (latitude in -60..75 step 15) {
        val path = Path()
        (-180..180 step 8).forEachIndexed { index, longitude ->
            val pt = proj.projectZoomed(GeoCoordinate(latitude.toDouble(), longitude.toDouble()), userScale, panX, panY)
            if (index == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
        }
        drawPath(path, color, style = stroke)
    }
}

private fun DrawScope.drawZoomedRouteArc(
    from: GeoCoordinate,
    to: GeoCoordinate,
    color: Color,
    alpha: Float,
    isDashed: Boolean,
    proj: com.atlas.ui.components.geo.GeoProjection,
    userScale: Float,
    panX: Float,
    panY: Float,
) {
    val path = Path()
    val points = flightRouteCurvePoints(from, to)
    var started = false
    var previous: GeoCoordinate? = null
    points.forEach { point ->
        if (previous != null && isAntimeridianJump(previous!!, point, proj.centerLongitude)) {
            started = false
        }
        val offset = proj.projectZoomed(point, userScale, panX, panY)
        if (!started) {
            path.moveTo(offset.x, offset.y)
            started = true
        } else {
            path.lineTo(offset.x, offset.y)
        }
        previous = point
    }
    val pathEffect = if (isDashed) PathEffect.dashPathEffect(floatArrayOf(12.dp.toPx(), 9.dp.toPx())) else null
    drawPath(path, color.copy(alpha = 0.28f * alpha), style = Stroke(4.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = pathEffect))
    drawPath(path, color.copy(alpha = alpha), style = Stroke(2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = pathEffect))
}

private fun GeoPolygon.toZoomedPath(
    proj: com.atlas.ui.components.geo.GeoProjection,
    userScale: Float,
    panX: Float,
    panY: Float,
): Path = Path().apply {
    fillType = PathFillType.EvenOdd
    rings.forEach { ring ->
        var started = false
        var previous: GeoCoordinate? = null
        ring.points.forEach { point ->
            if (previous != null && isAntimeridianJump(previous!!, point, proj.centerLongitude)) {
                close()
                started = false
            }
            val offset = proj.projectZoomed(point, userScale, panX, panY)
            if (!started) {
                moveTo(offset.x, offset.y)
                started = true
            } else {
                lineTo(offset.x, offset.y)
            }
            previous = point
        }
        if (started) close()
    }
}

// ── Hit-test helpers ──────────────────────────────────────────────────────────

private fun routeMidpoint(
    route: StatsFlightMapRoute,
    proj: com.atlas.ui.components.geo.GeoProjection,
    userScale: Float,
    panX: Float,
    panY: Float,
): Offset {
    val points = flightRouteCurvePoints(
        GeoCoordinate(route.fromLatitude, route.fromLongitude),
        GeoCoordinate(route.toLatitude, route.toLongitude),
    )
    val mid = points.getOrNull(points.size / 2) ?: points.first()
    return proj.projectZoomed(mid, userScale, panX, panY)
}

private fun pointInPolygon(point: GeoCoordinate, polygon: GeoPolygon, centerLongitude: Double): Boolean {
    if (polygon.rings.isEmpty()) return false
    fun isInRing(ring: GeoRing): Boolean {
        val px = normalizeLongitudeAround(point.longitude, centerLongitude)
        val py = point.latitude
        var inside = false
        var j = ring.points.lastIndex
        for (i in ring.points.indices) {
            val xi = normalizeLongitudeAround(ring.points[i].longitude, centerLongitude)
            val yi = ring.points[i].latitude
            val xj = normalizeLongitudeAround(ring.points[j].longitude, centerLongitude)
            val yj = ring.points[j].latitude
            if ((yi > py) != (yj > py) && px < (xj - xi) * (py - yi) / (yj - yi) + xi) {
                inside = !inside
            }
            j = i
        }
        return inside
    }
    return isInRing(polygon.rings[0]) && polygon.rings.drop(1).none { isInRing(it) }
}

private fun StatsMapMarker.statusColor(): Color = when (status) {
    TravelStatus.COMPLETED -> AtlasVisited
    TravelStatus.IN_PROGRESS -> AtlasInProgress
    TravelStatus.PLANNED -> AtlasPlanned
    else -> AtlasOnSurfaceMuted
}
