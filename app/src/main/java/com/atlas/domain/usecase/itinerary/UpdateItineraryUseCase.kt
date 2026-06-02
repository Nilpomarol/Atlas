package com.atlas.domain.usecase.itinerary

import com.atlas.domain.model.Itinerary
import com.atlas.domain.repository.ItineraryRepository

class UpdateItineraryUseCase(
    private val itineraryRepository: ItineraryRepository,
) {
    suspend operator fun invoke(itinerary: Itinerary) {
        itineraryRepository.updateItinerary(itinerary)
    }
}
