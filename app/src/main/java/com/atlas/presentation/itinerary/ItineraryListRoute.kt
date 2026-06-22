package com.atlas.presentation.itinerary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atlas.app.AtlasApplication
import com.atlas.ui.screens.itinerary.ItineraryListScreen

@Composable
fun ItineraryListRoute(onItineraryClick: (String) -> Unit) {
    val app = LocalContext.current.applicationContext as AtlasApplication
    val vm: ItineraryListViewModel = viewModel(
        factory = ItineraryListViewModel.Factory(
            itineraryRepository = app.container.itineraryRepository,
            airportRepository = app.container.airportRepository,
            createItineraryUseCase = app.container.createItineraryUseCase,
            deleteItineraryUseCase = app.container.deleteItineraryUseCase,
        ),
    )
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(vm) {
        vm.navigationEvents.collect { itineraryId -> onItineraryClick(itineraryId) }
    }

    ItineraryListScreen(
        uiState = uiState,
        onItineraryClick = onItineraryClick,
        onCreateItineraryClick = vm::onCreateItineraryClick,
        onDeleteItineraryClick = vm::onDeleteItinerary,
    )
}
