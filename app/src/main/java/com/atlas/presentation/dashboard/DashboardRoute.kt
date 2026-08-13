package com.atlas.presentation.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atlas.app.AtlasApplication
import com.atlas.ui.screens.dashboard.DashboardScreen

@Composable
fun DashboardRoute(
    onSettingsClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onTimelineClick: () -> Unit = {},
    onTripsClick: () -> Unit = {},
    onFlightsClick: () -> Unit = {},
    onTripClick: (String) -> Unit = {},
    onFlightClick: (String) -> Unit = {},
    onItineraryClick: (String) -> Unit = {},
) {
    val app = LocalContext.current.applicationContext as AtlasApplication
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.Factory(
            countryRepository = app.container.countryRepository,
            tripRepository = app.container.tripRepository,
            flightRepository = app.container.flightRepository,
            itineraryRepository = app.container.itineraryRepository,
            countryStatsScopePreferencesRepository = app.container.countryStatsScopePreferencesRepository,
            airportRepository = app.container.airportRepository,
            countryStateDerivationService = app.container.countryStateDerivationService,
            flexibleDateFormatter = app.container.flexibleDateFormatter,
        ),
    )
    val uiState by viewModel.uiState.collectAsState()

    DashboardScreen(
        uiState = uiState,
        onSettingsClick = onSettingsClick,
        onStatsClick = onStatsClick,
        onTimelineClick = onTimelineClick,
        onTripsClick = onTripsClick,
        onFlightsClick = onFlightsClick,
        onTripClick = onTripClick,
        onFlightClick = onFlightClick,
        onItineraryClick = onItineraryClick,
    )
}
