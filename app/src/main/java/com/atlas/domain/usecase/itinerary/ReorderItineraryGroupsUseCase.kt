package com.atlas.domain.usecase.itinerary

import com.atlas.domain.model.ItineraryGroup
import com.atlas.domain.repository.ItineraryRepository

class ReorderItineraryGroupsUseCase(
    private val itineraryRepository: ItineraryRepository,
) {
    suspend operator fun invoke(groups: List<ItineraryGroup>) {
        itineraryRepository.reorderGroups(
            groups.mapIndexed { index, group -> group.copy(sortOrder = index) },
        )
    }
}
