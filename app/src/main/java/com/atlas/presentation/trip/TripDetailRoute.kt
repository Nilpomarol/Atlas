package com.atlas.presentation.trip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atlas.app.AtlasApplication
import com.atlas.ui.screens.trip.TripDetailScreen

@Composable
fun TripDetailRoute(
    tripId: String,
    onBackClick: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as AtlasApplication
    val viewModel: TripDetailViewModel = viewModel(
        key = "trip-detail-$tripId",
        factory = TripDetailViewModel.Factory(
            tripRepository = app.container.tripRepository,
            countryRepository = app.container.countryRepository,
            deleteTripUseCase = app.container.deleteTripUseCase,
            updateTripUseCase = app.container.updateTripUseCase,
            createTripStopUseCase = app.container.createTripStopUseCase,
            updateTripStopUseCase = app.container.updateTripStopUseCase,
            reorderTripStopsUseCase = app.container.reorderTripStopsUseCase,
            deleteTripStopUseCase = app.container.deleteTripStopUseCase,
            searchLocationsUseCase = app.container.searchLocationsUseCase,
            flexibleDateValidator = app.container.flexibleDateValidator,
            tripId = tripId,
        ),
    )
    val uiState by viewModel.uiState.collectAsState()

    TripDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onDeleteTrip = {
            viewModel.onDeleteTrip()
            onBackClick()
        },
        onEditTripClick = viewModel::onEditTripClick,
        onDismissTripDraft = viewModel::onDismissTripDraft,
        onTripTitleChanged = viewModel::onTripTitleChanged,
        onTripStatusChanged = viewModel::onTripStatusChanged,
        onTripDatePrecisionChanged = viewModel::onTripDatePrecisionChanged,
        onTripDateFieldChanged = viewModel::onTripDateFieldChanged,
        onTripNotesChanged = viewModel::onTripNotesChanged,
        onSaveTripDraft = viewModel::onSaveTripDraft,
        onAddStopClick = viewModel::onAddStopClick,
        onDismissStopDraft = viewModel::onDismissStopDraft,
        onStopLocationNameChanged = viewModel::onStopLocationNameChanged,
        onLocationSearchQueryChanged = viewModel::onLocationSearchQueryChanged,
        onSearchLocationClick = viewModel::onSearchLocationClick,
        onLocationSearchResultSelected = viewModel::onLocationSearchResultSelected,
        onUseManualStopEntryClick = viewModel::onUseManualStopEntryClick,
        onStopCountryChanged = viewModel::onStopCountryChanged,
        onStopLatitudeChanged = viewModel::onStopLatitudeChanged,
        onStopLongitudeChanged = viewModel::onStopLongitudeChanged,
        onStopDatePrecisionChanged = viewModel::onStopDatePrecisionChanged,
        onStopDateFieldChanged = viewModel::onStopDateFieldChanged,
        onStopNotesChanged = viewModel::onStopNotesChanged,
        onSaveStopDraft = viewModel::onSaveStopDraft,
        onEditStop = viewModel::onEditStop,
        onMoveStopUp = viewModel::onMoveStopUp,
        onMoveStopDown = viewModel::onMoveStopDown,
        onDeleteStop = viewModel::onDeleteStop,
    )
}
