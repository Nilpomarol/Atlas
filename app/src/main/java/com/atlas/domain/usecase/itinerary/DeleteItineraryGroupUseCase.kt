package com.atlas.domain.usecase.itinerary

import com.atlas.domain.model.ItineraryGroup
import com.atlas.domain.repository.ItineraryRepository

class DeleteItineraryGroupUseCase(
    private val itineraryRepository: ItineraryRepository,
) {
    suspend operator fun invoke(group: ItineraryGroup) {
        itineraryRepository.deleteGroup(group)
    }
}
