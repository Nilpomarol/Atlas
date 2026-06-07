package com.atlas.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.atlas.presentation.dashboard.DashboardUiState
import com.atlas.ui.components.AtlasCard
import com.atlas.ui.components.AtlasSectionTitle
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline

@Composable
internal fun WorldStatsCard(uiState: DashboardUiState, onStatsClick: () -> Unit) {
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
