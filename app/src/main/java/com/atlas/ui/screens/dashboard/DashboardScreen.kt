package com.atlas.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.domain.model.TravelStatus
import com.atlas.presentation.dashboard.DashboardFlightUiState
import com.atlas.presentation.dashboard.DashboardTripUiState
import com.atlas.presentation.dashboard.DashboardUiState
import com.atlas.presentation.trip.TripStopMapPoint
import com.atlas.ui.components.AtlasCard
import com.atlas.ui.components.AtlasPage
import com.atlas.ui.components.AtlasSectionTitle
import com.atlas.ui.components.geo.AtlasGeoCanvas
import com.atlas.ui.components.geo.GeoCoordinate
import com.atlas.ui.components.geo.GeoMarker
import com.atlas.ui.components.geo.GeoRouteSegment
import com.atlas.ui.components.geo.GeoViewport
import com.atlas.ui.components.tripStatusColors
import com.atlas.ui.theme.AtlasLiving
import com.atlas.ui.theme.AtlasLived
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasOnSurfaceFaint
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPlanned
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasSurfaceSubtle
import com.atlas.ui.theme.AtlasVisited
import com.atlas.ui.theme.AtlasWished

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
) {
    AtlasPage(contentPadding = PaddingValues(0.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            DashboardHero(uiState = uiState)

            uiState.featuredTrip?.let { trip ->
                InProgressTripCard(trip = trip)
            }

            WorldStatsCard(uiState = uiState)

            if (uiState.upcomingTrips.isNotEmpty()) {
                UpcomingTripsSection(trips = uiState.upcomingTrips)
            }

            if (uiState.upcomingFlights.isNotEmpty()) {
                UpcomingFlightsSection(flights = uiState.upcomingFlights)
            }

            if (uiState.recentCompletedTrips.isNotEmpty()) {
                RecentTripsSection(trips = uiState.recentCompletedTrips)
            }

            if (uiState.recentFlights.isNotEmpty()) {
                RecentFlightsSection(flights = uiState.recentFlights)
            }
        }
    }
}

// ── Hero ─────────────────────────────────────────────────────────────────────

@Composable
private fun DashboardHero(uiState: DashboardUiState) {
    Column {
        val highlightColorByIso2 = remember(
            uiState.livingIso2s, uiState.livedIso2s, uiState.visitedIso2s,
            uiState.plannedIso2s, uiState.wishedIso2s,
        ) {
            buildMap {
                uiState.wishedIso2s.forEach { put(it, AtlasWished) }
                uiState.plannedIso2s.forEach { put(it, AtlasPlanned) }
                uiState.visitedIso2s.forEach { put(it, AtlasVisited) }
                uiState.livedIso2s.forEach { put(it, AtlasLived) }
                uiState.livingIso2s.forEach { put(it, AtlasLiving) }
            }
        }
        AtlasGeoCanvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            highlightColorByIso2 = highlightColorByIso2,
        )
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
            Text(
                text = "EL TEU MÓN",
                style = MaterialTheme.typography.labelSmall,
                color = AtlasOnSurfaceFaint,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = uiState.visitedCount.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = AtlasOnSurfaceStrong,
                )
                Text(
                    text = "/${uiState.trackableCountryCount}",
                    modifier = Modifier.padding(start = 2.dp, bottom = 6.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = AtlasOnSurfaceMuted,
                )
                Text(
                    text = "${uiState.visitedContinentCount}/7 continents",
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp, bottom = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = AtlasOnSurfaceMuted,
                )
            }
            Text(
                text = "${uiState.visitedCount} visitats · ${uiState.livedCount} viscuts",
                style = MaterialTheme.typography.labelMedium,
                color = AtlasOnSurfaceMuted,
            )
            StateLegend()
            CountryKpiRow(uiState = uiState)
        }
    }
}

@Composable
private fun StateLegend() {
    Row(
        modifier = Modifier.padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendDot("Vivint", AtlasLiving)
        LegendDot("Viscut", AtlasLived)
        LegendDot("Visitat", AtlasVisited)
        LegendDot("Planificat", AtlasPlanned)
        LegendDot("Desitjat", AtlasWished)
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AtlasOnSurfaceMuted,
        )
    }
}

@Composable
private fun CountryKpiRow(uiState: DashboardUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CountryKpiChip(modifier = Modifier.weight(1f), value = uiState.visitedCount, label = "Visitats", color = AtlasVisited)
        CountryKpiChip(modifier = Modifier.weight(1f), value = uiState.livedCount, label = "Viscuts", color = AtlasLived)
        CountryKpiChip(modifier = Modifier.weight(1f), value = uiState.plannedCount, label = "Plans", color = AtlasPlanned)
        CountryKpiChip(modifier = Modifier.weight(1f), value = uiState.wishedCount, label = "Desitjats", color = AtlasWished)
    }
}

