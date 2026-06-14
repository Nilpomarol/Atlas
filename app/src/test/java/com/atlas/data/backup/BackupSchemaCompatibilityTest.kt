package com.atlas.data.backup

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupSchemaCompatibilityTest {
    private val json = Json { ignoreUnknownKeys = false }

    @Test
    fun v1JsonDecodesWithPhotoDefaults() {
        val backup = json.decodeFromString(
            AtlasBackupV3.serializer(),
            """
            {
              "backupVersion": 1,
              "createdAt": "2026-01-01T00:00:00Z",
              "countryDatasetVersion": "test",
              "data": {
                "trips": [{
                  "id": "trip-1",
                  "title": "Viatge",
                  "status": "COMPLETED",
                  "createdAt": "2026-01-01T00:00:00Z",
                  "updatedAt": "2026-01-01T00:00:00Z"
                }],
                "tripStops": [{
                  "id": "stop-1",
                  "tripId": "trip-1",
                  "locationName": "Barcelona",
                  "country_iso2": "ES",
                  "sortOrder": 0,
                  "createdAt": "2026-01-01T00:00:00Z",
                  "updatedAt": "2026-01-01T00:00:00Z"
                }]
              }
            }
            """.trimIndent(),
        )

        assertEquals(1, backup.backupVersion)
        assertNull(backup.data.trips.single().coverPhotoFilename)
        assertEquals("MANUAL", backup.data.tripStops.single().source)
        assertTrue(backup.data.stopPhotos.isEmpty())
    }

    @Test
    fun v2JsonDecodesWithPhotoDefaults() {
        val backup = json.decodeFromString(
            AtlasBackupV3.serializer(),
            """
            {
              "backupVersion": 2,
              "createdAt": "2026-01-01T00:00:00Z",
              "countryDatasetVersion": "test",
              "airportDatasetVersion": "test",
              "data": {
                "trips": [],
                "tripStops": [],
                "flights": [],
                "itineraries": [],
                "itineraryGroups": [],
                "excursions": [],
                "excursionStops": []
              }
            }
            """.trimIndent(),
        )

        assertEquals(2, backup.backupVersion)
        assertTrue(backup.data.stopPhotos.isEmpty())
    }
}
