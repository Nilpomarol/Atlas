package com.atlas.ui.screens.trip

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
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
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.Excursion
import com.atlas.domain.model.TripStop
import com.atlas.domain.model.TripStopSource
import com.atlas.ui.components.map.AtlasMapView
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.PropertyFactory.iconAllowOverlap
import org.maplibre.android.style.layers.PropertyFactory.iconAnchor
import org.maplibre.android.style.layers.PropertyFactory.iconIgnorePlacement
import org.maplibre.android.style.layers.PropertyFactory.iconImage
import org.maplibre.android.style.layers.PropertyFactory.iconOffset
import org.maplibre.android.style.layers.PropertyFactory.iconSize
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineCap
import org.maplibre.android.style.layers.PropertyFactory.lineJoin
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.layers.PropertyFactory.textAllowOverlap
import org.maplibre.android.style.layers.PropertyFactory.textAnchor
import org.maplibre.android.style.layers.PropertyFactory.textColor
import org.maplibre.android.style.layers.PropertyFactory.textField
import org.maplibre.android.style.layers.PropertyFactory.textFont
import org.maplibre.android.style.layers.PropertyFactory.textHaloColor
import org.maplibre.android.style.layers.PropertyFactory.textHaloWidth
import org.maplibre.android.style.layers.PropertyFactory.textIgnorePlacement
import org.maplibre.android.style.layers.PropertyFactory.textMaxWidth
import org.maplibre.android.style.layers.PropertyFactory.textOffset
import org.maplibre.android.style.layers.PropertyFactory.textSize
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

