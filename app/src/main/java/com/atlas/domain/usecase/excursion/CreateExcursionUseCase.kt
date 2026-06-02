package com.atlas.domain.usecase.excursion

import com.atlas.domain.repository.ExcursionRepository

class CreateExcursionUseCase(
    private val excursionRepository: ExcursionRepository,
) {
    suspend operator fun invoke(
        tripId: String,
        anchorTripStopId: String?,
        title: String,
        notes: String?,
    ) {
        excursionRepository.createExcursion(
            tripId = tripId,
            anchorTripStopId = anchorTripStopId,
            title = title.trim(),
            notes = notes?.trim()?.ifBlank { null },
        )
    }
}
