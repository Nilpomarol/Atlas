package com.atlas.presentation.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop
import com.atlas.domain.repository.TripRepository
import com.atlas.domain.usecase.trip.CreateTripUseCase
import com.atlas.domain.usecase.trip.UpdateTripUseCase
import com.atlas.domain.validation.FlexibleDateValidator
import com.atlas.presentation.date.FlexibleDateRangeDraftField
import com.atlas.presentation.date.updateField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TripListViewModel(
    tripRepository: TripRepository,
    private val createTripUseCase: CreateTripUseCase,
    private val updateTripUseCase: UpdateTripUseCase,
    private val flexibleDateValidator: FlexibleDateValidator,
) : ViewModel() {
    private val draft = MutableStateFlow(TripEditorDraftUiState())

    val uiState: StateFlow<TripListUiState> = combine(
        tripRepository.observeTrips(),
        tripRepository.observeTripStops(),
        draft,
    ) { trips, stops, draft ->
        TripListUiState(
            tripItems = trips.map { trip ->
                trip.toListItem(stops.filter { it.tripId == trip.id })
            },
            draft = draft,
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

    fun onEditTripClick(trip: Trip) {
        draft.update { TripEditorDraftUiState.fromTrip(trip) }
    }

    fun onDismissDraft() {
        draft.update { TripEditorDraftUiState() }
    }

    fun onTitleChanged(title: String) {
        draft.update { it.copy(title = title, validationError = null) }
    }

    fun onStatusChanged(status: TravelStatus) {
        draft.update { it.copy(status = status) }
    }

    fun onDatePrecisionChanged(precision: DatePrecision) {
        draft.update {
            it.copy(
                dateRange = it.dateRange.copy(precision = precision),
                validationError = null,
            )
        }
    }

    fun onDateFieldChanged(
        field: FlexibleDateRangeDraftField,
        value: String,
    ) {
        draft.update {
            it.copy(
                dateRange = it.dateRange.updateField(field, value),
                validationError = null,
            )
        }
    }

    fun onNotesChanged(notes: String) {
        draft.update { it.copy(notes = notes) }
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
                    ),
                )
            }
            onDismissDraft()
        }
    }

    class Factory(
        private val tripRepository: TripRepository,
        private val createTripUseCase: CreateTripUseCase,
        private val updateTripUseCase: UpdateTripUseCase,
        private val flexibleDateValidator: FlexibleDateValidator,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TripListViewModel(
                tripRepository = tripRepository,
                createTripUseCase = createTripUseCase,
                updateTripUseCase = updateTripUseCase,
                flexibleDateValidator = flexibleDateValidator,
            ) as T
        }
    }
}

data class TripListUiState(
    val tripItems: List<TripListItemUiState> = emptyList(),
    val draft: TripEditorDraftUiState = TripEditorDraftUiState(),
)

data class TripListItemUiState(
    val trip: Trip,
    val stopCount: Int,
    val firstStopName: String?,
    val lastStopName: String?,
    val mapPoints: List<TripStopMapPoint> = emptyList(),
)

data class TripStopMapPoint(
    val latitude: Double,
    val longitude: Double,
)

private fun Trip.toListItem(stops: List<TripStop>): TripListItemUiState {
    val orderedStops = stops.sortedBy { it.sortOrder }
    return TripListItemUiState(
        trip = this,
        stopCount = orderedStops.size,
        firstStopName = orderedStops.firstOrNull()?.locationName,
        lastStopName = orderedStops.lastOrNull()?.locationName,
        mapPoints = orderedStops
            .filter { it.isVisible }
            .mapNotNull { stop ->
                val latitude = stop.latitude ?: return@mapNotNull null
                val longitude = stop.longitude ?: return@mapNotNull null
                TripStopMapPoint(latitude = latitude, longitude = longitude)
            },
    )
}
