package com.atlas.domain.usecase.status

import com.atlas.domain.repository.FlightRepository
import com.atlas.domain.repository.TripRepository
import java.time.Instant
import java.time.LocalDate

class UpdateCurrentTravelStatusesUseCase(
    private val tripRepository: TripRepository,
    private val flightRepository: FlightRepository,
    private val todayProvider: () -> LocalDate = { LocalDate.now() },
    private val nowProvider: () -> String = { Instant.now().toString() },
) {
    suspend operator fun invoke() {
        val today = todayProvider()
        val now = nowProvider()

        tripRepository.getTripsForStatusRefresh().forEach { trip ->
            val refreshed = TravelStatusRefreshPolicy.refreshedTripStatus(trip, today)
            if (refreshed != null) {
                tripRepository.updateTripStatus(id = trip.id, status = refreshed, updatedAt = now)
            }
        }

        flightRepository.getFlightsForStatusRefresh().forEach { flight ->
            val refreshed = TravelStatusRefreshPolicy.refreshedFlightStatus(flight, today)
            if (refreshed != null) {
                flightRepository.updateFlightStatus(id = flight.id, status = refreshed, updatedAt = now)
            }
        }
    }
}
