package com.atlas.domain.usecase.trip

import com.atlas.domain.model.StopType
import com.atlas.domain.model.TripStop
import com.atlas.domain.repository.StopPhotoRepository
import com.atlas.domain.repository.TripRepository

class DeleteTripStopUseCase(
    private val tripRepository: TripRepository,
    private val stopPhotoRepository: StopPhotoRepository,
) {
    suspend operator fun invoke(stop: TripStop) {
        stopPhotoRepository.deleteAllForStop(stop.id, StopType.TRIP_STOP)
        tripRepository.deleteTripStop(stop)
    }
}
