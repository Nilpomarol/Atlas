package com.atlas.domain.service

import com.atlas.domain.model.Airport
import com.atlas.domain.model.Flight
import com.atlas.domain.model.ItineraryGroup
import com.atlas.domain.model.TravelStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ItineraryGeneratedStopServiceTest {
    private val service = ItineraryGeneratedStopService()

    @Test
    fun oneGroupUsesLastFlightDestination() {
        val group = group(
            id = "group-1",
            sortOrder = 0,
            flights = listOf(flight(originAirportId = "BCN", destinationAirportId = "HND")),
        )

        val stops = service.buildGeneratedStops(
            groups = listOf(group),
            airportsById = airportsById,
        )

        assertEquals(1, stops.size)
        assertEquals("group-1", stops.single().itineraryGroupId)
        assertEquals("Tokyo", stops.single().locationName)
        assertEquals("JP", stops.single().countryIso2)
        assertEquals("Barcelona → Tokyo", stops.single().displayTitle)
    }

    @Test
    fun lastGroupInMultiGroupItineraryUsesFirstFlightOrigin() {
        val outbound = group(
            id = "group-1",
            sortOrder = 0,
            flights = listOf(flight(originAirportId = "BCN", destinationAirportId = "HND")),
        )
        val returnGroup = group(
            id = "group-2",
            sortOrder = 1,
            flights = listOf(flight(originAirportId = "HND", destinationAirportId = "BCN")),
        )

        val stops = service.buildGeneratedStops(
            groups = listOf(outbound, returnGroup),
            airportsById = airportsById,
        )

        assertEquals(listOf("Tokyo", "Tokyo"), stops.map { it.locationName })
        assertEquals(listOf("JP", "JP"), stops.map { it.countryIso2 })
        assertEquals(listOf("Barcelona → Tokyo", "Tokyo → Barcelona"), stops.map { it.displayTitle })
    }

    @Test
    fun groupTitleOverridesGeneratedRouteTitle() {
        val group = group(
            id = "group-1",
            sortOrder = 0,
            title = "Anada",
            flights = listOf(flight(originAirportId = "BCN", destinationAirportId = "HND")),
        )

        val stops = service.buildGeneratedStops(
            groups = listOf(group),
            airportsById = airportsById,
        )

        assertEquals("Anada", stops.single().displayTitle)
    }

    private fun group(
        id: String,
        sortOrder: Int,
        title: String? = null,
        flights: List<Flight>,
    ): ItineraryGroup = ItineraryGroup(
        id = id,
        itineraryId = "itinerary-1",
        title = title,
        status = TravelStatus.PLANNED,
        sortOrder = sortOrder,
        flights = flights,
    )

    private fun flight(
        originAirportId: String,
        destinationAirportId: String,
    ): Flight = Flight(
        id = "$originAirportId-$destinationAirportId",
        originAirportId = originAirportId,
        destinationAirportId = destinationAirportId,
        status = TravelStatus.PLANNED,
        scheduledDepartureAt = null,
        scheduledArrivalAt = null,
        actualDepartureAt = null,
        actualArrivalAt = null,
        airline = null,
        flightNumber = null,
        aircraft = null,
        notes = null,
        itineraryGroupId = null,
        sortOrder = null,
    )

    private val airportsById = listOf(
        airport(id = "BCN", city = "Barcelona", countryIso2 = "ES"),
        airport(id = "HND", city = "Tokyo", countryIso2 = "JP"),
    ).associateBy { it.id }

    private fun airport(
        id: String,
        city: String,
        countryIso2: String,
    ): Airport = Airport(
        id = id,
        iata = id,
        icao = null,
        name = "$city Airport",
        city = city,
        countryIso2 = countryIso2,
        latitude = 0.0,
        longitude = 0.0,
        timezone = null,
    )
}
