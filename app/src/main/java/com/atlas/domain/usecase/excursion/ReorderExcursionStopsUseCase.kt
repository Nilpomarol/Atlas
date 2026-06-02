package com.atlas.domain.usecase.excursion

import com.atlas.domain.model.ExcursionStop
import com.atlas.domain.repository.ExcursionRepository

class ReorderExcursionStopsUseCase(
    private val excursionRepository: ExcursionRepository,
) {
    suspend operator fun invoke(stops: List<ExcursionStop>) {
        excursionRepository.reorderExcursionStops(
            stops.mapIndexed { index, stop -> stop.copy(sortOrder = index) },
        )
    }
}
