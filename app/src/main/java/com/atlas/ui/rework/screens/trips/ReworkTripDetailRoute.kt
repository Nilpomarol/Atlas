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
        // Stop editing arrives with the next slice; the row is already the tap target.
        onStopOpened = {},
        onStoryOpened = { onStoryOpened(tripId) },
    )
}
