package com.atlas.data.backup

import org.junit.Assert.assertThrows
import org.junit.Test

class BackupValidatorTest {
    private val validator = BackupValidator()
    private val validCountries = setOf("AD", "ES", "FR")

    @Test
    fun acceptsValidV3Backup() {
        validator.validate(backup = validBackup(), validCountryIso2 = validCountries)
    }

    @Test
    fun acceptsV1BackupFormat() {
        // v1 backup (version = 1, no new fields) must still import cleanly
        validator.validate(backup = validBackup().copy(backupVersion = 1), validCountryIso2 = validCountries)
    }

    @Test
    fun acceptsV2BackupFormat() {
        validator.validate(backup = validBackup().copy(backupVersion = 2), validCountryIso2 = validCountries)
    }

    @Test
    fun rejectsUnsupportedBackupVersion() {
        assertThrows(BackupValidationException::class.java) {
            validator.validate(backup = validBackup().copy(backupVersion = 99), validCountryIso2 = validCountries)
        }
    }

    @Test
    fun rejectsUnknownCountryReference() {
        assertThrows(BackupValidationException::class.java) {
            validator.validate(
                backup = validBackup(countryLogs = listOf(validCountryLog().copy(countryIso2 = "ZZ"))),
                validCountryIso2 = validCountries,
            )
        }
    }

    @Test
    fun rejectsTripStopWithMissingTrip() {
        assertThrows(BackupValidationException::class.java) {
            validator.validate(
                backup = validBackup(tripStops = listOf(validTripStop().copy(tripId = "missing-trip"))),
                validCountryIso2 = validCountries,
            )
        }
    }

    @Test
    fun rejectsTripStopWithIncompleteCoordinates() {
        assertThrows(BackupValidationException::class.java) {
            validator.validate(
                backup = validBackup(tripStops = listOf(validTripStop().copy(latitude = 42.5063, longitude = null))),
                validCountryIso2 = validCountries,
            )
        }
    }

    @Test
    fun rejectsTripStopWithOutOfRangeCoordinates() {
        assertThrows(BackupValidationException::class.java) {
            validator.validate(
                backup = validBackup(tripStops = listOf(validTripStop().copy(latitude = 91.0, longitude = 1.5218))),
                validCountryIso2 = validCountries,
            )
        }
    }

    @Test
    fun rejectsInvalidFlexibleDate() {
        assertThrows(BackupValidationException::class.java) {
            validator.validate(
                backup = validBackup(
                    trips = listOf(validTrip().copy(datePrecision = "MONTH", startYear = 2026, startMonth = 5, startDay = 30)),
                ),
                validCountryIso2 = validCountries,
            )
        }
    }

    @Test
    fun rejectsMultipleCurrentlyLivingCountries() {
        assertThrows(BackupValidationException::class.java) {
            validator.validate(
                backup = validBackup(
                    countryUserStates = listOf(
                        validCountryUserState("AD", currentlyLiving = true),
                        validCountryUserState("ES", currentlyLiving = true),
                    ),
                ),
                validCountryIso2 = validCountries,
            )
        }
    }

    // ── v2 validation tests ───────────────────────────────────────────────────

    @Test
    fun acceptsBackupWithFlightsAndItineraries() {
        val itinerary = validItinerary()
        val group = validItineraryGroup(itineraryId = itinerary.id)
        val flight = validFlight(groupId = group.id)
        validator.validate(
            backup = validBackup(
                itineraries = listOf(itinerary),
                itineraryGroups = listOf(group),
                flights = listOf(flight),
            ),
            validCountryIso2 = validCountries,
        )
    }

    @Test
    fun acceptsItineraryWithoutTitle() {
        validator.validate(
            backup = validBackup(itineraries = listOf(validItinerary().copy(title = null))),
            validCountryIso2 = validCountries,
        )
    }

    @Test
    fun rejectsFlightWithMissingGroup() {
        assertThrows(BackupValidationException::class.java) {
            validator.validate(
                backup = validBackup(flights = listOf(validFlight(groupId = "missing-group"))),
                validCountryIso2 = validCountries,
            )
        }
    }

    @Test
    fun rejectsItineraryGroupWithMissingItinerary() {
        assertThrows(BackupValidationException::class.java) {
            validator.validate(
                backup = validBackup(itineraryGroups = listOf(validItineraryGroup(itineraryId = "missing-itinerary"))),
                validCountryIso2 = validCountries,
            )
        }
    }

    @Test
    fun acceptsBackupWithExcursions() {
        val excursion = validExcursion()
        val stop = validExcursionStop(excursionId = excursion.id)
        validator.validate(
            backup = validBackup(excursions = listOf(excursion), excursionStops = listOf(stop)),
            validCountryIso2 = validCountries,
        )
    }

    @Test
    fun rejectsExcursionStopWithMissingExcursion() {
        assertThrows(BackupValidationException::class.java) {
            validator.validate(
                backup = validBackup(excursionStops = listOf(validExcursionStop(excursionId = "missing-excursion"))),
                validCountryIso2 = validCountries,
            )
        }
    }

