package com.atlas.data.local.mapper

import com.atlas.data.local.entity.FlightEntity
import com.atlas.domain.model.Flight
import com.atlas.domain.model.TravelStatus

fun FlightEntity.toDomain(): Flight = Flight(
    id = id,
    originAirportId = originAirportId,
    destinationAirportId = destinationAirportId,
    status = TravelStatus.valueOf(status),
    scheduledDepartureAt = scheduledDepartureAt,
    scheduledArrivalAt = scheduledArrivalAt,
    actualDepartureAt = actualDepartureAt,
    actualArrivalAt = actualArrivalAt,
    scheduledDepartureUtc = scheduledDepartureUtc,
    scheduledArrivalUtc = scheduledArrivalUtc,
    actualDepartureUtc = actualDepartureUtc,
    actualArrivalUtc = actualArrivalUtc,
    distanceKm = distanceKm,
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
)
