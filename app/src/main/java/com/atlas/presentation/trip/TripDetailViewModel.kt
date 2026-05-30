package com.atlas.presentation.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atlas.domain.model.Country
import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.model.LocationSearchResult
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop
import com.atlas.domain.repository.CountryRepository
import com.atlas.domain.repository.TripRepository
import com.atlas.domain.usecase.trip.CreateTripStopUseCase
import com.atlas.domain.usecase.trip.DeleteTripUseCase
import com.atlas.domain.usecase.trip.DeleteTripStopUseCase
import com.atlas.domain.usecase.trip.ReorderTripStopsUseCase
import com.atlas.domain.usecase.trip.UpdateTripStopUseCase
import com.atlas.domain.usecase.trip.UpdateTripUseCase
import com.atlas.domain.usecase.location.SearchLocationsUseCase
import com.atlas.domain.validation.FlexibleDateValidator
import com.atlas.presentation.date.FlexibleDateRangeDraftField
import com.atlas.presentation.date.FlexibleDateRangeDraftUiState
import com.atlas.presentation.date.updateField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TripDetailViewModel(
    tripRepository: TripRepository,
    countryRepository: CountryRepository,
    private val deleteTripUseCase: DeleteTripUseCase,
    private val updateTripUseCase: UpdateTripUseCase,
    private val createTripStopUseCase: CreateTripStopUseCase,
    private val updateTripStopUseCase: UpdateTripStopUseCase,
    private val reorderTripStopsUseCase: ReorderTripStopsUseCase,
    private val deleteTripStopUseCase: DeleteTripStopUseCase,
    private val searchLocationsUseCase: SearchLocationsUseCase,
    private val flexibleDateValidator: FlexibleDateValidator,
    tripId: String,
) : ViewModel() {
    private val stopDraft = MutableStateFlow(TripStopDraftUiState())
    private val tripDraft = MutableStateFlow(TripEditorDraftUiState())

    val uiState: StateFlow<TripDetailUiState> = combine(
        tripRepository.observeTrip(tripId),
        tripRepository.observeTripStops(tripId),
        countryRepository.observeTrackableCountries(),
        stopDraft,
        tripDraft,
    ) { trip, stops, countries, stopDraft, tripDraft ->
        TripDetailUiState(
            trip = trip,
            stops = stops,
            countries = countries,
            stopDraft = stopDraft,
            tripDraft = tripDraft,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TripDetailUiState(),
        )

    fun onAddStopClick() {
        val firstCountryIso2 = uiState.value.countries.firstOrNull()?.iso2.orEmpty()
        val dateRange = uiState.value.trip?.dateRange?.start?.let { startDate ->
            FlexibleDateRangeDraftUiState.fromDateRange(
                FlexibleDateRange(
                    start = startDate,
                    end = null,
                    precision = uiState.value.trip?.dateRange?.precision ?: startDate.precision,
                ),
            )
        } ?: FlexibleDateRangeDraftUiState()

        stopDraft.update {
            TripStopDraftUiState(
                isOpen = true,
                countryIso2 = firstCountryIso2,
                dateRange = dateRange,
            )
        }
    }

    fun onDismissStopDraft() {
        stopDraft.update { TripStopDraftUiState() }
    }

    fun onEditTripClick() {
        val trip = uiState.value.trip ?: return
        tripDraft.update { TripEditorDraftUiState.fromTrip(trip) }
    }

    fun onDismissTripDraft() {
        tripDraft.update { TripEditorDraftUiState() }
    }

    fun onTripTitleChanged(title: String) {
        tripDraft.update { it.copy(title = title, validationError = null) }
    }

    fun onTripStatusChanged(status: com.atlas.domain.model.TravelStatus) {
        tripDraft.update { it.copy(status = status) }
    }

    fun onTripDatePrecisionChanged(precision: DatePrecision) {
        tripDraft.update {
            it.copy(
                dateRange = it.dateRange.copy(precision = precision),
                validationError = null,
            )
        }
    }

    fun onTripDateFieldChanged(
        field: FlexibleDateRangeDraftField,
        value: String,
    ) {
        tripDraft.update {
            it.copy(
                dateRange = it.dateRange.updateField(field, value),
                validationError = null,
            )
        }
    }

    fun onTripNotesChanged(notes: String) {
        tripDraft.update { it.copy(notes = notes) }
    }

    fun onSaveTripDraft() {
        val draft = tripDraft.value
        val tripId = draft.tripId ?: return
        val title = draft.title.trim()
        val dateRange = draft.dateRange.toDateRange()

        if (title.isBlank()) {
            tripDraft.update { it.copy(validationError = "El títol és obligatori.") }
            return
        }

        if (dateRange == null && draft.dateRange.hasAnyInput()) {
            tripDraft.update { it.copy(validationError = "Revisa la data del viatge: falta algun camp o el format no és vàlid.") }
            return
        }

        if (dateRange != null && !flexibleDateValidator.isValid(dateRange)) {
            tripDraft.update { it.copy(validationError = "Revisa la data del viatge: el rang o la precisió no són vàlids.") }
            return
        }

        viewModelScope.launch {
            updateTripUseCase(
                Trip(
                    id = tripId,
                    title = title,
                    status = draft.status,
                    dateRange = dateRange,
                    notes = draft.notes,
                ),
            )
            onDismissTripDraft()
        }
    }

    fun onEditStop(stop: TripStop) {
        stopDraft.update { TripStopDraftUiState.fromStop(stop) }
    }

    fun onStopLocationNameChanged(locationName: String) {
        stopDraft.update { it.copy(locationName = locationName, validationError = null) }
    }

    fun onLocationSearchQueryChanged(query: String) {
        stopDraft.update {
            it.copy(
                locationSearchQuery = query,
                locationSearchError = null,
                locationSearchResults = emptyList(),
            )
        }
    }

    fun onUseManualStopEntryClick() {
        stopDraft.update {
            it.copy(
                isManualEntryVisible = true,
                locationSearchResults = emptyList(),
                locationSearchError = null,
                validationError = null,
            )
        }
    }

    fun onSearchLocationClick() {
        val query = stopDraft.value.locationSearchQuery.trim()
        if (query.length < 3) {
            stopDraft.update { it.copy(locationSearchError = "Escriu almenys 3 caràcters per cercar.") }
            return
        }
        val currentDraft = stopDraft.value
        if (currentDraft.isSearchingLocation) return
        if (currentDraft.lastLocationSearchQuery == query && currentDraft.locationSearchResults.isNotEmpty()) {
            stopDraft.update { it.copy(locationSearchError = null) }
            return
        }

        stopDraft.update {
            it.copy(
                        isSearchingLocation = true,
                        locationSearchError = null,
                        locationSearchResults = emptyList(),
                        isManualEntryVisible = false,
                    )
                }

        viewModelScope.launch {
            runCatching {
                searchLocationsUseCase(query)
            }.onSuccess { results ->
                stopDraft.update {
                    it.copy(
                        isSearchingLocation = false,
                        locationSearchResults = results,
                        lastLocationSearchQuery = query,
                        isManualEntryVisible = results.isEmpty(),
                        locationSearchError = if (results.isEmpty()) {
                            "No s'ha trobat cap lloc. Pots afegir-lo manualment."
                        } else {
                            null
                        },
                    )
                }
            }.onFailure { error ->
                stopDraft.update {
                    it.copy(
                        isSearchingLocation = false,
                        locationSearchError = error.message ?: "La cerca no està disponible ara mateix.",
                    )
                }
            }
        }
    }

    fun onLocationSearchResultSelected(result: LocationSearchResult) {
        val supportedCountryIso2 = result.countryIso2
            ?.takeIf { iso2 -> uiState.value.countries.any { it.iso2 == iso2 } }

        stopDraft.update {
            it.copy(
                locationName = result.name,
                countryIso2 = supportedCountryIso2 ?: it.countryIso2,
                latitude = result.latitude.toString(),
                longitude = result.longitude.toString(),
                locationSearchQuery = result.name,
                locationSearchResults = emptyList(),
                isManualEntryVisible = supportedCountryIso2 == null,
                locationSearchError = if (supportedCountryIso2 == null) {
                    "S'han omplert nom i coordenades. Revisa el país manualment."
                } else {
                    null
                },
                validationError = null,
            )
        }
    }

    fun onStopCountryChanged(countryIso2: String) {
        stopDraft.update { it.copy(countryIso2 = countryIso2, validationError = null) }
    }

    fun onStopLatitudeChanged(latitude: String) {
        stopDraft.update { it.copy(latitude = latitude, validationError = null) }
    }

    fun onStopLongitudeChanged(longitude: String) {
        stopDraft.update { it.copy(longitude = longitude, validationError = null) }
    }

    fun onStopDatePrecisionChanged(precision: DatePrecision) {
        stopDraft.update {
            it.copy(
                dateRange = it.dateRange.copy(precision = precision),
                validationError = null,
            )
        }
    }

    fun onStopDateFieldChanged(
        field: FlexibleDateRangeDraftField,
        value: String,
    ) {
        stopDraft.update {
            it.copy(
                dateRange = it.dateRange.updateField(field, value),
                validationError = null,
            )
        }
    }

    fun onStopNotesChanged(notes: String) {
        stopDraft.update { it.copy(notes = notes) }
    }

    fun onSaveStopDraft() {
        val trip = uiState.value.trip ?: return
        val draft = stopDraft.value
        val locationName = draft.locationName.trim()
        val dateRange = draft.dateRange.toDateRange()
        val latitude = draft.latitude.trim().ifBlank { null }?.toDoubleOrNull()
        val longitude = draft.longitude.trim().ifBlank { null }?.toDoubleOrNull()

        if (locationName.isBlank()) {
            stopDraft.update { it.copy(validationError = "El nom del lloc és obligatori.") }
            return
        }

        if (draft.countryIso2.isBlank()) {
            stopDraft.update { it.copy(validationError = "Cal seleccionar un país o territori.") }
            return
        }

        if ((draft.latitude.isNotBlank() && latitude == null) || (draft.longitude.isNotBlank() && longitude == null)) {
            stopDraft.update { it.copy(validationError = "Les coordenades han de ser números vàlids.") }
            return
        }

        if ((draft.latitude.isBlank() && draft.longitude.isNotBlank()) || (draft.latitude.isNotBlank() && draft.longitude.isBlank())) {
            stopDraft.update { it.copy(validationError = "Informa latitud i longitud, o deixa totes dues buides.") }
            return
        }

        if (latitude != null && latitude !in -90.0..90.0) {
            stopDraft.update { it.copy(validationError = "La latitud ha d'estar entre -90 i 90.") }
            return
        }

        if (longitude != null && longitude !in -180.0..180.0) {
            stopDraft.update { it.copy(validationError = "La longitud ha d'estar entre -180 i 180.") }
            return
        }

        if (dateRange == null && draft.dateRange.hasAnyInput()) {
            stopDraft.update { it.copy(validationError = "Revisa la data de la parada: falta algun camp o el format no és vàlid.") }
            return
        }

        if (dateRange != null && !flexibleDateValidator.isValid(dateRange)) {
            stopDraft.update { it.copy(validationError = "Revisa la data de la parada: el rang o la precisió no són vàlids.") }
            return
        }

        viewModelScope.launch {
            if (draft.stopId == null) {
                createTripStopUseCase(
                    tripId = trip.id,
                    locationName = locationName,
                    countryIso2 = draft.countryIso2,
                    latitude = latitude,
                    longitude = longitude,
                    dateRange = dateRange,
                    notes = draft.notes,
                )
            } else {
                updateTripStopUseCase(
                    TripStop(
                        id = draft.stopId,
                        tripId = trip.id,
                        locationName = locationName,
                        countryIso2 = draft.countryIso2,
                        latitude = latitude,
                        longitude = longitude,
                        dateRange = dateRange,
                        notes = draft.notes,
                        sortOrder = draft.sortOrder,
                    ),
                )
            }
            onDismissStopDraft()
        }
    }

    fun onDeleteStop(stop: TripStop) {
        viewModelScope.launch {
            deleteTripStopUseCase(stop)
        }
    }

    fun onMoveStopUp(stop: TripStop) {
        moveStop(stop = stop, offset = -1)
    }

    fun onMoveStopDown(stop: TripStop) {
        moveStop(stop = stop, offset = 1)
    }

    fun onDeleteTrip() {
        val trip = uiState.value.trip ?: return
        viewModelScope.launch {
            deleteTripUseCase(trip)
        }
    }

    private fun moveStop(
        stop: TripStop,
        offset: Int,
    ) {
        val stops = uiState.value.stops.toMutableList()
        val fromIndex = stops.indexOfFirst { it.id == stop.id }
        val toIndex = fromIndex + offset
        if (fromIndex !in stops.indices || toIndex !in stops.indices) return

        val moved = stops.removeAt(fromIndex)
        stops.add(toIndex, moved)

        viewModelScope.launch {
            reorderTripStopsUseCase(stops)
        }
    }

    class Factory(
        private val tripRepository: TripRepository,
        private val countryRepository: CountryRepository,
        private val deleteTripUseCase: DeleteTripUseCase,
        private val updateTripUseCase: UpdateTripUseCase,
        private val createTripStopUseCase: CreateTripStopUseCase,
        private val updateTripStopUseCase: UpdateTripStopUseCase,
        private val reorderTripStopsUseCase: ReorderTripStopsUseCase,
        private val deleteTripStopUseCase: DeleteTripStopUseCase,
        private val searchLocationsUseCase: SearchLocationsUseCase,
        private val flexibleDateValidator: FlexibleDateValidator,
        private val tripId: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TripDetailViewModel(
                tripRepository = tripRepository,
                countryRepository = countryRepository,
                deleteTripUseCase = deleteTripUseCase,
                updateTripUseCase = updateTripUseCase,
                createTripStopUseCase = createTripStopUseCase,
                updateTripStopUseCase = updateTripStopUseCase,
                reorderTripStopsUseCase = reorderTripStopsUseCase,
                deleteTripStopUseCase = deleteTripStopUseCase,
                searchLocationsUseCase = searchLocationsUseCase,
                flexibleDateValidator = flexibleDateValidator,
                tripId = tripId,
            ) as T
        }
    }
}

data class TripDetailUiState(
    val trip: Trip? = null,
    val stops: List<TripStop> = emptyList(),
    val countries: List<Country> = emptyList(),
    val stopDraft: TripStopDraftUiState = TripStopDraftUiState(),
    val tripDraft: TripEditorDraftUiState = TripEditorDraftUiState(),
)
