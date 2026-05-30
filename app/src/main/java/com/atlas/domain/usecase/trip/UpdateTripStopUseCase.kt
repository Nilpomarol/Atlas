package com.atlas.domain.usecase.trip

import com.atlas.domain.model.TripStop
import com.atlas.domain.repository.TripRepository

class UpdateTripStopUseCase(
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(stop: TripStop) {
        tripRepository.updateTripStop(
            stop.copy(
                locationName = stop.locationName.trim(),
                notes = stop.notes?.trim()?.ifBlank { null },
            ),
        )
    }
}
