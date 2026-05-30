package com.atlas.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.atlas.presentation.dashboard.DashboardRecentItemUiState
import com.atlas.presentation.dashboard.DashboardTripUiState
import com.atlas.presentation.dashboard.DashboardUiState
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasLived
import com.atlas.ui.theme.AtlasLivedContainer
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasPlanned
import com.atlas.ui.theme.AtlasPlannedContainer
import com.atlas.ui.theme.AtlasSurface
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AtlasBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DashboardHero(uiState = uiState)
        StatsGrid(uiState = uiState)
        UpcomingTripCard(trip = uiState.upcomingTrip)
        RecentActivityCard(items = uiState.recentItems)
    }
}

@Composable
private fun DashboardHero(
    uiState: DashboardUiState,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            AtlasVisited,
                            AtlasTrip,
                        ),
                    ),
                )
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "El teu atlas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                )
                Text(
                    text = if (uiState.trackableCountryCount == 0) {
                        "Preparant el teu atlas"
                    } else {
                        "${uiState.visitedCount} de ${uiState.trackableCountryCount} llocs visitats"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.92f),
                )
                Text(
                    text = if (uiState.trackableCountryCount == 0) {
                        "El dataset inicial es carregarà automàticament en un moment."
                    } else {
                        "Continua marcant països, registres i viatges per veure el resum créixer."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.82f),
                )
            }
        }
    }
}

@Composable
private fun StatsGrid(
    uiState: DashboardUiState,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Visitats",
                value = uiState.visitedCount.toString(),
                icon = Icons.Filled.Public,
                color = AtlasVisited,
                containerColor = AtlasVisitedContainer,
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Desitjats",
                value = uiState.wishedCount.toString(),
                icon = Icons.Filled.Favorite,
                color = AtlasWished,
                containerColor = AtlasWishedContainer,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Planificats",
                value = uiState.plannedCount.toString(),
                icon = Icons.Filled.Flag,
                color = AtlasPlanned,
                containerColor = AtlasPlannedContainer,
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Viscuts",
                value = uiState.livedCount.toString(),
                icon = Icons.Filled.Home,
                color = AtlasLived,
                containerColor = AtlasLivedContainer,
            )
        }
        StatCard(
            modifier = Modifier.fillMaxWidth(),
            label = "Viatges",
            value = uiState.tripCount.toString(),
            icon = Icons.Filled.Work,
            color = AtlasTrip,
            containerColor = AtlasTripContainer,
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AtlasSurface),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = containerColor,
                        shape = RoundedCornerShape(10.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                )
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = AtlasOnSurfaceStrong,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceMuted,
                )
            }
        }
    }
}

@Composable
private fun UpcomingTripCard(
    trip: DashboardTripUiState?,
) {
    SectionCard(
        title = "Proper viatge",
        emptyText = "Encara no tens cap viatge planificat.",
        isEmpty = trip == null,
    ) {
        if (trip != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = AtlasTripContainer,
                            shape = RoundedCornerShape(10.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = null,
                        tint = AtlasTrip,
                    )
                }
                Column {
                    Text(
                        text = trip.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = AtlasOnSurfaceStrong,
                    )
                    Text(
                        text = trip.dateText ?: "Sense data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AtlasOnSurfaceMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentActivityCard(
    items: List<DashboardRecentItemUiState>,
) {
    SectionCard(
        title = "Activitat recent",
        emptyText = "Afegeix registres o viatges per omplir aquest resum.",
        isEmpty = items.isEmpty(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.forEach { item ->
                RecentActivityRow(item = item)
            }
        }
    }
}

@Composable
private fun RecentActivityRow(
    item: DashboardRecentItemUiState,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = AtlasPlannedContainer,
                    shape = RoundedCornerShape(9.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Flag,
                contentDescription = null,
                tint = AtlasPlanned,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceStrong,
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = AtlasOnSurfaceMuted,
            )
        }
        Text(
            text = item.dateText ?: "",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = AtlasOnSurfaceMuted,
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    emptyText: String,
    isEmpty: Boolean,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AtlasSurface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceStrong,
            )
            if (isEmpty) {
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AtlasOnSurfaceMuted,
                )
            } else {
                content()
            }
        }
    }
}
