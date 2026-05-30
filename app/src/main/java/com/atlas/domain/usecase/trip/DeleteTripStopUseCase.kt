package com.atlas.domain.usecase.trip

import com.atlas.domain.model.TripStop
import com.atlas.domain.repository.TripRepository

class DeleteTripStopUseCase(
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(stop: TripStop) {
        tripRepository.deleteTripStop(stop)
    }
}
