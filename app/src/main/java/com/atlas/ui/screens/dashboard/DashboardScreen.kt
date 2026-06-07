package com.atlas.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Flight
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.R
import com.atlas.domain.model.TravelStatus
import com.atlas.presentation.dashboard.DashboardFlightUiState
import com.atlas.presentation.dashboard.DashboardTripUiState
import com.atlas.presentation.dashboard.DashboardUiState
import com.atlas.presentation.flight.FlightListItemUiState
import com.atlas.presentation.trip.TripStopMapPoint
import com.atlas.ui.components.AirlineLogo
import com.atlas.ui.components.AtlasCard
import com.atlas.ui.components.FlightCard
import com.atlas.ui.components.AtlasPill
import com.atlas.ui.components.AtlasPage
import com.atlas.ui.components.AtlasSectionTitle
import com.atlas.ui.components.geo.AtlasGeoCanvas
import com.atlas.ui.components.geo.GeoCoordinate
import com.atlas.ui.components.geo.GeoMarker
import com.atlas.ui.components.geo.GeoRouteSegment
import com.atlas.ui.components.geo.GeoViewport
import com.atlas.ui.components.tripStatusColors
import com.atlas.ui.theme.AtlasBackground
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

private val DashboardWorldViewport = GeoViewport.World(minLatitudeDeg = -45.0, maxLatitudeDeg = 72.0)

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onStatsClick: () -> Unit = {},
    onTripsClick: () -> Unit = {},
    onFlightsClick: () -> Unit = {},
    onTripClick: (String) -> Unit = {},
    onFlightClick: (String) -> Unit = {},
    onItineraryClick: (String) -> Unit = {},
) {
    AtlasPage(contentPadding = PaddingValues(0.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            DashboardPageHeader()
            DashboardMapHero(uiState = uiState)
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                uiState.featuredTrip?.let { InProgressTripCard(it, onTripClick) }
                WorldStatsCard(uiState = uiState, onStatsClick = onStatsClick)

                if (uiState.upcomingTrips.isNotEmpty()) {
                    UpcomingTripsSection(trips = uiState.upcomingTrips, onSeeAll = onTripsClick, onTripClick = onTripClick)
                }
                if (uiState.upcomingFlights.isNotEmpty()) {
                    UpcomingFlightsSection(flights = uiState.upcomingFlights, onSeeAll = onFlightsClick, onFlightClick = onFlightClick, onItineraryClick = onItineraryClick)
                }
                if (uiState.recentCompletedTrips.isNotEmpty()) {
                    RecentTripsSection(trips = uiState.recentCompletedTrips, onTripClick = onTripClick)
                }
                if (uiState.recentFlights.isNotEmpty()) {
                    RecentFlightsSection(flights = uiState.recentFlights, onFlightClick = onFlightClick, onItineraryClick = onItineraryClick)
                }
            }
        }
    }
}

// ── Page header ───────────────────────────────────────────────────────────────

@Composable
private fun DashboardPageHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AtlasBackground)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "El teu atlas",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineSmall,
            color = AtlasOnSurfaceStrong,
        )
        Box(
            modifier = Modifier.size(34.dp).clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.atlas_logo),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

// ── World map (full-width, no card) ──────────────────────────────────────────

@Composable
private fun DashboardMapHero(uiState: DashboardUiState) {
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AtlasBackground)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        AtlasGeoCanvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2.05f)
                .clipToBounds(),
            viewport = DashboardWorldViewport,
            highlightColorByIso2 = highlightColorByIso2,
            highlightFillAlpha = 1f,
            highlightStrokeAlpha = 1f,
            markers = emptyList(),
            mapPaddingDp = 6f,
        )
        DashboardHeroStatsCard(
            uiState = uiState,
            modifier = Modifier
                .padding(top = 10.dp)
                .padding(horizontal = 20.dp),
        )
    }
}

// ── Hero stats (below map, no card) ──────────────────────────────────────────

