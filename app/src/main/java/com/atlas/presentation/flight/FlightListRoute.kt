package com.atlas.presentation.flight

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atlas.app.AtlasApplication
import com.atlas.ui.screens.flight.FlightListScreen

@Composable
fun FlightListRoute(
    onFlightClick: (String) -> Unit,
    onItineraryClick: (String) -> Unit,
) {
    val app = LocalContext.current.applicationContext as AtlasApplication
    val vm: FlightListViewModel = viewModel(
        factory = FlightListViewModel.Factory(
            flightRepository = app.container.flightRepository,
            itineraryRepository = app.container.itineraryRepository,
            tripRepository = app.container.tripRepository,
            airportRepository = app.container.airportRepository,
            airlineRepository = app.container.airlineRepository,
            searchAirportsUseCase = app.container.searchAirportsUseCase,
            searchAirlinesUseCase = app.container.searchAirlinesUseCase,
            createFlightUseCase = app.container.createFlightUseCase,
            updateFlightUseCase = app.container.updateFlightUseCase,
            deleteFlightUseCase = app.container.deleteFlightUseCase,
            lookupFlightUseCase = app.container.lookupFlightUseCase,
            lookupAircraftUseCase = app.container.lookupAircraftUseCase,
            createItineraryUseCase = app.container.createItineraryUseCase,
        ),
    )
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(vm) {
        vm.navigationEvents.collect { itineraryId -> onItineraryClick(itineraryId) }
    }

    FlightListScreen(
        uiState = uiState,
        onFlightClick = onFlightClick,
        onItineraryClick = onItineraryClick,
        onCreateFlightClick = vm::onCreateFlightClick,
        onCreateItineraryClick = vm::onCreateItineraryClick,
        onEditFlightClick = vm::onEditFlightClick,
        onDeleteFlightClick = vm::onDeleteFlight,
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
        onApiFlightNumberChanged = vm::onApiFlightNumberChanged,
        onApiSearchDateChanged = vm::onApiSearchDateChanged,
        onSearchByFlightNumber = vm::onSearchByFlightNumber,
        onApplyApiResult = vm::onApplyApiResult,
        onManualEntryClick = vm::onManualEntryClick,
        onBackToSearch = vm::onBackToSearch,
        onSaveDraft = vm::onSaveDraft,
    )
}
