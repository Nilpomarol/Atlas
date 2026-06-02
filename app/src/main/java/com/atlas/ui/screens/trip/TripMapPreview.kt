package com.atlas.ui.screens.trip

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.Excursion
import com.atlas.domain.model.TripStop
import com.atlas.domain.model.TripStopSource
import com.atlas.ui.components.map.AtlasMapView
import com.atlas.ui.theme.AtlasAccentContainer
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
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
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineCap
import org.maplibre.android.style.layers.PropertyFactory.lineJoin
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
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
) {
    val coordinateStops = stops.filter { it.latitude != null && it.longitude != null }
    val mappableExcursionStops = excursions.sumOf { e -> e.stops.count { it.latitude != null && it.longitude != null } }
    val mappableCount = coordinateStops.size + mappableExcursionStops
    val missingCoordinateCount = stops.size + excursions.sumOf { it.stops.size } - mappableCount

    // Holds the loaded map so content can be updated independently of style loading
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
                .height(210.dp),
        ) {
            AtlasMapView(
                modifier = Modifier.matchParentSize(),
                onMapReady = { map, style -> mapRef.value = Pair(map, style) },
            )
        }

        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AtlasAccentContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Place, contentDescription = null,
                    modifier = Modifier.size(20.dp), tint = AtlasPrimary)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "$mappableCount ${if (mappableCount == 1) "parada" else "parades"} al mapa",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AtlasOnSurfaceStrong,
                )
                Text(
                    text = if (missingCoordinateCount == 0) "Ruta completa."
                           else "$missingCoordinateCount sense coordenades.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

    // Re-runs when the map becomes ready OR when stop/excursion data changes
    LaunchedEffect(mapRef.value, coordinateStops, excursions) {
        val (map, style) = mapRef.value ?: return@LaunchedEffect
        clearTripLayers(style)
        addTripContent(map, style, coordinateStops, excursions)
    }
}

// ── Layer/source IDs ─────────────────────────────────────────────────────────

private val TRIP_LAYER_IDS = listOf(
    "main-route-line", "excursion-route-lines",
    "main-stops-layer", "generated-stops-layer", "excursion-stops-layer",
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
    map: MapLibreMap,
    style: Style,
    coordinateStops: List<TripStop>,
    excursions: List<Excursion>,
) {
    // Main route line (through all coordinate stops in order)
    val mainCoords = coordinateStops.map { Point.fromLngLat(it.longitude!!, it.latitude!!) }
    if (mainCoords.size >= 2) {
        style.addSource(GeoJsonSource("main-route",
            Feature.fromGeometry(LineString.fromLngLats(mainCoords))))
        style.addLayer(LineLayer("main-route-line", "main-route").apply {
            setProperties(lineWidth(3f), lineColor("#2563EB"), lineCap("round"), lineJoin("round"))
        })
    }

    // Excursion route lines (one LineString per excursion)
    val excursionLines = excursions.mapNotNull { e ->
        val coords = e.stops
            .filter { it.latitude != null && it.longitude != null }
            .sortedBy { it.sortOrder }
            .map { Point.fromLngLat(it.longitude!!, it.latitude!!) }
        if (coords.size >= 2) Feature.fromGeometry(LineString.fromLngLats(coords)) else null
    }
    if (excursionLines.isNotEmpty()) {
        style.addSource(GeoJsonSource("excursion-routes",
            FeatureCollection.fromFeatures(excursionLines)))
        style.addLayer(LineLayer("excursion-route-lines", "excursion-routes").apply {
            setProperties(lineWidth(2.5f), lineColor("#9333EA"), lineCap("round"), lineJoin("round"))
        })
    }

    // Main stops (blue)
    val mainFeatures = coordinateStops
        .filter { it.source != TripStopSource.ITINERARY_GROUP }
        .map { Feature.fromGeometry(Point.fromLngLat(it.longitude!!, it.latitude!!)) }
    if (mainFeatures.isNotEmpty()) {
        style.addSource(GeoJsonSource("main-stops", FeatureCollection.fromFeatures(mainFeatures)))
        style.addLayer(CircleLayer("main-stops-layer", "main-stops").apply {
            setProperties(circleRadius(8f), circleColor("#2563EB"),
                circleStrokeWidth(2f), circleStrokeColor("#FFFFFF"))
        })
    }

    // Generated itinerary stops (amber)
    val generatedFeatures = coordinateStops
        .filter { it.source == TripStopSource.ITINERARY_GROUP }
        .map { Feature.fromGeometry(Point.fromLngLat(it.longitude!!, it.latitude!!)) }
    if (generatedFeatures.isNotEmpty()) {
        style.addSource(GeoJsonSource("generated-stops", FeatureCollection.fromFeatures(generatedFeatures)))
        style.addLayer(CircleLayer("generated-stops-layer", "generated-stops").apply {
            setProperties(circleRadius(8f), circleColor("#D97706"),
                circleStrokeWidth(2f), circleStrokeColor("#FFFFFF"))
        })
    }

    // Excursion stops (purple)
    val excursionStopFeatures = excursions.flatMap { e ->
        e.stops.filter { it.latitude != null && it.longitude != null }
               .map { Feature.fromGeometry(Point.fromLngLat(it.longitude!!, it.latitude!!)) }
    }
    if (excursionStopFeatures.isNotEmpty()) {
        style.addSource(GeoJsonSource("excursion-stops",
            FeatureCollection.fromFeatures(excursionStopFeatures)))
        style.addLayer(CircleLayer("excursion-stops-layer", "excursion-stops").apply {
            setProperties(circleRadius(7f), circleColor("#9333EA"),
                circleStrokeWidth(2f), circleStrokeColor("#FFFFFF"))
        })
    }

    // Fit camera to all plotted points
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
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 60))
        }
    }
}
