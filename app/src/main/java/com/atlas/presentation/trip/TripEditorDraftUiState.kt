package com.atlas.presentation.trip

import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import com.atlas.presentation.date.FlexibleDateRangeDraftUiState

data class TripEditorDraftUiState(
    val isOpen: Boolean = false,
    val tripId: String? = null,
    val title: String = "",
    val status: TravelStatus = TravelStatus.UNKNOWN,
    val dateRange: FlexibleDateRangeDraftUiState = FlexibleDateRangeDraftUiState(),
    val notes: String = "",
    val validationError: String? = null,
    val coverPhotoFilename: String? = null,
) {
    companion object {
        fun fromTrip(trip: Trip): TripEditorDraftUiState = TripEditorDraftUiState(
            isOpen = true,
            tripId = trip.id,
            title = trip.title,
            status = trip.status,
            dateRange = FlexibleDateRangeDraftUiState.fromDateRange(trip.dateRange),
            notes = trip.notes.orEmpty(),
            coverPhotoFilename = trip.coverPhotoFilename,
        )
    }
}
