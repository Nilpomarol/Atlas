package com.atlas.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BackupPhotoSanitizerTest {
    @Test
    fun missingPhotoFileRemovesRowAndClearsCover() {
        val filename = "11111111-1111-1111-1111-111111111111.jpg"
        val backup = backupWithPhoto(filename)

        val sanitized = backup.withAvailablePhotos(emptySet())

        assertEquals(emptyList<StopPhotoBackup>(), sanitized.data.stopPhotos)
        assertNull(sanitized.data.trips.single().coverPhotoFilename)
    }

    @Test
    fun availablePhotoKeepsRowAndCover() {
        val filename = "11111111-1111-1111-1111-111111111111.jpg"
        val backup = backupWithPhoto(filename)

        val sanitized = backup.withAvailablePhotos(setOf(filename))

        assertEquals(1, sanitized.data.stopPhotos.size)
        assertEquals(filename, sanitized.data.trips.single().coverPhotoFilename)
    }

    private fun backupWithPhoto(filename: String): AtlasBackupV4 =
        AtlasBackupV4(
            backupVersion = 3,
            createdAt = "2026-06-14T00:00:00Z",
            data = AtlasBackupDataV4(
                trips = listOf(
                    BackupTripV3(
                        id = "trip-1",
                        title = "Viatge",
                        status = "COMPLETED",
                        coverPhotoFilename = filename,
                        createdAt = "2026-06-14T00:00:00Z",
                        updatedAt = "2026-06-14T00:00:00Z",
                    ),
                ),
                tripStops = listOf(
                    BackupTripStopV4(
                        id = "stop-1",
                        tripId = "trip-1",
                        locationName = "Barcelona",
                        countryIso2 = "ES",
                        sortOrder = 0,
                        createdAt = "2026-06-14T00:00:00Z",
                        updatedAt = "2026-06-14T00:00:00Z",
                    ),
                ),
                stopPhotos = listOf(
                    StopPhotoBackup(
                        id = "photo-1",
                        stopId = "stop-1",
                        stopType = "TRIP_STOP",
                        filename = filename,
                        sortOrder = 0,
                        createdAt = "2026-06-14T00:00:00Z",
                    ),
                ),
            ),
        )
}
