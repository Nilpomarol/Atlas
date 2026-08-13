package com.atlas.ui.rework.screens.trips

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atlas.app.AtlasAppContainer
import com.atlas.presentation.trip.TripDetailViewModel

@Composable
fun ReworkTripDetailRoute(
    container: AtlasAppContainer,
    tripId: String,
    onBack: () -> Unit,
    onStoryOpened: (String) -> Unit,
) {
    val viewModel: TripDetailViewModel = viewModel(
        key = "rework-trip-detail-$tripId",
        factory = TripDetailViewModel.Factory(
            tripRepository = container.tripRepository,
            countryRepository = container.countryRepository,
            itineraryRepository = container.itineraryRepository,
            airportRepository = container.airportRepository,
            deleteTripUseCase = container.deleteTripUseCase,
            updateTripUseCase = container.updateTripUseCase,
            updateItineraryUseCase = container.updateItineraryUseCase,
            syncGeneratedTripStopsForItineraryUseCase = container.syncGeneratedTripStopsForItineraryUseCase,
            removeGeneratedTripStopsForItineraryUseCase = container.removeGeneratedTripStopsForItineraryUseCase,
            createTripStopUseCase = container.createTripStopUseCase,
            updateTripStopUseCase = container.updateTripStopUseCase,
            reorderTripStopsUseCase = container.reorderTripStopsUseCase,
            deleteTripStopUseCase = container.deleteTripStopUseCase,
            searchLocationsUseCase = container.searchLocationsUseCase,
            flexibleDateValidator = container.flexibleDateValidator,
            tripMapPreferencesRepository = container.tripMapPreferencesRepository,
            stopPhotoRepository = container.stopPhotoRepository,
            addStopPhotosUseCase = container.addStopPhotosUseCase,
            deleteStopPhotoUseCase = container.deleteStopPhotoUseCase,
            rotateStopPhotoUseCase = container.rotateStopPhotoUseCase,
            setTripCoverPhotoUseCase = container.setTripCoverPhotoUseCase,
            tripId = tripId,
        ),
    )
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    ReworkTripDetailScreen(
        state = state,
        onBack = onBack,
        onStopOpened = { stopId ->
            state.stops.firstOrNull { it.id == stopId }?.let(viewModel::onEditStop)
        },
        onAddStop = viewModel::onAddStopClick,
        onAddSideTrip = viewModel::onAddSideTripClick,
        onDeleteStop = viewModel::onDeleteStop,
        onStoryOpened = { onStoryOpened(tripId) },
    )

    ReworkStopEditorSheet(
        draft = state.stopDraft,
        countries = state.countries,
        onDismiss = viewModel::onDismissStopDraft,
        onLocationNameChanged = viewModel::onStopLocationNameChanged,
        onCountryChanged = viewModel::onStopCountryChanged,
        onSideTripLabelChanged = viewModel::onSideTripLabelChanged,
        onDatePrecisionChanged = viewModel::onStopDatePrecisionChanged,
        onDateFieldChanged = viewModel::onStopDateFieldChanged,
        onNotesChanged = viewModel::onStopNotesChanged,
        onSave = viewModel::onSaveStopDraft,
    )
}
