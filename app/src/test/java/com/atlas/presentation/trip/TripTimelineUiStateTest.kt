package com.atlas.presentation.trip

import com.atlas.domain.model.Airport
import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.model.Flight
import com.atlas.domain.model.ItineraryGroup
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.TripStop
import com.atlas.domain.model.TripStopSource
import org.junit.Assert.assertEquals
import org.junit.Test

class TripTimelineUiStateTest {

    /** The defect this ordering exists to fix: legs carry sortOrder 10_000+. */
    @Test
    fun `a mid-trip flight sits between the stops it connects`() {
        val timeline = buildTripTimeline(
            stops = listOf(
                stop("tokyo", "Tòquio", sortOrder = 0, day = 1),
                stop("osaka", "Osaka", sortOrder = 1, day = 8),
                legStop("leg", sortOrder = 10_000),
            ),
            groups = listOf(group("leg", departure = "2026-06-06T09:00")),
            airportsById = airports(),
        )

        assertEquals(listOf("Tòquio", "VOL", "Osaka"), timeline.labels())
    }

    @Test
    fun `a leg after the last stop stays at the end`() {
        val timeline = buildTripTimeline(
            stops = listOf(
                stop("tokyo", "Tòquio", sortOrder = 0, day = 1),
                stop("osaka", "Osaka", sortOrder = 1, day = 8),
                legStop("leg", sortOrder = 10_000),
            ),
            groups = listOf(group("leg", departure = "2026-06-14T09:00")),
            airportsById = airports(),
        )

        assertEquals(listOf("Tòquio", "Osaka", "VOL"), timeline.labels())
    }

    @Test
    fun `undated stops keep their manual order`() {
        val timeline = buildTripTimeline(
            stops = listOf(
                stop("c", "Kyoto", sortOrder = 2),
                stop("a", "Tòquio", sortOrder = 0),
                stop("b", "Osaka", sortOrder = 1),
            ),
        )

        assertEquals(listOf("Tòquio", "Osaka", "Kyoto"), timeline.labels())
    }

    @Test
    fun `an undated stop stays with the dated stop it follows`() {
        val timeline = buildTripTimeline(
            stops = listOf(
                stop("tokyo", "Tòquio", sortOrder = 0, day = 1),
                stop("hakone", "Hakone", sortOrder = 1),
                stop("osaka", "Osaka", sortOrder = 2, day = 8),
            ),
        )

        assertEquals(listOf("Tòquio", "Hakone", "Osaka"), timeline.labels())
    }

    @Test
    fun `places are numbered ignoring flight legs`() {
        val timeline = buildTripTimeline(
            stops = listOf(
                stop("tokyo", "Tòquio", sortOrder = 0, day = 1),
                legStop("leg", sortOrder = 10_000),
                stop("osaka", "Osaka", sortOrder = 1, day = 8),
            ),
            groups = listOf(group("leg", departure = "2026-06-06T09:00")),
            airportsById = airports(),
        )

        val numbers = timeline.filterIsInstance<TripTimelineEntry.Place>().map { it.number }
        assertEquals(listOf(1, 2), numbers)
    }

    @Test
    fun `side trips hang off their parent and never appear as their own row`() {
        val timeline = buildTripTimeline(
            stops = listOf(
                stop("tokyo", "Tòquio", sortOrder = 0),
                stop("kamakura", "Kamakura", sortOrder = 1, parentStopId = "tokyo"),
                stop("enoshima", "Enoshima", sortOrder = 2, parentStopId = "tokyo"),
            ),
        )

        val place = timeline.single() as TripTimelineEntry.Place
        assertEquals(listOf("Kamakura", "Enoshima"), place.sideTrips.map { it.locationName })
    }

    @Test
    fun `a leg falls back to its route when it has no display title`() {
        val timeline = buildTripTimeline(
            stops = listOf(legStop("leg", sortOrder = 0, displayTitle = null)),
            groups = listOf(group("leg", departure = "2026-06-06T09:00")),
            airportsById = airports(),
        )

        assertEquals("Tòquio → Osaka", (timeline.single() as TripTimelineEntry.Leg).title)
    }

    private fun List<TripTimelineEntry>.labels() = map { entry ->
        when (entry) {
            is TripTimelineEntry.Place -> entry.stop.locationName
            is TripTimelineEntry.Leg -> "VOL"
        }
    }

    private fun stop(
        id: String,
        name: String,
        sortOrder: Int,
        day: Int? = null,
        parentStopId: String? = null,
    ) = TripStop(
        id = id,
        tripId = "trip",
        parentStopId = parentStopId,
        locationName = name,
        countryIso2 = "JP",
        latitude = null,
        longitude = null,
        dateRange = day?.let {
            val date = FlexibleDate(year = 2026, month = 6, day = it, precision = DatePrecision.DAY)
            FlexibleDateRange(start = date, end = date, precision = DatePrecision.DAY)
        },
        notes = null,
        sortOrder = sortOrder,
    )

    private fun legStop(
        id: String,
        sortOrder: Int,
        displayTitle: String? = null,
    ) = TripStop(
        id = id,
        tripId = "trip",
        locationName = "Osaka",
        countryIso2 = "JP",
        latitude = null,
        longitude = null,
        dateRange = null,
        notes = null,
        sortOrder = sortOrder,
        source = TripStopSource.ITINERARY_GROUP,
        itineraryGroupId = id,
        displayTitle = displayTitle,
    )

    private fun group(id: String, departure: String) = ItineraryGroup(
        id = id,
        itineraryId = "itinerary",
        title = null,
        status = null,
        sortOrder = 0,
        flights = listOf(
            Flight(
                id = "flight-$id",
                originAirportId = "HND",
                destinationAirportId = "ITM",
                status = TravelStatus.COMPLETED,
                scheduledDepartureAt = departure,
                scheduledArrivalAt = null,
                actualDepartureAt = null,
                actualArrivalAt = null,
                airline = null,
                flightNumber = null,
                aircraft = null,
                notes = null,
                itineraryGroupId = id,
                sortOrder = 0,
            ),
        ),
    )

    private fun airports() = mapOf(
        "HND" to airport("HND", "Tòquio"),
        "ITM" to airport("ITM", "Osaka"),
    )

    private fun airport(id: String, city: String) = Airport(
        id = id,
        iata = id,
        icao = null,
        name = city,
        city = city,
        countryIso2 = "JP",
        latitude = 0.0,
        longitude = 0.0,
        timezone = null,
    )
}
