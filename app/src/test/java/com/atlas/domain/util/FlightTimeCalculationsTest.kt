package com.atlas.domain.util

import com.atlas.domain.model.Flight
import com.atlas.domain.model.TravelStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class FlightTimeCalculationsTest {
    @Test
    fun durationUsesUtcWhenAvailable() {
        val flight = flight(
            scheduledDepartureAt = "2026-06-03T10:30",
            scheduledArrivalAt = "2026-06-04T06:45",
            scheduledDepartureUtc = "2026-06-03T08:30:00Z",
            scheduledArrivalUtc = "2026-06-03T21:45:00Z",
        )

        assertEquals(13L * 60 + 15, flight.utcAwareDurationMinutes())
    }

    @Test
    fun durationFallsBackToLocalWhenUtcIsMissing() {
        val flight = flight(
            scheduledDepartureAt = "2026-06-03T10:30",
            scheduledArrivalAt = "2026-06-03T12:00",
        )

        assertEquals(90L, flight.utcAwareDurationMinutes())
    }

    @Test
    fun layoverUsesUtcWhenAvailable() {
        val first = flight(
            scheduledArrivalAt = "2026-06-03T23:30",
            scheduledArrivalUtc = "2026-06-03T20:30:00Z",
        )
        val next = flight(
            scheduledDepartureAt = "2026-06-04T01:00",
            scheduledDepartureUtc = "2026-06-03T22:00:00Z",
        )

        assertEquals(90L, first.utcAwareLayoverDurationMinutesTo(next))
    }

    private fun flight(
        scheduledDepartureAt: String? = null,
        scheduledArrivalAt: String? = null,
        actualDepartureAt: String? = null,
        actualArrivalAt: String? = null,
        scheduledDepartureUtc: String? = null,
        scheduledArrivalUtc: String? = null,
        actualDepartureUtc: String? = null,
        actualArrivalUtc: String? = null,
    ): Flight = Flight(
        id = "flight-id",
        originAirportId = "BCN",
        destinationAirportId = "HND",
        status = TravelStatus.PLANNED,
        scheduledDepartureAt = scheduledDepartureAt,
        scheduledArrivalAt = scheduledArrivalAt,
        actualDepartureAt = actualDepartureAt,
        actualArrivalAt = actualArrivalAt,
        scheduledDepartureUtc = scheduledDepartureUtc,
        scheduledArrivalUtc = scheduledArrivalUtc,
        actualDepartureUtc = actualDepartureUtc,
        actualArrivalUtc = actualArrivalUtc,
        airline = null,
        flightNumber = null,
        aircraft = null,
        notes = null,
        itineraryGroupId = null,
        sortOrder = null,
    )
}
