package com.atlas.domain.service

import com.atlas.domain.model.Airport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FlightDerivedDataServiceTest {
    private val service = FlightDerivedDataService()

    @Test
    fun convertsAirportLocalTimesToUtc() {
        val derived = service.derive(
            originAirport = barcelona,
            destinationAirport = tokyo,
            scheduledDepartureAt = "2026-06-03T10:30",
            scheduledArrivalAt = "2026-06-04T06:45",
            actualDepartureAt = null,
            actualArrivalAt = null,
        )

        assertEquals("2026-06-03T08:30:00Z", derived.scheduledDepartureUtc)
        assertEquals("2026-06-03T21:45:00Z", derived.scheduledArrivalUtc)
    }

    @Test
    fun leavesUtcNullWhenTimezoneIsMissing() {
        val derived = service.derive(
            originAirport = barcelona.copy(timezone = null),
            destinationAirport = tokyo,
            scheduledDepartureAt = "2026-06-03T10:30",
            scheduledArrivalAt = null,
            actualDepartureAt = null,
            actualArrivalAt = null,
        )

        assertNull(derived.scheduledDepartureUtc)
    }

    @Test
    fun calculatesGreatCircleDistance() {
        val derived = service.derive(
            originAirport = barcelona,
            destinationAirport = tokyo,
            scheduledDepartureAt = null,
            scheduledArrivalAt = null,
            actualDepartureAt = null,
            actualArrivalAt = null,
        )

        assertEquals(10434.0, derived.distanceKm!!, 25.0)
    }

    private companion object {
        val barcelona = Airport(
            id = "BCN",
            iata = "BCN",
            icao = "LEBL",
            name = "Barcelona-El Prat Airport",
            city = "Barcelona",
            countryIso2 = "ES",
            latitude = 41.2974,
            longitude = 2.0833,
            timezone = "Europe/Madrid",
        )

        val tokyo = Airport(
            id = "HND",
            iata = "HND",
            icao = "RJTT",
            name = "Tokyo Haneda Airport",
            city = "Tokyo",
            countryIso2 = "JP",
            latitude = 35.5494,
            longitude = 139.7798,
            timezone = "Asia/Tokyo",
        )
    }
}
