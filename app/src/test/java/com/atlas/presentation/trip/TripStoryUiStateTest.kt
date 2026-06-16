package com.atlas.presentation.trip

import com.atlas.domain.model.Country
import com.atlas.domain.model.CountryType
import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.Excursion
import com.atlas.domain.model.ExcursionStop
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

class TripStoryUiStateTest {

    @Test
    fun `orders stops anchored excursions and unanchored excursions narratively`() {
        val firstStop = stop("stop-a", sortOrder = 10)
        val secondStop = stop("stop-b", sortOrder = 20)
        val anchored = excursion(
            id = "anchored",
            anchorTripStopId = firstStop.id,
            stops = listOf(excursionStop("anchored-stop", "anchored")),
        )
        val unanchored = excursion(
            id = "unanchored",
            sortOrder = 10,
            anchorTripStopId = null,
            stops = listOf(excursionStop("unanchored-stop", "unanchored")),
        )

        val result = buildTripStoryUiState(
            trip = trip(),
            stops = listOf(secondStop, firstStop),
            countries = listOf(country("ES")),
            excursions = listOf(unanchored, anchored),
            itinerary = null,
            itineraryGroups = emptyList(),
            tripStopPhotoMap = emptyMap(),
            excursionStopPhotoMap = emptyMap(),
        )

        assertEquals(listOf("stop-a", "stop-b"), result.mapStops.map { it.id })
        assertEquals(listOf("anchored", "unanchored"), result.mapExcursions.map { it.id })
        assertEquals(
            listOf(
                "stop-stop-a",
                "excursion-anchored",
                "excursion-stop-anchored-stop",
                "stop-stop-b",
                "excursion-unanchored",
                "excursion-stop-unanchored-stop",
            ),
            result.slides.filterIsInstance<TripStorySlideUiState.Place>().map { it.id },
        )
    }

    @Test
    fun `flattens stop and excursion photos into viewer items with context`() {
        val stop = stop("stop")
        val excursion = excursion(
            id = "excursion",
            anchorTripStopId = stop.id,
            title = "Costa",
            stops = listOf(excursionStop("excursion-stop", "excursion")),
        )

        val result = buildTripStoryUiState(
            trip = trip(),
            stops = listOf(stop),
            countries = listOf(country("ES")),
            excursions = listOf(excursion),
            itinerary = null,
            itineraryGroups = emptyList(),
            tripStopPhotoMap = mapOf(
                stop.id to listOf(
                    photo("stop-photo-2", stop.id, sortOrder = 20),
                    photo("stop-photo-1", stop.id, sortOrder = 10),
                ),
            ),
            excursionStopPhotoMap = mapOf(
                "excursion-stop" to listOf(
                    photo(
                        id = "excursion-photo",
                        stopId = "excursion-stop",
                        stopType = StopType.EXCURSION_STOP,
                    ),
                ),
            ),
        )

        assertEquals(
            listOf("stop-photo-1", "stop-photo-2", "excursion-photo"),
            result.viewerItems.map { it.photo.id },
        )
        assertEquals("PARADA", result.viewerItems.first().contextLabel)
        assertEquals("EXCURSIÓ · Costa", result.viewerItems.last().contextLabel)
        assertEquals(3, result.photoCount)
        assertEquals(
            listOf("stop-photo-1", "stop-photo-2", "excursion-photo"),
            result.slides.filterIsInstance<TripStorySlideUiState.Photo>().map { it.item.photo.id },
        )
        assertTrue(result.slides.any { it is TripStorySlideUiState.Route })
    }

    private fun trip() = Trip(
        id = "trip",
        title = "Ruta",
        status = TravelStatus.COMPLETED,
        dateRange = FlexibleDateRange(
            start = FlexibleDate(2025, 1, 1, DatePrecision.DAY),
            end = FlexibleDate(2025, 1, 3, DatePrecision.DAY),
            precision = DatePrecision.DAY,
        ),
        notes = null,
    )

    private fun stop(
        id: String,
        sortOrder: Int = 0,
    ) = TripStop(
        id = id,
        tripId = "trip",
        locationName = id,
        countryIso2 = "ES",
        latitude = null,
        longitude = null,
        dateRange = null,
        notes = null,
        sortOrder = sortOrder,
    )

    private fun excursion(
        id: String,
        sortOrder: Int = 0,
        anchorTripStopId: String?,
        title: String = id,
        stops: List<ExcursionStop>,
    ) = Excursion(
        id = id,
        tripId = "trip",
        anchorTripStopId = anchorTripStopId,
        title = title,
        notes = null,
        sortOrder = sortOrder,
        stops = stops,
    )

    private fun excursionStop(
        id: String,
        excursionId: String,
    ) = ExcursionStop(
        id = id,
        excursionId = excursionId,
        locationName = id,
        countryIso2 = "ES",
        latitude = null,
        longitude = null,
        dateRange = null,
        notes = null,
        sortOrder = 0,
    )

    private fun country(iso2: String) = Country(
        iso2 = iso2,
        iso3 = null,
        nameCa = "Espanya",
        nameEn = "Spain",
        type = CountryType.SOVEREIGN_STATE,
        parentIso2 = null,
        isUnMember = true,
        isObserverState = false,
        isTrackable = true,
        continent = "Europe",
        subregion = "Southern Europe",
        flagEmoji = "🇪🇸",
        flagAsset = null,
        latitude = null,
        longitude = null,
        capitalNameCa = null,
        capitalNameEn = null,
        capitalLatitude = null,
        capitalLongitude = null,
    )

    private fun photo(
        id: String,
        stopId: String,
        stopType: StopType = StopType.TRIP_STOP,
        sortOrder: Int = 0,
    ) = StopPhoto(
        id = id,
        stopId = stopId,
        stopType = stopType,
        filename = "$id.jpg",
        sortOrder = sortOrder,
        createdAt = "2026-06-16T00:00:00Z",
    )
}
