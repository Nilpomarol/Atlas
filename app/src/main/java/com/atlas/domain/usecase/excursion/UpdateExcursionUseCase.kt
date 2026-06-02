package com.atlas.domain.usecase.excursion

import com.atlas.domain.model.Excursion
import com.atlas.domain.repository.ExcursionRepository

class UpdateExcursionUseCase(
    private val excursionRepository: ExcursionRepository,
) {
    suspend operator fun invoke(excursion: Excursion) {
        excursionRepository.updateExcursion(
            excursion.copy(
                title = excursion.title.trim(),
                notes = excursion.notes?.trim()?.ifBlank { null },
            ),
        )
    }
}
