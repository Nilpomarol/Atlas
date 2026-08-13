package com.atlas.presentation.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atlas.domain.model.Country
import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.model.LocationSearchResult
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop
import com.atlas.domain.repository.CountryRepository
import com.atlas.domain.repository.TripRepository
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.domain.usecase.location.SearchLocationsUseCase
import com.atlas.domain.usecase.status.TravelStatusRefreshPolicy
import com.atlas.domain.usecase.trip.CreateTripWithFirstStopUseCase
import com.atlas.domain.usecase.trip.CreateTripUseCase
import com.atlas.domain.usecase.trip.UpdateTripUseCase
import com.atlas.domain.validation.FlexibleDateValidator
import com.atlas.presentation.date.FlexibleDateRangeDraftField
import com.atlas.presentation.date.FlexibleDateRangeDraftUiState
import com.atlas.presentation.date.updateField
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class TripListViewModel(
    tripRepository: TripRepository,
    countryRepository: CountryRepository,
    private val createTripUseCase: CreateTripUseCase,
    private val createTripWithFirstStopUseCase: CreateTripWithFirstStopUseCase,
    private val updateTripUseCase: UpdateTripUseCase,
    private val searchLocationsUseCase: SearchLocationsUseCase,
    private val flexibleDateValidator: FlexibleDateValidator,
    private val flexibleDateFormatter: FlexibleDateFormatter,
) : ViewModel() {
    private val draft = MutableStateFlow(TripEditorDraftUiState())
    private val quickDraft = MutableStateFlow(QuickTripDraftUiState())
    private var quickLocationSearchJob: Job? = null

    val uiState: StateFlow<TripListUiState> = combine(
        tripRepository.observeTrips(),
        tripRepository.observeTripStops(),
        countryRepository.observeTrackableCountries(),
        draft,
        quickDraft,
    ) { trips, stops, countries, draft, quickDraft ->
        val countryNamesByIso2 = countries.associate { it.iso2 to it.nameCa }
        val countryFlagsByIso2 = countries.associate { it.iso2 to it.flagEmoji }
        TripListUiState(
            tripItems = trips.map { trip ->
                trip.toListItem(
                    stops = stops.filter { it.tripId == trip.id },
                    formatter = flexibleDateFormatter,
                    countryNamesByIso2 = countryNamesByIso2,
                    countryFlagsByIso2 = countryFlagsByIso2,
                )
            }.sortedByTripDate(),
            countries = countries,
            draft = draft,
            quickDraft = quickDraft,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TripListUiState(),
        )

    fun onCreateTripClick() {
        draft.update { TripEditorDraftUiState(isOpen = true) }
    }

    fun onCreateQuickTripClick() {
        quickDraft.update { QuickTripDraftUiState(isOpen = true) }
    }

    fun onEditTripClick(trip: Trip) {
        draft.update { TripEditorDraftUiState.fromTrip(trip) }
    }

    fun onDismissDraft() {
        draft.update { TripEditorDraftUiState() }
    }

    fun onDismissQuickDraft() {
        quickLocationSearchJob?.cancel()
        quickDraft.update { QuickTripDraftUiState() }
    }

    fun onTitleChanged(title: String) {
        draft.update { it.copy(title = title, validationError = null) }
    }

    fun onStatusChanged(status: TravelStatus) {
        draft.update { it.copy(status = status) }
    }

    fun onDatePrecisionChanged(precision: DatePrecision) {
        draft.update {
            val dateRange = it.dateRange.copy(precision = precision)
            it.copy(
                dateRange = dateRange,
                status = inferDraftStatus(
                    dateRange = dateRange,
                    currentStatus = it.status,
                    isNew = it.tripId == null,
                    validator = flexibleDateValidator,
                ),
                validationError = null,
            )
        }
    }

    fun onDateFieldChanged(
        field: FlexibleDateRangeDraftField,
        value: String,
    ) {
        draft.update {
            val dateRange = it.dateRange.updateField(field, value)
            it.copy(
                dateRange = dateRange,
                status = inferDraftStatus(
                    dateRange = dateRange,
                    currentStatus = it.status,
                    isNew = it.tripId == null,
                    validator = flexibleDateValidator,
                ),
                validationError = null,
            )
        }
    }

    fun onNotesChanged(notes: String) {
        draft.update { it.copy(notes = notes) }
    }

    fun onQuickTitleChanged(title: String) {
        quickDraft.update { it.copy(title = title, validationError = null) }
    }

    fun onQuickStatusChanged(status: TravelStatus) {
        quickDraft.update { it.copy(status = status) }
    }

    fun onQuickDatePrecisionChanged(precision: DatePrecision) {
        quickDraft.update {
            val dateRange = it.dateRange.copy(precision = precision)
            it.copy(
                dateRange = dateRange,
                status = inferDraftStatus(
                    dateRange = dateRange,
                    currentStatus = it.status,
                    isNew = true,
                    validator = flexibleDateValidator,
                ),
                validationError = null,
            )
        }
    }

    fun onQuickDateFieldChanged(
        field: FlexibleDateRangeDraftField,
        value: String,
    ) {
        quickDraft.update {
            val dateRange = it.dateRange.updateField(field, value)
            it.copy(
                dateRange = dateRange,
                status = inferDraftStatus(
                    dateRange = dateRange,
                    currentStatus = it.status,
                    isNew = true,
                    validator = flexibleDateValidator,
                ),
                validationError = null,
            )
        }
    }

    fun onQuickLocationSearchQueryChanged(query: String) {
        quickDraft.update {
            it.copy(
                locationSearchQuery = query,
                locationSearchResults = emptyList(),
                isSearchingLocation = false,
                locationSearchError = null,
                validationError = null,
            )
        }

        quickLocationSearchJob?.cancel()
        val trimmedQuery = query.trim()
        if (trimmedQuery.length < 3) return

        quickLocationSearchJob = viewModelScope.launch {
            delay(400)
            quickDraft.update { it.copy(isSearchingLocation = true) }
            runCatching { searchLocationsUseCase(trimmedQuery) }
                .onSuccess { results ->
                    quickDraft.update {
                        it.copy(
                            isSearchingLocation = false,
                            locationSearchResults = results,
                            isManualEntryVisible = results.isEmpty(),
                            locationSearchError = if (results.isEmpty()) {
                                "No s'ha trobat cap lloc. Pots afegir-lo manualment."
                            } else {
                                null
                            },
                        )
                    }
                }
                .onFailure {
                    quickDraft.update {
                        it.copy(
                            isSearchingLocation = false,
                            locationSearchError = "La cerca no està disponible ara mateix.",
                        )
                    }
                }
        }
    }

    fun onQuickLocationSearchResultSelected(result: LocationSearchResult) {
        val supportedCountryIso2 = result.countryIso2
            ?.takeIf { iso2 -> uiState.value.countries.any { it.iso2 == iso2 } }

        quickDraft.update {
            it.copy(
                locationName = result.name,
                countryIso2 = supportedCountryIso2.orEmpty(),
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

    fun onUseManualQuickLocationClick() {
        quickDraft.update {
            it.copy(
                isManualEntryVisible = true,
                locationSearchResults = emptyList(),
                locationSearchError = null,
            )
        }
    }

    fun onQuickLocationNameChanged(locationName: String) {
        quickDraft.update { it.copy(locationName = locationName, validationError = null) }
    }

    fun onQuickCountryChanged(countryIso2: String) {
        quickDraft.update { it.copy(countryIso2 = countryIso2, validationError = null) }
    }

    fun onQuickLatitudeChanged(latitude: String) {
        quickDraft.update { it.copy(latitude = latitude, validationError = null) }
    }

    fun onQuickLongitudeChanged(longitude: String) {
        quickDraft.update { it.copy(longitude = longitude, validationError = null) }
    }

    fun onSaveDraft() {
        val currentDraft = draft.value
        val title = currentDraft.title.trim()
        val dateRange = currentDraft.dateRange.toDateRange()

        if (title.isBlank()) {
            draft.update { it.copy(validationError = "El títol és obligatori.") }
            return
        }

        if (dateRange == null && currentDraft.dateRange.hasAnyInput()) {
            draft.update { it.copy(validationError = "Revisa la data del viatge: falta algun camp o el format no és vàlid.") }
            return
        }

        if (dateRange != null && !flexibleDateValidator.isValid(dateRange)) {
            draft.update { it.copy(validationError = "Revisa la data del viatge: el rang o la precisió no són vàlids.") }
            return
        }

        viewModelScope.launch {
            if (currentDraft.tripId == null) {
                createTripUseCase(
                    title = title,
                    status = currentDraft.status,
                    dateRange = dateRange,
                    notes = currentDraft.notes,
                )
            } else {
                updateTripUseCase(
                    Trip(
                        id = currentDraft.tripId,
                        title = title,
                        status = currentDraft.status,
                        dateRange = dateRange,
                        notes = currentDraft.notes,
                        coverPhotoFilename = currentDraft.coverPhotoFilename,
                    ),
                )
            }
            onDismissDraft()
        }
    }

    fun onSaveQuickDraft() {
        val currentDraft = quickDraft.value
        val title = currentDraft.title.trim()
        val locationName = currentDraft.locationName.trim()
        val countryIso2 = currentDraft.countryIso2.trim().uppercase()
        val dateRange = currentDraft.dateRange.toDateRange()
        val latitude = currentDraft.latitude.trim().takeIf { it.isNotBlank() }?.toDoubleOrNull()
        val longitude = currentDraft.longitude.trim().takeIf { it.isNotBlank() }?.toDoubleOrNull()
        val hasInvalidLatitude = currentDraft.latitude.isNotBlank() && latitude == null
        val hasInvalidLongitude = currentDraft.longitude.isNotBlank() && longitude == null

        when {
            title.isBlank() -> {
                quickDraft.update { it.copy(validationError = "El títol és obligatori.") }
                return
            }
            dateRange == null && currentDraft.dateRange.hasAnyInput() -> {
                quickDraft.update { it.copy(validationError = "Revisa la data del viatge: falta algun camp o el format no és vàlid.") }
                return
            }
            dateRange != null && !flexibleDateValidator.isValid(dateRange) -> {
                quickDraft.update { it.copy(validationError = "Revisa la data del viatge: el rang o la precisió no són vàlids.") }
                return
            }
            locationName.isBlank() -> {
                quickDraft.update { it.copy(validationError = "El lloc és obligatori.") }
                return
            }
            countryIso2.isBlank() || uiState.value.countries.none { it.iso2 == countryIso2 } -> {
                quickDraft.update { it.copy(validationError = "Tria un país o territori de la llista.") }
                return
            }
            hasInvalidLatitude || hasInvalidLongitude -> {
                quickDraft.update { it.copy(validationError = "Revisa les coordenades del lloc.") }
                return
            }
        }

        viewModelScope.launch {
            createTripWithFirstStopUseCase(
                title = title,
                status = currentDraft.status,
                dateRange = dateRange,
                locationName = locationName,
                countryIso2 = countryIso2,
                latitude = latitude,
                longitude = longitude,
            )
            onDismissQuickDraft()
        }
    }

    class Factory(
        private val tripRepository: TripRepository,
        private val countryRepository: CountryRepository,
        private val createTripUseCase: CreateTripUseCase,
        private val createTripWithFirstStopUseCase: CreateTripWithFirstStopUseCase,
        private val updateTripUseCase: UpdateTripUseCase,
        private val searchLocationsUseCase: SearchLocationsUseCase,
        private val flexibleDateValidator: FlexibleDateValidator,
        private val flexibleDateFormatter: FlexibleDateFormatter,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TripListViewModel(
                tripRepository = tripRepository,
                countryRepository = countryRepository,
                createTripUseCase = createTripUseCase,
                createTripWithFirstStopUseCase = createTripWithFirstStopUseCase,
                updateTripUseCase = updateTripUseCase,
                searchLocationsUseCase = searchLocationsUseCase,
                flexibleDateValidator = flexibleDateValidator,
                flexibleDateFormatter = flexibleDateFormatter,
            ) as T
        }
    }
}

data class TripListUiState(
    val tripItems: List<TripListItemUiState> = emptyList(),
    val countries: List<Country> = emptyList(),
    val draft: TripEditorDraftUiState = TripEditorDraftUiState(),
    val quickDraft: QuickTripDraftUiState = QuickTripDraftUiState(),
)

data class TripListItemUiState(
    val trip: Trip,
    val stopCount: Int,
    val firstStopName: String?,
    val lastStopName: String?,
    val singleStopCountryIso2: String? = null,
    val singleStopCountryName: String? = null,
    val singleStopFlag: String? = null,
    val countryText: String? = null,
    val mapPoints: List<TripStopMapPoint> = emptyList(),
    val coverPhotoFilename: String? = null,
    val datePillText: String? = null,
) {
    /** A trip with a single place renders compactly; derived, never stored. */
    val isSinglePlace: Boolean = stopCount == 1
}

data class QuickTripDraftUiState(
    val isOpen: Boolean = false,
    val title: String = "",
    val status: TravelStatus = TravelStatus.PLANNED,
    val dateRange: FlexibleDateRangeDraftUiState = FlexibleDateRangeDraftUiState(),
    val locationName: String = "",
    val countryIso2: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val locationSearchQuery: String = "",
    val locationSearchResults: List<LocationSearchResult> = emptyList(),
    val isSearchingLocation: Boolean = false,
    val isManualEntryVisible: Boolean = false,
    val locationSearchError: String? = null,
    val validationError: String? = null,
)

data class TripStopMapPoint(
    val latitude: Double,
    val longitude: Double,
)

private fun Trip.toListItem(
    stops: List<TripStop>,
    formatter: FlexibleDateFormatter,
    countryNamesByIso2: Map<String, String>,
    countryFlagsByIso2: Map<String, String?>,
): TripListItemUiState {
    val orderedStops = stops.sortedBy { it.sortOrder }
    val singleStop = orderedStops.singleOrNull()
    val countryText = orderedStops
        .map { stop -> countryNamesByIso2[stop.countryIso2] ?: stop.countryIso2 }
        .distinct()
        .joinToString(", ")
        .ifBlank { null }
    return TripListItemUiState(
        trip = this,
        stopCount = orderedStops.size,
        firstStopName = orderedStops.firstOrNull()?.locationName,
        lastStopName = orderedStops.lastOrNull()?.locationName,
        singleStopCountryIso2 = singleStop?.countryIso2,
        singleStopCountryName = singleStop?.countryIso2?.let { countryNamesByIso2[it] },
        singleStopFlag = singleStop?.countryIso2?.let { countryFlagsByIso2[it] }?.takeIf { it.isNotBlank() },
        countryText = countryText,
        mapPoints = orderedStops
            .filter { it.isVisible }
            .mapNotNull { stop ->
                val latitude = stop.latitude ?: return@mapNotNull null
                val longitude = stop.longitude ?: return@mapNotNull null
                TripStopMapPoint(latitude = latitude, longitude = longitude)
            },
        coverPhotoFilename = coverPhotoFilename,
        datePillText = formatter.formatTripPill(dateRange),
    )
}

private fun List<TripListItemUiState>.sortedByTripDate(): List<TripListItemUiState> =
    sortedWith(
        compareBy<TripListItemUiState> { item -> item.trip.dateRange?.sortDate() == null }
            .thenByDescending { item -> item.trip.dateRange?.sortDate() ?: LocalDate.MIN }
            .thenBy { item -> item.trip.title.lowercase() },
    )

private fun FlexibleDateRange.sortDate(): LocalDate? =
    (start ?: end)?.sortBoundary()

private fun FlexibleDate.sortBoundary(): LocalDate? = runCatching {
    when (precision) {
        DatePrecision.YEAR -> LocalDate.of(year, 1, 1)
        DatePrecision.MONTH -> LocalDate.of(year, month ?: return null, 1)
        DatePrecision.DAY -> LocalDate.of(year, month ?: return null, day ?: return null)
    }
}.getOrNull()

private fun inferDraftStatus(
    dateRange: FlexibleDateRangeDraftUiState,
    currentStatus: TravelStatus,
    isNew: Boolean,
    validator: FlexibleDateValidator,
): TravelStatus {
    val parsedDateRange = dateRange.toDateRange()

    if (parsedDateRange == null) {
        return if (isNew && !dateRange.hasAnyInput()) {
            TravelStatus.PLANNED
        } else {
            currentStatus
        }
    }

    if (!validator.isValid(parsedDateRange)) {
        return currentStatus
    }

    return TravelStatusRefreshPolicy.inferredTripStatus(
        dateRange = parsedDateRange,
        today = LocalDate.now(),
    )
}
