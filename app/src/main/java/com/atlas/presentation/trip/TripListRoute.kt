package com.atlas.presentation.trip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atlas.app.AtlasApplication
import com.atlas.ui.screens.trip.TripListScreen

@Composable
fun TripListRoute(
    onTripClick: (String) -> Unit,
) {
    val app = LocalContext.current.applicationContext as AtlasApplication
    val viewModel: TripListViewModel = viewModel(
        factory = TripListViewModel.Factory(
            tripRepository = app.container.tripRepository,
            createTripUseCase = app.container.createTripUseCase,
            updateTripUseCase = app.container.updateTripUseCase,
            flexibleDateValidator = app.container.flexibleDateValidator,
            flexibleDateFormatter = app.container.flexibleDateFormatter,
        ),
    )
    val uiState by viewModel.uiState.collectAsState()

    TripListScreen(
        uiState = uiState,
        onTripClick = onTripClick,
        onCreateTripClick = viewModel::onCreateTripClick,
        onDismissDraft = viewModel::onDismissDraft,
        onTitleChanged = viewModel::onTitleChanged,
        onStatusChanged = viewModel::onStatusChanged,
        onDatePrecisionChanged = viewModel::onDatePrecisionChanged,
        onDateFieldChanged = viewModel::onDateFieldChanged,
        onNotesChanged = viewModel::onNotesChanged,
        onSaveDraft = viewModel::onSaveDraft,
    )
}
