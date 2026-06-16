package com.atlas.presentation.trip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atlas.app.AtlasApplication
import com.atlas.ui.screens.trip.TripStoryScreen

@Composable
fun TripStoryRoute(
    tripId: String,
    onBackClick: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as AtlasApplication
    val viewModel: TripStoryViewModel = viewModel(
        key = "trip-story-$tripId",
        factory = TripStoryViewModel.Factory(
            tripRepository = app.container.tripRepository,
            countryRepository = app.container.countryRepository,
            excursionRepository = app.container.excursionRepository,
            itineraryRepository = app.container.itineraryRepository,
            stopPhotoRepository = app.container.stopPhotoRepository,
            tripId = tripId,
        ),
    )
    val uiState by viewModel.uiState.collectAsState()

    TripStoryScreen(
        uiState = uiState,
        onBackClick = onBackClick,
    )
}
