package com.atlas.presentation.flight

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
import com.atlas.domain.repository.AirlineRepository
import com.atlas.domain.repository.AirportRepository
import com.atlas.domain.repository.FlightRepository
import com.atlas.domain.repository.ItineraryRepository
import com.atlas.domain.usecase.airline.SearchAirlinesUseCase
import com.atlas.domain.usecase.aircraft.LookupAircraftUseCase
import com.atlas.domain.usecase.airport.SearchAirportsUseCase
import com.atlas.domain.usecase.flight.CreateFlightUseCase
import com.atlas.domain.usecase.flight.DeleteFlightUseCase
import com.atlas.domain.usecase.flight.LookupFlightUseCase
import com.atlas.domain.usecase.flight.UpdateFlightUseCase
import com.atlas.domain.usecase.itinerary.CreateItineraryUseCase
import com.atlas.domain.usecase.itinerary.DeleteItineraryUseCase
import com.atlas.domain.usecase.itinerary.UpdateItineraryUseCase
import com.atlas.domain.util.inferFlightStatus
import com.atlas.presentation.itinerary.ItineraryEditorDraft
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
class FlightListViewModel(
    flightRepository: FlightRepository,
    itineraryRepository: ItineraryRepository,
    private val airportRepository: AirportRepository,
    private val airlineRepository: AirlineRepository,
    private val searchAirportsUseCase: SearchAirportsUseCase,
    private val searchAirlinesUseCase: SearchAirlinesUseCase,
    private val createFlightUseCase: CreateFlightUseCase,
    private val updateFlightUseCase: UpdateFlightUseCase,
    private val deleteFlightUseCase: DeleteFlightUseCase,
    private val lookupFlightUseCase: LookupFlightUseCase,
    private val lookupAircraftUseCase: LookupAircraftUseCase,
    private val createItineraryUseCase: CreateItineraryUseCase,
    private val updateItineraryUseCase: UpdateItineraryUseCase,
    private val deleteItineraryUseCase: DeleteItineraryUseCase,
) : ViewModel() {

    private val draft = MutableStateFlow(FlightEditorDraftUiState())
    private val itineraryDraft = MutableStateFlow(ItineraryEditorDraft())
    private val originQuery = MutableStateFlow("")
    private val destinationQuery = MutableStateFlow("")
    private val airlineQuery = MutableStateFlow("")

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

    private val flightItems = flightRepository.observeFlights().map { flights ->
        flights.filter { it.itineraryGroupId == null }.map { flight ->
            val resolvedAirlineName = flight.airline?.let { iata ->
                airlineRepository.getAirlineByIata(iata.uppercase())?.name ?: iata
            }
            FlightListItemUiState(
                flight = flight,
                originLabel = airportRepository.getAirportById(flight.originAirportId)?.shortLabel()
                    ?: flight.originAirportId.uppercase(),
                destinationLabel = airportRepository.getAirportById(flight.destinationAirportId)?.shortLabel()
                    ?: flight.destinationAirportId.uppercase(),
                resolvedAirlineName = resolvedAirlineName,
            )
        }
    }

    private val itineraryItems = combine(
        itineraryRepository.observeItineraries(),
        itineraryRepository.observeAllGroups(),
    ) { itineraries, groups ->
        val groupsByItinerary = groups.groupBy { it.itineraryId }
        itineraries.map { itinerary ->
            val itineraryGroups = groupsByItinerary[itinerary.id].orEmpty()
            ItinerarySummaryUiState(
                itinerary = itinerary,
                groupCount = itineraryGroups.size,
                flightCount = itineraryGroups.sumOf { it.flights.size },
                routeSummary = buildRouteSummary(itineraryGroups),
            )
        }
    }

    private val listState = combine(
        flightItems,
        itineraryItems,
        draft,
        itineraryDraft,
    ) { flightItems, itineraryItems, draft, itineraryDraft ->
        FlightListUiState(
            flightItems = flightItems,
            itineraryItems = itineraryItems,
            draft = draft,
            itineraryDraft = itineraryDraft,
        )
    }

    val uiState: StateFlow<FlightListUiState> = combine(
        listState,
        originResults,
        destinationResults,
        airlineResults,
    ) { state, originRes, destRes, airlineRes ->
        state.copy(
            originSearchResults = originRes,
            destinationSearchResults = destRes,
            airlineSearchResults = airlineRes,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FlightListUiState(),
    )

    fun onCreateFlightClick() {
        draft.update { FlightEditorDraftUiState(isOpen = true) }
        originQuery.value = ""
        destinationQuery.value = ""
        airlineQuery.value = ""
    }

    fun onEditFlightClick(flight: Flight) {
        viewModelScope.launch {
            val origin = airportRepository.getAirportById(flight.originAirportId)
            val destination = airportRepository.getAirportById(flight.destinationAirportId)
            val airline = flight.airline?.let { airlineRepository.getAirlineByIata(it.uppercase()) }
            draft.update { FlightEditorDraftUiState.fromFlight(flight, origin, destination, airline) }
            originQuery.value = ""
            destinationQuery.value = ""
            airlineQuery.value = ""
        }
    }

    fun onDismissDraft() {
        draft.update { FlightEditorDraftUiState() }
        airlineQuery.value = ""
    }

    fun onOriginQueryChanged(query: String) {
        originQuery.value = query
        draft.update { it.copy(originQuery = query, originAirport = null) }
    }

    fun onOriginSelected(airport: Airport) {
        draft.update { it.copy(originAirport = airport, originQuery = airport.displayLabel()) }
        originQuery.value = ""
    }

    fun onDestinationQueryChanged(query: String) {
        destinationQuery.value = query
        draft.update { it.copy(destinationQuery = query, destinationAirport = null) }
    }

    fun onDestinationSelected(airport: Airport) {
        draft.update { it.copy(destinationAirport = airport, destinationQuery = airport.displayLabel()) }
        destinationQuery.value = ""
    }

    fun onStatusChanged(status: TravelStatus) {
        draft.update { it.copy(status = status) }
    }

    fun onScheduledDepartureAtChanged(value: String) {
        draft.update { current ->
            val inferred = if (current.flightId == null) inferFlightStatus(value) else null
            current.copy(
                scheduledDepartureAt = value,
                status = inferred ?: current.status,
            )
        }
    }

    fun onScheduledArrivalAtChanged(value: String) {
        draft.update { it.copy(scheduledArrivalAt = value) }
    }

    fun onActualDepartureAtChanged(value: String) {
        draft.update { it.copy(actualDepartureAt = value) }
    }

    fun onActualArrivalAtChanged(value: String) {
        draft.update { it.copy(actualArrivalAt = value) }
    }

    fun onAirlineQueryChanged(value: String) {
        airlineQuery.value = value
        draft.update { it.copy(airlineQuery = value, airlineIata = null) }
    }

    fun onAirlineSelected(airline: Airline) {
        airlineQuery.value = ""
        draft.update { it.copy(airlineQuery = airline.name, airlineIata = airline.iata) }
    }
    fun onFlightNumberChanged(value: String) { draft.update { it.copy(flightNumber = value) } }
    fun onAircraftChanged(value: String) { draft.update { it.copy(aircraft = value) } }
    fun onAircraftRegistrationChanged(value: String) { draft.update { it.copy(aircraftRegistration = value) } }
    fun onNotesChanged(value: String) { draft.update { it.copy(notes = value) } }

    // ── API search ───────────────────────────────────────────────────────────

    fun onApiFlightNumberChanged(value: String) {
        draft.update { it.copy(apiFlightNumber = value, apiSearchState = FlightApiSearchState.Idle) }
    }

    fun onApiSearchDateChanged(value: String) {
        draft.update { it.copy(apiSearchDate = value, apiSearchState = FlightApiSearchState.Idle) }
    }

    fun onSearchByFlightNumber() {
        val d = draft.value
        val number = d.apiFlightNumber.trim()
        val date = d.apiSearchDate.trim()
        if (number.isBlank() || date.isBlank()) return

        draft.update { it.copy(apiSearchState = FlightApiSearchState.Searching) }
        viewModelScope.launch {
            val state = when (val result = lookupFlightUseCase(number, date)) {
                is FlightApiResult.Success -> FlightApiSearchState.Found(result.prefill)
                is FlightApiResult.NotFound -> FlightApiSearchState.NotFound
                is FlightApiResult.NoApiKey -> FlightApiSearchState.NoApiKey
                is FlightApiResult.RateLimited -> FlightApiSearchState.RateLimited
                is FlightApiResult.NetworkError -> FlightApiSearchState.Error(result.message)
            }
            draft.update { it.copy(apiSearchState = state) }
        }
    }

    fun onApplyApiResult() {
        val found = draft.value.apiSearchState as? FlightApiSearchState.Found ?: return
        val prefill = found.prefill
        viewModelScope.launch {
            val origin = prefill.originIata?.let { airportRepository.getAirportByIata(it) }
            val destination = prefill.destinationIata?.let { airportRepository.getAirportByIata(it) }
            val airline = prefill.airlineIata?.let { airlineRepository.getAirlineByIata(it.uppercase()) }
            lookupAircraftUseCase(prefill.aircraftRegistration)
            val inferredStatus = inferFlightStatus(prefill.scheduledDepartureAt)
            draft.update { current ->
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
                )
            }
            airlineQuery.value = ""
        }
    }

    fun onSaveDraft() {
        val d = draft.value
        val origin = d.originAirport
        val destination = d.destinationAirport

        if (origin == null || destination == null) {
            draft.update { it.copy(validationError = "Cal seleccionar l'aeroport d'origen i de destí.") }
            return
        }
        if (origin.id == destination.id) {
            draft.update { it.copy(validationError = "L'origen i el destí no poden ser el mateix aeroport.") }
            return
        }

        val resolvedAirline = d.airlineIata ?: d.airlineQuery.trim().ifBlank { null }
        viewModelScope.launch {
            if (d.flightId == null) {
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
                    fetchedFrom = d.fetchedFrom,
                    externalProvider = d.externalProvider,
                    externalId = d.externalId,
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
                        airline = resolvedAirline,
                        flightNumber = d.flightNumber.trim().ifBlank { null },
                        aircraft = d.aircraft.trim().ifBlank { null },
                        aircraftRegistration = d.aircraftRegistration.trim().ifBlank { null },
                        notes = d.notes.trim().ifBlank { null },
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
            onDismissDraft()
        }
    }

    fun onDeleteFlight(flight: Flight) {
        viewModelScope.launch { deleteFlightUseCase(flight) }
    }

    fun onCreateItineraryClick() {
        itineraryDraft.update { ItineraryEditorDraft(isOpen = true) }
    }

    fun onEditItineraryClick(itinerary: Itinerary) {
        itineraryDraft.update {
            ItineraryEditorDraft(
                isOpen = true,
                itineraryId = itinerary.id,
                title = itinerary.title,
                notes = itinerary.notes ?: "",
            )
        }
    }

    fun onDismissItineraryDraft() {
        itineraryDraft.update { ItineraryEditorDraft() }
    }

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
        viewModelScope.launch {
            if (d.itineraryId == null) {
                createItineraryUseCase(title = title, notes = d.notes.trim().ifBlank { null })
            } else {
                val existingItinerary = uiState.value.itineraryItems
                    .firstOrNull { it.itinerary.id == d.itineraryId }
                    ?.itinerary
                updateItineraryUseCase(
                    Itinerary(
                        id = d.itineraryId,
                        title = title,
                        tripId = existingItinerary?.tripId,
                        notes = d.notes.trim().ifBlank { null },
                    ),
                )
            }
            onDismissItineraryDraft()
        }
    }

    fun onDeleteItinerary(itinerary: Itinerary) {
        viewModelScope.launch { deleteItineraryUseCase(itinerary) }
    }

    private suspend fun buildRouteSummary(groups: List<ItineraryGroup>): String {
        return groups
            .sortedBy { it.sortOrder }
            .mapNotNull { group ->
                val sortedFlights = group.flights.sortedWith(
                    compareBy<Flight> { it.sortOrder ?: Int.MAX_VALUE }
                        .thenBy { it.scheduledDepartureAt ?: "" },
                )
                val firstFlight = sortedFlights.firstOrNull()
                val lastFlight = sortedFlights.lastOrNull()
                if (firstFlight == null || lastFlight == null) {
                    null
                } else {
                    val origin = airportRepository.getAirportById(firstFlight.originAirportId)?.shortLabel()
                        ?: firstFlight.originAirportId.uppercase()
                    val destination = airportRepository.getAirportById(lastFlight.destinationAirportId)?.shortLabel()
                        ?: lastFlight.destinationAirportId.uppercase()
                    "$origin -> $destination"
                }
            }
            .joinToString(" · ")
    }

    class Factory(
        private val flightRepository: FlightRepository,
        private val itineraryRepository: ItineraryRepository,
        private val airportRepository: AirportRepository,
        private val airlineRepository: AirlineRepository,
        private val searchAirportsUseCase: SearchAirportsUseCase,
        private val searchAirlinesUseCase: SearchAirlinesUseCase,
        private val createFlightUseCase: CreateFlightUseCase,
        private val updateFlightUseCase: UpdateFlightUseCase,
        private val deleteFlightUseCase: DeleteFlightUseCase,
        private val lookupFlightUseCase: LookupFlightUseCase,
        private val lookupAircraftUseCase: LookupAircraftUseCase,
        private val createItineraryUseCase: CreateItineraryUseCase,
        private val updateItineraryUseCase: UpdateItineraryUseCase,
        private val deleteItineraryUseCase: DeleteItineraryUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = FlightListViewModel(
            flightRepository = flightRepository,
            itineraryRepository = itineraryRepository,
            airportRepository = airportRepository,
            airlineRepository = airlineRepository,
            searchAirportsUseCase = searchAirportsUseCase,
            searchAirlinesUseCase = searchAirlinesUseCase,
            createFlightUseCase = createFlightUseCase,
            updateFlightUseCase = updateFlightUseCase,
            deleteFlightUseCase = deleteFlightUseCase,
            lookupFlightUseCase = lookupFlightUseCase,
            lookupAircraftUseCase = lookupAircraftUseCase,
            createItineraryUseCase = createItineraryUseCase,
            updateItineraryUseCase = updateItineraryUseCase,
            deleteItineraryUseCase = deleteItineraryUseCase,
        ) as T
    }
}

