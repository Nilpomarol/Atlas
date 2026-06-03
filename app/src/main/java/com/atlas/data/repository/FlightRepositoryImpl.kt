package com.atlas.data.repository

import com.atlas.data.local.database.AtlasDatabase
import com.atlas.data.local.entity.FlightEntity
import com.atlas.data.local.mapper.toDomain
import com.atlas.domain.model.Flight
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.repository.FlightRepository
import com.atlas.domain.service.FlightDerivedDataService
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FlightRepositoryImpl(
    database: AtlasDatabase,
) : FlightRepository {
    private val flightDao = database.flightDao()
    private val airportDao = database.airportDao()
    private val flightDerivedDataService = FlightDerivedDataService()

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
        aircraftRegistration: String?,
        itineraryGroupId: String?,
        sortOrder: Int?,
        fetchedFrom: String,
        externalProvider: String?,
        externalId: String?,
        destinationCountsForCountryTracking: Boolean,
        originCountsForCountryTracking: Boolean,
    ) {
        val now = Instant.now().toString()
        val derived = deriveFlightData(
            originAirportId = originAirportId,
            destinationAirportId = destinationAirportId,
            scheduledDepartureAt = scheduledDepartureAt,
            scheduledArrivalAt = scheduledArrivalAt,
            actualDepartureAt = actualDepartureAt,
            actualArrivalAt = actualArrivalAt,
        )
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
                scheduledDepartureUtc = derived.scheduledDepartureUtc,
                scheduledArrivalUtc = derived.scheduledArrivalUtc,
                actualDepartureUtc = derived.actualDepartureUtc,
                actualArrivalUtc = derived.actualArrivalUtc,
                distanceKm = derived.distanceKm,
                airline = airline,
                flightNumber = flightNumber,
                aircraft = aircraft,
                aircraftRegistration = aircraftRegistration,
                notes = notes,
                itineraryGroupId = itineraryGroupId,
                sortOrder = sortOrder,
                fetchedFrom = fetchedFrom,
                externalProvider = externalProvider,
                externalId = externalId,
                destinationCountsForCountryTracking = destinationCountsForCountryTracking,
                originCountsForCountryTracking = originCountsForCountryTracking,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    override suspend fun updateFlight(flight: Flight) {
        val now = Instant.now().toString()
        val derived = deriveFlightData(
            originAirportId = flight.originAirportId,
            destinationAirportId = flight.destinationAirportId,
            scheduledDepartureAt = flight.scheduledDepartureAt,
            scheduledArrivalAt = flight.scheduledArrivalAt,
            actualDepartureAt = flight.actualDepartureAt,
            actualArrivalAt = flight.actualArrivalAt,
        )
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
                scheduledDepartureUtc = derived.scheduledDepartureUtc,
                scheduledArrivalUtc = derived.scheduledArrivalUtc,
                actualDepartureUtc = derived.actualDepartureUtc,
                actualArrivalUtc = derived.actualArrivalUtc,
                distanceKm = derived.distanceKm,
                airline = flight.airline,
                flightNumber = flight.flightNumber,
                aircraft = flight.aircraft,
                aircraftRegistration = flight.aircraftRegistration,
                notes = flight.notes,
                itineraryGroupId = flight.itineraryGroupId,
                sortOrder = flight.sortOrder,
                fetchedFrom = flight.fetchedFrom,
                externalProvider = flight.externalProvider,
                externalId = flight.externalId,
                destinationCountsForCountryTracking = flight.destinationCountsForCountryTracking,
                originCountsForCountryTracking = flight.originCountsForCountryTracking,
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
                scheduledDepartureUtc = flight.scheduledDepartureUtc,
                scheduledArrivalUtc = flight.scheduledArrivalUtc,
                actualDepartureUtc = flight.actualDepartureUtc,
                actualArrivalUtc = flight.actualArrivalUtc,
                distanceKm = flight.distanceKm,
                airline = flight.airline,
                flightNumber = flight.flightNumber,
                aircraft = flight.aircraft,
                aircraftRegistration = flight.aircraftRegistration,
                notes = flight.notes,
                itineraryGroupId = flight.itineraryGroupId,
                sortOrder = flight.sortOrder,
                fetchedFrom = flight.fetchedFrom,
                externalProvider = flight.externalProvider,
                externalId = flight.externalId,
                destinationCountsForCountryTracking = flight.destinationCountsForCountryTracking,
                originCountsForCountryTracking = flight.originCountsForCountryTracking,
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

    private suspend fun deriveFlightData(
        originAirportId: String,
        destinationAirportId: String,
        scheduledDepartureAt: String?,
        scheduledArrivalAt: String?,
        actualDepartureAt: String?,
        actualArrivalAt: String?,
    ) = flightDerivedDataService.derive(
        originAirport = airportDao.getById(originAirportId)?.toDomain(),
        destinationAirport = airportDao.getById(destinationAirportId)?.toDomain(),
        scheduledDepartureAt = scheduledDepartureAt,
        scheduledArrivalAt = scheduledArrivalAt,
        actualDepartureAt = actualDepartureAt,
        actualArrivalAt = actualArrivalAt,
    )
}
