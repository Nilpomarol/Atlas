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
            tripRepository = app.container.tripRepository,
            airportRepository = app.container.airportRepository,
            searchAirportsUseCase = app.container.searchAirportsUseCase,
            updateItineraryUseCase = app.container.updateItineraryUseCase,
            deleteItineraryUseCase = app.container.deleteItineraryUseCase,
            syncGeneratedTripStopsForItineraryUseCase = app.container.syncGeneratedTripStopsForItineraryUseCase,
            removeGeneratedTripStopsForItineraryUseCase = app.container.removeGeneratedTripStopsForItineraryUseCase,
            createGroupUseCase = app.container.createItineraryGroupUseCase,
            updateGroupUseCase = app.container.updateItineraryGroupUseCase,
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
        onEditItineraryClick = vm::onEditItineraryClick,
        onDeleteItineraryClick = vm::onDeleteItinerary,
        onDismissItineraryDraft = vm::onDismissItineraryDraft,
        onItineraryTitleChanged = vm::onItineraryTitleChanged,
        onItineraryNotesChanged = vm::onItineraryNotesChanged,
        onSaveItineraryDraft = vm::onSaveItineraryDraft,
        onAddGroupClick = vm::onAddGroupClick,
        onEditGroupClick = vm::onEditGroupClick,
        onDeleteGroupClick = vm::onDeleteGroup,
        onDismissGroupDraft = vm::onDismissGroupDraft,
        onGroupTitleChanged = vm::onGroupTitleChanged,
        onGroupStatusChanged = vm::onGroupStatusChanged,
        onSaveGroupDraft = vm::onSaveGroupDraft,
        onToggleGroupReorderMode = vm::onToggleGroupReorderMode,
        onMoveGroupUp = vm::onMoveGroupUp,
        onMoveGroupDown = vm::onMoveGroupDown,
        onAddFlightToGroupClick = vm::onAddFlightToGroupClick,
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
        onAirlineChanged = vm::onAirlineChanged,
        onFlightNumberChanged = vm::onFlightNumberChanged,
        onAircraftChanged = vm::onAircraftChanged,
        onFlightNotesChanged = vm::onFlightNotesChanged,
        onSaveFlightDraft = vm::onSaveFlightDraft,
    )
}
