package com.atlas.presentation.itinerary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atlas.app.AtlasApplication
import com.atlas.ui.screens.itinerary.ItineraryDetailScreen

@Composable
fun ItineraryDetailRoute(
    itineraryId: String,
    onBackClick: () -> Unit,
    onTripClick: (String) -> Unit,
    onFlightClick: (String) -> Unit,
) {
    val app = LocalContext.current.applicationContext as AtlasApplication
    val vm: ItineraryDetailViewModel = viewModel(
        key = itineraryId,
        factory = ItineraryDetailViewModel.Factory(
            itineraryRepository = app.container.itineraryRepository,
            flightRepository = app.container.flightRepository,
            tripRepository = app.container.tripRepository,
            airportRepository = app.container.airportRepository,
            airlineRepository = app.container.airlineRepository,
            searchAirportsUseCase = app.container.searchAirportsUseCase,
            searchAirlinesUseCase = app.container.searchAirlinesUseCase,
            lookupFlightUseCase = app.container.lookupFlightUseCase,
            lookupAircraftUseCase = app.container.lookupAircraftUseCase,
            updateItineraryUseCase = app.container.updateItineraryUseCase,
            deleteItineraryUseCase = app.container.deleteItineraryUseCase,
            syncGeneratedTripStopsForItineraryUseCase = app.container.syncGeneratedTripStopsForItineraryUseCase,
            removeGeneratedTripStopsForItineraryUseCase = app.container.removeGeneratedTripStopsForItineraryUseCase,
            createGroupUseCase = app.container.createItineraryGroupUseCase,
            deleteGroupUseCase = app.container.deleteItineraryGroupUseCase,
            reorderGroupsUseCase = app.container.reorderItineraryGroupsUseCase,
            createFlightUseCase = app.container.createFlightUseCase,
            updateFlightUseCase = app.container.updateFlightUseCase,
            deleteFlightUseCase = app.container.deleteFlightUseCase,
            reorderGroupFlightsUseCase = app.container.reorderGroupFlightsUseCase,
            itineraryId = itineraryId,
        ),
    )
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    ItineraryDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onTripClick = onTripClick,
        onUnlinkTripClick = vm::onUnlinkTrip,
        onAssignTripClick = vm::onAssignTripClick,
        onDismissTripPicker = vm::onDismissTripPicker,
        onLinkTrip = vm::onLinkTrip,
        onDeleteItineraryClick = vm::onDeleteItinerary,
        onAddGroupClick = vm::onAddGroupClick,
        onDeleteGroupClick = vm::onDeleteGroup,
        onToggleGroupReorderMode = vm::onToggleGroupReorderMode,
        onMoveGroupUp = vm::onMoveGroupUp,
        onMoveGroupDown = vm::onMoveGroupDown,
        onAddFlightToGroupClick = vm::onAddFlightToGroupClick,
        onAddExistingFlightToGroupClick = vm::onAddExistingFlightToGroupClick,
        onDismissSoloFlightPicker = vm::onDismissSoloFlightPicker,
        onAssignSoloFlight = vm::onAssignSoloFlight,
        onRemoveFlightFromItineraryClick = vm::onRemoveFlightFromItineraryClick,
        onDismissRemoveFlightConfirm = vm::onDismissRemoveFlightConfirm,
        onConfirmRemoveFlightFromItinerary = vm::onConfirmRemoveFlightFromItinerary,
        onMoveFlightToGroupClick = vm::onMoveFlightToGroupClick,
        onDismissMoveFlightPicker = vm::onDismissMoveFlightPicker,
        onMoveFlightToGroup = vm::onMoveFlightToGroup,
        onFlightClick = onFlightClick,
        onEditFlightClick = vm::onEditFlightClick,
        onDeleteFlightClick = vm::onDeleteFlight,
        onToggleFlightReorderMode = vm::onToggleFlightReorderMode,
        onMoveFlightUp = vm::onMoveFlightUp,
        onMoveFlightDown = vm::onMoveFlightDown,
        onDismissFlightDraft = vm::onDismissFlightDraft,
        onFlightOriginQueryChanged = vm::onFlightOriginQueryChanged,
        onFlightOriginSelected = vm::onFlightOriginSelected,
        onFlightDestinationQueryChanged = vm::onFlightDestinationQueryChanged,
        onFlightDestinationSelected = vm::onFlightDestinationSelected,
        onFlightStatusChanged = vm::onFlightStatusChanged,
        onScheduledDepartureAtChanged = vm::onScheduledDepartureAtChanged,
        onScheduledArrivalAtChanged = vm::onScheduledArrivalAtChanged,
        onActualDepartureAtChanged = vm::onActualDepartureAtChanged,
        onActualArrivalAtChanged = vm::onActualArrivalAtChanged,
        onAirlineQueryChanged = vm::onAirlineQueryChanged,
        onAirlineSelected = vm::onAirlineSelected,
        onFlightNumberChanged = vm::onFlightNumberChanged,
        onAircraftChanged = vm::onAircraftChanged,
        onAircraftRegistrationChanged = vm::onAircraftRegistrationChanged,
        onFlightNotesChanged = vm::onFlightNotesChanged,
        onApiFlightNumberChanged = vm::onApiFlightNumberChanged,
        onApiSearchDateChanged = vm::onApiSearchDateChanged,
        onSearchByFlightNumber = vm::onSearchByFlightNumber,
        onApplyApiResult = vm::onApplyApiResult,
        onManualEntryClick = vm::onManualEntryClick,
        onBackToSearch = vm::onBackToSearch,
        onSaveFlightDraft = vm::onSaveFlightDraft,
    )
}
