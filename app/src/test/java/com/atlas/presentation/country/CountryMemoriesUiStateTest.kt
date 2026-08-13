package com.atlas.presentation.country

import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.model.StopPhoto
import com.atlas.domain.model.StopType
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CountryMemoriesUiStateTest {

    @Test
    fun `keeps matching stops once and orders recent trips first`() {
        val olderTrip = trip("older", "Viatge antic", 2020)
        val recentTrip = trip("recent", "Viatge recent", 2025)
        val recentStop = tripStop("recent-es", recentTrip.id, "ES", sortOrder = 20)
        val recentOtherCountry = tripStop("recent-fr", recentTrip.id, "FR", sortOrder = 10)
        val olderStop = tripStop("older-es", olderTrip.id, "ES")

        val result = buildCountryMemoriesUiState(
            countryIso2 = "ES",
            trips = listOf(olderTrip, recentTrip),
            tripStops = listOf(olderStop, recentStop, recentOtherCountry),
            stopPhotoMap = mapOf(
                recentStop.id to listOf(photo("recent-photo", recentStop.id)),
                recentOtherCountry.id to listOf(photo("fr-photo", recentOtherCountry.id)),
                olderStop.id to listOf(photo("older-photo", olderStop.id)),
            ),
        )

        assertEquals(listOf("recent", "older"), result.trips.map { it.tripId })
        assertEquals(
            listOf("recent-photo", "older-photo"),
            result.viewerItems.map { it.photo.id },
        )
        assertEquals(2, result.photoCount)
    }

    @Test
    fun `groups all matching stops from one trip into one card`() {
        val trip = trip("trip", "Ruta")
        val firstStop = tripStop("first-stop", trip.id, "ES", sortOrder = 10)
        val secondStop = tripStop("second-stop", trip.id, "ES", sortOrder = 20)

        val result = buildCountryMemoriesUiState(
            countryIso2 = "ES",
            trips = listOf(trip),
            tripStops = listOf(secondStop, firstStop),
            stopPhotoMap = mapOf(
                firstStop.id to listOf(photo("first-photo", firstStop.id)),
                secondStop.id to listOf(photo("second-photo", secondStop.id)),
            ),
        )

        assertEquals(1, result.trips.size)
        assertEquals(listOf("first-stop", "second-stop"), result.trips.single().locationNames)
        assertEquals(
            listOf("first-photo", "second-photo"),
            result.trips.single().items.map { it.photo.id },
        )
    }

    @Test
    fun `includes nested stop photos in the same trip narrative sequence`() {
        val trip = trip("trip", "Ruta")
        val tripStop = tripStop("stop", trip.id, "ES")
        val nested = nestedStop("nested-stop", trip.id, tripStop.id, "Costa", "ES")

        val result = buildCountryMemoriesUiState(
            countryIso2 = "es",
            trips = listOf(trip),
            tripStops = listOf(tripStop, nested),
            stopPhotoMap = mapOf(
                tripStop.id to listOf(photo("trip-photo", tripStop.id)),
                nested.id to listOf(photo("nested-photo", nested.id)),
            ),
        )

        assertEquals(
            listOf("trip-photo", "nested-photo"),
            result.viewerItems.map { it.photo.id },
        )
        assertEquals(1, result.trips.size)
        assertEquals(
            StopType.TRIP_STOP,
            result.trips.single().items.last().stopType,
        )
        assertEquals(
            "VIATGE · Ruta · SORTIDA · Costa",
            result.trips.single().items.last().contextLabel,
        )
        assertEquals("trip", result.viewerItems.last().tripId)
    }

    @Test
    fun `returns no groups when the country has no personal photos`() {
        val trip = trip("trip", "Ruta")

        val result = buildCountryMemoriesUiState(
            countryIso2 = "ES",
            trips = listOf(trip),
            tripStops = listOf(tripStop("stop", trip.id, "ES")),
            stopPhotoMap = emptyMap(),
        )

        assertTrue(result.trips.isEmpty())
        assertTrue(result.viewerItems.isEmpty())
    }

    private fun trip(
        id: String,
        title: String,
        year: Int = 2024,
    ) = Trip(
        id = id,
        title = title,
        status = TravelStatus.COMPLETED,
        dateRange = FlexibleDateRange(
            start = FlexibleDate(year, 1, 1, DatePrecision.DAY),
            end = null,
            precision = DatePrecision.DAY,
        ),
        notes = null,
    )

    private fun tripStop(
        id: String,
        tripId: String,
        countryIso2: String,
        sortOrder: Int = 0,
    ) = TripStop(
        id = id,
        tripId = tripId,
        locationName = id,
        countryIso2 = countryIso2,
        latitude = null,
        longitude = null,
        dateRange = null,
        notes = null,
        sortOrder = sortOrder,
    )

    private fun nestedStop(
        id: String,
        tripId: String,
        parentStopId: String,
        label: String?,
        countryIso2: String,
    ) = TripStop(
        id = id,
        tripId = tripId,
        parentStopId = parentStopId,
        sideTripLabel = label,
        locationName = id,
        countryIso2 = countryIso2,
        latitude = null,
        longitude = null,
        dateRange = null,
        notes = null,
        sortOrder = 1,
    )

    private fun photo(
        id: String,
        stopId: String,
        stopType: StopType = StopType.TRIP_STOP,
    ) = StopPhoto(
        id = id,
        stopId = stopId,
        stopType = stopType,
        filename = "$id.jpg",
        sortOrder = 0,
        createdAt = "2026-06-15T00:00:00Z",
    )
}