    @Test
    fun acceptsValidStopPhotos() {
        validator.validate(
            backup = validBackup(stopPhotos = listOf(validStopPhoto())),
            validCountryIso2 = validCountries,
        )
    }

    @Test
    fun rejectsPhotoWithMissingStop() {
        assertThrows(BackupValidationException::class.java) {
            validator.validate(
                backup = validBackup(stopPhotos = listOf(validStopPhoto().copy(stopId = "missing-stop"))),
                validCountryIso2 = validCountries,
            )
        }
    }

    @Test
    fun rejectsPhotoWithUnsafeFilename() {
        assertThrows(BackupValidationException::class.java) {
            validator.validate(
                backup = validBackup(stopPhotos = listOf(validStopPhoto().copy(filename = "../photo.jpg"))),
                validCountryIso2 = validCountries,
            )
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun validBackup(
        countryUserStates: List<BackupCountryUserStateV1> = listOf(validCountryUserState()),
        countryLogs: List<BackupCountryLogV1> = listOf(validCountryLog()),
        trips: List<BackupTripV3> = listOf(validTrip()),
        tripStops: List<BackupTripStopV2> = listOf(validTripStop()),
        flights: List<BackupFlightV2> = emptyList(),
        itineraries: List<BackupItineraryV2> = emptyList(),
        itineraryGroups: List<BackupItineraryGroupV2> = emptyList(),
        excursions: List<BackupExcursionV2> = emptyList(),
        excursionStops: List<BackupExcursionStopV2> = emptyList(),
        stopPhotos: List<StopPhotoBackup> = emptyList(),
    ): AtlasBackupV3 =
        AtlasBackupV3(
            backupVersion = 3,
            createdAt = "2026-05-30T00:00:00Z",
            countryDatasetVersion = "test",
            data = AtlasBackupDataV3(
                countryUserStates = countryUserStates,
                countryLogs = countryLogs,
                trips = trips,
                tripStops = tripStops,
                flights = flights,
                itineraries = itineraries,
                itineraryGroups = itineraryGroups,
                excursions = excursions,
                excursionStops = excursionStops,
                stopPhotos = stopPhotos,
            ),
        )

    private fun validCountryUserState(countryIso2: String = "AD", currentlyLiving: Boolean = false) =
        BackupCountryUserStateV1(countryIso2 = countryIso2, wished = true, currentlyLiving = currentlyLiving, updatedAt = "2026-05-30T00:00:00Z")

    private fun validCountryLog() =
        BackupCountryLogV1(id = "log-1", countryIso2 = "AD", type = "VISIT", startYear = 2026, startMonth = 5, datePrecision = "MONTH", createdAt = "2026-05-30T00:00:00Z", updatedAt = "2026-05-30T00:00:00Z")

    private fun validTrip() =
        BackupTripV3(id = "trip-1", title = "Viatge", status = "COMPLETED", startYear = 2026, datePrecision = "YEAR", createdAt = "2026-05-30T00:00:00Z", updatedAt = "2026-05-30T00:00:00Z")

    private fun validTripStop() =
        BackupTripStopV2(id = "stop-1", tripId = "trip-1", locationName = "Andorra la Vella", countryIso2 = "AD", sortOrder = 0, createdAt = "2026-05-30T00:00:00Z", updatedAt = "2026-05-30T00:00:00Z")

    private fun validFlight(groupId: String? = null) =
        BackupFlightV2(id = "flight-1", originAirportId = "BCN", destinationAirportId = "NRT", status = "COMPLETED", itineraryGroupId = groupId, createdAt = "2026-05-30T00:00:00Z", updatedAt = "2026-05-30T00:00:00Z")

    private fun validItinerary() =
        BackupItineraryV2(id = "itin-1", title = "Japan 2026", createdAt = "2026-05-30T00:00:00Z", updatedAt = "2026-05-30T00:00:00Z")

    private fun validItineraryGroup(itineraryId: String = "itin-1") =
        BackupItineraryGroupV2(id = "group-1", itineraryId = itineraryId, sortOrder = 0, createdAt = "2026-05-30T00:00:00Z", updatedAt = "2026-05-30T00:00:00Z")

    private fun validExcursion() =
        BackupExcursionV2(id = "excursion-1", tripId = "trip-1", title = "Kamakura", sortOrder = 0, createdAt = "2026-05-30T00:00:00Z", updatedAt = "2026-05-30T00:00:00Z")

    private fun validExcursionStop(excursionId: String = "excursion-1") =
        BackupExcursionStopV2(id = "excursion-stop-1", excursionId = excursionId, locationName = "Kamakura", countryIso2 = "AD", sortOrder = 0, createdAt = "2026-05-30T00:00:00Z", updatedAt = "2026-05-30T00:00:00Z")

    private fun validStopPhoto() =
        StopPhotoBackup(
            id = "photo-1",
            stopId = "stop-1",
            stopType = "TRIP_STOP",
            filename = "11111111-1111-1111-1111-111111111111.jpg",
            sortOrder = 0,
            createdAt = "2026-05-30T00:00:00Z",
        )
}