@Composable
fun TripMapPreview(
    stops: List<TripStop>,
    excursions: List<Excursion> = emptyList(),
    modifier: Modifier = Modifier,
    mapHeight: Dp = 210.dp,
    gesturesEnabled: Boolean = true,
    showFooter: Boolean = true,
    generatedStopsVisible: Boolean = true,
    onGeneratedStopsVisibilityChanged: ((Boolean) -> Unit)? = null,
    onExpandClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val hasGeneratedStops = stops.any { it.source == TripStopSource.ITINERARY_GROUP }
    val visibleStops = if (generatedStopsVisible) {
        stops
    } else {
        stops.filter { it.source != TripStopSource.ITINERARY_GROUP }
    }
    val coordinateStops = visibleStops.filter { it.latitude != null && it.longitude != null }
    val mappableExcursionStops = excursions.sumOf { e -> e.stops.count { it.latitude != null && it.longitude != null } }
    val mappableCount = coordinateStops.size + mappableExcursionStops
    val missingCoordinateCount = visibleStops.size + excursions.sumOf { it.stops.size } - mappableCount

    val mapRef = remember { mutableStateOf<Pair<MapLibreMap, Style>?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(AtlasSurface)
            .border(1.dp, AtlasOutline, RoundedCornerShape(22.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(mapHeight),
        ) {
            AtlasMapView(
                modifier = Modifier.matchParentSize(),
                onMapReady = { map, style ->
                    if (!gesturesEnabled) {
                        map.uiSettings.isScrollGesturesEnabled = false
                        map.uiSettings.isZoomGesturesEnabled = false
                        map.uiSettings.isRotateGesturesEnabled = false
                        map.uiSettings.isTiltGesturesEnabled = false
                    }
                    mapRef.value = Pair(map, style)
                },
            )
        }

        if (showFooter) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = buildString {
                        append("$mappableCount ${if (mappableCount == 1) "parada" else "parades"}")
                        if (missingCoordinateCount > 0) append(" · $missingCoordinateCount sense coordenades")
                        else append(" · Ruta completa")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AtlasOnSurfaceMuted,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (hasGeneratedStops && onGeneratedStopsVisibilityChanged != null) {
                    TextButton(
                        onClick = { onGeneratedStopsVisibilityChanged(!generatedStopsVisible) },
                        modifier = Modifier.height(30.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    ) {
                        Text(
                            text = if (generatedStopsVisible) "Amaga vols" else "Mostra vols",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = AtlasPrimary,
                        )
                    }
                }
                if (onExpandClick != null) {
                    IconButton(
                        onClick = onExpandClick,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Fullscreen,
                            contentDescription = "Obrir mapa interactiu",
                            tint = AtlasPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(mapRef.value, coordinateStops, excursions) {
        val (map, style) = mapRef.value ?: return@LaunchedEffect
        clearTripLayers(style)
        addTripContent(context, map, style, coordinateStops, excursions)
    }
}

// ── Layer/source IDs ─────────────────────────────────────────────────────────

private val TRIP_LAYER_IDS = listOf(
    "main-route-line", "excursion-route-lines",
    "main-stops-halo", "main-stops-layer", "main-stops-markers", "main-stops-numbers", "main-stops-labels",
    "generated-stops-halo", "generated-stops-layer", "generated-stops-markers", "generated-stops-numbers", "generated-stops-labels",
    "excursion-stops-halo", "excursion-stops-layer", "excursion-stops-markers", "excursion-stops-numbers", "excursion-stops-labels",
)
private val TRIP_SOURCE_IDS = listOf(
    "main-route", "excursion-routes",
    "main-stops", "generated-stops", "excursion-stops",
)

private fun clearTripLayers(style: Style) {
    TRIP_LAYER_IDS.forEach { if (style.getLayer(it) != null) style.removeLayer(it) }
    TRIP_SOURCE_IDS.forEach { if (style.getSource(it) != null) style.removeSource(it) }
}

private fun addTripContent(
    context: android.content.Context,
    map: MapLibreMap,
    style: Style,
    coordinateStops: List<TripStop>,
    excursions: List<Excursion>,
) {
    map.clear()
    style.addTripDotImages()

    // Main route line
    val mainCoords = coordinateStops.map { Point.fromLngLat(it.longitude!!, it.latitude!!) }
    if (mainCoords.size >= 2) {
        style.addSource(GeoJsonSource("main-route",
            Feature.fromGeometry(LineString.fromLngLats(mainCoords))))
        style.addLayer(LineLayer("main-route-line", "main-route").apply {
            setProperties(lineWidth(3.5f), lineColor("#1D4ED8"), lineCap("round"), lineJoin("round"))
        })
    }

    // Excursion route lines
    val excursionLines = excursions.mapNotNull { e ->
        val anchor = e.anchorTripStopId
            ?.let { anchorId -> coordinateStops.firstOrNull { it.id == anchorId } }
        val anchorPoint = anchor?.let { Point.fromLngLat(it.longitude!!, it.latitude!!) }
        val stopCoords = e.stops
            .filter { it.latitude != null && it.longitude != null }
            .sortedBy { it.sortOrder }
            .map { Point.fromLngLat(it.longitude!!, it.latitude!!) }
        val coords = if (anchorPoint != null && stopCoords.isNotEmpty()) {
            listOf(anchorPoint) + stopCoords + anchorPoint
        } else {
            stopCoords
        }
        if (coords.size >= 2) Feature.fromGeometry(LineString.fromLngLats(coords)) else null
    }
    if (excursionLines.isNotEmpty()) {
        style.addSource(GeoJsonSource("excursion-routes", FeatureCollection.fromFeatures(excursionLines)))
        style.addLayer(LineLayer("excursion-route-lines", "excursion-routes").apply {
            setProperties(lineWidth(2.5f), lineColor("#7C3AED"), lineCap("round"), lineJoin("round"))
        })
    }

    // Main stops: larger ringed markers with centered route numbers and readable labels.
    val mainStops = coordinateStops.filter { it.source != TripStopSource.ITINERARY_GROUP }
    val mainFeatures = mainStops.mapIndexed { index, stop ->
        Feature.fromGeometry(Point.fromLngLat(stop.longitude!!, stop.latitude!!))
            .also {
                it.addStringProperty("n", "${index + 1}")
                it.addStringProperty("label", stop.mapLabel())
            }
    }
    if (mainFeatures.isNotEmpty()) {
        style.addSource(GeoJsonSource("main-stops", FeatureCollection.fromFeatures(mainFeatures)))
        style.addLayer(CircleLayer("main-stops-halo", "main-stops").apply {
            setProperties(
                circleRadius(12f),
                circleColor("#EFF6FF"),
                circleStrokeWidth(2.5f),
                circleStrokeColor("#1D4ED8"),
            )
        })
        style.addLayer(CircleLayer("main-stops-layer", "main-stops").apply {
            setProperties(
                circleRadius(8f),
                circleColor("#1D4ED8"),
                circleStrokeWidth(1.5f),
                circleStrokeColor("#FFFFFF"),
            )
        })
        style.addLayer(SymbolLayer("main-stops-markers", "main-stops").apply {
            setProperties(
                iconImage("trip-dot-main"),
                iconSize(0.78f),
                iconAnchor("center"),
                iconOffset(arrayOf(0f, 0f)),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
            )
        })
        style.addLayer(SymbolLayer("main-stops-numbers", "main-stops").apply {
            setProperties(
                textField("{n}"),
                textSize(10.5f),
                textFont(arrayOf("Noto Sans Bold", "Open Sans Bold")),
                textColor("#FFFFFF"),
                textHaloColor("#1D4ED8"),
                textHaloWidth(1f),
                textAnchor("center"),
                textOffset(arrayOf(0f, 0f)),
                textIgnorePlacement(true),
                textAllowOverlap(true),
            )
        })
        style.addLayer(SymbolLayer("main-stops-labels", "main-stops").apply {
            setProperties(
                textField("{label}"),
                textSize(11f),
                textFont(arrayOf("Noto Sans Bold", "Open Sans Bold")),
                textColor("#172033"),
                textHaloColor("#FFFFFF"),
                textHaloWidth(1.5f),
                textAnchor("top"),
                textOffset(arrayOf(0f, 1.25f)),
                textMaxWidth(8f),
                textIgnorePlacement(true),
                textAllowOverlap(true),
            )
        })
    }

    // Generated itinerary stops: amber flight markers, kept visually distinct from manual stops.
    val generatedFeatures = coordinateStops
        .filter { it.source == TripStopSource.ITINERARY_GROUP }
        .map { stop ->
            Feature.fromGeometry(Point.fromLngLat(stop.longitude!!, stop.latitude!!))
                .also {
                    it.addStringProperty("n", "V")
                    it.addStringProperty("label", stop.mapLabel())
                }
        }
    if (generatedFeatures.isNotEmpty()) {
        style.addSource(GeoJsonSource("generated-stops", FeatureCollection.fromFeatures(generatedFeatures)))
        style.addLayer(CircleLayer("generated-stops-halo", "generated-stops").apply {
            setProperties(
                circleRadius(11f),
                circleColor("#FFF7ED"),
                circleStrokeWidth(2.25f),
                circleStrokeColor("#D97706"),
            )
        })
        style.addLayer(CircleLayer("generated-stops-layer", "generated-stops").apply {
            setProperties(
                circleRadius(7f),
                circleColor("#D97706"),
                circleStrokeWidth(1.5f),
                circleStrokeColor("#FFFFFF"),
            )
        })
        style.addLayer(SymbolLayer("generated-stops-markers", "generated-stops").apply {
            setProperties(
                iconImage("trip-dot-generated"),
                iconSize(0.72f),
                iconAnchor("center"),
                iconOffset(arrayOf(0f, 0f)),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
            )
        })
        style.addLayer(SymbolLayer("generated-stops-numbers", "generated-stops").apply {
            setProperties(
                textField("{n}"),
                textSize(9.5f),
                textFont(arrayOf("Noto Sans Bold", "Open Sans Bold")),
                textColor("#FFFFFF"),
                textHaloColor("#D97706"),
                textHaloWidth(1f),
                textAnchor("center"),
                textOffset(arrayOf(0f, 0f)),
                textIgnorePlacement(true),
                textAllowOverlap(true),
            )
        })
        style.addLayer(SymbolLayer("generated-stops-labels", "generated-stops").apply {
            setProperties(
                textField("{label}"),
                textSize(10.5f),
                textFont(arrayOf("Noto Sans Bold", "Open Sans Bold")),
                textColor("#172033"),
                textHaloColor("#FFFFFF"),
                textHaloWidth(1.5f),
                textAnchor("top"),
                textOffset(arrayOf(0f, 1.2f)),
                textMaxWidth(8f),
                textIgnorePlacement(true),
                textAllowOverlap(true),
            )
        })
    }

    // Excursion stops: purple markers with labels, so side routes are visible on the same map.
    val excursionStopFeatures = excursions.flatMap { e ->
        e.stops.filter { it.latitude != null && it.longitude != null }
               .map { stop ->
                   Feature.fromGeometry(Point.fromLngLat(stop.longitude!!, stop.latitude!!))
                       .also {
                           it.addStringProperty("n", "E")
                           it.addStringProperty("label", stop.locationName.mapLabel())
                       }
               }
    }
    if (excursionStopFeatures.isNotEmpty()) {
        style.addSource(GeoJsonSource("excursion-stops", FeatureCollection.fromFeatures(excursionStopFeatures)))
        style.addLayer(CircleLayer("excursion-stops-halo", "excursion-stops").apply {
            setProperties(
                circleRadius(10f),
                circleColor("#F5F3FF"),
                circleStrokeWidth(2.25f),
                circleStrokeColor("#7C3AED"),
            )
        })
        style.addLayer(CircleLayer("excursion-stops-layer", "excursion-stops").apply {
            setProperties(
                circleRadius(6.5f),
                circleColor("#7C3AED"),
                circleStrokeWidth(1.5f),
                circleStrokeColor("#FFFFFF"),
            )
        })
        style.addLayer(SymbolLayer("excursion-stops-markers", "excursion-stops").apply {
            setProperties(
                iconImage("trip-dot-excursion"),
                iconSize(0.68f),
                iconAnchor("center"),
                iconOffset(arrayOf(0f, 0f)),
                iconIgnorePlacement(true),
                iconAllowOverlap(true),
            )
        })
        style.addLayer(SymbolLayer("excursion-stops-numbers", "excursion-stops").apply {
            setProperties(
                textField("{n}"),
                textSize(9f),
                textFont(arrayOf("Noto Sans Bold", "Open Sans Bold")),
                textColor("#FFFFFF"),
                textHaloColor("#7C3AED"),
                textHaloWidth(1f),
                textAnchor("center"),
                textOffset(arrayOf(0f, 0f)),
                textIgnorePlacement(true),
                textAllowOverlap(true),
            )
        })
        style.addLayer(SymbolLayer("excursion-stops-labels", "excursion-stops").apply {
            setProperties(
                textField("{label}"),
                textSize(10f),
                textFont(arrayOf("Noto Sans Bold", "Open Sans Bold")),
                textColor("#172033"),
                textHaloColor("#FFFFFF"),
                textHaloWidth(1.5f),
                textAnchor("top"),
                textOffset(arrayOf(0f, 1.1f)),
                textMaxWidth(8f),
                textIgnorePlacement(true),
                textAllowOverlap(true),
            )
        })
    }

    addStopAnnotations(context, map, coordinateStops, excursions)

    // Fit camera
    val allCoords = mutableListOf<LatLng>()
    coordinateStops.forEach { allCoords.add(LatLng(it.latitude!!, it.longitude!!)) }
    excursions.forEach { e ->
        e.stops.filter { it.latitude != null && it.longitude != null }
               .forEach { allCoords.add(LatLng(it.latitude!!, it.longitude!!)) }
    }
    when {
        allCoords.isEmpty() ->
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(20.0, 0.0), 1.5))
        allCoords.size == 1 ->
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(allCoords[0], 10.0))
        else -> {
            val bounds = LatLngBounds.Builder().apply { allCoords.forEach { include(it) } }.build()
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 52))
        }
    }
}

private fun addStopAnnotations(
    context: android.content.Context,
    map: MapLibreMap,
    coordinateStops: List<TripStop>,
    excursions: List<Excursion>,
) {
    val iconFactory = IconFactory.getInstance(context)
    val mainIcon = iconFactory.fromBitmap(createDotBitmap(fill = 0xFF1D4ED8.toInt()))
    val generatedIcon = iconFactory.fromBitmap(createDotBitmap(fill = 0xFFD97706.toInt()))
    val excursionIcon = iconFactory.fromBitmap(createDotBitmap(fill = 0xFF7C3AED.toInt()))

    coordinateStops.forEachIndexed { index, stop ->
        val latitude = stop.latitude ?: return@forEachIndexed
        val longitude = stop.longitude ?: return@forEachIndexed
        val icon = if (stop.source == TripStopSource.ITINERARY_GROUP) generatedIcon else mainIcon
        map.addMarker(
            MarkerOptions()
                .position(LatLng(latitude, longitude))
                .title("${index + 1}. ${stop.mapLabel()}")
                .icon(icon),
        )
    }
    excursions.forEach { excursion ->
        excursion.stops
            .filter { it.latitude != null && it.longitude != null }
            .forEach { stop ->
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(stop.latitude!!, stop.longitude!!))
                        .title("Excursio: ${stop.locationName.mapLabel()}")
                        .icon(excursionIcon),
                )
            }
    }
}

private fun TripStop.mapLabel(): String =
    (displayTitle ?: locationName).mapLabel()

private fun String.mapLabel(): String =
    trim()
        .split(",")
        .firstOrNull()
        ?.trim()
        ?.take(24)
        .orEmpty()

private fun Style.addTripDotImages() {
    addImage("trip-dot-main", createDotBitmap(fill = 0xFF1D4ED8.toInt()))
    addImage("trip-dot-generated", createDotBitmap(fill = 0xFFD97706.toInt()))
    addImage("trip-dot-excursion", createDotBitmap(fill = 0xFF7C3AED.toInt()))
}

private fun createDotBitmap(fill: Int): Bitmap {
    val width = 34
    val height = 34
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL
    }
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fill
        style = Paint.Style.FILL
    }

    canvas.drawCircle(width / 2f, height / 2f, 15f, strokePaint)
    canvas.drawCircle(width / 2f, height / 2f, 10f, fillPaint)
    return bitmap
}
