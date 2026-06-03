package com.atlas.domain.usecase.flight

import com.atlas.domain.model.TravelStatus
import com.atlas.domain.repository.FlightRepository

class CreateFlightUseCase(
    private val flightRepository: FlightRepository,
) {
    suspend operator fun invoke(
        originAirportId: String,
        destinationAirportId: String,
        status: TravelStatus,
        scheduledDepartureAt: String?,
        scheduledArrivalAt: String?,
        actualDepartureAt: String?,
        actualArrivalAt: String?,
        airline: String?,
        flightNumber: String?,
        aircraft: String?,
        notes: String?,
        aircraftRegistration: String? = null,
        itineraryGroupId: String? = null,
        sortOrder: Int? = null,
        fetchedFrom: String = "manual",
        externalProvider: String? = null,
        externalId: String? = null,
        destinationCountsForCountryTracking: Boolean = true,
        originCountsForCountryTracking: Boolean = false,
    ) {
        flightRepository.createFlight(
            originAirportId = originAirportId,
            destinationAirportId = destinationAirportId,
            status = status,
            scheduledDepartureAt = scheduledDepartureAt,
            scheduledArrivalAt = scheduledArrivalAt,
            actualDepartureAt = actualDepartureAt,
            actualArrivalAt = actualArrivalAt,
            airline = airline?.trim()?.ifBlank { null },
            flightNumber = flightNumber?.trim()?.ifBlank { null },
            aircraft = aircraft?.trim()?.ifBlank { null },
            aircraftRegistration = aircraftRegistration?.trim()?.ifBlank { null },
            notes = notes?.trim()?.ifBlank { null },
            itineraryGroupId = itineraryGroupId,
            sortOrder = sortOrder,
            fetchedFrom = fetchedFrom,
            externalProvider = externalProvider,
            externalId = externalId,
            destinationCountsForCountryTracking = destinationCountsForCountryTracking,
            originCountsForCountryTracking = originCountsForCountryTracking,
        )
    }
}
