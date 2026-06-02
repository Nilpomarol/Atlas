package com.atlas.data.repository

import com.atlas.data.local.database.AtlasDatabase
import com.atlas.data.local.entity.FlightEntity
import com.atlas.data.local.mapper.toDomain
import com.atlas.domain.model.Flight
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.repository.FlightRepository
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FlightRepositoryImpl(
    database: AtlasDatabase,
) : FlightRepository {
    private val flightDao = database.flightDao()

    override fun observeFlights(): Flow<List<Flight>> =
        flightDao.observeAll().map { it.map { entity -> entity.toDomain() } }

    override fun observeFlight(id: String): Flow<Flight?> =
        flightDao.observeById(id).map { it?.toDomain() }

    override suspend fun createFlight(
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
    ) {
        val now = Instant.now().toString()
        flightDao.upsert(
            FlightEntity(
                id = UUID.randomUUID().toString(),
                originAirportId = originAirportId,
                destinationAirportId = destinationAirportId,
                status = status.name,
                scheduledDepartureAt = scheduledDepartureAt,
                scheduledArrivalAt = scheduledArrivalAt,
                actualDepartureAt = actualDepartureAt,
                actualArrivalAt = actualArrivalAt,
                airline = airline,
                flightNumber = flightNumber,
                aircraft = aircraft,
                notes = notes,
                itineraryGroupId = itineraryGroupId,
                sortOrder = sortOrder,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    override suspend fun updateFlight(flight: Flight) {
        val now = Instant.now().toString()
        flightDao.upsert(
            FlightEntity(
                id = flight.id,
                originAirportId = flight.originAirportId,
                destinationAirportId = flight.destinationAirportId,
                status = flight.status.name,
                scheduledDepartureAt = flight.scheduledDepartureAt,
                scheduledArrivalAt = flight.scheduledArrivalAt,
                actualDepartureAt = flight.actualDepartureAt,
                actualArrivalAt = flight.actualArrivalAt,
                airline = flight.airline,
                flightNumber = flight.flightNumber,
                aircraft = flight.aircraft,
                notes = flight.notes,
                itineraryGroupId = flight.itineraryGroupId,
                sortOrder = flight.sortOrder,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    override suspend fun deleteFlight(flight: Flight) {
        flightDao.delete(
            FlightEntity(
                id = flight.id,
                originAirportId = flight.originAirportId,
                destinationAirportId = flight.destinationAirportId,
                status = flight.status.name,
                scheduledDepartureAt = flight.scheduledDepartureAt,
                scheduledArrivalAt = flight.scheduledArrivalAt,
                actualDepartureAt = flight.actualDepartureAt,
                actualArrivalAt = flight.actualArrivalAt,
                airline = flight.airline,
                flightNumber = flight.flightNumber,
                aircraft = flight.aircraft,
                notes = flight.notes,
                itineraryGroupId = flight.itineraryGroupId,
                sortOrder = flight.sortOrder,
                createdAt = "",
                updatedAt = "",
            ),
        )
    }

    override suspend fun reorderFlightsInGroup(flights: List<Flight>) {
        val now = Instant.now().toString()
        flights.forEachIndexed { index, flight ->
            flightDao.updateSortOrder(id = flight.id, sortOrder = index, updatedAt = now)
        }
    }
}
