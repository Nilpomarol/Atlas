package com.atlas.domain.model

data class Flight(
    val id: String,
    val originAirportId: String,
    val destinationAirportId: String,
    val status: TravelStatus,
    // All datetimes are ISO "YYYY-MM-DDTHH:mm", null when not recorded
    val scheduledDepartureAt: String?,
    val scheduledArrivalAt: String?,
    val actualDepartureAt: String?,
    val actualArrivalAt: String?,
    val scheduledDepartureUtc: String? = null,
    val scheduledArrivalUtc: String? = null,
    val actualDepartureUtc: String? = null,
    val actualArrivalUtc: String? = null,
    val distanceKm: Double? = null,
    val airline: String?,
    val flightNumber: String?,
    val aircraft: String?,
    val notes: String?,
    val aircraftRegistration: String? = null,
    val itineraryGroupId: String?,
    val sortOrder: Int?,
    // Provenance — how this flight record was created
    val fetchedFrom: String = "manual",   // "manual" | "api"
    val externalProvider: String? = null, // e.g. "aerodatabox"
    val externalId: String? = null,       // e.g. "LH401/2024-06-01"
    // Country tracking control
    val destinationCountsForCountryTracking: Boolean = true,
    val originCountsForCountryTracking: Boolean = false,
)
