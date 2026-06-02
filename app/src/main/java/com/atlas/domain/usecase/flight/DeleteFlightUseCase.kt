package com.atlas.domain.usecase.flight

import com.atlas.domain.model.Flight
import com.atlas.domain.repository.FlightRepository

class DeleteFlightUseCase(
    private val flightRepository: FlightRepository,
) {
    suspend operator fun invoke(flight: Flight) {
        flightRepository.deleteFlight(flight)
    }
}
