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
    onItineraryClick: (String) -> Unit,
) {
    val app = LocalContext.current.applicationContext as AtlasApplication
    val viewModel: TripDetailViewModel = viewModel(
        key = "trip-detail-$tripId",
        factory = TripDetailViewModel.Factory(
            tripRepository = app.container.tripRepository,
            countryRepository = app.container.countryRepository,
            itineraryRepository = app.container.itineraryRepository,
            excursionRepository = app.container.excursionRepository,
            deleteTripUseCase = app.container.deleteTripUseCase,
            updateTripUseCase = app.container.updateTripUseCase,
            updateItineraryUseCase = app.container.updateItineraryUseCase,
            syncGeneratedTripStopsForItineraryUseCase = app.container.syncGeneratedTripStopsForItineraryUseCase,
            removeGeneratedTripStopsForItineraryUseCase = app.container.removeGeneratedTripStopsForItineraryUseCase,
            createTripStopUseCase = app.container.createTripStopUseCase,
            updateTripStopUseCase = app.container.updateTripStopUseCase,
            reorderTripStopsUseCase = app.container.reorderTripStopsUseCase,
            deleteTripStopUseCase = app.container.deleteTripStopUseCase,
            searchLocationsUseCase = app.container.searchLocationsUseCase,
            createExcursionUseCase = app.container.createExcursionUseCase,
            updateExcursionUseCase = app.container.updateExcursionUseCase,
            deleteExcursionUseCase = app.container.deleteExcursionUseCase,
            reorderExcursionsUseCase = app.container.reorderExcursionsUseCase,
            createExcursionStopUseCase = app.container.createExcursionStopUseCase,
            updateExcursionStopUseCase = app.container.updateExcursionStopUseCase,
            deleteExcursionStopUseCase = app.container.deleteExcursionStopUseCase,
            reorderExcursionStopsUseCase = app.container.reorderExcursionStopsUseCase,
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
        onItineraryClick = onItineraryClick,
        onOpenItineraryPicker = viewModel::onOpenItineraryPicker,
        onDismissItineraryPicker = viewModel::onDismissItineraryPicker,
        onLinkItinerary = viewModel::onLinkItinerary,
        onUnlinkItinerary = viewModel::onUnlinkItinerary,
        onAddStopClick = viewModel::onAddStopClick,
        onDismissStopDraft = viewModel::onDismissStopDraft,
        onStopLocationNameChanged = viewModel::onStopLocationNameChanged,
        onLocationSearchQueryChanged = viewModel::onLocationSearchQueryChanged,
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
        onAddExcursionClick = viewModel::onAddExcursionClick,
        onEditExcursion = viewModel::onEditExcursion,
        onDeleteExcursion = viewModel::onDeleteExcursion,
        onMoveExcursionUp = viewModel::onMoveExcursionUp,
        onMoveExcursionDown = viewModel::onMoveExcursionDown,
        onDismissExcursionDraft = viewModel::onDismissExcursionDraft,
        onExcursionTitleChanged = viewModel::onExcursionTitleChanged,
        onExcursionAnchorChanged = viewModel::onExcursionAnchorChanged,
        onExcursionNotesChanged = viewModel::onExcursionNotesChanged,
        onSaveExcursionDraft = viewModel::onSaveExcursionDraft,
        onAddExcursionStopClick = viewModel::onAddExcursionStopClick,
        onEditExcursionStop = viewModel::onEditExcursionStop,
        onDeleteExcursionStop = viewModel::onDeleteExcursionStop,
        onMoveExcursionStopUp = viewModel::onMoveExcursionStopUp,
        onMoveExcursionStopDown = viewModel::onMoveExcursionStopDown,
        onDismissExcursionStopDraft = viewModel::onDismissExcursionStopDraft,
        onExcursionStopLocationSearchQueryChanged = viewModel::onExcursionStopLocationSearchQueryChanged,
        onExcursionStopLocationSearchResultSelected = viewModel::onExcursionStopLocationSearchResultSelected,
        onUseManualExcursionStopEntryClick = viewModel::onUseManualExcursionStopEntryClick,
        onExcursionStopLocationNameChanged = viewModel::onExcursionStopLocationNameChanged,
        onExcursionStopCountryChanged = viewModel::onExcursionStopCountryChanged,
        onExcursionStopLatitudeChanged = viewModel::onExcursionStopLatitudeChanged,
        onExcursionStopLongitudeChanged = viewModel::onExcursionStopLongitudeChanged,
        onExcursionStopDatePrecisionChanged = viewModel::onExcursionStopDatePrecisionChanged,
        onExcursionStopDateFieldChanged = viewModel::onExcursionStopDateFieldChanged,
        onExcursionStopNotesChanged = viewModel::onExcursionStopNotesChanged,
        onSaveExcursionStopDraft = viewModel::onSaveExcursionStopDraft,
    )
}
