package com.atlas.presentation.timeline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atlas.app.AtlasApplication
import com.atlas.ui.screens.timeline.TimelineScreen

@Composable
fun TimelineRoute(
    onBackClick: () -> Unit,
    onTripClick: (String) -> Unit,
    onItineraryClick: (String) -> Unit,
) {
    val app = LocalContext.current.applicationContext as AtlasApplication
    val viewModel: TimelineViewModel = viewModel(
        factory = TimelineViewModel.Factory(
            tripRepository = app.container.tripRepository,
            itineraryRepository = app.container.itineraryRepository,
            countryRepository = app.container.countryRepository,
            airportRepository = app.container.airportRepository,
            flexibleDateFormatter = app.container.flexibleDateFormatter,
        ),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TimelineScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onTripClick = onTripClick,
        onItineraryClick = onItineraryClick,
    )
}
