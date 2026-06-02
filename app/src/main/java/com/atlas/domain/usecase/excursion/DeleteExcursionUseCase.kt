package com.atlas.domain.usecase.excursion

import com.atlas.domain.model.Excursion
import com.atlas.domain.repository.ExcursionRepository

class DeleteExcursionUseCase(
    private val excursionRepository: ExcursionRepository,
) {
    suspend operator fun invoke(excursion: Excursion) {
        excursionRepository.deleteExcursion(excursion)
    }
}