@Composable
private fun DashboardHeroStatsCard(uiState: DashboardUiState, modifier: Modifier = Modifier) {
    AtlasCard(
        modifier = modifier,
        contentPadding = PaddingValues(14.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                HeroPrimaryMetric(
                    modifier = Modifier.weight(1f),
                    value = uiState.visitedCount.toString(),
                    suffix = "/${uiState.trackableCountryCount}",
                    label = "Territoris visitats",
                    accent = AtlasVisited,
                )
                HeroPrimaryMetric(
                    modifier = Modifier.weight(1f),
                    value = "%.0f".format(uiState.worldPercentage),
                    suffix = "%",
                    label = "${uiState.visitedContinentCount}/7 continents",
                    accent = AtlasPrimary,
                    alignEnd = true,
                )
            }
            HeroKpiRow(uiState = uiState)
        }
    }
}

@Composable
private fun HeroPrimaryMetric(
    value: String,
    suffix: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                color = accent,
                maxLines = 1,
            )
            Text(
                text = suffix,
                modifier = Modifier.padding(start = 3.dp, bottom = 6.dp),
                style = MaterialTheme.typography.titleMedium,
                color = AtlasOnSurfaceMuted,
                maxLines = 1,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AtlasOnSurfaceMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DashboardHeroStats(uiState: DashboardUiState, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
    Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
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
                text = "· ${"%.0f".format(uiState.worldPercentage)}% del món · ${uiState.visitedContinentCount}/7 continents",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineLarge,
                color = AtlasOnSurfaceStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
            )
        }
        HeroKpiRow(uiState = uiState)
    }
    }
}

@Composable
private fun HeroKpiRow(uiState: DashboardUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HeroKpiItem(Modifier.weight(1f), uiState.visitedCount, "Visitats", AtlasVisited)
        HeroKpiItem(Modifier.weight(1f), uiState.livedCount, "Viscuts", AtlasLived)
        HeroKpiItem(Modifier.weight(1f), uiState.plannedCount, "Plans", AtlasPlanned)
        HeroKpiItem(Modifier.weight(1f), uiState.wishedCount, "Desitjats", AtlasWished)
    }
}

@Composable
private fun HeroKpiItem(modifier: Modifier, value: Int, label: String, color: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = AtlasSurface.copy(alpha = 0.76f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.18f)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color,
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

// ── In-progress trip card ────────────────────────────────────────────────────

@Composable
private fun InProgressTripCard(trip: DashboardTripUiState, onTripClick: (String) -> Unit) {
    val colors = trip.status.tripStatusColors()
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        AtlasSectionTitle(title = "En curs")
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            onClick = { onTripClick(trip.tripId) },
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
                    TripCardMap(trip.mapPoints, trip.stopCount, colors.foreground)
                    TripStatePill(
                        label = colors.label,
                        color = colors.foreground,
                        modifier = Modifier.align(Alignment.TopEnd).padding(14.dp),
                    )
                    Text(
                        text = (trip.dateText ?: "Sense data").uppercase(),
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
                    Text(
                        text = trip.title,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 18.dp, end = 18.dp, bottom = 3.dp),
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 27.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasOnSurfaceStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().background(AtlasSurface)
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Filled.Map, null, tint = AtlasPrimary, modifier = Modifier.size(16.dp))
                    Text(
                        text = trip.routeText ?: "Sense parades",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasOnSurfaceStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(18.dp).width(1.dp).background(AtlasOutline))
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

// ── Stats card ────────────────────────────────────────────────────────────────

@Composable
private fun WorldStatsCard(uiState: DashboardUiState, onStatsClick: () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AtlasSectionTitle(
            title = "Estadístiques",
            action = { SeeAllLink("Veure tot", onStatsClick) },
        )
        AtlasCard(contentPadding = PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            StatsRow(
                left = StatItem(uiState.tripCount.toString(), "Viatges"),
                right = StatItem(uiState.flightCount.toString(), "Vols"),
            )
            StatRowDivider()
            StatsRow(
                left = StatItem(uiState.flownDistanceKm.toCompactKm(), "Km volats"),
                right = StatItem(uiState.uniqueAirportCount.toString(), "Aeroports"),
            )
            StatRowDivider()
            StatsRow(
                left = StatItem(uiState.daysTraveled.toString(), "Dies viatjats"),
                right = StatItem(uiState.avgTripLengthDays?.let { "%.1f".format(it) } ?: "—", "Durada mitj."),
            )
        }
        }
    }
}

