package com.atlas.data.repository

import androidx.room.withTransaction
import com.atlas.core.constants.DatasetConstants
import com.atlas.data.backup.AtlasBackupDataV1
import com.atlas.data.backup.AtlasBackupV1
import com.atlas.data.backup.BackupValidationException
import com.atlas.data.backup.BackupValidator
import com.atlas.data.backup.toBackupV1
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
            .getByKey(DatasetConstants.COUNTRIES_KEY)
            ?.version
            ?: DatasetConstants.COUNTRIES_VERSION

        val backup = AtlasBackupV1(
            backupVersion = BackupValidator.BACKUP_VERSION,
            createdAt = Instant.now().toString(),
            countryDatasetVersion = countryDatasetVersion,
            data = AtlasBackupDataV1(
                countryUserStates = database.countryUserStateDao().getAll().map { it.toBackupV1() },
                countryLogs = database.countryLogDao().getAll().map { it.toBackupV1() },
                trips = database.tripDao().getAll().map { it.toBackupV1() },
                tripStops = database.tripStopDao().getAll().map { it.toBackupV1() },
                places = emptyList(),
            ),
        )

        return json.encodeToString(AtlasBackupV1.serializer(), backup)
    }

    override suspend fun previewImport(json: String): BackupImportPreview {
        val backup = decode(json)
        validator.validate(
            backup = backup,
            validCountryIso2 = database.countryDao().getAllIso2().toSet(),
        )
        return backup.toPreview()
    }

    override suspend fun importBackupJson(json: String): BackupImportPreview {
        val backup = decode(json)
        validator.validate(
            backup = backup,
            validCountryIso2 = database.countryDao().getAllIso2().toSet(),
        )

        database.withTransaction {
            database.tripStopDao().deleteAll()
            database.tripDao().deleteAll()
            database.countryLogDao().deleteAll()
            database.countryUserStateDao().deleteAll()

            database.countryUserStateDao().upsertAll(
                backup.data.countryUserStates.map { it.toEntity() },
            )
            database.countryLogDao().upsertAll(
                backup.data.countryLogs.map { it.toEntity() },
            )
            database.tripDao().upsertAll(
                backup.data.trips.map { it.toEntity() },
            )
            database.tripStopDao().upsertAll(
                backup.data.tripStops.map { it.toEntity() },
            )
        }

        return backup.toPreview()
    }

    private fun decode(rawJson: String): AtlasBackupV1 =
        try {
            json.decodeFromString(AtlasBackupV1.serializer(), rawJson)
        } catch (_: SerializationException) {
            throw BackupValidationException("El fitxer no és una còpia JSON vàlida d'Atlas.")
        } catch (_: IllegalArgumentException) {
            throw BackupValidationException("El fitxer no és una còpia JSON vàlida d'Atlas.")
        }

    private fun AtlasBackupV1.toPreview(): BackupImportPreview =
        BackupImportPreview(
            countryUserStateCount = data.countryUserStates.size,
            countryLogCount = data.countryLogs.size,
            tripCount = data.trips.size,
            tripStopCount = data.tripStops.size,
        )
}
