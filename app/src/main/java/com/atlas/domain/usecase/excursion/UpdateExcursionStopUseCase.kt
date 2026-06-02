package com.atlas.domain.usecase.excursion

import com.atlas.domain.model.ExcursionStop
import com.atlas.domain.repository.ExcursionRepository

class UpdateExcursionStopUseCase(
    private val excursionRepository: ExcursionRepository,
) {
    suspend operator fun invoke(stop: ExcursionStop) {
        excursionRepository.updateExcursionStop(
            stop.copy(
                locationName = stop.locationName.trim(),
                notes = stop.notes?.trim()?.ifBlank { null },
            ),
        )
    }
}
