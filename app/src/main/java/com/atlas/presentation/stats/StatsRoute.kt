package com.atlas.presentation.stats

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atlas.app.AtlasApplication
import com.atlas.ui.screens.stats.StatsScreen

@Composable
fun StatsRoute(onTimelineClick: () -> Unit) {
    val app = LocalContext.current.applicationContext as AtlasApplication
    val viewModel: StatsViewModel = viewModel(
        factory = StatsViewModel.Factory(
            countryRepository = app.container.countryRepository,
            tripRepository = app.container.tripRepository,
            flightRepository = app.container.flightRepository,
            itineraryRepository = app.container.itineraryRepository,
            countryStatsScopePreferencesRepository = app.container.countryStatsScopePreferencesRepository,
            airportRepository = app.container.airportRepository,
            stopPhotoRepository = app.container.stopPhotoRepository,
            aircraftTypeRepository = app.container.aircraftTypeRepository,
            countryStateDerivationService = app.container.countryStateDerivationService,
            flexibleDateFormatter = app.container.flexibleDateFormatter,
        ),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    StatsScreen(uiState = uiState, onTimelineClick = onTimelineClick)
}
