package com.atlas.domain.model

/**
 * Data pre-filled from a flight API lookup, ready to populate the flight editor draft.
 * All fields are nullable — the API may not return everything.
 */
data class FlightApiPrefill(
    val flightNumber: String?,
    val airlineIata: String?,
    val airlineName: String?,
    val originIata: String?,
    val destinationIata: String?,
    /** Local departure time as "YYYY-MM-DDTHH:mm" */
    val scheduledDepartureAt: String?,
    /** Local arrival time as "YYYY-MM-DDTHH:mm" */
    val scheduledArrivalAt: String?,
    val aircraftModel: String?,
    val aircraftRegistration: String?,
    val aircraftModeS: String?,
)
