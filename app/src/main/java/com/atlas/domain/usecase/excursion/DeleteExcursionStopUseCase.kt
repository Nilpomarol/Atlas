package com.atlas.domain.usecase.excursion

import com.atlas.domain.model.ExcursionStop
import com.atlas.domain.model.StopType
import com.atlas.domain.repository.ExcursionRepository
import com.atlas.domain.repository.StopPhotoRepository

class DeleteExcursionStopUseCase(
    private val excursionRepository: ExcursionRepository,
    private val stopPhotoRepository: StopPhotoRepository,
) {
    suspend operator fun invoke(stop: ExcursionStop) {
        stopPhotoRepository.deleteAllForStop(stop.id, StopType.EXCURSION_STOP)
        excursionRepository.deleteExcursionStop(stop)
    }
}