@Composable
private fun CountryKpiChip(
    value: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    AtlasCard(modifier = modifier, contentPadding = PaddingValues(10.dp)) {
        Column {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = AtlasOnSurfaceStrong,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(color),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ── In-progress trip card ────────────────────────────────────────────────────

@Composable
private fun InProgressTripCard(trip: DashboardTripUiState) {
    val colors = trip.status.tripStatusColors()
    val dateText = trip.dateText ?: "Sense data"
    val routeText = trip.routeText ?: "Sense parades"

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        AtlasSectionTitle(title = "En curs")
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = AtlasSurface,
            border = BorderStroke(1.dp, AtlasOutline),
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(124.dp)
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                        .background(AtlasSurfaceSubtle),
                ) {
                    TripCardMap(
                        mapPoints = trip.mapPoints,
                        stopCount = trip.stopCount,
                        routeColor = colors.foreground,
                    )
                    TripStatePill(
                        label = colors.label,
                        color = colors.foreground,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp),
                    )
                    Text(
                        text = dateText.uppercase(),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 14.dp, top = 14.dp, end = 110.dp)
                            .background(AtlasSurface.copy(alpha = 0.88f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 9.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AtlasOnSurfaceStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 18.dp, end = 18.dp, bottom = 3.dp),
                    ) {
                        Text(
                            text = trip.title,
                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 27.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = AtlasOnSurfaceStrong,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AtlasSurface,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Map,
                            contentDescription = null,
                            tint = AtlasPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = routeText,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AtlasOnSurfaceStrong,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(
                            modifier = Modifier
                                .height(18.dp)
                                .width(1.dp)
                                .background(AtlasOutline),
                        )
                        Text(
                            text = "${trip.stopCount} ${if (trip.stopCount == 1) "parada" else "parades"}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AtlasOnSurfaceMuted,
                        )
                    }
                }
            }
        }
    }
}

// ── World stats card ─────────────────────────────────────────────────────────

@Composable
private fun WorldStatsCard(uiState: DashboardUiState) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        AtlasSectionTitle(title = "El teu atlas")
        Spacer(modifier = Modifier.height(10.dp))
        AtlasCard(contentPadding = PaddingValues(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatGridItem(
                        modifier = Modifier.weight(1f),
                        value = uiState.tripCount.toString(),
                        label = "Viatges",
                    )
                    StatDivider()
                    StatGridItem(
                        modifier = Modifier.weight(1f),
                        value = uiState.flightCount.toString(),
                        label = "Vols",
                    )
                    StatDivider()
                    StatGridItem(
                        modifier = Modifier.weight(1f),
                        value = "${uiState.visitedCount}/${uiState.trackableCountryCount}",
                        label = "Països",
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(AtlasOutline),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatGridItem(
                        modifier = Modifier.weight(1f),
                        value = uiState.flownDistanceKm.toCompactKm(),
                        label = "Km volats",
                    )
                    StatDivider()
                    StatGridItem(
                        modifier = Modifier.weight(1f),
                        value = uiState.avgFlightDistanceKm?.toCompactKm() ?: "—",
                        label = "Distància mitj.",
                    )
                    StatDivider()
                    StatGridItem(
                        modifier = Modifier.weight(1f),
                        value = uiState.visitedContinentCount.toString(),
                        label = "Continents",
                    )
                }
            }
        }
    }
}

@Composable
private fun StatGridItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AtlasOnSurfaceStrong,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AtlasOnSurfaceMuted,
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(AtlasOutline),
    )
}

// ── Upcoming trips ───────────────────────────────────────────────────────────

@Composable
private fun UpcomingTripsSection(trips: List<DashboardTripUiState>) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AtlasSectionTitle(title = if (trips.size == 1) "Proper viatge" else "Propers viatges")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            trips.forEach { trip ->
                UpcomingTripRow(trip = trip)
            }
        }
    }
}

@Composable
private fun UpcomingTripRow(trip: DashboardTripUiState) {
    val colors = trip.status.tripStatusColors()
    AtlasCard(contentPadding = PaddingValues(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(colors.container, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Map,
                    contentDescription = null,
                    tint = colors.foreground,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = trip.routeText ?: trip.dateText ?: "Sense data",
                    style = MaterialTheme.typography.bodySmall,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = trip.dateText ?: "—",
                    style = MaterialTheme.typography.labelSmall,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 1,
                )
                trip.dayCount?.let { days ->
                    Text(
                        text = "$days dies",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AtlasPrimary,
                    )
                }
            }
        }
    }
}

// ── Upcoming flights ─────────────────────────────────────────────────────────

@Composable
private fun UpcomingFlightsSection(flights: List<DashboardFlightUiState>) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AtlasSectionTitle(title = if (flights.size == 1) "Proper vol" else "Propers vols")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            flights.forEach { flight ->
                UpcomingFlightRow(flight = flight)
            }
        }
    }
}

@Composable
private fun UpcomingFlightRow(flight: DashboardFlightUiState) {
    AtlasCard(contentPadding = PaddingValues(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(AtlasNavy.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.AirplanemodeActive,
                    contentDescription = null,
                    tint = AtlasNavy,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = flight.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = flight.meta ?: flight.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = flight.dateText ?: "—",
                style = MaterialTheme.typography.labelSmall,
                color = AtlasOnSurfaceMuted,
                maxLines = 1,
            )
        }
    }
}

// ── Recent trips ─────────────────────────────────────────────────────────────

