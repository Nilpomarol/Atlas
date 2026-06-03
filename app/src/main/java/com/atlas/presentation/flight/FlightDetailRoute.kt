package com.atlas.presentation.flight

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atlas.app.AtlasApplication
import com.atlas.ui.screens.flight.FlightDetailScreen

@Composable
fun FlightDetailRoute(
    flightId: String,
    onBackClick: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as AtlasApplication
    val vm: FlightDetailViewModel = viewModel(
        key = flightId,
        factory = FlightDetailViewModel.Factory(
            flightId = flightId,
            flightRepository = app.container.flightRepository,
            itineraryRepository = app.container.itineraryRepository,
            airportRepository = app.container.airportRepository,
            airlineRepository = app.container.airlineRepository,
            aircraftTypeRepository = app.container.aircraftTypeRepository,
            searchAirportsUseCase = app.container.searchAirportsUseCase,
            searchAirlinesUseCase = app.container.searchAirlinesUseCase,
            lookupAircraftUseCase = app.container.lookupAircraftUseCase,
            updateFlightUseCase = app.container.updateFlightUseCase,
            deleteFlightUseCase = app.container.deleteFlightUseCase,
        ),
    )
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    FlightDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onEditClick = vm::onEditClick,
        onDeleteConfirmed = { vm.onDeleteFlight(onBackClick) },
        onDismissDraft = vm::onDismissDraft,
        onOriginQueryChanged = vm::onOriginQueryChanged,
        onOriginSelected = vm::onOriginSelected,
        onDestinationQueryChanged = vm::onDestinationQueryChanged,
        onDestinationSelected = vm::onDestinationSelected,
        onStatusChanged = vm::onStatusChanged,
        onScheduledDepartureAtChanged = vm::onScheduledDepartureAtChanged,
        onScheduledArrivalAtChanged = vm::onScheduledArrivalAtChanged,
        onActualDepartureAtChanged = vm::onActualDepartureAtChanged,
        onActualArrivalAtChanged = vm::onActualArrivalAtChanged,
        onAirlineQueryChanged = vm::onAirlineQueryChanged,
        onAirlineSelected = vm::onAirlineSelected,
        onFlightNumberChanged = vm::onFlightNumberChanged,
        onAircraftChanged = vm::onAircraftChanged,
        onAircraftRegistrationChanged = vm::onAircraftRegistrationChanged,
        onNotesChanged = vm::onNotesChanged,
        onSaveDraft = vm::onSaveDraft,
    )
}
