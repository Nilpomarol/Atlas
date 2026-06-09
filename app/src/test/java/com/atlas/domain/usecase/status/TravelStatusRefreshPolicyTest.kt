package com.atlas.domain.usecase.status

import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.model.Flight
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TravelStatusRefreshPolicyTest {
    @Test
    fun plannedTripStartsOnStartBoundary() {
        val trip = trip(
            status = TravelStatus.PLANNED,
            dateRange = range(
                start = FlexibleDate(2026, 6, 8, DatePrecision.DAY),
                end = FlexibleDate(2026, 6, 10, DatePrecision.DAY),
            ),
        )

        assertEquals(
            TravelStatus.IN_PROGRESS,
            TravelStatusRefreshPolicy.refreshedTripStatus(trip, LocalDate.of(2026, 6, 8)),
        )
    }

    @Test
    fun tripCompletionWinsAfterEndBoundary() {
        val trip = trip(
            status = TravelStatus.PLANNED,
            dateRange = range(
                start = FlexibleDate(2026, 6, 1, DatePrecision.DAY),
                end = FlexibleDate(2026, 6, 5, DatePrecision.DAY),
            ),
        )

        assertEquals(
            TravelStatus.COMPLETED,
            TravelStatusRefreshPolicy.refreshedTripStatus(trip, LocalDate.of(2026, 6, 8)),
        )
    }

    @Test
    fun monthPrecisionTripUsesWholeMonthBoundaries() {
        val trip = trip(
            status = TravelStatus.PLANNED,
            dateRange = range(
                start = FlexibleDate(2026, 6, null, DatePrecision.MONTH),
                end = null,
            ),
        )

        assertEquals(
            TravelStatus.IN_PROGRESS,
            TravelStatusRefreshPolicy.refreshedTripStatus(trip, LocalDate.of(2026, 6, 30)),
        )
        assertEquals(
            TravelStatus.COMPLETED,
            TravelStatusRefreshPolicy.refreshedTripStatus(trip, LocalDate.of(2026, 7, 1)),
        )
    }

    @Test
    fun yearPrecisionTripUsesWholeYearBoundaries() {
        val trip = trip(
            status = TravelStatus.PLANNED,
            dateRange = range(
                start = FlexibleDate(2026, null, null, DatePrecision.YEAR),
                end = null,
            ),
        )

        assertEquals(
            TravelStatus.IN_PROGRESS,
            TravelStatusRefreshPolicy.refreshedTripStatus(trip, LocalDate.of(2026, 12, 31)),
        )
        assertEquals(
            TravelStatus.COMPLETED,
            TravelStatusRefreshPolicy.refreshedTripStatus(trip, LocalDate.of(2027, 1, 1)),
        )
    }

    @Test
    fun tripsWithoutDatesAndCompletedTripsAreUntouched() {
        assertNull(
            TravelStatusRefreshPolicy.refreshedTripStatus(
                trip(status = TravelStatus.PLANNED, dateRange = null),
                LocalDate.of(2026, 6, 8),
            ),
        )
        assertNull(
            TravelStatusRefreshPolicy.refreshedTripStatus(
                trip(
                    status = TravelStatus.COMPLETED,
                    dateRange = range(start = FlexibleDate(2026, 6, 1, DatePrecision.DAY), end = null),
                ),
                LocalDate.of(2026, 6, 8),
            ),
        )
    }

    @Test
    fun plannedFlightStartsOnDepartureDate() {
        val flight = flight(
            status = TravelStatus.PLANNED,
            scheduledDepartureAt = "2026-06-08T09:30",
            scheduledArrivalAt = "2026-06-08T11:00",
        )

        assertEquals(
            TravelStatus.IN_PROGRESS,
            TravelStatusRefreshPolicy.refreshedFlightStatus(flight, LocalDate.of(2026, 6, 8)),
        )
    }

    @Test
    fun flightCompletesAfterArrivalDate() {
        val flight = flight(
            status = TravelStatus.IN_PROGRESS,
            scheduledDepartureAt = "2026-06-07T22:30",
            scheduledArrivalAt = "2026-06-08T02:15",
        )

        assertEquals(
            TravelStatus.COMPLETED,
            TravelStatusRefreshPolicy.refreshedFlightStatus(flight, LocalDate.of(2026, 6, 9)),
        )
    }

    @Test
    fun flightWithoutArrivalDoesNotAutoComplete() {
        val flight = flight(
            status = TravelStatus.PLANNED,
            scheduledDepartureAt = "2026-06-07T22:30",
            scheduledArrivalAt = null,
        )

        assertEquals(
            TravelStatus.IN_PROGRESS,
            TravelStatusRefreshPolicy.refreshedFlightStatus(flight, LocalDate.of(2026, 6, 9)),
        )
    }

    @Test
    fun flightsWithoutDepartureAndCompletedFlightsAreUntouched() {
        assertNull(
            TravelStatusRefreshPolicy.refreshedFlightStatus(
                flight(
                    status = TravelStatus.PLANNED,
                    scheduledDepartureAt = null,
                    scheduledArrivalAt = "2026-06-07T22:30",
                ),
                LocalDate.of(2026, 6, 9),
            ),
        )
        assertNull(
            TravelStatusRefreshPolicy.refreshedFlightStatus(
                flight(
                    status = TravelStatus.COMPLETED,
                    scheduledDepartureAt = "2026-06-07T22:30",
                    scheduledArrivalAt = "2026-06-08T02:15",
                ),
                LocalDate.of(2026, 6, 9),
            ),
        )
    }

    private fun range(
        start: FlexibleDate?,
        end: FlexibleDate?,
    ): FlexibleDateRange = FlexibleDateRange(
        start = start,
        end = end,
        precision = start?.precision ?: end?.precision,
    )

    private fun trip(
        status: TravelStatus,
        dateRange: FlexibleDateRange?,
    ): Trip = Trip(
        id = "trip-1",
        title = "Trip",
        status = status,
        dateRange = dateRange,
        notes = null,
    )

    private fun flight(
        status: TravelStatus,
        scheduledDepartureAt: String?,
        scheduledArrivalAt: String?,
    ): Flight = Flight(
        id = "flight-1",
        originAirportId = "BCN",
        destinationAirportId = "NRT",
        status = status,
        scheduledDepartureAt = scheduledDepartureAt,
        scheduledArrivalAt = scheduledArrivalAt,
        actualDepartureAt = null,
        actualArrivalAt = null,
        airline = null,
        flightNumber = null,
        aircraft = null,
        notes = null,
        itineraryGroupId = null,
        sortOrder = null,
    )
}
