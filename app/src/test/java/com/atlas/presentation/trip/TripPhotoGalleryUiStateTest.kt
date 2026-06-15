package com.atlas.presentation.trip

import com.atlas.domain.model.Excursion
import com.atlas.domain.model.ExcursionStop
import com.atlas.domain.model.StopPhoto
import com.atlas.domain.model.StopType
import com.atlas.domain.model.TripStop
import org.junit.Assert.assertEquals
import org.junit.Test

class TripPhotoGalleryUiStateTest {

    @Test
    fun `orders trip stops photos and anchored excursions narratively`() {
        val firstStop = tripStop(id = "stop-a", sortOrder = 10)
        val secondStop = tripStop(id = "stop-b", sortOrder = 20)
        val excursionStop = excursionStop(id = "excursion-stop")

        val result = buildTripPhotoGalleryUiState(
            stops = listOf(secondStop, firstStop),
            excursions = listOf(
                excursion(
                    id = "excursion",
                    anchorTripStopId = firstStop.id,
                    stops = listOf(excursionStop),
                ),
            ),
            tripStopPhotoMap = mapOf(
                firstStop.id to listOf(
                    photo(id = "photo-2", stopId = firstStop.id, sortOrder = 20),
                    photo(id = "photo-1", stopId = firstStop.id, sortOrder = 10),
                ),
                secondStop.id to listOf(photo(id = "photo-4", stopId = secondStop.id)),
            ),
            excursionStopPhotoMap = mapOf(
                excursionStop.id to listOf(
                    photo(
                        id = "photo-3",
                        stopId = excursionStop.id,
                        stopType = StopType.EXCURSION_STOP,
                    ),
                ),
            ),
        )

        assertEquals(
            listOf("stop-a", "excursion-stop", "stop-b"),
            result.groups.map { it.stopId },
        )
        assertEquals(listOf("photo-1", "photo-2"), result.groups.first().photos.map { it.id })
        assertEquals(
            listOf("photo-1", "photo-2", "photo-3", "photo-4"),
            result.viewerItems.map { it.photo.id },
        )
        assertEquals("EXCURSIÓ · excursion", result.viewerItems[2].contextLabel)
        assertEquals(4, result.photoCount)
    }

    @Test
    fun `appends unanchored and missing-anchor excursions without dropping photos`() {
        val unanchoredStop = excursionStop(id = "unanchored-stop")
        val orphanStop = excursionStop(id = "orphan-stop")

        val result = buildTripPhotoGalleryUiState(
            stops = listOf(tripStop(id = "trip-stop")),
            excursions = listOf(
                excursion(
                    id = "orphan",
                    sortOrder = 20,
                    anchorTripStopId = "missing-stop",
                    stops = listOf(orphanStop),
                ),
                excursion(
                    id = "unanchored",
                    sortOrder = 10,
                    anchorTripStopId = null,
                    stops = listOf(unanchoredStop),
                ),
            ),
            tripStopPhotoMap = emptyMap(),
            excursionStopPhotoMap = mapOf(
                unanchoredStop.id to listOf(
                    photo("photo-a", unanchoredStop.id, StopType.EXCURSION_STOP),
                ),
                orphanStop.id to listOf(
                    photo("photo-b", orphanStop.id, StopType.EXCURSION_STOP),
                ),
            ),
        )

        assertEquals(
            listOf("unanchored-stop", "orphan-stop"),
            result.groups.map { it.stopId },
        )
        assertEquals(2, result.photoCount)
    }

    @Test
    fun `omits groups without photos`() {
        val result = buildTripPhotoGalleryUiState(
            stops = listOf(tripStop(id = "empty-stop")),
            excursions = emptyList(),
            tripStopPhotoMap = emptyMap(),
            excursionStopPhotoMap = emptyMap(),
        )

        assertEquals(emptyList<TripPhotoGroupUiState>(), result.groups)
        assertEquals(0, result.photoCount)
    }

    private fun tripStop(
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
        stops: List<ExcursionStop>,
    ) = Excursion(
        id = id,
        tripId = "trip",
        anchorTripStopId = anchorTripStopId,
        title = id,
        notes = null,
        sortOrder = sortOrder,
        stops = stops,
    )

    private fun excursionStop(
        id: String,
        sortOrder: Int = 0,
    ) = ExcursionStop(
        id = id,
        excursionId = "excursion",
        locationName = id,
        countryIso2 = "FR",
        latitude = null,
        longitude = null,
        dateRange = null,
        notes = null,
        sortOrder = sortOrder,
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
        createdAt = "2026-06-14T00:00:00",
    )
}
