package com.atlas.domain.usecase.excursion

import com.atlas.domain.model.ExcursionStop
import com.atlas.domain.repository.ExcursionRepository

class DeleteExcursionStopUseCase(
    private val excursionRepository: ExcursionRepository,
) {
    suspend operator fun invoke(stop: ExcursionStop) {
        excursionRepository.deleteExcursionStop(stop)
    }
}
