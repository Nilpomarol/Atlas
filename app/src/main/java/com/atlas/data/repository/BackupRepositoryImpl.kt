package com.atlas.data.repository

import androidx.room.withTransaction
import com.atlas.core.constants.DatasetConstants
import com.atlas.data.backup.AtlasBackupDataV2
import com.atlas.data.backup.AtlasBackupV2
import com.atlas.data.backup.BackupValidationException
import com.atlas.data.backup.BackupValidator
import com.atlas.data.backup.toBackupV1
import com.atlas.data.backup.toBackupV2
import com.atlas.data.backup.toEntity
import com.atlas.data.local.database.AtlasDatabase
import com.atlas.domain.repository.BackupImportPreview
import com.atlas.domain.repository.BackupRepository
import java.time.Instant
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class BackupRepositoryImpl(
    private val database: AtlasDatabase,
    private val validator: BackupValidator = BackupValidator(),
) : BackupRepository {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = false
        encodeDefaults = true
    }

    override suspend fun exportBackupJson(): String {
        val countryDatasetVersion = database.datasetMetadataDao()
            .getByKey(DatasetConstants.COUNTRIES_KEY)?.version ?: DatasetConstants.COUNTRIES_VERSION
        val airportDatasetVersion = database.datasetMetadataDao()
            .getByKey(DatasetConstants.AIRPORTS_KEY)?.version ?: DatasetConstants.AIRPORTS_VERSION

        val backup = AtlasBackupV2(
            backupVersion = BackupValidator.BACKUP_VERSION,
            createdAt = Instant.now().toString(),
            countryDatasetVersion = countryDatasetVersion,
            airportDatasetVersion = airportDatasetVersion,
            data = AtlasBackupDataV2(
                countryUserStates = database.countryUserStateDao().getAll().map { it.toBackupV1() },
                countryLogs = database.countryLogDao().getAll().map { it.toBackupV1() },
                trips = database.tripDao().getAll().map { it.toBackupV1() },
                tripStops = database.tripStopDao().getAll().map { it.toBackupV2() },
                places = emptyList(),
                flights = database.flightDao().getAll().map { it.toBackupV2() },
                itineraries = database.itineraryDao().getAll().map { it.toBackupV2() },
                itineraryGroups = database.itineraryDao().getAllGroups().map { it.toBackupV2() },
                excursions = database.excursionDao().getAll().map { it.toBackupV2() },
                excursionStops = database.excursionDao().getAllStops().map { it.toBackupV2() },
            ),
        )

        return json.encodeToString(AtlasBackupV2.serializer(), backup)
    }

    override suspend fun previewImport(json: String): BackupImportPreview {
        val backup = decode(json)
        validator.validate(backup = backup, validCountryIso2 = database.countryDao().getAllIso2().toSet())
        return backup.toPreview()
    }

    override suspend fun importBackupJson(json: String): BackupImportPreview {
        val backup = decode(json)
        validator.validate(backup = backup, validCountryIso2 = database.countryDao().getAllIso2().toSet())

        database.withTransaction {
            // Delete in reverse dependency order
            database.flightDao().deleteAll()
            database.itineraryDao().deleteAll()     // cascades to itinerary_groups
            database.excursionDao().deleteAll()     // cascades to excursion_stops
            database.tripStopDao().deleteAll()
            database.tripDao().deleteAll()
            database.countryLogDao().deleteAll()
            database.countryUserStateDao().deleteAll()

            // Insert in dependency order
            database.countryUserStateDao().upsertAll(backup.data.countryUserStates.map { it.toEntity() })
            database.countryLogDao().upsertAll(backup.data.countryLogs.map { it.toEntity() })
            database.tripDao().upsertAll(backup.data.trips.map { it.toEntity() })
            database.tripStopDao().upsertAll(backup.data.tripStops.map { it.toEntity() })
            // Itineraries before groups (FK dependency)
            backup.data.itineraries.forEach { database.itineraryDao().upsertItinerary(it.toEntity()) }
            database.itineraryDao().upsertGroups(backup.data.itineraryGroups.map { it.toEntity() })
            // Flights after groups (itinerary_group_id FK)
            database.flightDao().upsertAll(backup.data.flights.map { it.toEntity() })
            // Excursions after trip stops (anchor FK), excursion stops after excursions (CASCADE)
            backup.data.excursions.forEach { database.excursionDao().upsertExcursion(it.toEntity()) }
            backup.data.excursionStops.forEach { database.excursionDao().upsertStop(it.toEntity()) }
        }

        return backup.toPreview()
    }

    private fun decode(rawJson: String): AtlasBackupV2 =
        try {
            json.decodeFromString(AtlasBackupV2.serializer(), rawJson)
        } catch (_: SerializationException) {
            throw BackupValidationException("El fitxer no és una còpia JSON vàlida d'Atlas.")
        } catch (_: IllegalArgumentException) {
            throw BackupValidationException("El fitxer no és una còpia JSON vàlida d'Atlas.")
        }

    private fun AtlasBackupV2.toPreview(): BackupImportPreview =
        BackupImportPreview(
            countryUserStateCount = data.countryUserStates.size,
            countryLogCount = data.countryLogs.size,
            tripCount = data.trips.size,
            tripStopCount = data.tripStops.size,
            flightCount = data.flights.size,
            itineraryCount = data.itineraries.size,
            excursionCount = data.excursions.size,
        )
}
