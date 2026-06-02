package com.atlas.domain.model

data class ItineraryGroup(
    val id: String,
    val itineraryId: String,
    val title: String?,
    val status: TravelStatus?,
    val sortOrder: Int,
    val flights: List<Flight>,
)
