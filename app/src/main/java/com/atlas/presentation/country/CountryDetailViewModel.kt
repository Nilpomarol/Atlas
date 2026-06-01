package com.atlas.presentation.country

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atlas.domain.model.Country
import com.atlas.domain.model.CountryLog
import com.atlas.domain.model.CountryLogType
import com.atlas.domain.model.CountryTrackingState
import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop
import com.atlas.domain.repository.CountryRepository
import com.atlas.domain.repository.TripRepository
import com.atlas.domain.service.CountryStateDerivationService
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.domain.validation.FlexibleDateValidator
import com.atlas.presentation.date.FlexibleDateRangeDraftField
import com.atlas.presentation.date.FlexibleDateRangeDraftUiState
import com.atlas.presentation.date.updateField
import com.atlas.domain.usecase.country.AddCountryLogUseCase
import com.atlas.domain.usecase.country.DeleteCountryLogUseCase
import com.atlas.domain.usecase.country.SetCurrentlyLivingCountryUseCase
import com.atlas.domain.usecase.country.ToggleWishedCountryUseCase
import com.atlas.domain.usecase.country.UpdateCountryLogUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CountryDetailViewModel(
    countryRepository: CountryRepository,
    tripRepository: TripRepository,
    private val toggleWishedCountryUseCase: ToggleWishedCountryUseCase,
    private val setCurrentlyLivingCountryUseCase: SetCurrentlyLivingCountryUseCase,
    private val addCountryLogUseCase: AddCountryLogUseCase,
    private val updateCountryLogUseCase: UpdateCountryLogUseCase,
    private val deleteCountryLogUseCase: DeleteCountryLogUseCase,
    countryStateDerivationService: CountryStateDerivationService,
    private val flexibleDateValidator: FlexibleDateValidator,
    iso2: String,
) : ViewModel() {
    private val logDraft = MutableStateFlow(CountryLogDraftUiState())

    private val countryTrackingState = combine(
        countryRepository.observeUserState(iso2),
        countryRepository.observeCountryLogs(iso2),
        tripRepository.observeTrips(),
        tripRepository.observeTripStops(),
    ) { userState, logs, trips, tripStops ->
        countryStateDerivationService.derive(
            countryIso2 = iso2,
            userState = userState,
            logs = logs,
            trips = trips,
            tripStops = tripStops.filter { it.countryIso2 == iso2 },
        )
    }

    private val countryDetailPills = combine(
        countryRepository.observeUserState(iso2),
        countryRepository.observeCountryLogs(iso2),
        tripRepository.observeTrips(),
        tripRepository.observeTripStops(),
    ) { userState, logs, trips, tripStops ->
        val tripsById = trips.associateBy { it.id }
        val stopsForCountry = tripStops.filter { it.countryIso2 == iso2 }
        CountryDetailPillUiState(
            wished = userState?.wished == true,
            lived = logs.any { it.type == CountryLogType.LIVED },
            currentlyLiving = userState?.currentlyLiving == true,
            planned = stopsForCountry.any { stop ->
                tripsById[stop.tripId]?.status == TravelStatus.PLANNED
            },
            visited = logs.any { it.type == CountryLogType.VISIT } ||
                stopsForCountry.any { stop ->
                    tripsById[stop.tripId]?.status in setOf(
                        TravelStatus.IN_PROGRESS,
                        TravelStatus.COMPLETED,
                    )
                },
        )
    }

    private val countryTripSummaries = combine(
        tripRepository.observeTrips(),
        tripRepository.observeTripStops(),
    ) { trips, tripStops ->
        val tripsById = trips.associateBy { it.id }
        tripStops
            .filter { it.countryIso2 == iso2 }
            .groupBy { it.tripId }
            .mapNotNull { (tripId, countryStops) ->
                val allStops = tripStops
                    .filter { it.tripId == tripId }
                    .sortedBy { it.sortOrder }
                tripsById[tripId]?.toCountryTripSummary(
                    countryStops = countryStops,
                    allStops = allStops,
                )
            }
            .sortedWith(
                compareBy<CountryTripSummaryUiState> { it.status == TravelStatus.UNKNOWN }
                    .thenBy { it.title },
            )
    }

    val uiState: StateFlow<CountryDetailUiState> = combine(
        combine(
            countryRepository.observeCountry(iso2),
            countryRepository.observeCountryLogs(iso2),
            logDraft,
            countryTrackingState,
            countryTripSummaries,
        ) { country, logs, draft, trackingState, tripSummaries ->
            CountryDetailUiState(
                country = country,
                logs = logs,
                logDraft = draft,
                trackingState = trackingState,
                tripSummaries = tripSummaries,
            )
        },
        countryDetailPills,
    ) { uiState, detailPills ->
        uiState.copy(detailPills = detailPills)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CountryDetailUiState(),
        )

    fun onWishedChanged(wished: Boolean) {
        val countryIso2 = uiState.value.country?.iso2 ?: return
        viewModelScope.launch {
            toggleWishedCountryUseCase(
                countryIso2 = countryIso2,
                wished = wished,
            )
        }
    }

    fun onSetCurrentlyLiving() {
        val countryIso2 = uiState.value.country?.iso2 ?: return
        viewModelScope.launch {
            setCurrentlyLivingCountryUseCase(countryIso2)
        }
    }

    fun onAddVisitLog() {
        logDraft.update {
            CountryLogDraftUiState(
                isOpen = true,
                type = CountryLogType.VISIT,
            )
        }
    }

    fun onDeleteLog(log: CountryLog) {
        viewModelScope.launch {
            deleteCountryLogUseCase(log)
        }
    }

    fun onEditLog(log: CountryLog) {
        logDraft.update {
            CountryLogDraftUiState.fromLog(log)
        }
    }

    fun onDismissLogDraft() {
        logDraft.update { CountryLogDraftUiState() }
    }

    fun onLogTypeChanged(type: CountryLogType) {
        logDraft.update {
            it.copy(type = type, validationError = null)
        }
    }

    fun onLogPrecisionChanged(precision: DatePrecision) {
        logDraft.update {
            it.copy(
                dateRange = it.dateRange.copy(precision = precision),
                validationError = null,
            )
        }
    }

    fun onLogDraftFieldChanged(
        field: FlexibleDateRangeDraftField,
        value: String,
    ) {
        logDraft.update {
            it.copy(
                dateRange = it.dateRange.updateField(
                    field = field,
                    value = value,
                ),
                validationError = null,
            )
        }
    }

    fun onLogNotesChanged(notes: String) {
        logDraft.update {
            it.copy(notes = notes)
        }
    }

    fun onSaveLogDraft() {
        val countryIso2 = uiState.value.country?.iso2 ?: return
        val draft = logDraft.value
        val type = draft.type
        val dateRange = draft.dateRange.toDateRange()
        val notes = draft.notes.trim().ifBlank { null }

        if (dateRange == null && draft.dateRange.hasAnyInput()) {
            logDraft.update {
                it.copy(validationError = "La data no és vàlida.")
            }
            return
        }

        if (dateRange != null && !flexibleDateValidator.isValid(dateRange)) {
            logDraft.update {
                it.copy(validationError = "La data no és vàlida.")
            }
            return
        }

        viewModelScope.launch {
            if (draft.logId == null) {
                addCountryLogUseCase(
                    countryIso2 = countryIso2,
                    type = type,
                    dateRange = dateRange,
                    notes = notes,
                )
            } else {
                updateCountryLogUseCase(
                    CountryLog(
                        id = draft.logId,
                        countryIso2 = countryIso2,
                        type = type,
                        dateRange = dateRange,
                        notes = notes,
                    ),
                )
            }
            onDismissLogDraft()
        }
    }

    class Factory(
        private val countryRepository: CountryRepository,
        private val tripRepository: TripRepository,
        private val toggleWishedCountryUseCase: ToggleWishedCountryUseCase,
        private val setCurrentlyLivingCountryUseCase: SetCurrentlyLivingCountryUseCase,
        private val addCountryLogUseCase: AddCountryLogUseCase,
        private val updateCountryLogUseCase: UpdateCountryLogUseCase,
        private val deleteCountryLogUseCase: DeleteCountryLogUseCase,
        private val countryStateDerivationService: CountryStateDerivationService,
        private val flexibleDateValidator: FlexibleDateValidator,
        private val iso2: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CountryDetailViewModel(
                countryRepository = countryRepository,
                tripRepository = tripRepository,
                toggleWishedCountryUseCase = toggleWishedCountryUseCase,
                setCurrentlyLivingCountryUseCase = setCurrentlyLivingCountryUseCase,
                addCountryLogUseCase = addCountryLogUseCase,
                updateCountryLogUseCase = updateCountryLogUseCase,
                deleteCountryLogUseCase = deleteCountryLogUseCase,
                countryStateDerivationService = countryStateDerivationService,
                flexibleDateValidator = flexibleDateValidator,
                iso2 = iso2,
            ) as T
        }
    }
}

