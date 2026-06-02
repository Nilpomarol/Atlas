package com.atlas.ui.screens.country

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.Country
import com.atlas.domain.model.CountryTrackingState
import com.atlas.ui.components.map.AtlasMapView
import com.atlas.ui.components.map.toHexColor
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point

// Blank parchment background — no tiles, no roads, no labels.
// Country marker and (future) polygon are the only visual elements.
private const val COUNTRY_MAP_STYLE =
    """{"version":8,"sources":{},"layers":[{"id":"bg","type":"background","paint":{"background-color":"#F4EFE6"}}]}"""

@Composable
fun CountryMapHero(
    country: Country,
    style: CountryDetailStyle,
    trackingState: CountryTrackingState,
    onBackClick: () -> Unit,
) {
    val mapRef = remember { mutableStateOf<Pair<MapLibreMap, Style>?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(232.dp),
    ) {
        AtlasMapView(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp)),
            styleUrl = COUNTRY_MAP_STYLE,
            onMapReady = { map, mapStyle -> mapRef.value = Pair(map, mapStyle) },
        )

        BackPill(
            onBackClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 16.dp),
        )

        country.capitalNameCa?.let { capital ->
            Text(
                text = capital,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp),
                style = MaterialTheme.typography.labelMedium,
                color = AtlasOnSurfaceMuted,
            )
        }
    }

    LaunchedEffect(mapRef.value, country) {
        val (map, mapStyle) = mapRef.value ?: return@LaunchedEffect
        setupCountryMap(map, mapStyle, country, style)
    }
}

private fun setupCountryMap(
    map: MapLibreMap,
    style: Style,
    country: Country,
    detailStyle: CountryDetailStyle,
) {
    val lat = country.latitude ?: return
    val lng = country.longitude ?: return

    // Country marker
    val accentHex = detailStyle.primary.toHexColor()
    val sourceId = "country-marker"
    val layerId = "country-marker-layer"
    if (style.getLayer(layerId) != null) style.removeLayer(layerId)
    if (style.getSource(sourceId) != null) style.removeSource(sourceId)

    style.addSource(GeoJsonSource(sourceId,
        Feature.fromGeometry(Point.fromLngLat(lng, lat))))
    style.addLayer(CircleLayer(layerId, sourceId).apply {
        setProperties(
            circleRadius(12f),
            circleColor(accentHex),
            circleStrokeWidth(3f),
            circleStrokeColor("#FFFFFF"),
        )
    })

    // Capital marker (smaller, if different from country coords)
    val capLat = country.capitalLatitude
    val capLng = country.capitalLongitude
    if (capLat != null && capLng != null) {
        val capSourceId = "capital-marker"
        val capLayerId = "capital-marker-layer"
        if (style.getLayer(capLayerId) != null) style.removeLayer(capLayerId)
        if (style.getSource(capSourceId) != null) style.removeSource(capSourceId)

        style.addSource(GeoJsonSource(capSourceId,
            Feature.fromGeometry(Point.fromLngLat(capLng, capLat))))
        style.addLayer(CircleLayer(capLayerId, capSourceId).apply {
            setProperties(
                circleRadius(6f),
                circleColor("#FFFFFF"),
                circleStrokeWidth(2.5f),
                circleStrokeColor(accentHex),
            )
        })
    }

    map.moveCamera(
        CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder()
                .target(LatLng(lat, lng))
                .zoom(4.5)
                .build(),
        ),
    )
}

@Composable
fun BackPill(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onBackClick,
        modifier = modifier,
        shape = RoundedCornerShape(100.dp),
        color = Color.White.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, AtlasOutline),
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Enrere",
                modifier = Modifier.size(14.dp),
                tint = AtlasOnSurfaceStrong,
            )
            Text(
                text = "Enrere",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = AtlasOnSurfaceStrong,
            )
        }
    }
}
