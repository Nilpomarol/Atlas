package com.atlas.presentation.trip

import com.atlas.domain.model.StopPhoto
import com.atlas.domain.model.StopType
import com.atlas.domain.model.TripStop
import org.junit.Assert.assertEquals
import org.junit.Test

class TripPhotoGalleryUiStateTest {

    @Test
    fun `orders main stops and their nested places narratively`() {
        val firstStop = tripStop(id = "stop-a", sortOrder = 10)
        val secondStop = tripStop(id = "stop-b", sortOrder = 20)
        val nested = nestedStop(id = "nested-stop", parentStopId = firstStop.id, label = "Kamakura")

        val result = buildTripPhotoGalleryUiState(
            stops = listOf(secondStop, nested, firstStop),
            stopPhotoMap = mapOf(
                firstStop.id to listOf(
                    photo(id = "photo-2", stopId = firstStop.id, sortOrder = 20),
                    photo(id = "photo-1", stopId = firstStop.id, sortOrder = 10),
                ),
                nested.id to listOf(photo(id = "photo-3", stopId = nested.id)),
                secondStop.id to listOf(photo(id = "photo-4", stopId = secondStop.id)),
            ),
        )

        assertEquals(
            listOf("stop-a", "nested-stop", "stop-b"),
            result.groups.map { it.stopId },
        )
        assertEquals(listOf("photo-1", "photo-2"), result.groups.first().photos.map { it.id })
        assertEquals(
            listOf("photo-1", "photo-2", "photo-3", "photo-4"),
            result.viewerItems.map { it.photo.id },
        )
        assertEquals("SORTIDA · Kamakura", result.viewerItems[2].contextLabel)
        assertEquals(4, result.photoCount)
    }

    @Test
    fun `labels a nested stop without a side-trip label`() {
        val parent = tripStop(id = "parent")
        val nested = nestedStop(id = "nested", parentStopId = parent.id, label = null)

        val result = buildTripPhotoGalleryUiState(
            stops = listOf(parent, nested),
            stopPhotoMap = mapOf(nested.id to listOf(photo("photo-a", nested.id))),
        )

        assertEquals("SORTIDA", result.groups.single().contextLabel)
    }

    @Test
    fun `appends nested stops whose parent is missing without dropping photos`() {
        val orphan = nestedStop(id = "orphan-stop", parentStopId = "missing-stop", sortOrder = 20)

        val result = buildTripPhotoGalleryUiState(
            stops = listOf(tripStop(id = "trip-stop"), orphan),
            stopPhotoMap = mapOf(
                "trip-stop" to listOf(photo("photo-a", "trip-stop")),
                orphan.id to listOf(photo("photo-b", orphan.id)),
            ),
        )

        assertEquals(listOf("trip-stop", "orphan-stop"), result.groups.map { it.stopId })
        assertEquals(2, result.photoCount)
    }

    @Test
    fun `omits groups without photos`() {
        val result = buildTripPhotoGalleryUiState(
            stops = listOf(tripStop(id = "empty-stop")),
            stopPhotoMap = emptyMap(),
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

    private fun nestedStop(
        id: String,
        parentStopId: String,
        label: String? = null,
        sortOrder: Int = 0,
    ) = TripStop(
        id = id,
        tripId = "trip",
        parentStopId = parentStopId,
        sideTripLabel = label,
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
