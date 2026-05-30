package com.atlas.domain.model

data class TripStop(
    val id: String,
    val tripId: String,
    val locationName: String,
    val countryIso2: String,
    val latitude: Double?,
    val longitude: Double?,
    val dateRange: FlexibleDateRange?,
    val notes: String?,
    val sortOrder: Int,
)
