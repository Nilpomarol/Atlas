package com.atlas.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Guards the legacy-archive path of the trip model collapse. These expectations mirror
 * Room migration 25 → 26, so a change here should be matched there.
 */
class LegacyExcursionCollapseTest {

    @Test
    fun `keeps trip stops untouched when there is nothing legacy to fold`() {
        val stops = listOf(tripStop("stop-1"))

        val result = collapseLegacyExcursions(stops, emptyList(), emptyList())

        assertSame(stops, result)
    }

    @Test
    fun `nests an anchored excursion under its anchor and preserves ids`() {
        val result = collapseLegacyExcursions(
            tripStops = listOf(tripStop("stop-1")),
            excursions = listOf(excursion("exc-1", anchorTripStopId = "stop-1", title = "Kamakura")),
            excursionStops = listOf(
                excursionStop("es-1", "exc-1", sortOrder = 0),
                excursionStop("es-2", "exc-1", sortOrder = 1),
            ),
        )

        val nested = result.filter { it.parentStopId != null }
        assertEquals(listOf("es-1", "es-2"), nested.map { it.id })
        assertEquals(listOf("stop-1", "stop-1"), nested.map { it.parentStopId })
        assertEquals(listOf("Kamakura", "Kamakura"), nested.map { it.sideTripLabel })
        // Sort order stays monotonic among siblings.
        assertEquals(listOf(200, 201), nested.map { it.sortOrder })
    }

    @Test
    fun `promotes an unanchored excursion to main route stops`() {
        val result = collapseLegacyExcursions(
            tripStops = emptyList(),
            excursions = listOf(excursion("exc-1", anchorTripStopId = null, title = "Solta")),
            excursionStops = listOf(excursionStop("es-1", "exc-1")),
        )

        assertNull(result.single().parentStopId)
        assertEquals("Solta", result.single().sideTripLabel)
    }

    @Test
    fun `attaches excursion notes to the first stop only`() {
        val result = collapseLegacyExcursions(
            tripStops = emptyList(),
            excursions = listOf(
                excursion("exc-1", anchorTripStopId = null, title = "Costa", notes = "Dia de platja"),
            ),
            excursionStops = listOf(
                excursionStop("es-1", "exc-1", sortOrder = 0, notes = "Primera"),
                excursionStop("es-2", "exc-1", sortOrder = 1, notes = "Segona"),
            ),
        )

        assertEquals("Primera\n\nDia de platja", result.first { it.id == "es-1" }.notes)
        assertEquals("Segona", result.first { it.id == "es-2" }.notes)
    }

    @Test
    fun `folds a childless excursion into its anchor stop notes`() {
        val result = collapseLegacyExcursions(
            tripStops = listOf(tripStop("stop-1", notes = "Base")),
            excursions = listOf(
                excursion("exc-1", anchorTripStopId = "stop-1", title = "Pendent", notes = "No hi vam anar"),
            ),
            excursionStops = emptyList(),
        )

        assertEquals("Base\n\nPendent\n\nNo hi vam anar", result.single().notes)
    }

    @Test
    fun `drops excursion stops whose excursion is missing`() {
        val result = collapseLegacyExcursions(
            tripStops = emptyList(),
            excursions = emptyList(),
            excursionStops = listOf(excursionStop("es-1", "gone")),
        )

        assertEquals(emptyList<BackupTripStopV4>(), result)
    }

    private fun tripStop(id: String, notes: String? = null) = BackupTripStopV4(
        id = id,
        tripId = "trip-1",
        locationName = id,
        countryIso2 = "JP",
        notes = notes,
        sortOrder = 0,
        createdAt = TIMESTAMP,
        updatedAt = TIMESTAMP,
    )

    private fun excursion(
        id: String,
        anchorTripStopId: String?,
        title: String,
        notes: String? = null,
        sortOrder: Int = 2,
    ) = BackupExcursionV2(
        id = id,
        tripId = "trip-1",
        anchorTripStopId = anchorTripStopId,
        title = title,
        notes = notes,
        sortOrder = sortOrder,
        createdAt = TIMESTAMP,
        updatedAt = TIMESTAMP,
    )

    private fun excursionStop(
        id: String,
        excursionId: String,
        sortOrder: Int = 0,
        notes: String? = null,
    ) = BackupExcursionStopV2(
        id = id,
        excursionId = excursionId,
        locationName = id,
        countryIso2 = "JP",
        notes = notes,
        sortOrder = sortOrder,
        createdAt = TIMESTAMP,
        updatedAt = TIMESTAMP,
    )

    private companion object {
        const val TIMESTAMP = "2026-05-30T00:00:00Z"
    }
}