data class FlightListUiState(
    val flightItems: List<FlightListItemUiState> = emptyList(),
    val itineraryItems: List<ItinerarySummaryUiState> = emptyList(),
    val draft: FlightEditorDraftUiState = FlightEditorDraftUiState(),
    val itineraryDraft: ItineraryEditorDraft = ItineraryEditorDraft(),
    val originSearchResults: List<Airport> = emptyList(),
    val destinationSearchResults: List<Airport> = emptyList(),
    val airlineSearchResults: List<Airline> = emptyList(),
)

data class FlightListItemUiState(
    val flight: Flight,
    val originLabel: String,
    val destinationLabel: String,
    val resolvedAirlineName: String? = null,
)

data class ItinerarySummaryUiState(
    val itinerary: Itinerary,
    val groupCount: Int,
    val flightCount: Int,
    val routeSummary: String,
)

data class FlightEditorDraftUiState(
    val isOpen: Boolean = false,
    val flightId: String? = null,
    val originAirport: Airport? = null,
    val originQuery: String = "",
    val destinationAirport: Airport? = null,
    val destinationQuery: String = "",
    val status: TravelStatus = TravelStatus.PLANNED,
    val scheduledDepartureAt: String = "",
    val scheduledArrivalAt: String = "",
    val actualDepartureAt: String = "",
    val actualArrivalAt: String = "",
    /** Text shown in the airline field — the resolved name or raw user input. */
    val airlineQuery: String = "",
    /** Structured IATA code; null when the user typed free text without selecting a suggestion. */
    val airlineIata: String? = null,
    val flightNumber: String = "",
    val aircraft: String = "",
    val aircraftRegistration: String = "",
    val notes: String = "",
    val validationError: String? = null,
    // Preserved when editing grouped flights; not shown to the user
    val itineraryGroupId: String? = null,
    val sortOrder: Int? = null,
    // Provenance — set automatically, not editable by user
    val fetchedFrom: String = "manual",
    val externalProvider: String? = null,
    val externalId: String? = null,
    // Country tracking — preserved from existing flight on edit
    val destinationCountsForCountryTracking: Boolean = true,
    val originCountsForCountryTracking: Boolean = false,
    // API search state — only relevant for new flights
    val apiFlightNumber: String = "",
    val apiSearchDate: String = "",
    val apiSearchState: FlightApiSearchState = FlightApiSearchState.Idle,
) {
    companion object {
        fun fromFlight(
            flight: Flight,
            origin: Airport?,
            destination: Airport?,
            airline: com.atlas.domain.model.Airline? = null,
        ): FlightEditorDraftUiState = FlightEditorDraftUiState(
            isOpen = true,
            flightId = flight.id,
            originAirport = origin,
            originQuery = origin?.displayLabel() ?: flight.originAirportId,
            destinationAirport = destination,
            destinationQuery = destination?.displayLabel() ?: flight.destinationAirportId,
            status = flight.status,
            scheduledDepartureAt = flight.scheduledDepartureAt ?: "",
            scheduledArrivalAt = flight.scheduledArrivalAt ?: "",
            actualDepartureAt = flight.actualDepartureAt ?: "",
            actualArrivalAt = flight.actualArrivalAt ?: "",
            airlineQuery = airline?.name ?: flight.airline ?: "",
            airlineIata = airline?.iata,
            flightNumber = flight.flightNumber ?: "",
            aircraft = flight.aircraft ?: "",
            aircraftRegistration = flight.aircraftRegistration ?: "",
            notes = flight.notes ?: "",
            itineraryGroupId = flight.itineraryGroupId,
            sortOrder = flight.sortOrder,
            fetchedFrom = flight.fetchedFrom,
            externalProvider = flight.externalProvider,
            externalId = flight.externalId,
            destinationCountsForCountryTracking = flight.destinationCountsForCountryTracking,
            originCountsForCountryTracking = flight.originCountsForCountryTracking,
        )
    }
}

fun Airport.displayLabel(): String {
    val code = iata ?: icao ?: id
    return "$code - $city ($name)"
}

private fun Airport.shortLabel(): String = iata ?: icao ?: city
