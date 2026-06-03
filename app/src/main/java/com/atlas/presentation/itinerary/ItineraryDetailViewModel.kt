package com.atlas.presentation.itinerary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atlas.domain.model.Airline
import com.atlas.domain.model.Airport
import com.atlas.domain.model.Flight
import com.atlas.domain.model.Itinerary
import com.atlas.domain.model.ItineraryGroup
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import com.atlas.domain.repository.AirlineRepository
import com.atlas.domain.repository.AirportRepository
import com.atlas.domain.repository.ItineraryRepository
import com.atlas.domain.repository.TripRepository
import com.atlas.domain.usecase.airline.SearchAirlinesUseCase
import com.atlas.domain.usecase.airport.SearchAirportsUseCase
import com.atlas.domain.usecase.flight.CreateFlightUseCase
import com.atlas.domain.usecase.flight.DeleteFlightUseCase
import com.atlas.domain.usecase.flight.UpdateFlightUseCase
import com.atlas.domain.usecase.itinerary.CreateItineraryGroupUseCase
import com.atlas.domain.usecase.itinerary.DeleteItineraryGroupUseCase
import com.atlas.domain.usecase.itinerary.DeleteItineraryUseCase
import com.atlas.domain.usecase.itinerary.ReorderGroupFlightsUseCase
import com.atlas.domain.usecase.itinerary.ReorderItineraryGroupsUseCase
import com.atlas.domain.usecase.itinerary.RemoveGeneratedTripStopsForItineraryUseCase
import com.atlas.domain.usecase.itinerary.SyncGeneratedTripStopsForItineraryUseCase
import com.atlas.domain.usecase.itinerary.UpdateItineraryGroupUseCase
import com.atlas.domain.usecase.itinerary.UpdateItineraryUseCase
import com.atlas.presentation.flight.FlightEditorDraftUiState
import com.atlas.presentation.flight.displayLabel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class ItineraryDetailViewModel(
    itineraryRepository: ItineraryRepository,
    tripRepository: TripRepository,
    private val airportRepository: AirportRepository,
    private val airlineRepository: AirlineRepository,
    private val searchAirportsUseCase: SearchAirportsUseCase,
    private val searchAirlinesUseCase: SearchAirlinesUseCase,
    private val updateItineraryUseCase: UpdateItineraryUseCase,
    private val deleteItineraryUseCase: DeleteItineraryUseCase,
    private val syncGeneratedTripStopsForItineraryUseCase: SyncGeneratedTripStopsForItineraryUseCase,
    private val removeGeneratedTripStopsForItineraryUseCase: RemoveGeneratedTripStopsForItineraryUseCase,
    private val createGroupUseCase: CreateItineraryGroupUseCase,
    private val updateGroupUseCase: UpdateItineraryGroupUseCase,
    private val deleteGroupUseCase: DeleteItineraryGroupUseCase,
    private val reorderGroupsUseCase: ReorderItineraryGroupsUseCase,
    private val createFlightUseCase: CreateFlightUseCase,
    private val updateFlightUseCase: UpdateFlightUseCase,
    private val deleteFlightUseCase: DeleteFlightUseCase,
    private val reorderGroupFlightsUseCase: ReorderGroupFlightsUseCase,
    private val itineraryId: String,
) : ViewModel() {

    private val itineraryDraft = MutableStateFlow(ItineraryEditorDraft())
    private val groupDraft = MutableStateFlow(GroupEditorDraft())
    private val flightDraft = MutableStateFlow(FlightEditorDraftUiState())
    private val targetGroupId = MutableStateFlow<String?>(null)
    private val originQuery = MutableStateFlow("")
    private val destinationQuery = MutableStateFlow("")
    private val airlineQuery = MutableStateFlow("")
    private val isGroupReorderMode = MutableStateFlow(false)
    private val reorderingFlightsGroupId = MutableStateFlow<String?>(null)

    private val originResults: StateFlow<List<Airport>> = originQuery
        .debounce(300)
        .flatMapLatest { q -> if (q.isBlank()) flowOf(emptyList()) else searchAirportsUseCase(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val destinationResults: StateFlow<List<Airport>> = destinationQuery
        .debounce(300)
        .flatMapLatest { q -> if (q.isBlank()) flowOf(emptyList()) else searchAirportsUseCase(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Intermediate combines to stay within the 5-flow typed combine limit
    private val contentState = combine(
        itineraryRepository.observeItinerary(itineraryId),
        itineraryRepository.observeGroups(itineraryId),
        tripRepository.observeTrips(),
        airportRepository.observeAirports(),
    ) { itinerary, groups, trips, airports ->
        ItineraryDetailContentState(
            itinerary = itinerary,
            groups = groups,
            linkedTrip = itinerary?.tripId?.let { tripId -> trips.firstOrNull { it.id == tripId } },
            airports = airports,
        )
    }

    private val draftState = combine(
        itineraryDraft,
        groupDraft,
        flightDraft,
    ) { itinDraft, grpDraft, fltDraft -> Triple(itinDraft, grpDraft, fltDraft) }

    private val airlineResults: StateFlow<List<Airline>> = airlineQuery
        .debounce(300)
        .flatMapLatest { q -> if (q.isBlank()) flowOf(emptyList()) else searchAirlinesUseCase(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val searchState = combine(
        originResults,
        destinationResults,
        airlineResults,
    ) { origin, destination, airline -> Triple(origin, destination, airline) }

    private val reorderState = combine(
        isGroupReorderMode,
        reorderingFlightsGroupId,
    ) { groupReorder, flightReorderGroupId -> groupReorder to flightReorderGroupId }

    val uiState: StateFlow<ItineraryDetailUiState> = combine(
        contentState,
        draftState,
        searchState,
        reorderState,
    ) { content, drafts, searches, reorders ->
        val (itinDraft, grpDraft, fltDraft) = drafts
        val (originRes, destRes, airlineRes) = searches
        val (groupReorder, flightReorderGroupId) = reorders
        ItineraryDetailUiState(
            itinerary = content.itinerary,
            linkedTrip = content.linkedTrip,
            groups = content.groups,
            airports = content.airports,
            itineraryDraft = itinDraft,
            groupDraft = grpDraft,
            flightDraft = fltDraft,
            originSearchResults = originRes,
            destinationSearchResults = destRes,
            airlineSearchResults = airlineRes,
            isGroupReorderMode = groupReorder,
            reorderingFlightsGroupId = flightReorderGroupId,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ItineraryDetailUiState(),
    )

    // --- Itinerary editing ---

    fun onEditItineraryClick() {
        val itin = uiState.value.itinerary ?: return
        itineraryDraft.update {
            ItineraryEditorDraft(
                isOpen = true,
                itineraryId = itin.id,
                title = itin.title,
                notes = itin.notes ?: "",
            )
        }
    }

    fun onDismissItineraryDraft() { itineraryDraft.update { ItineraryEditorDraft() } }

    fun onItineraryTitleChanged(title: String) {
        itineraryDraft.update { it.copy(title = title, validationError = null) }
    }

    fun onItineraryNotesChanged(notes: String) {
        itineraryDraft.update { it.copy(notes = notes) }
    }

    fun onSaveItineraryDraft() {
        val d = itineraryDraft.value
        val title = d.title.trim()
        if (title.isBlank()) {
            itineraryDraft.update { it.copy(validationError = "El títol és obligatori.") }
            return
        }
        val itin = uiState.value.itinerary ?: return
        viewModelScope.launch {
            updateItineraryUseCase(
                Itinerary(
                    id = itin.id,
                    title = title,
                    tripId = itin.tripId,
                    notes = d.notes.trim().ifBlank { null },
                ),
            )
            onDismissItineraryDraft()
        }
    }

    fun onDeleteItinerary() {
        val itin = uiState.value.itinerary ?: return
        viewModelScope.launch { deleteItineraryUseCase(itin) }
    }

    fun onUnlinkTrip() {
        val itin = uiState.value.itinerary ?: return
        viewModelScope.launch {
            removeGeneratedTripStopsForItineraryUseCase(itin.id)
            updateItineraryUseCase(itin.copy(tripId = null))
        }
    }

    // --- Group editing ---

    fun onAddGroupClick() {
        groupDraft.update {
            GroupEditorDraft(
                isOpen = true,
                itineraryId = itineraryId,
                sortOrder = uiState.value.groups.size,
            )
        }
    }

    fun onEditGroupClick(group: ItineraryGroup) {
        groupDraft.update {
            GroupEditorDraft(
                isOpen = true,
                groupId = group.id,
                itineraryId = group.itineraryId,
                title = group.title ?: "",
                status = group.status,
                sortOrder = group.sortOrder,
            )
        }
    }

    fun onDismissGroupDraft() { groupDraft.update { GroupEditorDraft() } }

    fun onGroupTitleChanged(title: String) { groupDraft.update { it.copy(title = title) } }

    fun onGroupStatusChanged(status: TravelStatus?) { groupDraft.update { it.copy(status = status) } }

    fun onSaveGroupDraft() {
        val d = groupDraft.value
        viewModelScope.launch {
            if (d.groupId == null) {
                createGroupUseCase(
                    itineraryId = d.itineraryId,
                    title = d.title,
                    status = d.status,
                    sortOrder = d.sortOrder,
                )
            } else {
                updateGroupUseCase(
                    ItineraryGroup(
                        id = d.groupId,
                        itineraryId = d.itineraryId,
                        title = d.title.trim().ifBlank { null },
                        status = d.status,
                        sortOrder = d.sortOrder,
                        flights = emptyList(),
                    ),
                )
            }
            syncGeneratedStopsIfLinked()
            onDismissGroupDraft()
        }
    }

    fun onDeleteGroup(group: ItineraryGroup) {
        viewModelScope.launch {
            deleteGroupUseCase(group)
            syncGeneratedStopsIfLinked()
        }
    }

    // --- Group reordering ---

    fun onToggleGroupReorderMode() { isGroupReorderMode.update { !it } }

    fun onMoveGroupUp(group: ItineraryGroup) { moveGroup(group, -1) }
    fun onMoveGroupDown(group: ItineraryGroup) { moveGroup(group, 1) }

    private fun moveGroup(group: ItineraryGroup, offset: Int) {
        val groups = uiState.value.groups.toMutableList()
        val fromIndex = groups.indexOfFirst { it.id == group.id }
        val toIndex = fromIndex + offset
        if (fromIndex !in groups.indices || toIndex !in groups.indices) return
        val moved = groups.removeAt(fromIndex)
        groups.add(toIndex, moved)
        viewModelScope.launch {
            reorderGroupsUseCase(groups)
            syncGeneratedStopsIfLinked()
        }
    }

    // --- Flight reordering within a group ---

    fun onToggleFlightReorderMode(groupId: String) {
        reorderingFlightsGroupId.update { if (it == groupId) null else groupId }
    }

    fun onMoveFlightUp(groupId: String, flight: Flight) { moveFlight(groupId, flight, -1) }
    fun onMoveFlightDown(groupId: String, flight: Flight) { moveFlight(groupId, flight, 1) }

    private fun moveFlight(groupId: String, flight: Flight, offset: Int) {
        val flights = uiState.value.groups
            .find { it.id == groupId }
            ?.flights
            ?.toMutableList() ?: return
        val fromIndex = flights.indexOfFirst { it.id == flight.id }
        val toIndex = fromIndex + offset
        if (fromIndex !in flights.indices || toIndex !in flights.indices) return
        val moved = flights.removeAt(fromIndex)
        flights.add(toIndex, moved)
        viewModelScope.launch {
            reorderGroupFlightsUseCase(flights)
            syncGeneratedStopsIfLinked()
        }
    }

    // --- Flight CRUD within a group ---

    fun onAddFlightToGroupClick(groupId: String) {
        targetGroupId.value = groupId
        flightDraft.update { FlightEditorDraftUiState(isOpen = true) }
        originQuery.value = ""
        destinationQuery.value = ""
        airlineQuery.value = ""
    }

    fun onEditFlightClick(flight: Flight) {
        targetGroupId.value = null
        viewModelScope.launch {
            val origin = airportRepository.getAirportById(flight.originAirportId)
            val destination = airportRepository.getAirportById(flight.destinationAirportId)
            val airline = flight.airline?.let { airlineRepository.getAirlineByIata(it.uppercase()) }
            flightDraft.update { FlightEditorDraftUiState.fromFlight(flight, origin, destination, airline) }
            originQuery.value = ""
            destinationQuery.value = ""
            airlineQuery.value = ""
        }
    }

    fun onDismissFlightDraft() {
        flightDraft.update { FlightEditorDraftUiState() }
        targetGroupId.value = null
        originQuery.value = ""
        destinationQuery.value = ""
        airlineQuery.value = ""
    }

    fun onFlightOriginQueryChanged(query: String) {
        originQuery.value = query
        flightDraft.update { it.copy(originQuery = query, originAirport = null) }
    }

    fun onFlightOriginSelected(airport: Airport) {
        flightDraft.update { it.copy(originAirport = airport, originQuery = airport.displayLabel()) }
        originQuery.value = ""
    }

    fun onFlightDestinationQueryChanged(query: String) {
        destinationQuery.value = query
        flightDraft.update { it.copy(destinationQuery = query, destinationAirport = null) }
    }

    fun onFlightDestinationSelected(airport: Airport) {
        flightDraft.update { it.copy(destinationAirport = airport, destinationQuery = airport.displayLabel()) }
        destinationQuery.value = ""
    }

    fun onFlightStatusChanged(status: TravelStatus) { flightDraft.update { it.copy(status = status) } }
    fun onScheduledDepartureAtChanged(v: String) { flightDraft.update { it.copy(scheduledDepartureAt = v) } }
    fun onScheduledArrivalAtChanged(v: String) { flightDraft.update { it.copy(scheduledArrivalAt = v) } }
    fun onActualDepartureAtChanged(v: String) { flightDraft.update { it.copy(actualDepartureAt = v) } }
    fun onActualArrivalAtChanged(v: String) { flightDraft.update { it.copy(actualArrivalAt = v) } }
    fun onAirlineQueryChanged(v: String) {
        airlineQuery.value = v
        flightDraft.update { it.copy(airlineQuery = v, airlineIata = null) }
    }

    fun onAirlineSelected(airline: Airline) {
        airlineQuery.value = ""
        flightDraft.update { it.copy(airlineQuery = airline.name, airlineIata = airline.iata) }
    }
    fun onFlightNumberChanged(v: String) { flightDraft.update { it.copy(flightNumber = v) } }
    fun onAircraftChanged(v: String) { flightDraft.update { it.copy(aircraft = v) } }
    fun onAircraftRegistrationChanged(v: String) { flightDraft.update { it.copy(aircraftRegistration = v) } }
    fun onFlightNotesChanged(v: String) { flightDraft.update { it.copy(notes = v) } }

    fun onSaveFlightDraft() {
        val d = flightDraft.value
        val origin = d.originAirport
        val destination = d.destinationAirport

        if (origin == null || destination == null) {
            flightDraft.update { it.copy(validationError = "Cal seleccionar l'aeroport d'origen i de destí.") }
            return
        }
        if (origin.id == destination.id) {
            flightDraft.update { it.copy(validationError = "L'origen i el destí no poden ser el mateix aeroport.") }
            return
        }

        viewModelScope.launch {
            if (d.flightId == null) {
                val groupId = targetGroupId.value
                val sortOrder = groupId?.let { gid ->
                    uiState.value.groups.find { it.id == gid }?.flights?.size ?: 0
                }
                val resolvedAirline = d.airlineIata ?: d.airlineQuery.trim().ifBlank { null }
                createFlightUseCase(
                    originAirportId = origin.id,
                    destinationAirportId = destination.id,
                    status = d.status,
                    scheduledDepartureAt = d.scheduledDepartureAt.ifBlank { null },
                    scheduledArrivalAt = d.scheduledArrivalAt.ifBlank { null },
                    actualDepartureAt = d.actualDepartureAt.ifBlank { null },
                    actualArrivalAt = d.actualArrivalAt.ifBlank { null },
                    airline = resolvedAirline,
                    flightNumber = d.flightNumber,
                    aircraft = d.aircraft,
                    aircraftRegistration = d.aircraftRegistration,
                    notes = d.notes,
                    itineraryGroupId = groupId,
                    sortOrder = sortOrder,
                )
            } else {
                updateFlightUseCase(
                    Flight(
                        id = d.flightId,
                        originAirportId = origin.id,
                        destinationAirportId = destination.id,
                        status = d.status,
                        scheduledDepartureAt = d.scheduledDepartureAt.ifBlank { null },
                        scheduledArrivalAt = d.scheduledArrivalAt.ifBlank { null },
                        actualDepartureAt = d.actualDepartureAt.ifBlank { null },
                        actualArrivalAt = d.actualArrivalAt.ifBlank { null },
                        airline = d.airlineIata ?: d.airlineQuery.trim().ifBlank { null },
                        flightNumber = d.flightNumber.trim().ifBlank { null },
                        aircraft = d.aircraft.trim().ifBlank { null },
                        notes = d.notes.trim().ifBlank { null },
                        aircraftRegistration = d.aircraftRegistration.trim().ifBlank { null },
                        itineraryGroupId = d.itineraryGroupId,
                        sortOrder = d.sortOrder,
                        destinationCountsForCountryTracking = d.destinationCountsForCountryTracking,
                        originCountsForCountryTracking = d.originCountsForCountryTracking,
                    ),
                )
            }
            syncGeneratedStopsIfLinked()
            onDismissFlightDraft()
        }
    }

    fun onDeleteFlight(flight: Flight) {
        viewModelScope.launch {
            deleteFlightUseCase(flight)
            syncGeneratedStopsIfLinked()
        }
    }

    private suspend fun syncGeneratedStopsIfLinked() {
        val tripId = uiState.value.itinerary?.tripId ?: return
        syncGeneratedTripStopsForItineraryUseCase(itineraryId = itineraryId, tripId = tripId)
    }

    class Factory(
        private val itineraryRepository: ItineraryRepository,
        private val tripRepository: TripRepository,
        private val airportRepository: AirportRepository,
        private val airlineRepository: AirlineRepository,
        private val searchAirportsUseCase: SearchAirportsUseCase,
        private val searchAirlinesUseCase: SearchAirlinesUseCase,
        private val updateItineraryUseCase: UpdateItineraryUseCase,
        private val deleteItineraryUseCase: DeleteItineraryUseCase,
        private val syncGeneratedTripStopsForItineraryUseCase: SyncGeneratedTripStopsForItineraryUseCase,
        private val removeGeneratedTripStopsForItineraryUseCase: RemoveGeneratedTripStopsForItineraryUseCase,
        private val createGroupUseCase: CreateItineraryGroupUseCase,
        private val updateGroupUseCase: UpdateItineraryGroupUseCase,
        private val deleteGroupUseCase: DeleteItineraryGroupUseCase,
        private val reorderGroupsUseCase: ReorderItineraryGroupsUseCase,
        private val createFlightUseCase: CreateFlightUseCase,
        private val updateFlightUseCase: UpdateFlightUseCase,
        private val deleteFlightUseCase: DeleteFlightUseCase,
        private val reorderGroupFlightsUseCase: ReorderGroupFlightsUseCase,
        private val itineraryId: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ItineraryDetailViewModel(
            itineraryRepository = itineraryRepository,
            tripRepository = tripRepository,
            airportRepository = airportRepository,
            airlineRepository = airlineRepository,
            searchAirportsUseCase = searchAirportsUseCase,
            searchAirlinesUseCase = searchAirlinesUseCase,
            updateItineraryUseCase = updateItineraryUseCase,
            deleteItineraryUseCase = deleteItineraryUseCase,
            syncGeneratedTripStopsForItineraryUseCase = syncGeneratedTripStopsForItineraryUseCase,
            removeGeneratedTripStopsForItineraryUseCase = removeGeneratedTripStopsForItineraryUseCase,
            createGroupUseCase = createGroupUseCase,
            updateGroupUseCase = updateGroupUseCase,
            deleteGroupUseCase = deleteGroupUseCase,
            reorderGroupsUseCase = reorderGroupsUseCase,
            createFlightUseCase = createFlightUseCase,
            updateFlightUseCase = updateFlightUseCase,
            deleteFlightUseCase = deleteFlightUseCase,
            reorderGroupFlightsUseCase = reorderGroupFlightsUseCase,
            itineraryId = itineraryId,
        ) as T
    }
}

private data class ItineraryDetailContentState(
    val itinerary: Itinerary?,
    val groups: List<ItineraryGroup>,
    val linkedTrip: Trip?,
    val airports: List<Airport>,
)

data class ItineraryDetailUiState(
    val itinerary: Itinerary? = null,
    val linkedTrip: Trip? = null,
    val groups: List<ItineraryGroup> = emptyList(),
    val airports: List<Airport> = emptyList(),
    val itineraryDraft: ItineraryEditorDraft = ItineraryEditorDraft(),
    val groupDraft: GroupEditorDraft = GroupEditorDraft(),
    val flightDraft: FlightEditorDraftUiState = FlightEditorDraftUiState(),
    val originSearchResults: List<Airport> = emptyList(),
    val destinationSearchResults: List<Airport> = emptyList(),
    val airlineSearchResults: List<Airline> = emptyList(),
    val isGroupReorderMode: Boolean = false,
    val reorderingFlightsGroupId: String? = null,
)

data class GroupEditorDraft(
    val isOpen: Boolean = false,
    val groupId: String? = null,
    val itineraryId: String = "",
    val title: String = "",
    val status: TravelStatus? = null,
    val sortOrder: Int = 0,
)
