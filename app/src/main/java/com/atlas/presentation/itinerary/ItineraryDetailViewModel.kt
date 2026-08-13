package com.atlas.presentation.itinerary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atlas.domain.model.Airline
import com.atlas.domain.model.Airport
import com.atlas.domain.model.Flight
import com.atlas.domain.model.FlightApiResult
import com.atlas.domain.model.Itinerary
import com.atlas.domain.model.ItineraryGroup
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import com.atlas.domain.repository.AirlineRepository
import com.atlas.domain.repository.AirportRepository
import com.atlas.domain.repository.FlightRepository
import com.atlas.domain.repository.ItineraryRepository
import com.atlas.domain.repository.TripRepository
import com.atlas.domain.usecase.aircraft.LookupAircraftUseCase
import com.atlas.domain.usecase.airline.SearchAirlinesUseCase
import com.atlas.domain.usecase.airport.SearchAirportsUseCase
import com.atlas.domain.usecase.flight.CreateFlightUseCase
import com.atlas.domain.usecase.flight.DeleteFlightUseCase
import com.atlas.domain.usecase.flight.LookupFlightUseCase
import com.atlas.domain.usecase.flight.UpdateFlightUseCase
import com.atlas.domain.usecase.itinerary.CreateItineraryGroupUseCase
import com.atlas.domain.usecase.itinerary.DeleteItineraryGroupUseCase
import com.atlas.domain.usecase.itinerary.DeleteItineraryUseCase
import com.atlas.domain.usecase.itinerary.ReorderGroupFlightsUseCase
import com.atlas.domain.usecase.itinerary.ReorderItineraryGroupsUseCase
import com.atlas.domain.usecase.itinerary.RemoveGeneratedTripStopsForItineraryUseCase
import com.atlas.domain.usecase.itinerary.SyncGeneratedTripStopsForItineraryUseCase
import com.atlas.domain.usecase.itinerary.UpdateItineraryUseCase
import com.atlas.domain.util.inferFlightStatus
import com.atlas.presentation.flight.FlightApiSearchState
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class ItineraryDetailViewModel(
    itineraryRepository: ItineraryRepository,
    flightRepository: FlightRepository,
    tripRepository: TripRepository,
    private val airportRepository: AirportRepository,
    private val airlineRepository: AirlineRepository,
    private val searchAirportsUseCase: SearchAirportsUseCase,
    private val searchAirlinesUseCase: SearchAirlinesUseCase,
    private val lookupFlightUseCase: LookupFlightUseCase,
    private val lookupAircraftUseCase: LookupAircraftUseCase,
    private val updateItineraryUseCase: UpdateItineraryUseCase,
    private val deleteItineraryUseCase: DeleteItineraryUseCase,
    private val syncGeneratedTripStopsForItineraryUseCase: SyncGeneratedTripStopsForItineraryUseCase,
    private val removeGeneratedTripStopsForItineraryUseCase: RemoveGeneratedTripStopsForItineraryUseCase,
    private val createGroupUseCase: CreateItineraryGroupUseCase,
    private val deleteGroupUseCase: DeleteItineraryGroupUseCase,
    private val reorderGroupsUseCase: ReorderItineraryGroupsUseCase,
    private val createFlightUseCase: CreateFlightUseCase,
    private val updateFlightUseCase: UpdateFlightUseCase,
    private val deleteFlightUseCase: DeleteFlightUseCase,
    private val reorderGroupFlightsUseCase: ReorderGroupFlightsUseCase,
    private val itineraryId: String,
) : ViewModel() {

    private val flightDraft = MutableStateFlow(FlightEditorDraftUiState())
    private val targetGroupId = MutableStateFlow<String?>(null)
    private val originQuery = MutableStateFlow("")
    private val destinationQuery = MutableStateFlow("")
    private val airlineQuery = MutableStateFlow("")
    private val isGroupReorderMode = MutableStateFlow(false)
    private val reorderingFlightsGroupId = MutableStateFlow<String?>(null)
    private val showTripPicker = MutableStateFlow(false)
    private val showSoloFlightPicker = MutableStateFlow(false)
    private val soloFlightPickerGroupId = MutableStateFlow<String?>(null)
    private val flightActionState = MutableStateFlow(FlightActionState())

    private val soloFlights: StateFlow<List<Flight>> = flightRepository.observeFlights()
        .map { it.filter { f -> f.itineraryGroupId == null } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val originResults: StateFlow<List<Airport>> = originQuery
        .debounce(300)
        .flatMapLatest { q -> if (q.isBlank()) flowOf(emptyList()) else searchAirportsUseCase(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val destinationResults: StateFlow<List<Airport>> = destinationQuery
        .debounce(300)
        .flatMapLatest { q -> if (q.isBlank()) flowOf(emptyList()) else searchAirportsUseCase(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val airlineResults: StateFlow<List<Airline>> = airlineQuery
        .debounce(300)
        .flatMapLatest { q -> if (q.isBlank()) flowOf(emptyList()) else searchAirlinesUseCase(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val contentState = combine(
        itineraryRepository.observeItinerary(itineraryId),
        itineraryRepository.observeGroups(itineraryId),
        tripRepository.observeTrips(),
        airportRepository.observeAirports(),
        itineraryRepository.observeItineraries(),
    ) { itinerary, groups, trips, airports, allItineraries ->
        val linkedTripIds = allItineraries.mapNotNullTo(mutableSetOf()) { it.tripId }
        val airlineNamesByCode = groups
            .flatMap { it.flights }
            .mapNotNull { it.airline?.trim()?.takeIf { airline -> airline.isNotBlank() } }
            .distinctBy { it.uppercase() }
            .associate { airlineCode ->
                val normalizedCode = airlineCode.uppercase()
                normalizedCode to (airlineRepository.getAirlineByIata(normalizedCode)?.name ?: airlineCode)
            }
        ItineraryDetailContentState(
            itinerary = itinerary,
            groups = groups,
            linkedTrip = itinerary?.tripId?.let { tripId -> trips.firstOrNull { it.id == tripId } },
            airports = airports,
            airlineNamesByCode = airlineNamesByCode,
            // A trip holds at most one itinerary, so trips already spoken for are not
            // offered. Without this the trip ends up with legs it cannot account for:
            // the second itinerary still generates route stops while the trip page,
            // which resolves its link with firstOrNull, shows only the first.
            availableTrips = trips.filter { trip ->
                trip.id !in linkedTripIds || trip.id == itinerary?.tripId
            },
        )
    }

    private val controlState = combine(
        isGroupReorderMode,
        reorderingFlightsGroupId,
        showTripPicker,
        showSoloFlightPicker,
        soloFlightPickerGroupId,
    ) { arr ->
        ItineraryControlState(
            isGroupReorderMode = arr[0] as Boolean,
            reorderingFlightsGroupId = arr[1] as String?,
            showTripPicker = arr[2] as Boolean,
            showSoloFlightPicker = arr[3] as Boolean,
            soloFlightPickerGroupId = arr[4] as String?,
        )
    }

    val uiState: StateFlow<ItineraryDetailUiState> = combine(
        combine(contentState, soloFlights) { c, s -> c to s },
        combine(flightDraft, flightActionState) { d, a -> d to a },
        combine(originResults, destinationResults, airlineResults) { o, d, a -> Triple(o, d, a) },
        controlState,
    ) { (content, soloFlts), (fltDraft, flightAction), (originRes, destRes, airlineRes), control ->
        ItineraryDetailUiState(
            itinerary = content.itinerary,
            linkedTrip = content.linkedTrip,
            groups = content.groups,
            airports = content.airports,
            airlineNamesByCode = content.airlineNamesByCode,
            availableTrips = content.availableTrips,
            soloFlights = soloFlts,
            flightDraft = fltDraft,
            originSearchResults = originRes,
            destinationSearchResults = destRes,
            airlineSearchResults = airlineRes,
            isGroupReorderMode = control.isGroupReorderMode,
            reorderingFlightsGroupId = control.reorderingFlightsGroupId,
            showTripPicker = control.showTripPicker,
            showSoloFlightPicker = control.showSoloFlightPicker,
            soloFlightPickerGroupId = control.soloFlightPickerGroupId,
            flightPendingRemoval = flightAction.flightPendingRemoval,
            flightPendingMove = flightAction.flightPendingMove,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ItineraryDetailUiState(),
    )

    // --- Itinerary actions ---

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

    fun onAssignTripClick() { showTripPicker.update { true } }
    fun onDismissTripPicker() { showTripPicker.update { false } }

    fun onLinkTrip(trip: Trip) {
        val itin = uiState.value.itinerary ?: return
        viewModelScope.launch {
            updateItineraryUseCase(itin.copy(tripId = trip.id))
            syncGeneratedTripStopsForItineraryUseCase(itineraryId = itineraryId, tripId = trip.id)
            showTripPicker.update { false }
        }
    }

    // --- Group actions ---

    fun onAddGroupClick() {
        viewModelScope.launch {
            createGroupUseCase(
                itineraryId = itineraryId,
                title = null,
                status = null,
                sortOrder = uiState.value.groups.size,
            )
            syncGeneratedStopsIfLinked()
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

    // --- Flight CRUD ---

    fun onAddFlightToGroupClick(groupId: String) {
        targetGroupId.value = groupId
        flightDraft.update { FlightEditorDraftUiState(isOpen = true, showForm = false) }
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

    fun onManualEntryClick() { flightDraft.update { it.copy(showForm = true) } }
    fun onBackToSearch() { flightDraft.update { it.copy(showForm = false, apiSearchState = FlightApiSearchState.Idle) } }

    // ── API search ──────────────────────────────────────────────────────────

    fun onApiFlightNumberChanged(value: String) {
        flightDraft.update { it.copy(apiFlightNumber = value, apiSearchState = FlightApiSearchState.Idle) }
    }

    fun onApiSearchDateChanged(value: String) {
        flightDraft.update { it.copy(apiSearchDate = value, apiSearchState = FlightApiSearchState.Idle) }
    }

    fun onSearchByFlightNumber() {
        val d = flightDraft.value
        val number = d.apiFlightNumber.trim()
        val date = d.apiSearchDate.trim()
        if (number.isBlank() || date.isBlank()) return
        flightDraft.update { it.copy(apiSearchState = FlightApiSearchState.Searching) }
        viewModelScope.launch {
            val state = when (val result = lookupFlightUseCase(number, date)) {
                is FlightApiResult.Success -> FlightApiSearchState.Found(result.prefill)
                is FlightApiResult.NotFound -> FlightApiSearchState.NotFound
                is FlightApiResult.NoApiKey -> FlightApiSearchState.NoApiKey
                is FlightApiResult.RateLimited -> FlightApiSearchState.RateLimited
                is FlightApiResult.NetworkError -> FlightApiSearchState.Error(result.message)
            }
            flightDraft.update { it.copy(apiSearchState = state) }
        }
    }

    fun onApplyApiResult() {
        val found = flightDraft.value.apiSearchState as? FlightApiSearchState.Found ?: return
        val prefill = found.prefill
        viewModelScope.launch {
            val origin = prefill.originIata?.let { airportRepository.getAirportByIata(it) }
            val destination = prefill.destinationIata?.let { airportRepository.getAirportByIata(it) }
            val airline = prefill.airlineIata?.let { airlineRepository.getAirlineByIata(it.uppercase()) }
            lookupAircraftUseCase(prefill.aircraftRegistration)
            val inferredStatus = inferFlightStatus(prefill.scheduledDepartureAt)
            flightDraft.update { current ->
                current.copy(
                    originAirport = origin ?: current.originAirport,
                    originQuery = origin?.displayLabel() ?: current.originQuery,
                    destinationAirport = destination ?: current.destinationAirport,
                    destinationQuery = destination?.displayLabel() ?: current.destinationQuery,
                    airlineQuery = airline?.name ?: prefill.airlineIata ?: current.airlineQuery,
                    airlineIata = prefill.airlineIata ?: current.airlineIata,
                    flightNumber = prefill.flightNumber ?: current.flightNumber,
                    aircraft = prefill.aircraftModel ?: current.aircraft,
                    aircraftRegistration = prefill.aircraftRegistration ?: current.aircraftRegistration,
                    scheduledDepartureAt = prefill.scheduledDepartureAt ?: current.scheduledDepartureAt,
                    scheduledArrivalAt = prefill.scheduledArrivalAt ?: current.scheduledArrivalAt,
                    status = inferredStatus ?: current.status,
                    fetchedFrom = "api",
                    externalProvider = "aerodatabox",
                    externalId = "${prefill.flightNumber}/${current.apiSearchDate}",
                    apiSearchState = FlightApiSearchState.Idle,
                    showForm = true,
                )
            }
            airlineQuery.value = ""
        }
    }

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
                createFlightUseCase(
                    originAirportId = origin.id,
                    destinationAirportId = destination.id,
                    status = d.status,
                    scheduledDepartureAt = d.scheduledDepartureAt.ifBlank { null },
                    scheduledArrivalAt = d.scheduledArrivalAt.ifBlank { null },
                    actualDepartureAt = d.actualDepartureAt.ifBlank { null },
                    actualArrivalAt = d.actualArrivalAt.ifBlank { null },
                    airline = d.airlineIata ?: d.airlineQuery.trim().ifBlank { null },
                    flightNumber = d.flightNumber,
                    aircraft = d.aircraft,
                    aircraftRegistration = d.aircraftRegistration,
                    notes = d.notes,
                    fetchedFrom = d.fetchedFrom,
                    externalProvider = d.externalProvider,
                    externalId = d.externalId,
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
                        fetchedFrom = d.fetchedFrom,
                        externalProvider = d.externalProvider,
                        externalId = d.externalId,
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

    // --- Assign existing solo flight ---

    fun onAddExistingFlightToGroupClick(groupId: String) {
        soloFlightPickerGroupId.value = groupId
        showSoloFlightPicker.value = true
    }

    fun onDismissSoloFlightPicker() {
        showSoloFlightPicker.value = false
        soloFlightPickerGroupId.value = null
    }

    fun onAssignSoloFlight(flight: Flight) {
        val groupId = soloFlightPickerGroupId.value ?: return
        val sortOrder = uiState.value.groups.find { it.id == groupId }?.flights?.size ?: 0
        viewModelScope.launch {
            updateFlightUseCase(flight.copy(itineraryGroupId = groupId, sortOrder = sortOrder))
            syncGeneratedStopsIfLinked()
            onDismissSoloFlightPicker()
        }
    }

    // --- Remove flight from itinerary ---

    fun onRemoveFlightFromItineraryClick(flight: Flight) {
        flightActionState.update { it.copy(flightPendingRemoval = flight) }
    }

    fun onDismissRemoveFlightConfirm() {
        flightActionState.update { it.copy(flightPendingRemoval = null) }
    }

    fun onConfirmRemoveFlightFromItinerary() {
        val flight = flightActionState.value.flightPendingRemoval ?: return
        viewModelScope.launch {
            updateFlightUseCase(flight.copy(itineraryGroupId = null, sortOrder = null))
            syncGeneratedStopsIfLinked()
            onDismissRemoveFlightConfirm()
        }
    }

    // --- Move flight to another group ---

    fun onMoveFlightToGroupClick(flight: Flight) {
        flightActionState.update { it.copy(flightPendingMove = flight) }
    }

    fun onDismissMoveFlightPicker() {
        flightActionState.update { it.copy(flightPendingMove = null) }
    }

    fun onMoveFlightToGroup(flight: Flight, targetGroup: ItineraryGroup) {
        val sortOrder = targetGroup.flights.size
        viewModelScope.launch {
            updateFlightUseCase(flight.copy(itineraryGroupId = targetGroup.id, sortOrder = sortOrder))
            syncGeneratedStopsIfLinked()
            onDismissMoveFlightPicker()
        }
    }

    private suspend fun syncGeneratedStopsIfLinked() {
        val tripId = uiState.value.itinerary?.tripId ?: return
        syncGeneratedTripStopsForItineraryUseCase(itineraryId = itineraryId, tripId = tripId)
    }

    class Factory(
        private val itineraryRepository: ItineraryRepository,
        private val flightRepository: FlightRepository,
        private val tripRepository: TripRepository,
        private val airportRepository: AirportRepository,
        private val airlineRepository: AirlineRepository,
        private val searchAirportsUseCase: SearchAirportsUseCase,
        private val searchAirlinesUseCase: SearchAirlinesUseCase,
        private val lookupFlightUseCase: LookupFlightUseCase,
        private val lookupAircraftUseCase: LookupAircraftUseCase,
        private val updateItineraryUseCase: UpdateItineraryUseCase,
        private val deleteItineraryUseCase: DeleteItineraryUseCase,
        private val syncGeneratedTripStopsForItineraryUseCase: SyncGeneratedTripStopsForItineraryUseCase,
        private val removeGeneratedTripStopsForItineraryUseCase: RemoveGeneratedTripStopsForItineraryUseCase,
        private val createGroupUseCase: CreateItineraryGroupUseCase,
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
            flightRepository = flightRepository,
            tripRepository = tripRepository,
            airportRepository = airportRepository,
            airlineRepository = airlineRepository,
            searchAirportsUseCase = searchAirportsUseCase,
            searchAirlinesUseCase = searchAirlinesUseCase,
            lookupFlightUseCase = lookupFlightUseCase,
            lookupAircraftUseCase = lookupAircraftUseCase,
            updateItineraryUseCase = updateItineraryUseCase,
            deleteItineraryUseCase = deleteItineraryUseCase,
            syncGeneratedTripStopsForItineraryUseCase = syncGeneratedTripStopsForItineraryUseCase,
            removeGeneratedTripStopsForItineraryUseCase = removeGeneratedTripStopsForItineraryUseCase,
            createGroupUseCase = createGroupUseCase,
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
    val airlineNamesByCode: Map<String, String>,
    val availableTrips: List<Trip>,
)

private data class ItineraryControlState(
    val isGroupReorderMode: Boolean = false,
    val reorderingFlightsGroupId: String? = null,
    val showTripPicker: Boolean = false,
    val showSoloFlightPicker: Boolean = false,
    val soloFlightPickerGroupId: String? = null,
)

private data class FlightActionState(
    val flightPendingRemoval: Flight? = null,
    val flightPendingMove: Flight? = null,
)

data class ItineraryDetailUiState(
    val itinerary: Itinerary? = null,
    val linkedTrip: Trip? = null,
    val groups: List<ItineraryGroup> = emptyList(),
    val airports: List<Airport> = emptyList(),
    val airlineNamesByCode: Map<String, String> = emptyMap(),
    val availableTrips: List<Trip> = emptyList(),
    val soloFlights: List<Flight> = emptyList(),
    val flightDraft: FlightEditorDraftUiState = FlightEditorDraftUiState(),
    val originSearchResults: List<Airport> = emptyList(),
    val destinationSearchResults: List<Airport> = emptyList(),
    val airlineSearchResults: List<Airline> = emptyList(),
    val isGroupReorderMode: Boolean = false,
    val reorderingFlightsGroupId: String? = null,
    val showTripPicker: Boolean = false,
    val showSoloFlightPicker: Boolean = false,
    val soloFlightPickerGroupId: String? = null,
    val flightPendingRemoval: Flight? = null,
    val flightPendingMove: Flight? = null,
)
