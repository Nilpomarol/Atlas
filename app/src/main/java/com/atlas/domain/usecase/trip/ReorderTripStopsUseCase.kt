package com.atlas.domain.usecase.trip

import com.atlas.domain.model.TripStop
import com.atlas.domain.repository.TripRepository

class ReorderTripStopsUseCase(
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(stops: List<TripStop>) {
        tripRepository.reorderTripStops(
            stops.mapIndexed { index, stop ->
                stop.copy(sortOrder = index)
            },
        )
    }
}
