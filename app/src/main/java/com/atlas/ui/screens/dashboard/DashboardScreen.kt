package com.atlas.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.remember
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.R
import com.atlas.presentation.dashboard.DashboardTripUiState
import com.atlas.presentation.dashboard.DashboardUiState
import com.atlas.ui.components.AtlasCard
import com.atlas.ui.components.AtlasPage
import com.atlas.ui.components.geo.AtlasGeoCanvas
import com.atlas.ui.components.AtlasPill
import com.atlas.ui.components.AtlasSectionTitle
import com.atlas.ui.components.AtlasSemanticColors
import com.atlas.ui.components.tripStatusColors
import com.atlas.ui.theme.AtlasLiving
import com.atlas.ui.theme.AtlasLivingContainer
import com.atlas.ui.theme.AtlasLived
import com.atlas.ui.theme.AtlasLivedContainer
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasOnSurfaceFaint
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasPlanned
import com.atlas.ui.theme.AtlasPlannedContainer
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasSurfaceRaised
import com.atlas.ui.theme.AtlasTrip
import com.atlas.ui.theme.AtlasTripContainer
import com.atlas.ui.theme.AtlasVisited
import com.atlas.ui.theme.AtlasVisitedContainer
import com.atlas.ui.theme.AtlasWished
import com.atlas.ui.theme.AtlasWishedContainer

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
            DashboardAtlasHero(uiState = uiState)
            FeaturedTripCard(trip = uiState.featuredTrip)
            CountryKpis(uiState = uiState)
            TravelKpis(uiState = uiState)
            NextUpCard(trip = uiState.upcomingTrips.firstOrNull())
            RecentCompletedSection(trips = uiState.recentCompletedTrips)
        }
    }
}

@Composable
private fun DashboardAtlasHero(uiState: DashboardUiState) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "El teu atlas",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall,
                color = AtlasOnSurfaceStrong,
            )
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.atlas_logo),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
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
                .height(150.dp),
            highlightColorByIso2 = highlightColorByIso2,
        )
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(
                text = "EL TEU MON",
                style = MaterialTheme.typography.labelSmall,
                color = AtlasOnSurfaceFaint,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = uiState.visitedCount.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = AtlasOnSurfaceStrong,
                )
                Text(
                    text = "/${uiState.trackableCountryCount}",
                    modifier = Modifier.padding(start = 2.dp, bottom = 5.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = AtlasOnSurfaceMuted,
                )
                Text(
                    text = "${uiState.visitedContinentCount}/7\nCONTINENTS",
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = AtlasOnSurfaceMuted,
                    textAlign = TextAlign.End,
                )
            }
            Text(
                text = "${uiState.visitedCount} visitats · ${uiState.livedCount} viscuts",
                style = MaterialTheme.typography.labelMedium,
                color = AtlasOnSurfaceMuted,
            )
            StateLegend()
        }
    }
}

@Composable
private fun StateLegend() {
    Row(
        modifier = Modifier.padding(top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem("Vivint", AtlasLiving)
        LegendItem("Viscut", AtlasLived)
        LegendItem("Visitat", AtlasVisited)
        LegendItem("Planificat", AtlasPlanned)
        LegendItem("Desitjat", AtlasWished)
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
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
private fun FeaturedTripCard(trip: DashboardTripUiState?) {
    if (trip == null) return
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
    ) {
        TripFeatureCard(trip = trip)
    }
}

@Composable
private fun TripFeatureCard(trip: DashboardTripUiState) {
    val colors = trip.status.tripStatusColors()
    AtlasCard(
        contentPadding = PaddingValues(0.dp),
        color = AtlasSurface,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(142.dp)
                    .background(colors.foreground.copy(alpha = 0.78f)),
            ) {
                RouteLineCanvas()
                AtlasPill(
                    label = colors.label,
                    colors = colors.copy(container = AtlasSurface.copy(alpha = 0.9f)),
                    modifier = Modifier.padding(14.dp),
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                ) {
                    Text(
                        text = trip.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = trip.dateText ?: "Sense data",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
            }
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Map,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = AtlasPrimary,
                )
                Text(
                    text = trip.routeText ?: "${trip.stopCount} parades",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${trip.stopCount} ${if (trip.stopCount == 1) "parada" else "parades"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = AtlasOnSurfaceMuted,
                )
            }
        }
    }
}

@Composable
private fun CountryKpis(uiState: DashboardUiState) {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CompactKpi(
            modifier = Modifier.weight(1f),
            value = uiState.visitedCount.toString(),
            label = "Visitats",
            color = AtlasVisited,
        )
        CompactKpi(
            modifier = Modifier.weight(1f),
            value = uiState.livedCount.toString(),
            label = "Viscuts",
            color = AtlasLived,
        )
        CompactKpi(
            modifier = Modifier.weight(1f),
            value = uiState.plannedCount.toString(),
            label = "Plans",
            color = AtlasPlanned,
        )
        CompactKpi(
            modifier = Modifier.weight(1f),
            value = uiState.wishedCount.toString(),
            label = "Desitjats",
            color = AtlasWished,
        )
    }
}

