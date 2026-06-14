package com.atlas.ui.screens.country

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.Country
import com.atlas.ui.components.AtlasSectionTitle
import com.atlas.ui.components.geo.AtlasGeoCanvas
import com.atlas.ui.components.geo.GeoCoordinate
import com.atlas.ui.components.geo.GeoMarker
import com.atlas.ui.components.geo.GeoViewport
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasSurface

private val MAP_HEIGHT = 180.dp

/**
 * Compact offline map card for the country detail screen: the country shape filled with
 * its tracking-state color and a single marker at the capital. Uses the shared offline
 * [AtlasGeoCanvas]; no map tiles or network.
 */
@Composable
fun CountryMapCard(
    country: Country,
    style: CountryDetailStyle,
) {
    val markerLat = country.capitalLatitude ?: country.latitude
    val markerLng = country.capitalLongitude ?: country.longitude

    val viewport = if (markerLat != null && markerLng != null) {
        GeoViewport.FitPoints(
            points = listOf(GeoCoordinate(markerLat, markerLng)),
            minLongitudeSpanDegrees = 11.0,
            minLatitudeSpanDegrees = 8.0,
        )
    } else {
        GeoViewport.World()
    }

    val highlightColorByIso2 = country.iso2?.let { mapOf(it to style.primary) } ?: emptyMap()

    val markers = if (markerLat != null && markerLng != null) {
        listOf(
            GeoMarker(
                coordinate = GeoCoordinate(markerLat, markerLng),
                color = style.primary,
                label = country.capitalNameCa,
            ),
        )
    } else {
        emptyList()
    }

    Column {
        AtlasSectionTitle(title = "Mapa")
        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
                .height(MAP_HEIGHT)
                .clip(RoundedCornerShape(16.dp))
                .background(AtlasSurface)
                .border(1.dp, AtlasOutline, RoundedCornerShape(16.dp)),
        ) {
            AtlasGeoCanvas(
                modifier = Modifier.fillMaxWidth().height(MAP_HEIGHT),
                viewport = viewport,
                markers = markers,
                highlightColorByIso2 = highlightColorByIso2,
            )
        }
    }
}
