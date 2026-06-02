package com.atlas.data.local.mapper

import com.atlas.data.local.entity.ItineraryEntity
import com.atlas.data.local.entity.ItineraryGroupEntity
import com.atlas.data.local.entity.ItineraryGroupWithFlights
import com.atlas.domain.model.Itinerary
import com.atlas.domain.model.ItineraryGroup
import com.atlas.domain.model.TravelStatus

fun ItineraryEntity.toDomain(): Itinerary = Itinerary(
    id = id,
    title = title,
    tripId = tripId,
    notes = notes,
)

fun ItineraryGroupWithFlights.toDomain(): ItineraryGroup = ItineraryGroup(
    id = group.id,
    itineraryId = group.itineraryId,
    title = group.title,
    status = group.status?.let { runCatching { TravelStatus.valueOf(it) }.getOrNull() },
    sortOrder = group.sortOrder,
    flights = flights
        .sortedWith(compareBy({ it.sortOrder ?: Int.MAX_VALUE }, { it.createdAt }))
        .map { it.toDomain() },
)

fun ItineraryGroup.toEntity(createdAt: String, updatedAt: String): ItineraryGroupEntity =
    ItineraryGroupEntity(
        id = id,
        itineraryId = itineraryId,
        title = title,
        status = status?.name,
        sortOrder = sortOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
