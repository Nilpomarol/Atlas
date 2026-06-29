package com.atlas.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.atlas.presentation.dashboard.DashboardTripUiState
import java.io.File
import com.atlas.ui.components.AtlasSectionTitle
import com.atlas.ui.components.tripStatusColors
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasSurfaceSubtle

// ── In-progress trip card ────────────────────────────────────────────────────

@Composable
internal fun InProgressTripCard(trip: DashboardTripUiState, onTripClick: (String) -> Unit) {
    val colors = trip.status.tripStatusColors()
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        AtlasSectionTitle(title = "En curs")
        Spacer(modifier = Modifier.height(10.dp))
        if (trip.isQuickTrip()) {
            QuickDashboardTripCard(
                trip = trip,
                onTripClick = onTripClick,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
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

// ── Upcoming trips ────────────────────────────────────────────────────────────

}

@Composable
private fun QuickDashboardTripCard(
    trip: DashboardTripUiState,
    onTripClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = trip.status.tripStatusColors()
    val dateText = (trip.memoryDateText ?: trip.dateText ?: "Sense data").uppercase()
    val context = LocalContext.current

    Surface(
        onClick = { onTripClick(trip.tripId) },
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Row(modifier = Modifier.height(96.dp)) {
            Box(
                modifier = Modifier
                    .width(82.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp))
                    .background(colors.container),
            ) {
                if (trip.coverPhotoFilename != null) {
                    AsyncImage(
                        model = File(context.filesDir, "photos/${trip.coverPhotoFilename}"),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize(),
                    )
                } else {
                    TripCardMap(trip.mapPoints, trip.stopCount, colors.foreground)
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = dateText,
                        modifier = Modifier
                            .weight(1f)
                            .background(AtlasSurfaceSubtle, RoundedCornerShape(999.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AtlasOnSurfaceStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    TripStatePill(label = colors.label, color = colors.foreground)
                }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = trip.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasOnSurfaceStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = trip.quickTripLocationText(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasOnSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
internal fun UpcomingTripsSection(trips: List<DashboardTripUiState>, onSeeAll: () -> Unit, onTripClick: (String) -> Unit) {
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
    if (trip.isQuickTrip()) {
        QuickDashboardTripCard(
            trip = trip,
            onTripClick = onTripClick,
            modifier = Modifier.fillMaxWidth(),
        )
        return
    }

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

// ── Cronologia entry ──────────────────────────────────────────────────────────

@Composable
internal fun CronologiaCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AtlasPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Timeline,
                    contentDescription = null,
                    tint = AtlasPrimary,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Cronologia",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AtlasOnSurfaceStrong,
                )
                Text(
                    text = "Els teus viatges any rere any",
                    style = MaterialTheme.typography.bodySmall,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = AtlasOnSurfaceMuted,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ── Recent trips ─────────────────────────────────────────────────────────────

@Composable
internal fun RecentTripsSection(trips: List<DashboardTripUiState>, onTripClick: (String) -> Unit) {
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
    if (trip.isQuickTrip()) {
        QuickDashboardTripCard(
            trip = trip,
            onTripClick = onTripClick,
            modifier = Modifier.width(224.dp),
        )
        return
    }

    val colors = trip.status.tripStatusColors()
    val datePillText = (trip.memoryDateText ?: trip.dateText)?.uppercase()
    val context = LocalContext.current
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
                if (trip.coverPhotoFilename != null) {
                    AsyncImage(
                        model = File(context.filesDir, "photos/${trip.coverPhotoFilename}"),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize(),
                    )
                } else {
                    TripCardMap(trip.mapPoints, trip.stopCount, colors.foreground)
                }
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
                    TripStatePill(label = colors.label, color = colors.foreground)
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

private fun DashboardTripUiState.isQuickTrip(): Boolean = isQuickTrip

private fun DashboardTripUiState.quickTripLocationText(): String {
    val country = flagText?.let { flag -> "$flag ${countryText.orEmpty()}".trim() } ?: countryText
    val stopText = if (stopCount > 1) "$stopCount parades" else null
    return listOfNotNull(
        routeText?.takeIf { it.isNotBlank() },
        stopText ?: country?.takeIf { it.isNotBlank() },
    ).joinToString(" · ").ifBlank { "Sense ubicació" }
}
