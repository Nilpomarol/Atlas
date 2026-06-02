package com.atlas.domain.model

data class Excursion(
    val id: String,
    val tripId: String,
    val anchorTripStopId: String?,
    val title: String,
    val notes: String?,
    val sortOrder: Int,
    val stops: List<ExcursionStop> = emptyList(),
)