private data class StatItem(val value: String, val label: String)

@Composable
private fun StatsRow(left: StatItem, right: StatItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(left.value, style = MaterialTheme.typography.headlineSmall, color = AtlasOnSurfaceStrong)
            Text(left.label, style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceMuted)
        }
        Box(modifier = Modifier.width(1.dp).height(40.dp).background(AtlasOutline))
        Column(modifier = Modifier.weight(1f)) {
            Text(right.value, style = MaterialTheme.typography.headlineSmall, color = AtlasOnSurfaceStrong)
            Text(right.label, style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceMuted)
        }
    }
}

@Composable
private fun StatRowDivider() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AtlasOutline))
}

// ── Upcoming trips ────────────────────────────────────────────────────────────

@Composable
private fun UpcomingTripsSection(trips: List<DashboardTripUiState>, onSeeAll: () -> Unit, onTripClick: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AtlasSectionTitle(
            title = if (trips.size == 1) "Proper viatge" else "Propers viatges",
            action = { SeeAllLink("Tots els viatges", onSeeAll) },
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            trips.forEach { UpcomingTripCard(it, onTripClick) }
        }
    }
}

@Composable
private fun UpcomingTripCard(trip: DashboardTripUiState, onTripClick: (String) -> Unit) {
    val colors = trip.status.tripStatusColors()
    Surface(
        onClick = { onTripClick(trip.tripId) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Row(modifier = Modifier.height(132.dp)) {
            Box(
                modifier = Modifier
                    .width(112.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp))
                    .background(colors.container),
            ) {
                TripCardMap(trip.mapPoints, trip.stopCount, colors.foreground)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TripStatePill(
                        label = colors.label,
                        color = colors.foreground,
                    )
                Text(
                    text = trip.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                trip.dateText?.let { date ->
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AtlasOnSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = trip.routeText ?: "Sense parades",
                    style = MaterialTheme.typography.bodySmall,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                }
            trip.dayCount?.let { days ->
                Text(
                    text = "$days ${if (days == 1) "dia" else "dies"}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AtlasPrimary,
                )
            }
            }
        }
    }
}

// ── Upcoming flights ──────────────────────────────────────────────────────────

@Composable
private fun UpcomingFlightsSection(
    flights: List<DashboardFlightUiState>,
    onSeeAll: () -> Unit,
    onFlightClick: (String) -> Unit,
    onItineraryClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AtlasSectionTitle(
            title = if (flights.size == 1) "Proper vol" else "Propers vols",
            action = { SeeAllLink("Tots els vols", onSeeAll) },
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            flights.forEach { UpcomingFlightCard(it, onFlightClick, onItineraryClick) }
        }
    }
}

