package com.atlas.domain.usecase.itinerary

import com.atlas.domain.model.TravelStatus
import com.atlas.domain.repository.ItineraryRepository

class CreateItineraryGroupUseCase(
    private val itineraryRepository: ItineraryRepository,
) {
    suspend operator fun invoke(
        itineraryId: String,
        title: String?,
        status: TravelStatus?,
        sortOrder: Int,
    ): String = itineraryRepository.createGroup(
        itineraryId = itineraryId,
        title = title?.trim()?.ifBlank { null },
        status = status,
        sortOrder = sortOrder,
    )
}
