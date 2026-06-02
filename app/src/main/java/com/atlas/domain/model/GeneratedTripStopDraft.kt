package com.atlas.domain.model

data class GeneratedTripStopDraft(
    val itineraryGroupId: String,
    val locationName: String,
    val countryIso2: String,
    val latitude: Double?,
    val longitude: Double?,
    val displayTitle: String?,
    val sortOrder: Int,
)
