package com.atlas.data.backup

import com.atlas.data.local.entity.TripStopEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupMappersTest {
    @Test
    fun tripStopBackupRoundTripPreservesAllFields() {
        val entity = TripStopEntity(
            id = "stop-1",
            tripId = "trip-1",
            locationName = "Andorra la Vella",
            countryIso2 = "AD",
            latitude = 42.5063,
            longitude = 1.5218,
            startYear = 2026,
            startMonth = 5,
            startDay = 30,
            endYear = 2026,
            endMonth = 6,
            endDay = 1,
            datePrecision = "DAY",
            notes = "Centre historic",
            sortOrder = 7,
            createdAt = "2026-05-30T00:00:00Z",
            updatedAt = "2026-05-30T01:00:00Z",
        )

        val backup = entity.toBackupV1()
        val restored = backup.toEntity()

        assertEquals(entity, restored)
    }
}
