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
    onStoryClick: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as AtlasApplication
    val viewModel: TripDetailViewModel = viewModel(
        key = "trip-detail-$tripId",
        factory = TripDetailViewModel.Factory(
            tripRepository = app.container.tripRepository,
            countryRepository = app.container.countryRepository,
            itineraryRepository = app.container.itineraryRepository,
            airportRepository = app.container.airportRepository,
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
            flexibleDateValidator = app.container.flexibleDateValidator,
            tripMapPreferencesRepository = app.container.tripMapPreferencesRepository,
            stopPhotoRepository = app.container.stopPhotoRepository,
            addStopPhotosUseCase = app.container.addStopPhotosUseCase,
            deleteStopPhotoUseCase = app.container.deleteStopPhotoUseCase,
            rotateStopPhotoUseCase = app.container.rotateStopPhotoUseCase,
            setTripCoverPhotoUseCase = app.container.setTripCoverPhotoUseCase,
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
        onStoryClick = onStoryClick,
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
        onShowGeneratedStopsOnMapChanged = viewModel::onGeneratedStopsVisibleOnMapChanged,
        onEditStop = viewModel::onEditStop,
        onMoveStopUp = viewModel::onMoveStopUp,
        onMoveStopDown = viewModel::onMoveStopDown,
        onDeleteStop = viewModel::onDeleteStop,
        onAddPhotos = viewModel::onAddPhotos,
        onDeletePhoto = viewModel::onDeletePhoto,
        onRotatePhoto = viewModel::onRotatePhoto,
        onSetCoverPhoto = viewModel::onSetCoverPhoto,
    )
}
