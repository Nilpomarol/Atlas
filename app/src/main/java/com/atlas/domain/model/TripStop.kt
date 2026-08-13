package com.atlas.domain.model

data class TripStop(
    val id: String,
    val tripId: String,
    /** Null for a main-route stop; set for a place visited from [parentStopId]. */
    val parentStopId: String? = null,
    /** Optional label grouping siblings under one parent, e.g. "Dia de temples". */
    val sideTripLabel: String? = null,
    val locationName: String,
    val countryIso2: String,
    val latitude: Double?,
    val longitude: Double?,
    val dateRange: FlexibleDateRange?,
    val notes: String?,
    val sortOrder: Int,
    val source: TripStopSource = TripStopSource.MANUAL,
    val itineraryGroupId: String? = null,
    val isVisible: Boolean = true,
    val displayTitle: String? = null,
)

enum class TripStopSource {
    MANUAL,
    ITINERARY_GROUP,
}
