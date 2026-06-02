package com.atlas.presentation.itinerary

import androidx.compose.runtime.Composable
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
            createItineraryUseCase = app.container.createItineraryUseCase,
            updateItineraryUseCase = app.container.updateItineraryUseCase,
            deleteItineraryUseCase = app.container.deleteItineraryUseCase,
        ),
    )
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    ItineraryListScreen(
        uiState = uiState,
        onItineraryClick = onItineraryClick,
        onCreateItineraryClick = vm::onCreateItineraryClick,
        onEditItineraryClick = vm::onEditItineraryClick,
        onDeleteItineraryClick = vm::onDeleteItinerary,
        onDismissDraft = vm::onDismissDraft,
        onTitleChanged = vm::onTitleChanged,
        onNotesChanged = vm::onNotesChanged,
        onSaveDraft = vm::onSaveDraft,
    )
}
