package com.atlas.domain.usecase.excursion

import com.atlas.domain.model.Excursion
import com.atlas.domain.repository.ExcursionRepository

class ReorderExcursionsUseCase(
    private val excursionRepository: ExcursionRepository,
) {
    suspend operator fun invoke(excursions: List<Excursion>) {
        excursionRepository.reorderExcursions(
            excursions.mapIndexed { index, excursion -> excursion.copy(sortOrder = index) },
        )
    }
}
