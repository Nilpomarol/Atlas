package com.atlas.ui.rework.screens.trips

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atlas.app.AtlasAppContainer
import com.atlas.presentation.trip.TripListViewModel

@Composable
fun ReworkTripsRoute(
    container: AtlasAppContainer,
    onTripOpened: (String) -> Unit,
) {
    val viewModel: TripListViewModel = viewModel(
        factory = TripListViewModel.Factory(
            tripRepository = container.tripRepository,
            countryRepository = container.countryRepository,
            createTripUseCase = container.createTripUseCase,
            createTripWithFirstStopUseCase = container.createTripWithFirstStopUseCase,
            updateTripUseCase = container.updateTripUseCase,
            searchLocationsUseCase = container.searchLocationsUseCase,
            flexibleDateValidator = container.flexibleDateValidator,
            flexibleDateFormatter = container.flexibleDateFormatter,
        ),
    )
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    ReworkTripsScreen(
        state = state,
        onTripOpened = onTripOpened,
        // Creation lands with the rework trip editor; the list only routes to it.
        onCreateTrip = viewModel::onCreateTripClick,
    )
}
