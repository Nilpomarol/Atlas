package com.atlas.domain.usecase.itinerary

import com.atlas.domain.model.ItineraryGroup
import com.atlas.domain.repository.ItineraryRepository

class UpdateItineraryGroupUseCase(
    private val itineraryRepository: ItineraryRepository,
) {
    suspend operator fun invoke(group: ItineraryGroup) {
        itineraryRepository.updateGroup(group)
    }
}
