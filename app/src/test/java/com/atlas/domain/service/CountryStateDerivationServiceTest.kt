package com.atlas.domain.service

import com.atlas.domain.model.CountryLog
import com.atlas.domain.model.CountryLogType
import com.atlas.domain.model.CountryUserState
import com.atlas.domain.model.Excursion
import com.atlas.domain.model.ExcursionStop
import com.atlas.domain.model.Flight
import com.atlas.domain.model.ItineraryGroup
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CountryStateDerivationServiceTest {
    private val service = CountryStateDerivationService()

    @Test
    fun emptyInputsAreNeverVisited() {
        val state = service.derive(userState = null)

        assertFalse(state.wished)
        assertFalse(state.currentlyLiving)
        assertFalse(state.lived)
        assertFalse(state.visited)
        assertFalse(state.planned)
        assertTrue(state.neverVisited)
    }

    @Test
    fun wishedDoesNotImplyVisited() {
        val state = service.derive(
            userState = CountryUserState(
                countryIso2 = COUNTRY_ISO2,
                wished = true,
                currentlyLiving = false,
            ),
        )

        assertTrue(state.wished)
        assertFalse(state.visited)
        assertFalse(state.lived)
        assertTrue(state.neverVisited)
    }

    @Test
    fun currentlyLivingImpliesLivedAndVisited() {
        val state = service.derive(
            userState = CountryUserState(
                countryIso2 = COUNTRY_ISO2,
                wished = false,
                currentlyLiving = true,
            ),
        )

        assertTrue(state.currentlyLiving)
        assertTrue(state.lived)
        assertTrue(state.visited)
        assertFalse(state.neverVisited)
    }

    @Test
    fun visitLogMarksVisitedOnly() {
        val state = service.derive(
            userState = null,
            logs = listOf(countryLog(type = CountryLogType.VISIT)),
        )

        assertTrue(state.visited)
        assertFalse(state.lived)
        assertFalse(state.neverVisited)
    }

    @Test
    fun livedLogMarksLivedAndVisited() {
        val state = service.derive(
            userState = null,
            logs = listOf(countryLog(type = CountryLogType.LIVED)),
        )

        assertTrue(state.lived)
        assertTrue(state.visited)
        assertFalse(state.neverVisited)
    }

    @Test
    fun wishedCanCoexistWithVisited() {
        val state = service.derive(
            userState = CountryUserState(
                countryIso2 = COUNTRY_ISO2,
                wished = true,
                currentlyLiving = false,
            ),
            logs = listOf(countryLog(type = CountryLogType.VISIT)),
        )

        assertTrue(state.wished)
        assertTrue(state.visited)
        assertFalse(state.neverVisited)
    }

    @Test
    fun plannedTripStopMarksCountryPlanned() {
        val state = service.derive(
            countryIso2 = COUNTRY_ISO2,
            userState = null,
            trips = listOf(trip(status = TravelStatus.PLANNED)),
            tripStops = listOf(tripStop(countryIso2 = COUNTRY_ISO2)),
        )

        assertTrue(state.planned)
        assertFalse(state.visited)
        assertTrue(state.neverVisited)
    }

    @Test
    fun completedTripStopMarksCountryVisited() {
        val state = service.derive(
            countryIso2 = COUNTRY_ISO2,
            userState = null,
            trips = listOf(trip(status = TravelStatus.COMPLETED)),
            tripStops = listOf(tripStop(countryIso2 = COUNTRY_ISO2)),
        )

        assertTrue(state.visited)
        assertFalse(state.neverVisited)
    }

    @Test
    fun inProgressTripStopMarksCountryVisited() {
        val state = service.derive(
            countryIso2 = COUNTRY_ISO2,
            userState = null,
            trips = listOf(trip(status = TravelStatus.IN_PROGRESS)),
            tripStops = listOf(tripStop(countryIso2 = COUNTRY_ISO2)),
        )

        assertTrue(state.visited)
        assertFalse(state.planned)
        assertFalse(state.neverVisited)
    }

    @Test
    fun unknownTripStopDoesNotAffectCountryState() {
        val state = service.derive(
            countryIso2 = COUNTRY_ISO2,
            userState = null,
            trips = listOf(trip(status = TravelStatus.UNKNOWN)),
            tripStops = listOf(tripStop(countryIso2 = COUNTRY_ISO2)),
        )

        assertFalse(state.planned)
        assertFalse(state.visited)
        assertTrue(state.neverVisited)
    }

    @Test
    fun tripStopForDifferentCountryDoesNotAffectRequestedCountry() {
        val state = service.derive(
            countryIso2 = COUNTRY_ISO2,
            userState = null,
            trips = listOf(trip(status = TravelStatus.COMPLETED)),
            tripStops = listOf(tripStop(countryIso2 = "FR")),
        )

        assertFalse(state.planned)
        assertFalse(state.visited)
        assertTrue(state.neverVisited)
    }

    @Test
    fun tripStopWithoutMatchingTripDoesNotAffectCountryState() {
        val state = service.derive(
            countryIso2 = COUNTRY_ISO2,
            userState = null,
            trips = emptyList(),
            tripStops = listOf(tripStop(countryIso2 = COUNTRY_ISO2)),
        )

        assertFalse(state.planned)
        assertFalse(state.visited)
        assertTrue(state.neverVisited)
    }

    @Test
    fun completedSoloFlightMarksDestinationVisited() {
        val state = service.derive(
            countryIso2 = COUNTRY_ISO2,
            userState = null,
            flights = listOf(flight(destinationAirportId = "HND", status = TravelStatus.COMPLETED)),
            airportCountryIso2ById = mapOf("HND" to COUNTRY_ISO2),
        )

        assertTrue(state.visited)
        assertFalse(state.planned)
        assertFalse(state.neverVisited)
    }

    @Test
    fun plannedSoloFlightMarksDestinationPlanned() {
        val state = service.derive(
            countryIso2 = COUNTRY_ISO2,
            userState = null,
            flights = listOf(flight(destinationAirportId = "HND", status = TravelStatus.PLANNED)),
            airportCountryIso2ById = mapOf("HND" to COUNTRY_ISO2),
        )

        assertTrue(state.planned)
        assertFalse(state.visited)
        assertTrue(state.neverVisited)
    }

    @Test
    fun inProgressAndUnknownSoloFlightsDoNotAffectCountryState() {
        listOf(TravelStatus.IN_PROGRESS, TravelStatus.UNKNOWN).forEach { status ->
            val state = service.derive(
                countryIso2 = COUNTRY_ISO2,
                userState = null,
                flights = listOf(flight(destinationAirportId = "HND", status = status)),
                airportCountryIso2ById = mapOf("HND" to COUNTRY_ISO2),
            )

            assertFalse(state.planned)
            assertFalse(state.visited)
            assertTrue(state.neverVisited)
        }
    }

    @Test
    fun groupedFlightsDoNotDirectlyMarkLayoverCountryVisited() {
        val outbound = itineraryGroup(
            id = "outbound",
            sortOrder = 0,
            status = TravelStatus.COMPLETED,
            flights = listOf(
                flight(id = "bcn-doh", originAirportId = "BCN", destinationAirportId = "DOH", itineraryGroupId = "outbound", sortOrder = 0),
                flight(id = "doh-hnd", originAirportId = "DOH", destinationAirportId = "HND", itineraryGroupId = "outbound", sortOrder = 1),
            ),
        )

        val state = service.derive(
            countryIso2 = "QA",
            userState = null,
            flights = outbound.flights,
            itineraryGroups = listOf(outbound),
            airportCountryIso2ById = mapOf(
                "BCN" to "ES",
                "DOH" to "QA",
                "HND" to COUNTRY_ISO2,
            ),
        )

        assertFalse(state.planned)
        assertFalse(state.visited)
        assertTrue(state.neverVisited)
    }

    @Test
    fun singleGroupItineraryDerivesDestinationOfLastFlight() {
        val outbound = itineraryGroup(
            id = "outbound",
            sortOrder = 0,
            status = TravelStatus.COMPLETED,
            flights = listOf(
                flight(id = "bcn-doh", originAirportId = "BCN", destinationAirportId = "DOH", itineraryGroupId = "outbound", sortOrder = 0),
                flight(id = "doh-hnd", originAirportId = "DOH", destinationAirportId = "HND", itineraryGroupId = "outbound", sortOrder = 1),
            ),
        )

        val state = service.derive(
            countryIso2 = COUNTRY_ISO2,
            userState = null,
            flights = outbound.flights,
            itineraryGroups = listOf(outbound),
            airportCountryIso2ById = mapOf(
                "BCN" to "ES",
                "DOH" to "QA",
                "HND" to COUNTRY_ISO2,
            ),
        )

        assertTrue(state.visited)
        assertFalse(state.planned)
        assertFalse(state.neverVisited)
    }

    @Test
    fun returnGroupDerivesOriginOfFirstFlightInMultiGroupItinerary() {
        val outbound = itineraryGroup(
            id = "outbound",
            sortOrder = 0,
            status = TravelStatus.COMPLETED,
            flights = listOf(
                flight(id = "bcn-doh", originAirportId = "BCN", destinationAirportId = "DOH", itineraryGroupId = "outbound", sortOrder = 0),
                flight(id = "doh-hnd", originAirportId = "DOH", destinationAirportId = "HND", itineraryGroupId = "outbound", sortOrder = 1),
            ),
        )
        val returnGroup = itineraryGroup(
            id = "return",
            sortOrder = 1,
            status = TravelStatus.COMPLETED,
            flights = listOf(
                flight(id = "kix-doh", originAirportId = "KIX", destinationAirportId = "DOH", itineraryGroupId = "return", sortOrder = 0),
                flight(id = "doh-bcn", originAirportId = "DOH", destinationAirportId = "BCN", itineraryGroupId = "return", sortOrder = 1),
            ),
        )

        val japanState = service.derive(
            countryIso2 = COUNTRY_ISO2,
            userState = null,
            flights = outbound.flights + returnGroup.flights,
            itineraryGroups = listOf(outbound, returnGroup),
            airportCountryIso2ById = mapOf(
                "BCN" to "ES",
                "DOH" to "QA",
                "HND" to COUNTRY_ISO2,
                "KIX" to COUNTRY_ISO2,
            ),
        )
        val spainState = service.derive(
            countryIso2 = "ES",
            userState = null,
            flights = outbound.flights + returnGroup.flights,
            itineraryGroups = listOf(outbound, returnGroup),
            airportCountryIso2ById = mapOf(
                "BCN" to "ES",
                "DOH" to "QA",
                "HND" to COUNTRY_ISO2,
                "KIX" to COUNTRY_ISO2,
            ),
        )

        assertTrue(japanState.visited)
        assertFalse(spainState.visited)
    }

    @Test
    fun completedParentTripExcursionStopMarksCountryVisited() {
        val state = service.derive(
            countryIso2 = COUNTRY_ISO2,
            userState = null,
            trips = listOf(trip(status = TravelStatus.COMPLETED)),
            excursions = listOf(excursion(countryIso2 = COUNTRY_ISO2)),
        )

        assertTrue(state.visited)
        assertFalse(state.neverVisited)
    }

    private fun countryLog(type: CountryLogType): CountryLog = CountryLog(
        id = "log-id",
        countryIso2 = COUNTRY_ISO2,
        type = type,
        dateRange = null,
        notes = null,
    )

    private fun trip(status: TravelStatus): Trip = Trip(
        id = TRIP_ID,
        title = "Japan",
        status = status,
        dateRange = null,
        notes = null,
    )

    private fun tripStop(countryIso2: String): TripStop = TripStop(
        id = "stop-id",
        tripId = TRIP_ID,
        locationName = "Tokyo",
        countryIso2 = countryIso2,
        latitude = null,
        longitude = null,
        dateRange = null,
        notes = null,
        sortOrder = 0,
    )

    private fun flight(
        id: String = "flight-id",
        originAirportId: String = "BCN",
        destinationAirportId: String = "HND",
        status: TravelStatus = TravelStatus.COMPLETED,
        itineraryGroupId: String? = null,
        sortOrder: Int? = null,
    ): Flight = Flight(
        id = id,
        originAirportId = originAirportId,
        destinationAirportId = destinationAirportId,
        status = status,
        scheduledDepartureAt = null,
        scheduledArrivalAt = null,
        actualDepartureAt = null,
        actualArrivalAt = null,
        airline = null,
        flightNumber = null,
        aircraft = null,
        notes = null,
        itineraryGroupId = itineraryGroupId,
        sortOrder = sortOrder,
    )

    private fun itineraryGroup(
        id: String,
        sortOrder: Int,
        status: TravelStatus?,
        flights: List<Flight>,
    ): ItineraryGroup = ItineraryGroup(
        id = id,
        itineraryId = "itinerary-id",
        title = null,
        status = status,
        sortOrder = sortOrder,
        flights = flights,
    )

    private fun excursion(countryIso2: String): Excursion = Excursion(
        id = "excursion-id",
        tripId = TRIP_ID,
        anchorTripStopId = null,
        title = "Nara",
        notes = null,
        sortOrder = 0,
        stops = listOf(
            ExcursionStop(
                id = "excursion-stop-id",
                excursionId = "excursion-id",
                locationName = "Nara",
                countryIso2 = countryIso2,
                latitude = null,
                longitude = null,
                dateRange = null,
                notes = null,
                sortOrder = 0,
            ),
        ),
    )

    private companion object {
        const val COUNTRY_ISO2 = "JP"
        const val TRIP_ID = "trip-id"
    }
}
