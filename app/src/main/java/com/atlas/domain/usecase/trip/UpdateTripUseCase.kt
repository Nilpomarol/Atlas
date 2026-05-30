package com.atlas.domain.usecase.trip

import com.atlas.domain.model.Trip
import com.atlas.domain.repository.TripRepository

class UpdateTripUseCase(
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(trip: Trip) {
        tripRepository.updateTrip(
            trip.copy(
                title = trip.title.trim(),
                notes = trip.notes?.trim()?.ifBlank { null },
            ),
        )
    }
}
