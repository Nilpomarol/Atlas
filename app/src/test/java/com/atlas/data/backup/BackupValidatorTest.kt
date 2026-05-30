package com.atlas.data.backup

import org.junit.Assert.assertThrows
import org.junit.Test

class BackupValidatorTest {
    private val validator = BackupValidator()
    private val validCountries = setOf("AD", "ES", "FR")

    @Test
    fun acceptsValidMvpBackup() {
        validator.validate(
            backup = validBackup(),
            validCountryIso2 = validCountries,
        )
    }

    @Test
    fun rejectsUnsupportedBackupVersion() {
        assertThrows(BackupValidationException::class.java) {
            validator.validate(
                backup = validBackup().copy(backupVersion = 2),
                validCountryIso2 = validCountries,
            )
        }
    }

    @Test
    fun rejectsUnknownCountryReference() {
        assertThrows(BackupValidationException::class.java) {
            validator.validate(
                backup = validBackup(
                    countryLogs = listOf(
                        validCountryLog().copy(countryIso2 = "ZZ"),
                    ),
                ),
                validCountryIso2 = validCountries,
            )
        }
    }

    @Test
    fun rejectsTripStopWithMissingTrip() {
        assertThrows(BackupValidationException::class.java) {
            validator.validate(
                backup = validBackup(
                    tripStops = listOf(
                        validTripStop().copy(tripId = "missing-trip"),
                    ),
                ),
                validCountryIso2 = validCountries,
            )
        }
    }

    @Test
    fun rejectsTripStopWithIncompleteCoordinates() {
        assertThrows(BackupValidationException::class.java) {
            validator.validate(
                backup = validBackup(
                    tripStops = listOf(
                        validTripStop().copy(latitude = 42.5063, longitude = null),
                    ),
                ),
                validCountryIso2 = validCountries,
            )
        }
    }

    @Test
    fun rejectsTripStopWithOutOfRangeCoordinates() {
        assertThrows(BackupValidationException::class.java) {
            validator.validate(
                backup = validBackup(
                    tripStops = listOf(
                        validTripStop().copy(latitude = 91.0, longitude = 1.5218),
                    ),
                ),
                validCountryIso2 = validCountries,
            )
        }
    }

    @Test
    fun rejectsInvalidFlexibleDate() {
        assertThrows(BackupValidationException::class.java) {
            validator.validate(
                backup = validBackup(
                    trips = listOf(
                        validTrip().copy(
                            datePrecision = "MONTH",
                            startYear = 2026,
                            startMonth = 5,
                            startDay = 30,
                        ),
                    ),
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
                        validCountryUserState(countryIso2 = "AD", currentlyLiving = true),
                        validCountryUserState(countryIso2 = "ES", currentlyLiving = true),
                    ),
                ),
                validCountryIso2 = validCountries,
            )
        }
    }

    private fun validBackup(
        countryUserStates: List<BackupCountryUserStateV1> = listOf(validCountryUserState()),
        countryLogs: List<BackupCountryLogV1> = listOf(validCountryLog()),
        trips: List<BackupTripV1> = listOf(validTrip()),
        tripStops: List<BackupTripStopV1> = listOf(validTripStop()),
    ): AtlasBackupV1 =
        AtlasBackupV1(
            backupVersion = 1,
            createdAt = "2026-05-30T00:00:00Z",
            countryDatasetVersion = "test",
            data = AtlasBackupDataV1(
                countryUserStates = countryUserStates,
                countryLogs = countryLogs,
                trips = trips,
                tripStops = tripStops,
            ),
        )

    private fun validCountryUserState(
        countryIso2: String = "AD",
        currentlyLiving: Boolean = false,
    ): BackupCountryUserStateV1 =
        BackupCountryUserStateV1(
            countryIso2 = countryIso2,
            wished = true,
            currentlyLiving = currentlyLiving,
            updatedAt = "2026-05-30T00:00:00Z",
        )

    private fun validCountryLog(): BackupCountryLogV1 =
        BackupCountryLogV1(
            id = "log-1",
            countryIso2 = "AD",
            type = "VISIT",
            startYear = 2026,
            startMonth = 5,
            startDay = null,
            endYear = null,
            endMonth = null,
            endDay = null,
            datePrecision = "MONTH",
            notes = "Andorra",
            createdAt = "2026-05-30T00:00:00Z",
            updatedAt = "2026-05-30T00:00:00Z",
        )

    private fun validTrip(): BackupTripV1 =
        BackupTripV1(
            id = "trip-1",
            title = "Viatge",
            status = "COMPLETED",
            startYear = 2026,
            startMonth = null,
            startDay = null,
            endYear = null,
            endMonth = null,
            endDay = null,
            datePrecision = "YEAR",
            notes = null,
            createdAt = "2026-05-30T00:00:00Z",
            updatedAt = "2026-05-30T00:00:00Z",
        )

    private fun validTripStop(): BackupTripStopV1 =
        BackupTripStopV1(
            id = "stop-1",
            tripId = "trip-1",
            locationName = "Andorra la Vella",
            countryIso2 = "AD",
            latitude = null,
            longitude = null,
            startYear = null,
            startMonth = null,
            startDay = null,
            endYear = null,
            endMonth = null,
            endDay = null,
            datePrecision = null,
            notes = null,
            sortOrder = 0,
            createdAt = "2026-05-30T00:00:00Z",
            updatedAt = "2026-05-30T00:00:00Z",
        )
}
