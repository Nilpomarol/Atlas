package com.atlas.domain.usecase.itinerary

import com.atlas.domain.repository.ItineraryRepository
import com.atlas.domain.repository.TripRepository

class RemoveGeneratedTripStopsForItineraryUseCase(
    private val itineraryRepository: ItineraryRepository,
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(itineraryId: String) {
        val groupIds = itineraryRepository.getGroups(itineraryId).map { it.id }
        tripRepository.deleteGeneratedItineraryGroupStops(groupIds)
    }
}
