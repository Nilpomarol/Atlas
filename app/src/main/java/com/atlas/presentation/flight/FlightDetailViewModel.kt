package com.atlas.presentation.flight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atlas.domain.model.Aircraft
import com.atlas.domain.model.AircraftType
import com.atlas.domain.model.Airline
import com.atlas.domain.model.Airport
import com.atlas.domain.model.Flight
import com.atlas.domain.repository.AircraftTypeRepository
import com.atlas.domain.repository.AirlineRepository
import com.atlas.domain.repository.AirportRepository
import com.atlas.domain.repository.FlightRepository
import com.atlas.domain.repository.ItineraryRepository
import com.atlas.domain.usecase.airline.SearchAirlinesUseCase
import com.atlas.domain.usecase.aircraft.LookupAircraftUseCase
import com.atlas.domain.usecase.airport.SearchAirportsUseCase
import com.atlas.domain.usecase.flight.DeleteFlightUseCase
import com.atlas.domain.usecase.flight.UpdateFlightUseCase
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
class FlightDetailViewModel(
    private val flightId: String,
    flightRepository: FlightRepository,
    itineraryRepository: ItineraryRepository,
    private val airportRepository: AirportRepository,
    private val airlineRepository: AirlineRepository,
    private val aircraftTypeRepository: AircraftTypeRepository,
    private val searchAirportsUseCase: SearchAirportsUseCase,
    private val searchAirlinesUseCase: SearchAirlinesUseCase,
    private val lookupAircraftUseCase: LookupAircraftUseCase,
    private val updateFlightUseCase: UpdateFlightUseCase,
    private val deleteFlightUseCase: DeleteFlightUseCase,
) : ViewModel() {

    private val draft = MutableStateFlow(FlightEditorDraftUiState())
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

    // Resolves the flight + its airports + context airports from adjacent group flights
    private val coreData = combine(
        flightRepository.observeFlight(flightId),
        itineraryRepository.observeAllGroups(),
    ) { flight, allGroups ->
        if (flight == null) return@combine FlightDetailCoreData()

        val origin = airportRepository.getAirportById(flight.originAirportId)
        val destination = airportRepository.getAirportById(flight.destinationAirportId)
        val resolvedAirlineName = flight.airline?.let { iata ->
            airlineRepository.getAirlineByIata(iata.uppercase())?.name ?: iata
        }
        val resolvedAircraftType = flight.aircraft?.let { aircraft ->
            aircraftTypeRepository.resolveAircraftType(aircraft)
        }
        val resolvedAircraft = lookupAircraftUseCase(flight.aircraftRegistration)

        val groupId = flight.itineraryGroupId
        if (groupId == null) {
            return@combine FlightDetailCoreData(
                flight = flight,
                originAirport = origin,
                destinationAirport = destination,
                resolvedAirlineName = resolvedAirlineName,
                resolvedAircraftType = resolvedAircraftType,
                resolvedAircraft = resolvedAircraft,
            )
        }

        val group = allGroups.firstOrNull { it.id == groupId }
        val sortedFlights = group?.flights?.sortedBy { it.sortOrder ?: Int.MAX_VALUE } ?: emptyList()
        val index = sortedFlights.indexOfFirst { it.id == flightId }

        val prevFlight = if (index > 0) sortedFlights[index - 1] else null
        val nextFlight = if (index >= 0 && index < sortedFlights.lastIndex) sortedFlights[index + 1] else null

        // Context: prev flight's origin (where the journey started), next flight's destination (where it ends)
        val prevContextAirport = prevFlight?.let { airportRepository.getAirportById(it.originAirportId) }
        val nextContextAirport = nextFlight?.let { airportRepository.getAirportById(it.destinationAirportId) }

        val positionLabel = if (group != null && sortedFlights.size > 1) {
            buildString {
                append("Vol ${index + 1} de ${sortedFlights.size}")
                group.title?.let { append(" · $it") }
            }
        } else null

        FlightDetailCoreData(
            flight = flight,
            originAirport = origin,
            destinationAirport = destination,
            prevContextAirport = prevContextAirport,
            nextContextAirport = nextContextAirport,
            groupPositionLabel = positionLabel,
            resolvedAirlineName = resolvedAirlineName,
            resolvedAircraftType = resolvedAircraftType,
            resolvedAircraft = resolvedAircraft,
        )
    }

    val uiState: StateFlow<FlightDetailUiState> = combine(
        coreData,
        draft,
        originResults,
        destinationResults,
        airlineResults,
    ) { data, draft, originRes, destRes, airlineRes ->
        FlightDetailUiState(
            flight = data.flight,
            originAirport = data.originAirport,
            destinationAirport = data.destinationAirport,
            prevContextAirport = data.prevContextAirport,
            nextContextAirport = data.nextContextAirport,
            groupPositionLabel = data.groupPositionLabel,
            resolvedAirlineName = data.resolvedAirlineName,
            resolvedAircraftType = data.resolvedAircraftType,
            resolvedAircraft = data.resolvedAircraft,
            draft = draft,
            originSearchResults = originRes,
            destinationSearchResults = destRes,
            airlineSearchResults = airlineRes,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FlightDetailUiState(),
    )

    fun onEditClick() {
        val flight = uiState.value.flight ?: return
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

    fun onStatusChanged(status: com.atlas.domain.model.TravelStatus) {
        draft.update { it.copy(status = status) }
    }

    fun onScheduledDepartureAtChanged(value: String) { draft.update { it.copy(scheduledDepartureAt = value) } }
    fun onScheduledArrivalAtChanged(value: String) { draft.update { it.copy(scheduledArrivalAt = value) } }
    fun onActualDepartureAtChanged(value: String) { draft.update { it.copy(actualDepartureAt = value) } }
    fun onActualArrivalAtChanged(value: String) { draft.update { it.copy(actualArrivalAt = value) } }
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

        viewModelScope.launch {
            updateFlightUseCase(
                Flight(
                    id = d.flightId ?: return@launch,
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
            onDismissDraft()
        }
    }

    fun onDeleteFlight(onDeleted: () -> Unit) {
        val flight = uiState.value.flight ?: return
        viewModelScope.launch {
            deleteFlightUseCase(flight)
            onDeleted()
        }
    }

    class Factory(
        private val flightId: String,
        private val flightRepository: FlightRepository,
        private val itineraryRepository: ItineraryRepository,
        private val airportRepository: AirportRepository,
        private val airlineRepository: AirlineRepository,
        private val aircraftTypeRepository: AircraftTypeRepository,
        private val searchAirportsUseCase: SearchAirportsUseCase,
        private val searchAirlinesUseCase: SearchAirlinesUseCase,
        private val lookupAircraftUseCase: LookupAircraftUseCase,
        private val updateFlightUseCase: UpdateFlightUseCase,
        private val deleteFlightUseCase: DeleteFlightUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = FlightDetailViewModel(
            flightId = flightId,
            flightRepository = flightRepository,
            itineraryRepository = itineraryRepository,
            airportRepository = airportRepository,
            airlineRepository = airlineRepository,
            aircraftTypeRepository = aircraftTypeRepository,
            searchAirportsUseCase = searchAirportsUseCase,
            searchAirlinesUseCase = searchAirlinesUseCase,
            lookupAircraftUseCase = lookupAircraftUseCase,
            updateFlightUseCase = updateFlightUseCase,
            deleteFlightUseCase = deleteFlightUseCase,
        ) as T
    }
}

private data class FlightDetailCoreData(
    val flight: Flight? = null,
    val originAirport: Airport? = null,
    val destinationAirport: Airport? = null,
    val prevContextAirport: Airport? = null,
    val nextContextAirport: Airport? = null,
    val groupPositionLabel: String? = null,
    val resolvedAirlineName: String? = null,
    val resolvedAircraftType: AircraftType? = null,
    val resolvedAircraft: Aircraft? = null,
)

data class FlightDetailUiState(
    val flight: Flight? = null,
    val originAirport: Airport? = null,
    val destinationAirport: Airport? = null,
    val prevContextAirport: Airport? = null,
    val nextContextAirport: Airport? = null,
    val groupPositionLabel: String? = null,
    val resolvedAirlineName: String? = null,
    val resolvedAircraftType: AircraftType? = null,
    val resolvedAircraft: Aircraft? = null,
    val draft: FlightEditorDraftUiState = FlightEditorDraftUiState(),
    val originSearchResults: List<Airport> = emptyList(),
    val destinationSearchResults: List<Airport> = emptyList(),
    val airlineSearchResults: List<Airline> = emptyList(),
)
