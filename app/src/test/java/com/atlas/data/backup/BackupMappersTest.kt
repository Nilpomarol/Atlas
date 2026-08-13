package com.atlas.data.backup

import com.atlas.data.local.entity.FlightEntity
import com.atlas.data.local.entity.ItineraryEntity
import com.atlas.data.local.entity.ItineraryGroupEntity
import com.atlas.data.local.entity.StopPhotoEntity
import com.atlas.data.local.entity.TripEntity
import com.atlas.data.local.entity.TripStopEntity
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupMappersTest {
    @Test
    fun tripV3RoundTripPreservesCoverPhoto() {
        val entity = TripEntity(
            id = "trip-1",
            title = "Viatge",
            status = "COMPLETED",
            startYear = 2026,
            startMonth = 6,
            startDay = null,
            endYear = null,
            endMonth = null,
            endDay = null,
            datePrecision = "MONTH",
            notes = "Notes",
            createdAt = "2026-05-30T00:00:00Z",
            updatedAt = "2026-05-30T01:00:00Z",
            coverPhotoFilename = "11111111-1111-1111-1111-111111111111.jpg",
        )

        assertEquals(entity, entity.toBackupV3().toEntity())
    }

    @Test
    fun tripStopBackupRoundTripPreservesAllFields() {
        val entity = TripStopEntity(
            id = "stop-1", tripId = "trip-1", locationName = "Andorra la Vella", countryIso2 = "AD",
            latitude = 42.5063, longitude = 1.5218,
            startYear = 2026, startMonth = 5, startDay = 30,
            endYear = 2026, endMonth = 6, endDay = 1,
            datePrecision = "DAY", notes = "Centre historic", sortOrder = 7,
            createdAt = "2026-05-30T00:00:00Z", updatedAt = "2026-05-30T01:00:00Z",
        )
        assertEquals(entity, entity.toBackupV1().toEntity())
    }

    @Test
    fun tripStopV2RoundTripPreservesSourceAndGroupFields() {
        val entity = TripStopEntity(
            id = "stop-2", tripId = "trip-1", locationName = "Tokyo", countryIso2 = "JP",
            latitude = 35.6895, longitude = 139.6917,
            startYear = null, startMonth = null, startDay = null,
            endYear = null, endMonth = null, endDay = null,
            datePrecision = null, notes = null, sortOrder = 1,
            source = "ITINERARY_GROUP", itineraryGroupId = "group-1",
            isVisible = true, displayTitle = "Tokyo (Itinerari)",
            createdAt = "2026-05-30T00:00:00Z", updatedAt = "2026-05-30T00:00:00Z",
        )
        assertEquals(entity, entity.toBackupV2().toEntity())
    }

    @Test
    fun flightRoundTripPreservesAllFields() {
        val entity = FlightEntity(
            id = "flight-1", originAirportId = "BCN", destinationAirportId = "NRT",
            status = "COMPLETED", scheduledDepartureAt = "2026-06-01T10:30",
            scheduledArrivalAt = "2026-06-02T07:00", actualDepartureAt = null, actualArrivalAt = null,
            airline = "Vueling", flightNumber = "VY1234", aircraft = "A320", notes = null,
            itineraryGroupId = "group-1", sortOrder = 0,
            createdAt = "2026-05-30T00:00:00Z", updatedAt = "2026-05-30T00:00:00Z",
        )
        assertEquals(entity, entity.toBackupV2().toEntity())
    }

    @Test
    fun itineraryRoundTripPreservesAllFields() {
        val entity = ItineraryEntity(
            id = "itin-1", title = "Japan 2026", tripId = "trip-1", notes = "Anada i tornada",
            createdAt = "2026-05-30T00:00:00Z", updatedAt = "2026-05-30T00:00:00Z",
        )
        assertEquals(entity, entity.toBackupV2().toEntity())
    }

    @Test
    fun itineraryBackupWithMissingTitleMapsToEmptyTitle() {
        val backup = json.decodeFromString(
            BackupItineraryV2.serializer(),
            """{"id":"itin-1","createdAt":"2026-05-30T00:00:00Z","updatedAt":"2026-05-30T00:00:00Z"}""",
        )

        assertEquals("", backup.toEntity().title)
    }

    @Test
    fun itineraryBackupWithNullTitleMapsToEmptyTitle() {
        val backup = BackupItineraryV2(
            id = "itin-1",
            title = null,
            createdAt = "2026-05-30T00:00:00Z",
            updatedAt = "2026-05-30T00:00:00Z",
        )

        assertEquals("", backup.toEntity().title)
    }

    @Test
    fun itineraryGroupRoundTripPreservesAllFields() {
        val entity = ItineraryGroupEntity(
            id = "group-1", itineraryId = "itin-1", title = "Anada", status = "COMPLETED", sortOrder = 0,
            createdAt = "2026-05-30T00:00:00Z", updatedAt = "2026-05-30T00:00:00Z",
        )
        assertEquals(entity, entity.toBackupV2().toEntity())
    }

    @Test
    fun nestedTripStopRoundTripPreservesParentAndLabel() {
        val entity = TripStopEntity(
            id = "es-1", tripId = "trip-1",
            parentStopId = "stop-1", sideTripLabel = "Kamakura",
            locationName = "Gran Buda de Kamakura",
            countryIso2 = "JP", latitude = 35.3167, longitude = 139.5497,
            startYear = 2026, startMonth = 6, startDay = 2,
            endYear = null, endMonth = null, endDay = null,
            datePrecision = "DAY", notes = null, sortOrder = 0,
            createdAt = "2026-05-30T00:00:00Z", updatedAt = "2026-05-30T00:00:00Z",
        )
        assertEquals(entity, entity.toBackupV4().toEntity())
    }

    @Test
    fun stopPhotoRoundTripPreservesAllFields() {
        val entity = StopPhotoEntity(
            id = "photo-1",
            stopId = "stop-1",
            stopType = "TRIP_STOP",
            filename = "11111111-1111-1111-1111-111111111111.jpg",
            sortOrder = 2,
            createdAt = "2026-05-30T00:00:00Z",
        )

        assertEquals(entity, entity.toBackupV3().toEntity())
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = false }
    }
}
