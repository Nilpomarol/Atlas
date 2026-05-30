package com.atlas.data.backup

import com.atlas.domain.model.CountryLogType
import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.validation.FlexibleDateValidator

class BackupValidationException(message: String) : IllegalArgumentException(message)

class BackupValidator(
    private val flexibleDateValidator: FlexibleDateValidator = FlexibleDateValidator(),
) {
    fun validate(
        backup: AtlasBackupV1,
        validCountryIso2: Set<String>,
    ) {
        requireBackup(backup.backupVersion == BACKUP_VERSION) {
            "La còpia no és compatible amb aquesta versió d'Atlas."
        }

        val data = backup.data
        requireUnique(data.countryUserStates.map { it.countryIso2 }, "Hi ha països duplicats a l'estat d'usuari.")
        requireUnique(data.countryLogs.map { it.id }, "Hi ha registres duplicats.")
        requireUnique(data.trips.map { it.id }, "Hi ha viatges duplicats.")
        requireUnique(data.tripStops.map { it.id }, "Hi ha parades duplicades.")

        requireBackup(data.countryUserStates.count { it.currentlyLiving } <= 1) {
            "La còpia té més d'un país marcat com a vivint-hi."
        }

        val tripIds = data.trips.map { it.id }.toSet()

        data.countryUserStates.forEach {
            requireCountryExists(it.countryIso2, validCountryIso2)
        }
        data.countryLogs.forEach {
            requireCountryExists(it.countryIso2, validCountryIso2)
            requireEnum<CountryLogType>(it.type, "El tipus d'un registre no és vàlid.")
            validateDateRange(
                precision = it.datePrecision,
                startYear = it.startYear,
                startMonth = it.startMonth,
                startDay = it.startDay,
                endYear = it.endYear,
                endMonth = it.endMonth,
                endDay = it.endDay,
            )
        }
        data.trips.forEach {
            requireBackup(it.title.isNotBlank()) { "Hi ha un viatge sense títol." }
            requireEnum<TravelStatus>(it.status, "L'estat d'un viatge no és vàlid.")
            validateDateRange(
                precision = it.datePrecision,
                startYear = it.startYear,
                startMonth = it.startMonth,
                startDay = it.startDay,
                endYear = it.endYear,
                endMonth = it.endMonth,
                endDay = it.endDay,
            )
        }
        data.tripStops.forEach {
            requireBackup(it.tripId in tripIds) {
                "Hi ha una parada que apunta a un viatge inexistent."
            }
            requireCountryExists(it.countryIso2, validCountryIso2)
            requireBackup(it.locationName.isNotBlank()) { "Hi ha una parada sense nom." }
            validateCoordinates(
                latitude = it.latitude,
                longitude = it.longitude,
            )
            validateDateRange(
                precision = it.datePrecision,
                startYear = it.startYear,
                startMonth = it.startMonth,
                startDay = it.startDay,
                endYear = it.endYear,
                endMonth = it.endMonth,
                endDay = it.endDay,
            )
        }
    }

    private fun validateCoordinates(
        latitude: Double?,
        longitude: Double?,
    ) {
        requireBackup((latitude == null) == (longitude == null)) {
            "Hi ha una parada amb coordenades incompletes."
        }
        latitude?.let {
            requireBackup(it in -90.0..90.0) {
                "Hi ha una parada amb latitud no vàlida."
            }
        }
        longitude?.let {
            requireBackup(it in -180.0..180.0) {
                "Hi ha una parada amb longitud no vàlida."
            }
        }
    }

    private fun validateDateRange(
        precision: String?,
        startYear: Int?,
        startMonth: Int?,
        startDay: Int?,
        endYear: Int?,
        endMonth: Int?,
        endDay: Int?,
    ) {
        val hasAnyDatePart = listOf(
            startYear,
            startMonth,
            startDay,
            endYear,
            endMonth,
            endDay,
        ).any { it != null }
        if (!hasAnyDatePart) {
            requireBackup(precision == null) { "Hi ha una data buida amb precisió informada." }
            return
        }

        val datePrecision = precision?.let { value ->
            enumValueOrNull<DatePrecision>(value)
        } ?: throw BackupValidationException("Hi ha una data sense precisió.")

        val range = FlexibleDateRange(
            start = toFlexibleDate(startYear, startMonth, startDay, datePrecision),
            end = toFlexibleDate(endYear, endMonth, endDay, datePrecision),
            precision = datePrecision,
        )
        requireBackup(flexibleDateValidator.isValid(range)) {
            "Hi ha una data o rang de dates no vàlid."
        }
    }

    private fun toFlexibleDate(
        year: Int?,
        month: Int?,
        day: Int?,
        precision: DatePrecision,
    ): FlexibleDate? {
        if (year == null) {
            requireBackup(month == null && day == null) { "Hi ha una data parcial no vàlida." }
            return null
        }
        return FlexibleDate(
            year = year,
            month = month,
            day = day,
            precision = precision,
        )
    }

    private fun requireCountryExists(
        countryIso2: String,
        validCountryIso2: Set<String>,
    ) {
        requireBackup(countryIso2 in validCountryIso2) {
            "La còpia fa referència a un país que no existeix al dataset actual: $countryIso2."
        }
    }

    private inline fun <reified T : Enum<T>> requireEnum(
        value: String,
        message: String,
    ) {
        requireBackup(enumValueOrNull<T>(value) != null) { message }
    }

    private fun requireUnique(
        values: List<String>,
        message: String,
    ) {
        requireBackup(values.size == values.toSet().size) { message }
    }

    private inline fun requireBackup(
        condition: Boolean,
        message: () -> String,
    ) {
        if (!condition) {
            throw BackupValidationException(message())
        }
    }

    private inline fun <reified T : Enum<T>> enumValueOrNull(value: String): T? =
        runCatching { enumValueOf<T>(value) }.getOrNull()

    companion object {
        const val BACKUP_VERSION = 1
    }
}
