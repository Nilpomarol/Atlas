package com.atlas.domain.usecase.itinerary

import com.atlas.domain.repository.AirportRepository
import com.atlas.domain.repository.ItineraryRepository
import com.atlas.domain.repository.TripRepository
import com.atlas.domain.service.ItineraryGeneratedStopService

class SyncGeneratedTripStopsForItineraryUseCase(
    private val itineraryRepository: ItineraryRepository,
    private val tripRepository: TripRepository,
    private val airportRepository: AirportRepository,
    private val itineraryGeneratedStopService: ItineraryGeneratedStopService,
) {
    suspend operator fun invoke(itineraryId: String, tripId: String) {
        val groups = itineraryRepository.getGroups(itineraryId)
        val airportIds = groups
            .flatMap { group ->
                group.flights.flatMap { flight ->
                    listOf(flight.originAirportId, flight.destinationAirportId)
                }
            }
            .distinct()
        val airportsById = airportIds.mapNotNull { airportId ->
            airportRepository.getAirportById(airportId)?.let { airport -> airportId to airport }
        }.toMap()
        val generatedStops = itineraryGeneratedStopService.buildGeneratedStops(
            groups = groups,
            airportsById = airportsById,
        )

        tripRepository.replaceGeneratedItineraryGroupStops(
            tripId = tripId,
            groupIds = groups.map { it.id },
            stops = generatedStops,
        )
    }
}
