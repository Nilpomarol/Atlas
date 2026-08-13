package com.atlas.presentation.trip

import com.atlas.domain.model.LocationSearchResult
import com.atlas.domain.model.TripStop
import com.atlas.presentation.date.FlexibleDateRangeDraftUiState

data class TripStopDraftUiState(
    val stopId: String? = null,
    val isOpen: Boolean = false,
    /** Non-null while adding or editing a place visited from another stop. */
    val parentStopId: String? = null,
    val parentStopName: String? = null,
    val sideTripLabel: String = "",
    val locationName: String = "",
    val countryIso2: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val locationSearchQuery: String = "",
    val locationSearchResults: List<LocationSearchResult> = emptyList(),
    val isSearchingLocation: Boolean = false,
    val isManualEntryVisible: Boolean = false,
    val locationSearchError: String? = null,
    val lastLocationSearchQuery: String? = null,
    val dateRange: FlexibleDateRangeDraftUiState = FlexibleDateRangeDraftUiState(),
    val notes: String = "",
    val sortOrder: Int = 0,
    val validationError: String? = null,
) {
    val isEditing: Boolean = stopId != null

    companion object {
        fun fromStop(stop: TripStop, parentStopName: String? = null): TripStopDraftUiState = TripStopDraftUiState(
            stopId = stop.id,
            isOpen = true,
            parentStopId = stop.parentStopId,
            parentStopName = parentStopName,
            sideTripLabel = stop.sideTripLabel.orEmpty(),
            locationName = stop.locationName,
            countryIso2 = stop.countryIso2,
            latitude = stop.latitude?.toString().orEmpty(),
            longitude = stop.longitude?.toString().orEmpty(),
            isManualEntryVisible = stop.latitude == null || stop.longitude == null,
            locationSearchQuery = stop.locationName,
            dateRange = FlexibleDateRangeDraftUiState.fromDateRange(stop.dateRange),
            notes = stop.notes.orEmpty(),
            sortOrder = stop.sortOrder,
        )
    }
}
