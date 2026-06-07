package com.atlas.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Flight
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.presentation.dashboard.DashboardFlightUiState
import com.atlas.presentation.flight.FlightListItemUiState
import com.atlas.ui.components.AirlineLogo
import com.atlas.ui.components.AtlasPill
import com.atlas.ui.components.AtlasSectionTitle
import com.atlas.ui.components.FlightCard
import com.atlas.ui.components.tripStatusColors
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasSurface

// ── Upcoming flights ──────────────────────────────────────────────────────────

@Composable
internal fun UpcomingFlightsSection(
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

// ── Recent flights ────────────────────────────────────────────────────────────

@Composable
internal fun RecentFlightsSection(
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
                        text = flight.originCode.takeIf { it.isNotBlank() } ?: "—",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasOnSurfaceStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    FlightRouteArrow(color = colors.foreground, modifier = Modifier.width(42.dp))
                    Text(
                        text = flight.destinationCode.takeIf { it.isNotBlank() } ?: "—",
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

// ── Flight card helpers ───────────────────────────────────────────────────────

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
