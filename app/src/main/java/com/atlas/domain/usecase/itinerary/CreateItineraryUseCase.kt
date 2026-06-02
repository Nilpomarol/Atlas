package com.atlas.domain.usecase.itinerary

import com.atlas.domain.repository.ItineraryRepository

class CreateItineraryUseCase(
    private val itineraryRepository: ItineraryRepository,
) {
    suspend operator fun invoke(title: String, notes: String?): String =
        itineraryRepository.createItinerary(
            title = title.trim(),
            notes = notes?.trim()?.ifBlank { null },
        )
}
