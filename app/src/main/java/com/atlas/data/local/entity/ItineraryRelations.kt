package com.atlas.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ItineraryGroupWithFlights(
    @Embedded val group: ItineraryGroupEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "itinerary_group_id",
    )
    val flights: List<FlightEntity>,
)