@Composable
private fun UpcomingFlightCard(
    flight: DashboardFlightUiState,
    onFlightClick: (String) -> Unit,
    onItineraryClick: (String) -> Unit,
) {
    val colors = flight.status.tripStatusColors()
    val onClick = when {
        flight.flightId != null -> { { onFlightClick(flight.flightId) } }
        flight.itineraryId != null -> { { onItineraryClick(flight.itineraryId) } }
        else -> { {} }
    }
    flight.flight?.let { sourceFlight ->
        FlightCard(
            item = FlightListItemUiState(
                flight = sourceFlight,
                originLabel = flight.originCode,
                destinationLabel = flight.destinationCode,
                originCity = flight.originCity,
                destinationCity = flight.destinationCity,
                sortKey = flight.sortKey,
            ),
            onClick = onClick,
        )
        return
    }
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: airline logo | flight number / label | status pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (flight.airlineIata != null) {
                    AirlineLogo(
                        iata = flight.airlineIata,
                        modifier = Modifier.height(34.dp).widthIn(max = 92.dp),
                    )
                } else {
                    FlightFallbackIcon(colors.foreground, colors.container)
                }
                Text(
                    text = flight.flightNumber ?: flight.label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                AtlasPill(
                    label = colors.label,
                    colors = colors,
                    contentPadding = PaddingValues(horizontal = 13.dp, vertical = 7.dp),
                )
            }

            // Route: origin IATA | line + plane | destination IATA
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 17.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = flight.originCode.takeIf { it.isNotBlank() } ?: "—",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 30.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                )
                FlightRouteArrow(color = colors.foreground, modifier = Modifier.weight(1.1f))
                Text(
                    text = flight.destinationCode.takeIf { it.isNotBlank() } ?: "—",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 30.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                )
            }

            flight.meta?.let { meta ->
                Text(
                    text = meta,
                    modifier = Modifier.padding(top = 6.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Footer: date
            Box(modifier = Modifier.fillMaxWidth().padding(top = 14.dp).height(1.dp).background(AtlasOutline))
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Filled.DateRange, null, tint = AtlasOnSurfaceMuted, modifier = Modifier.size(15.dp))
                Text(
                    text = flight.dateText ?: "Sense data",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceMuted,
                )
                flight.meta?.let { meta ->
                    Text("·", style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceMuted)
                    Text(meta, style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun FlightFallbackIcon(foreground: Color, container: Color) {
    Box(
        modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Flight, null, tint = foreground, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun FlightRouteArrow(color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(color.copy(alpha = 0.45f)))
            Box(
                modifier = Modifier.size(26.dp).clip(CircleShape).background(AtlasSurface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Flight, null, tint = color, modifier = Modifier.size(17.dp))
            }
        }
    }
}

// ── Recent trips ─────────────────────────────────────────────────────────────

@Composable
private fun RecentTripsSection(trips: List<DashboardTripUiState>, onTripClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AtlasSectionTitle("Viatges recents", modifier = Modifier.padding(horizontal = 20.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            trips.forEach { RecentTripCard(it, onTripClick) }
        }
    }
}

@Composable
private fun RecentTripCard(trip: DashboardTripUiState, onTripClick: (String) -> Unit) {
    val colors = trip.status.tripStatusColors()
    val datePillText = (trip.memoryDateText ?: trip.dateText)?.uppercase()
    Surface(
        onClick = { onTripClick(trip.tripId) },
        modifier = Modifier.width(224.dp),
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, colors.foreground.copy(alpha = 0.18f)),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(122.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(colors.container),
            ) {
                TripCardMap(trip.mapPoints, trip.stopCount, colors.foreground)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .padding(11.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        datePillText?.takeIf { it.isNotBlank() }?.let { text ->
                            Text(
                                text = text,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .widthIn(max = 124.dp)
                                    .background(AtlasSurface.copy(alpha = 0.88f), RoundedCornerShape(999.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AtlasOnSurfaceStrong,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    AtlasPill(
                        label = colors.label,
                        colors = colors,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = trip.title,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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

// ── Recent flights ────────────────────────────────────────────────────────────

@Composable
private fun RecentFlightsSection(
    flights: List<DashboardFlightUiState>,
    onFlightClick: (String) -> Unit,
    onItineraryClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AtlasSectionTitle("Vols recents", modifier = Modifier.padding(horizontal = 20.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            flights.forEach { RecentFlightCard(it, onFlightClick, onItineraryClick) }
        }
    }
}

@Composable
private fun RecentFlightCard(
    flight: DashboardFlightUiState,
    onFlightClick: (String) -> Unit,
    onItineraryClick: (String) -> Unit,
) {
    val colors = flight.status.tripStatusColors()
    val onClick = when {
        flight.flightId != null -> { { onFlightClick(flight.flightId) } }
        flight.itineraryId != null -> { { onItineraryClick(flight.itineraryId) } }
        else -> { {} }
    }
    Surface(
        onClick = onClick,
        modifier = Modifier.width(196.dp),
        shape = RoundedCornerShape(16.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Column {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (flight.airlineIata != null) {
                        AirlineLogo(
                            iata = flight.airlineIata,
                            modifier = Modifier.height(26.dp).widthIn(max = 72.dp),
                        )
                    } else {
                        FlightFallbackIcon(colors.foreground, AtlasSurface.copy(alpha = 0.64f))
                    }
                    AtlasPill(
                        label = colors.label,
                        colors = colors,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Text(
                        text = flight.originCode.takeIf { it.isNotBlank() } ?: "â€”",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasOnSurfaceStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    FlightRouteArrow(color = colors.foreground, modifier = Modifier.width(42.dp))
                    Text(
                        text = flight.destinationCode.takeIf { it.isNotBlank() } ?: "â€”",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasOnSurfaceStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                    )
                }
                Text(
                    text = flight.flightNumber ?: flight.label,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = AtlasOnSurfaceStrong,
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
}

// ── Shared map helpers ────────────────────────────────────────────────────────

@Composable
private fun TripCardMap(mapPoints: List<TripStopMapPoint>, stopCount: Int, routeColor: Color) {
    val coordinates = mapPoints.map { GeoCoordinate(it.latitude, it.longitude) }
    if (coordinates.isEmpty()) {
        TripMapTexture()
        SchematicRouteCanvas(stopCount)
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
        markers = coordinates.mapIndexed { index, coord ->
            GeoMarker(
                coordinate = coord,
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
            drawLine(AtlasNavy.copy(alpha = 0.08f), Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
            x += spacing
        }
        var y = 0f
        while (y <= size.height) {
            drawLine(AtlasNavy.copy(alpha = 0.08f), Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
            y += spacing
        }
    }
}

@Composable
private fun SchematicRouteCanvas(stopCount: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val pts = listOf(
            Offset(size.width * 0.10f, size.height * 0.62f),
            Offset(size.width * 0.32f, size.height * 0.36f),
            Offset(size.width * 0.55f, size.height * 0.48f),
            Offset(size.width * 0.74f, size.height * 0.30f),
            Offset(size.width * 0.92f, size.height * 0.52f),
        ).take(stopCount.coerceIn(2, 5))
        val path = Path().apply {
            pts.firstOrNull()?.let { moveTo(it.x, it.y) }
            pts.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(path, Color.White.copy(alpha = 0.82f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        pts.forEachIndexed { i, pt ->
            if (i == pts.lastIndex) drawCircle(Color.White, 5.dp.toPx(), pt, style = Stroke(2.dp.toPx()))
            else drawCircle(Color.White, 5.dp.toPx(), pt)
        }
    }
}

@Composable
private fun TripStatePill(label: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(color, RoundedCornerShape(999.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(modifier = Modifier.size(6.dp).background(Color.White, CircleShape))
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun SeeAllLink(label: String, onClick: () -> Unit) {
    Text(
        text = "$label →",
        modifier = Modifier.clickable(onClick = onClick),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        color = AtlasPrimary,
    )
}

private fun Double.toCompactKm(): String {
    val rounded = kotlin.math.round(this).toLong()
    return if (rounded >= 10_000) "${rounded / 1_000} k" else rounded.toString()
}

private fun Double.toHoursText(): String {
    val hours = kotlin.math.round(this).toInt()
    return if (hours >= 1000) "${hours / 1000} k h" else "$hours h"
}
