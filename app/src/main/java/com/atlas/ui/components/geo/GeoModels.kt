package com.atlas.ui.components.geo

import androidx.compose.ui.graphics.Color

data class GeoCoordinate(
    val latitude: Double,
    val longitude: Double,
)

data class GeoRing(
    val points: List<GeoCoordinate>,
)

data class GeoPolygon(
    val rings: List<GeoRing>,
)

data class GeoCountryFeature(
    val iso2: String?,
    val iso3: String?,
    val name: String,
    val continent: String?,
    val polygons: List<GeoPolygon>,
)

data class GeoFeatureCollection(
    val countries: List<GeoCountryFeature>,
)

sealed interface GeoViewport {
    data object World : GeoViewport

    data class FitPoints(
        val points: List<GeoCoordinate>,
        val minLongitudeSpanDegrees: Double = 10.0,
        val minLatitudeSpanDegrees: Double = 7.0,
    ) : GeoViewport
}

data class GeoRouteSegment(
    val from: GeoCoordinate,
    val to: GeoCoordinate,
    val color: Color,
    val isDashed: Boolean = false,
    val alpha: Float = 1f,
)

data class GeoMarker(
    val coordinate: GeoCoordinate,
    val color: Color,
    val radiusMultiplier: Float = 1f,
    val alpha: Float = 1f,
    val isHollow: Boolean = false,
    val label: String? = null,
)