data class CountryDetailUiState(
    val country: Country? = null,
    val logs: List<CountryLog> = emptyList(),
    val tripSummaries: List<CountryTripSummaryUiState> = emptyList(),
    val logDraft: CountryLogDraftUiState = CountryLogDraftUiState(),
    val trackingState: CountryTrackingState = CountryTrackingState.Empty,
    val detailPills: CountryDetailPillUiState = CountryDetailPillUiState(),
)

data class CountryDetailPillUiState(
    val wished: Boolean = false,
    val lived: Boolean = false,
    val currentlyLiving: Boolean = false,
    val planned: Boolean = false,
    val visited: Boolean = false,
)

data class CountryTripSummaryUiState(
    val tripId: String,
    val title: String,
    val status: TravelStatus,
    val dateRangeText: String?,
    val routeText: String?,
    val stopCount: Int,
)

data class CountryLogDraftUiState(
    val isOpen: Boolean = false,
    val logId: String? = null,
    val type: CountryLogType = CountryLogType.VISIT,
    val dateRange: FlexibleDateRangeDraftUiState = FlexibleDateRangeDraftUiState(),
    val notes: String = "",
    val validationError: String? = null,
) {
    companion object {
        fun fromLog(log: CountryLog): CountryLogDraftUiState {
            return CountryLogDraftUiState(
                isOpen = true,
                logId = log.id,
                type = log.type,
                dateRange = FlexibleDateRangeDraftUiState.fromDateRange(log.dateRange),
                notes = log.notes.orEmpty(),
            )
        }
    }
}

private fun Trip.toCountryTripSummary(
    countryStops: List<TripStop>,
    allStops: List<TripStop>,
): CountryTripSummaryUiState {
    val first = allStops.firstOrNull()?.locationName
    val last = allStops.lastOrNull()?.locationName
    return CountryTripSummaryUiState(
        tripId = id,
        title = title,
        status = status,
        dateRangeText = dateRange?.let { FlexibleDateFormatter().format(it) },
        routeText = when {
            first == null -> null
            last == null || first == last -> first
            else -> "$first → $last"
        },
        stopCount = countryStops.size,
    )
}