@Composable
private fun RecentTripsSection(trips: List<DashboardTripUiState>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AtlasSectionTitle(
            title = "Viatges recents",
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            trips.forEach { trip ->
                RecentTripCard(trip = trip)
            }
        }
    }
}

@Composable
private fun RecentTripCard(trip: DashboardTripUiState) {
    val colors = trip.status.tripStatusColors()
    Surface(
        modifier = Modifier.width(190.dp),
        shape = RoundedCornerShape(16.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(AtlasSurfaceSubtle),
            ) {
                TripCardMap(
                    mapPoints = trip.mapPoints,
                    stopCount = trip.stopCount,
                    routeColor = colors.foreground,
                )
                Text(
                    text = trip.memoryDateText?.uppercase() ?: trip.dateText?.uppercase() ?: "",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .background(AtlasSurface.copy(alpha = 0.88f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                )
                Text(
                    text = trip.title,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 12.dp, end = 12.dp, bottom = 5.dp),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Map,
                    contentDescription = null,
                    tint = AtlasOnSurfaceMuted,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    text = trip.countryText ?: trip.routeText ?: "Sense país",
                    style = MaterialTheme.typography.bodySmall,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ── Recent flights ───────────────────────────────────────────────────────────

@Composable
private fun RecentFlightsSection(flights: List<DashboardFlightUiState>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AtlasSectionTitle(
            title = "Vols recents",
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            flights.forEach { flight ->
                RecentFlightCard(flight = flight)
            }
        }
    }
}

@Composable
private fun RecentFlightCard(flight: DashboardFlightUiState) {
    AtlasCard(
        modifier = Modifier.width(170.dp),
        contentPadding = PaddingValues(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                imageVector = Icons.Filled.AirplanemodeActive,
                contentDescription = null,
                tint = AtlasNavy,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = flight.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AtlasOnSurfaceStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = flight.meta ?: flight.label,
                style = MaterialTheme.typography.bodySmall,
                color = AtlasOnSurfaceMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = flight.dateText ?: "—",
                style = MaterialTheme.typography.labelSmall,
                color = AtlasOnSurfaceMuted,
            )
        }
    }
}

// ── Shared card helpers ──────────────────────────────────────────────────────

@Composable
private fun TripCardMap(
    mapPoints: List<TripStopMapPoint>,
    stopCount: Int,
    routeColor: Color,
) {
    val coordinates = mapPoints.map { GeoCoordinate(latitude = it.latitude, longitude = it.longitude) }
    if (coordinates.isEmpty()) {
        TripMapTexture()
        SchematicRouteCanvas(stopCount = stopCount)
        return
    }
    AtlasGeoCanvas(
        modifier = Modifier.fillMaxSize(),
        viewport = GeoViewport.FitPoints(
            points = coordinates,
            minLongitudeSpanDegrees = 4.8,
            minLatitudeSpanDegrees = 3.2,
        ),
        routeSegments = coordinates.zipWithNext { from, to ->
            GeoRouteSegment(from = from, to = to, color = routeColor, alpha = 0.9f)
        },
        markers = coordinates.mapIndexed { index, coordinate ->
            GeoMarker(
                coordinate = coordinate,
                color = routeColor,
                radiusMultiplier = if (index == 0 || index == coordinates.lastIndex) 0.58f else 0.46f,
                isHollow = index == coordinates.lastIndex && coordinates.size > 1,
            )
        },
    )
}

@Composable
private fun TripMapTexture() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val spacing = 24.dp.toPx()
        var x = 0f
        while (x <= size.width) {
            drawLine(AtlasNavy.copy(alpha = 0.08f), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
            x += spacing
        }
        var y = 0f
        while (y <= size.height) {
            drawLine(AtlasNavy.copy(alpha = 0.08f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            y += spacing
        }
    }
}

@Composable
private fun SchematicRouteCanvas(stopCount: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val visibleStops = stopCount.coerceIn(2, 5)
        val basePoints = listOf(
            Offset(size.width * 0.10f, size.height * 0.62f),
            Offset(size.width * 0.32f, size.height * 0.36f),
            Offset(size.width * 0.55f, size.height * 0.48f),
            Offset(size.width * 0.74f, size.height * 0.30f),
            Offset(size.width * 0.92f, size.height * 0.52f),
        )
        val points = basePoints.take(visibleStops)
        val path = Path().apply {
            points.firstOrNull()?.let { moveTo(it.x, it.y) }
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(path, Color.White.copy(alpha = 0.82f), style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
        points.forEachIndexed { index, point ->
            if (index == points.lastIndex) {
                drawCircle(Color.White, radius = 5.dp.toPx(), center = point, style = Stroke(width = 2.dp.toPx()))
            } else {
                drawCircle(Color.White, radius = 5.dp.toPx(), center = point)
            }
        }
    }
}

@Composable
private fun TripStatePill(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(color, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(Color.White, RoundedCornerShape(999.dp)),
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
        )
    }
}

private fun Double.toCompactKm(): String {
    val rounded = kotlin.math.round(this).toLong()
    return if (rounded >= 10_000) "${rounded / 1_000} k" else rounded.toString()
}
