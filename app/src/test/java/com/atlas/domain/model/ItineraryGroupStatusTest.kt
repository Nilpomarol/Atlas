package com.atlas.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ItineraryGroupStatusTest {

    @Test
    fun `explicit status wins over the flights`() {
        val group = group(
            status = TravelStatus.COMPLETED,
            flights = listOf(flight(TravelStatus.PLANNED), flight(TravelStatus.PLANNED)),
        )

        assertEquals(TravelStatus.COMPLETED, group.effectiveStatus())
        assertEquals(TravelStatus.COMPLETED, group.displayStatus())
    }

    @Test
    fun `a completed flight makes the leg completed`() {
        val group = group(flights = listOf(flight(TravelStatus.COMPLETED), flight(TravelStatus.PLANNED)))

        assertEquals(TravelStatus.COMPLETED, group.effectiveStatus())
    }

    @Test
    fun `all planned flights make the leg planned`() {
        val group = group(flights = listOf(flight(TravelStatus.PLANNED), flight(TravelStatus.PLANNED)))

        assertEquals(TravelStatus.PLANNED, group.effectiveStatus())
    }

    @Test
    fun `a leg with no flights resolves to nothing`() {
        assertNull(group(flights = emptyList()).effectiveStatus())
        assertEquals(TravelStatus.UNKNOWN, group(flights = emptyList()).displayStatus())
    }

    /** Derivation stays conservative so a destination is not counted before arrival. */
    @Test
    fun `an in-progress leg is unresolved for derivation but shown as in progress`() {
        val group = group(flights = listOf(flight(TravelStatus.IN_PROGRESS)))

        assertNull(group.effectiveStatus())
        assertEquals(TravelStatus.IN_PROGRESS, group.displayStatus())
    }

    @Test
    fun `an unresolvable mix stays unknown`() {
        val group = group(flights = listOf(flight(TravelStatus.PLANNED), flight(TravelStatus.UNKNOWN)))

        assertNull(group.effectiveStatus())
        assertEquals(TravelStatus.UNKNOWN, group.displayStatus())
    }

    @Test
    fun `an itinerary is completed only when every leg is`() {
        val completed = listOf(
            group(flights = listOf(flight(TravelStatus.COMPLETED))),
            group(flights = listOf(flight(TravelStatus.COMPLETED))),
        )
        val partial = listOf(
            group(flights = listOf(flight(TravelStatus.COMPLETED))),
            group(flights = listOf(flight(TravelStatus.PLANNED))),
        )

        assertEquals(TravelStatus.COMPLETED, completed.itineraryDisplayStatus())
        assertEquals(TravelStatus.PLANNED, partial.itineraryDisplayStatus())
    }

    @Test
    fun `an itinerary with a leg under way reads as in progress`() {
        val groups = listOf(
            group(flights = listOf(flight(TravelStatus.COMPLETED))),
            group(flights = listOf(flight(TravelStatus.IN_PROGRESS))),
        )

        assertEquals(TravelStatus.IN_PROGRESS, groups.itineraryDisplayStatus())
    }

    @Test
    fun `an empty itinerary is unknown`() {
        assertEquals(TravelStatus.UNKNOWN, emptyList<ItineraryGroup>().itineraryDisplayStatus())
    }

    private fun group(
        status: TravelStatus? = null,
        flights: List<Flight>,
    ) = ItineraryGroup(
        id = "group-1",
        itineraryId = "itinerary-1",
        title = null,
        status = status,
        sortOrder = 0,
        flights = flights,
    )

    private var flightSeq = 0

    private fun flight(status: TravelStatus) = Flight(
        id = "flight-${flightSeq++}",
        originAirportId = "BCN",
        destinationAirportId = "NRT",
        status = status,
        scheduledDepartureAt = null,
        scheduledArrivalAt = null,
        actualDepartureAt = null,
        actualArrivalAt = null,
        airline = null,
        flightNumber = null,
        aircraft = null,
        notes = null,
        itineraryGroupId = "group-1",
        sortOrder = null,
    )
}
