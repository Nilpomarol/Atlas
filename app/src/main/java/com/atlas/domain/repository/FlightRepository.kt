package com.atlas.domain.repository

import com.atlas.domain.model.Flight
import com.atlas.domain.model.TravelStatus
import kotlinx.coroutines.flow.Flow

interface FlightRepository {
    fun observeFlights(): Flow<List<Flight>>
    fun observeFlight(id: String): Flow<Flight?>

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
        itineraryGroupId: String?,
        sortOrder: Int?,
    )

    suspend fun updateFlight(flight: Flight)
    suspend fun deleteFlight(flight: Flight)
    suspend fun reorderFlightsInGroup(flights: List<Flight>)
}
