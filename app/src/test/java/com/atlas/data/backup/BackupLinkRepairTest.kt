package com.atlas.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class BackupLinkRepairTest {

    private val validator = BackupValidator()

    @Test
    fun `leaves a sound archive untouched`() {
        val backup = backup(trips = listOf(trip("trip-1")), itineraries = listOf(itinerary("i-1", "trip-1")))

        assertSame(backup, backup.withRepairedTripLinks())
    }

    @Test
    fun `detaches an itinerary whose trip is missing`() {
        val backup = backup(trips = emptyList(), itineraries = listOf(itinerary("i-1", "ghost-trip")))

        val repaired = backup.withRepairedTripLinks()

        assertNull(repaired.data.itineraries.single().tripId)
    }

    @Test
    fun `keeps sound links while detaching broken ones`() {
        val backup = backup(
            trips = listOf(trip("trip-1")),
            itineraries = listOf(itinerary("i-1", "trip-1"), itinerary("i-2", "ghost-trip")),
        )

        val repaired = backup.withRepairedTripLinks()

        assertEquals("trip-1", repaired.data.itineraries.first { it.id == "i-1" }.tripId)
        assertNull(repaired.data.itineraries.first { it.id == "i-2" }.tripId)
    }

    @Test
    fun `preserves the itinerary itself, not just the link`() {
        val backup = backup(trips = emptyList(), itineraries = listOf(itinerary("i-1", "ghost-trip")))

        val repaired = backup.withRepairedTripLinks()

        assertEquals(1, repaired.data.itineraries.size)
        assertEquals("Vols del Japó", repaired.data.itineraries.single().title)
    }

    /** The whole point: an archive that previously failed to import now restores. */
    @Test
    fun `repaired archive passes validation that rejected it before`() {
        val broken = backup(trips = emptyList(), itineraries = listOf(itinerary("i-1", "ghost-trip")))

        val rejected = runCatching {
            validator.validate(backup = broken, validCountryIso2 = emptySet())
        }.exceptionOrNull()
        assertEquals(BackupValidationException::class.java, rejected?.javaClass)

        validator.validate(backup = broken.withRepairedTripLinks(), validCountryIso2 = emptySet())
    }

    private fun backup(
        trips: List<BackupTripV3>,
        itineraries: List<BackupItineraryV2>,
    ) = AtlasBackupV4(
        backupVersion = BackupValidator.BACKUP_VERSION,
        createdAt = TIMESTAMP,
        data = AtlasBackupDataV4(trips = trips, itineraries = itineraries),
    )

    private fun trip(id: String) = BackupTripV3(
        id = id,
        title = "Japó",
        status = "COMPLETED",
        createdAt = TIMESTAMP,
        updatedAt = TIMESTAMP,
    )

    private fun itinerary(id: String, tripId: String?) = BackupItineraryV2(
        id = id,
        title = "Vols del Japó",
        tripId = tripId,
        notes = null,
        createdAt = TIMESTAMP,
        updatedAt = TIMESTAMP,
    )

    private companion object {
        const val TIMESTAMP = "2026-08-13T00:00:00Z"
    }
}