@Composable
private fun TravelKpis(uiState: DashboardUiState) {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        TravelKpi(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Work,
            value = uiState.tripCount.toString(),
            label = "Viatges",
            colors = AtlasSemanticColors(AtlasTrip, AtlasTripContainer, "Viatges"),
        )
        TravelKpi(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.AirplanemodeActive,
            value = uiState.flightCount.toString(),
            label = "Vols",
            colors = AtlasSemanticColors(AtlasPrimary, AtlasLivingContainer, "Vols"),
        )
        TravelKpi(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Flag,
            value = uiState.flownDistanceKm.toCompactKm(),
            label = "km volats",
            colors = AtlasSemanticColors(AtlasNavy, AtlasPlannedContainer, "km volats"),
        )
    }
}

@Composable
private fun CompactKpi(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    AtlasCard(
        modifier = modifier,
        contentPadding = PaddingValues(10.dp),
    ) {
        Column {
            Text(
                text = value,
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

@Composable
private fun TravelKpi(
    icon: ImageVector,
    value: String,
    label: String,
    colors: AtlasSemanticColors,
    modifier: Modifier = Modifier,
) {
    AtlasCard(
        modifier = modifier,
        contentPadding = PaddingValues(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(colors.container),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.foreground,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = AtlasOnSurfaceStrong,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = AtlasOnSurfaceMuted,
                )
            }
        }
    }
}

@Composable
private fun NextUpCard(trip: DashboardTripUiState?) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AtlasSectionTitle(title = "Proper")
        AtlasCard(
            contentPadding = PaddingValues(16.dp),
        ) {
            if (trip == null) {
                Text(
                    text = "No hi ha cap altre viatge planificat.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AtlasOnSurfaceMuted,
                )
            } else {
                NextUpRow(trip = trip)
            }
        }
    }
}

@Composable
private fun NextUpRow(trip: DashboardTripUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(AtlasSurfaceRaised),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = trip.flagText ?: "AT",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 22.sp,
                color = AtlasOnSurfaceStrong,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = trip.title,
                style = MaterialTheme.typography.titleMedium,
                color = AtlasOnSurfaceStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = trip.dateText ?: "Sense data",
                style = MaterialTheme.typography.bodySmall,
                color = AtlasOnSurfaceMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = (trip.dayCount ?: trip.stopCount.coerceAtLeast(1)).toString(),
                style = MaterialTheme.typography.titleLarge,
                color = AtlasPrimary,
            )
            Text(
                text = "DIES",
                style = MaterialTheme.typography.labelSmall,
                color = AtlasOnSurfaceMuted,
            )
        }
    }
}

@Composable
private fun RecentCompletedSection(trips: List<DashboardTripUiState>) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AtlasSectionTitle(
            title = "Records recents",
            action = {
                Text(
                    text = "Tots els viatges >",
                    style = MaterialTheme.typography.labelMedium,
                    color = AtlasPrimary,
                )
            },
        )
        if (trips.isEmpty()) {
            AtlasCard {
                Text(
                    text = "Encara no hi ha viatges completats.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AtlasOnSurfaceMuted,
                )
            }
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                trips.forEach { trip ->
                    RecentMemoryCard(trip = trip)
                }
            }
        }
    }
}

@Composable
private fun RecentMemoryCard(trip: DashboardTripUiState) {
    Column(
        modifier = Modifier.width(186.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                .background(trip.status.tripStatusColors().foreground.copy(alpha = 0.78f)),
        ) {
            RouteLineCanvas()
            Text(
                text = trip.memoryDateText.orEmpty(),
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f),
            )
            Text(
                text = trip.title,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Map,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = AtlasOnSurfaceMuted,
            )
            Text(
                text = trip.countryText ?: "Sense país",
                style = MaterialTheme.typography.bodySmall,
                color = AtlasOnSurfaceMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RouteLineCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val p1 = Offset(size.width * 0.16f, size.height * 0.62f)
        val p2 = Offset(size.width * 0.48f, size.height * 0.40f)
        val p3 = Offset(size.width * 0.84f, size.height * 0.24f)
        drawLine(Color.White.copy(alpha = 0.52f), p1, p2, strokeWidth = 2.dp.toPx())
        drawLine(Color.White.copy(alpha = 0.52f), p2, p3, strokeWidth = 2.dp.toPx())
        listOf(p1, p2, p3).forEach {
            drawCircle(Color.White, radius = 4.dp.toPx(), center = it)
        }
    }
}

private fun Double.toCompactKm(): String {
    val rounded = kotlin.math.round(this).toLong()
    return if (rounded >= 10_000) {
        "${rounded / 1_000} k"
    } else {
        rounded.toString()
    }
}
