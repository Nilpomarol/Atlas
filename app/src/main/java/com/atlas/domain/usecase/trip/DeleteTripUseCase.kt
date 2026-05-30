package com.atlas.domain.usecase.trip

import com.atlas.domain.model.Trip
import com.atlas.domain.repository.TripRepository

class DeleteTripUseCase(
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(trip: Trip) {
        tripRepository.deleteTrip(trip)
    }
}
