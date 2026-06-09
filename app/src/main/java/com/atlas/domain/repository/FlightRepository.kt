package com.atlas.domain.repository

import com.atlas.domain.model.Flight
import com.atlas.domain.model.TravelStatus
import kotlinx.coroutines.flow.Flow

interface FlightRepository {
    fun observeFlights(): Flow<List<Flight>>
    fun observeFlight(id: String): Flow<Flight?>
    suspend fun getFlightsForStatusRefresh(): List<Flight>

    suspend fun createFlight(
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
        itineraryGroupId: String?,
        sortOrder: Int?,
        fetchedFrom: String = "manual",
        externalProvider: String? = null,
        externalId: String? = null,
        destinationCountsForCountryTracking: Boolean = true,
        originCountsForCountryTracking: Boolean = false,
    )

    suspend fun updateFlight(flight: Flight)
    suspend fun updateFlightStatus(id: String, status: TravelStatus, updatedAt: String)
    suspend fun deleteFlight(flight: Flight)
    suspend fun reorderFlightsInGroup(flights: List<Flight>)
}
