package com.atlas.ui.screens.country

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.Country
import com.atlas.domain.model.CountryTrackingState
import com.atlas.ui.components.geo.AtlasGeoCanvas
import com.atlas.ui.components.geo.GeoCoordinate
import com.atlas.ui.components.geo.GeoMarker
import com.atlas.ui.components.geo.GeoViewport
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline

@Composable
fun CountryMapHero(
    country: Country,
    style: CountryDetailStyle,
    trackingState: CountryTrackingState,
    onBackClick: () -> Unit,
) {
    val lat = country.latitude
    val lng = country.longitude
    val capLat = country.capitalLatitude
    val capLng = country.capitalLongitude

    // Single marker at capital; fall back to country centre when no capital coords
    val markerLat = capLat ?: lat
    val markerLng = capLng ?: lng

    val viewport = if (markerLat != null && markerLng != null) {
        GeoViewport.FitPoints(
            points = listOf(GeoCoordinate(markerLat, markerLng)),
            minLongitudeSpanDegrees = 22.0,
            minLatitudeSpanDegrees = 16.0,
        )
    } else {
        GeoViewport.World()
    }

    val highlightColorByIso2 = if (country.iso2 != null) {
        mapOf(country.iso2 to style.primary)
    } else {
        emptyMap()
    }

    val markers = if (markerLat != null && markerLng != null) {
        listOf(
            GeoMarker(
                coordinate = GeoCoordinate(markerLat, markerLng),
                color = style.primary,
                label = country.capitalNameCa,
            )
        )
    } else {
        emptyList()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(232.dp),
    ) {
        AtlasGeoCanvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(232.dp)
                .clip(RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp)),
            viewport = viewport,
            markers = markers,
            highlightColorByIso2 = highlightColorByIso2,
        )

        BackPill(
            onBackClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 16.dp),
        )
    }
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
