package com.atlas.domain.model

data class Itinerary(
    val id: String,
    val title: String,
    val tripId: String?,
    val notes: String?,
)
