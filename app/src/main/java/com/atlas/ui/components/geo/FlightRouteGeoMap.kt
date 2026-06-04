package com.atlas.ui.components.geo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.atlas.domain.model.Airport
import com.atlas.ui.components.AtlasSemanticColors

@Composable
fun FlightRouteGeoMap(
    originAirport: Airport?,
    destinationAirport: Airport?,
    prevContextAirport: Airport?,
    nextContextAirport: Airport?,
    statusColors: AtlasSemanticColors,
    modifier: Modifier = Modifier,
) {
    val origin = originAirport?.toGeoCoordinate()
    val destination = destinationAirport?.toGeoCoordinate()
    val mainRoutePoints = if (origin != null && destination != null) {
        flightRouteCurvePoints(origin, destination)
    } else {
        emptyList()
    }
    val focusPoints = mainRoutePoints.ifEmpty { listOfNotNull(origin, destination) }
    val viewport = if (focusPoints.size >= 2) {
        GeoViewport.FitPoints(
            points = focusPoints,
            minLongitudeSpanDegrees = 12.0,
            minLatitudeSpanDegrees = 9.0,
        )
    } else {
        GeoViewport.World
    }

    val routeSegments = buildList {
        val prev = prevContextAirport?.toGeoCoordinate()
        if (prev != null && origin != null) {
            add(
                GeoRouteSegment(
                    from = prev,
                    to = origin,
                    color = statusColors.foreground,
                    isDashed = true,
                    alpha = 0.55f,
                ),
            )
        }
        if (origin != null && destination != null) {
            add(
                GeoRouteSegment(
                    from = origin,
                    to = destination,
                    color = statusColors.foreground,
                ),
            )
        }
        val next = nextContextAirport?.toGeoCoordinate()
        if (destination != null && next != null) {
            add(
                GeoRouteSegment(
                    from = destination,
                    to = next,
                    color = statusColors.foreground,
                    isDashed = true,
                    alpha = 0.55f,
                ),
            )
        }
    }

    val markers = buildList {
        prevContextAirport?.toGeoCoordinate()?.let {
            add(GeoMarker(it, statusColors.foreground, radiusMultiplier = 0.72f, alpha = 0.6f))
        }
        nextContextAirport?.toGeoCoordinate()?.let {
            add(GeoMarker(it, statusColors.foreground, radiusMultiplier = 0.72f, alpha = 0.6f))
        }
        origin?.let { add(GeoMarker(it, statusColors.foreground, radiusMultiplier = 0.72f, label = originAirport.city)) }
        destination?.let { add(GeoMarker(it, statusColors.foreground, radiusMultiplier = 0.72f, label = destinationAirport.city)) }
    }

    AtlasGeoCanvas(
        modifier = modifier,
        viewport = viewport,
        routeSegments = routeSegments,
        markers = markers,
    )
}

private fun Airport.toGeoCoordinate(): GeoCoordinate =
    GeoCoordinate(latitude = latitude, longitude = longitude)
