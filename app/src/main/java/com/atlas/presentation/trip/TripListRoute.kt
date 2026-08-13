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
            countryRepository = app.container.countryRepository,
            createTripUseCase = app.container.createTripUseCase,
            createTripWithFirstStopUseCase = app.container.createTripWithFirstStopUseCase,
            updateTripUseCase = app.container.updateTripUseCase,
            searchLocationsUseCase = app.container.searchLocationsUseCase,
            flexibleDateValidator = app.container.flexibleDateValidator,
            flexibleDateFormatter = app.container.flexibleDateFormatter,
        ),
    )
    val uiState by viewModel.uiState.collectAsState()

    TripListScreen(
        uiState = uiState,
        onTripClick = onTripClick,
        onCreateTripClick = viewModel::onCreateTripClick,
        onCreateQuickTripClick = viewModel::onCreateQuickTripClick,
        onDismissDraft = viewModel::onDismissDraft,
        onDismissQuickDraft = viewModel::onDismissQuickDraft,
        onTitleChanged = viewModel::onTitleChanged,
        onStatusChanged = viewModel::onStatusChanged,
        onDatePrecisionChanged = viewModel::onDatePrecisionChanged,
        onDateFieldChanged = viewModel::onDateFieldChanged,
        onNotesChanged = viewModel::onNotesChanged,
        onSaveDraft = viewModel::onSaveDraft,
        onQuickTitleChanged = viewModel::onQuickTitleChanged,
        onQuickStatusChanged = viewModel::onQuickStatusChanged,
        onQuickDatePrecisionChanged = viewModel::onQuickDatePrecisionChanged,
        onQuickDateFieldChanged = viewModel::onQuickDateFieldChanged,
        onQuickLocationSearchQueryChanged = viewModel::onQuickLocationSearchQueryChanged,
        onQuickLocationSearchResultSelected = viewModel::onQuickLocationSearchResultSelected,
        onUseManualQuickLocationClick = viewModel::onUseManualQuickLocationClick,
        onQuickLocationNameChanged = viewModel::onQuickLocationNameChanged,
        onQuickCountryChanged = viewModel::onQuickCountryChanged,
        onQuickLatitudeChanged = viewModel::onQuickLatitudeChanged,
        onQuickLongitudeChanged = viewModel::onQuickLongitudeChanged,
        onSaveQuickDraft = viewModel::onSaveQuickDraft,
    )
}
