package com.atlas.domain.usecase.itinerary

import com.atlas.domain.model.Flight
import com.atlas.domain.repository.FlightRepository

class ReorderGroupFlightsUseCase(
    private val flightRepository: FlightRepository,
) {
    suspend operator fun invoke(flights: List<Flight>) {
        flightRepository.reorderFlightsInGroup(
            flights.mapIndexed { index, flight -> flight.copy(sortOrder = index) },
        )
    }
}
