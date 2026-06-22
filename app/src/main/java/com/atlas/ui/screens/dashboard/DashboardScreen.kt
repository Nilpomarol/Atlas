package com.atlas.ui.screens.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.atlas.R
import com.atlas.presentation.dashboard.DashboardUiState
import com.atlas.ui.components.AtlasPage
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasOnSurfaceStrong

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onSettingsClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onTimelineClick: () -> Unit = {},
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
            DashboardPageHeader(onSettingsClick = onSettingsClick)
            DashboardMapHero(uiState = uiState)
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                uiState.featuredTrip?.let { InProgressTripCard(it, onTripClick) }
                WorldStatsCard(uiState = uiState, onStatsClick = onStatsClick)
                CronologiaCard(onClick = onTimelineClick)

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

@Composable
private fun DashboardPageHeader(onSettingsClick: () -> Unit) {
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
            modifier = Modifier.size(34.dp).clip(CircleShape).clickable(onClick = onSettingsClick),
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
