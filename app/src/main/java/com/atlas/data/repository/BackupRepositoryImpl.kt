package com.atlas.data.repository

import androidx.room.withTransaction
import com.atlas.core.constants.DatasetConstants
import com.atlas.data.backup.AtlasBackupDataV3
import com.atlas.data.backup.AtlasBackupV3
import com.atlas.data.backup.BackupArchive
import com.atlas.data.backup.BackupArchiveContents
import com.atlas.data.backup.BackupValidationException
import com.atlas.data.backup.BackupValidator
import com.atlas.data.backup.toBackupV1
import com.atlas.data.backup.toBackupV2
import com.atlas.data.backup.toBackupV3
import com.atlas.data.backup.toEntity
import com.atlas.data.backup.withAvailablePhotos
import com.atlas.data.local.database.AtlasDatabase
import com.atlas.domain.repository.BackupImportPreview
import com.atlas.domain.repository.BackupRepository
import java.io.File
import java.io.OutputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class BackupRepositoryImpl(
    private val database: AtlasDatabase,
    private val photosDirectory: File,
    private val validator: BackupValidator = BackupValidator(),
) : BackupRepository {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = false
        encodeDefaults = true
    }

    override suspend fun exportBackup(output: OutputStream) = withContext(Dispatchers.IO) {
        val backup = buildBackup()
        val jsonPayload = json.encodeToString(AtlasBackupV3.serializer(), backup)
        val photoFiles = backup.data.stopPhotos
            .associate { photo -> photo.filename to File(photosDirectory, photo.filename) }
        BackupArchive.write(
            output = output,
            jsonPayload = jsonPayload,
            photoFiles = photoFiles,
        )
    }

    override suspend fun previewImport(file: File): BackupImportPreview = withContext(Dispatchers.IO) {
        prepareImport(file).backup.toPreview()
    }

    override suspend fun importBackup(file: File): BackupImportPreview = withContext(Dispatchers.IO) {
        val prepared = prepareImport(file)
        val stagingDirectory = createWorkingDirectory("stage")
        var directorySwap: PhotoDirectorySwap? = null

        try {
            BackupArchive.extractPhotos(
                archiveFile = file,
                contents = prepared.archive,
                filenames = prepared.backup.data.stopPhotos.map { it.filename }.toSet(),
                targetDirectory = stagingDirectory,
            )

            database.withTransaction {
                replaceDatabaseRows(prepared.backup)
                directorySwap = replacePhotoDirectory(stagingDirectory)
            }
        } catch (error: Throwable) {
            val rollbackError = directorySwap?.let { swap ->
                runCatching { rollbackPhotoDirectory(swap) }.exceptionOrNull()
            }
            deleteRecursivelyBestEffort(stagingDirectory)
            if (rollbackError != null) {
                error.addSuppressed(rollbackError)
            }
            throw error
        }

        directorySwap?.previousDirectory?.let(::deleteRecursivelyBestEffort)
        prepared.backup.toPreview()
    }

    private suspend fun buildBackup(): AtlasBackupV3 {
        val backup = database.withTransaction {
            val countryDatasetVersion = database.datasetMetadataDao()
                .getByKey(DatasetConstants.COUNTRIES_KEY)?.version ?: DatasetConstants.COUNTRIES_VERSION
            val airportDatasetVersion = database.datasetMetadataDao()
                .getByKey(DatasetConstants.AIRPORTS_KEY)?.version ?: DatasetConstants.AIRPORTS_VERSION

            AtlasBackupV3(
                backupVersion = BackupValidator.BACKUP_VERSION,
                createdAt = Instant.now().toString(),
                countryDatasetVersion = countryDatasetVersion,
                airportDatasetVersion = airportDatasetVersion,
                data = AtlasBackupDataV3(
                    countryUserStates = database.countryUserStateDao().getAll().map { it.toBackupV1() },
                    countryLogs = database.countryLogDao().getAll().map { it.toBackupV1() },
                    trips = database.tripDao().getAll().map { it.toBackupV3() },
                    tripStops = database.tripStopDao().getAll().map { it.toBackupV2() },
                    places = emptyList(),
                    flights = database.flightDao().getAll().map { it.toBackupV2() },
                    itineraries = database.itineraryDao().getAll().map { it.toBackupV2() },
                    itineraryGroups = database.itineraryDao().getAllGroups().map { it.toBackupV2() },
                    excursions = database.excursionDao().getAll().map { it.toBackupV2() },
                    excursionStops = database.excursionDao().getAllStops().map { it.toBackupV2() },
                    stopPhotos = database.stopPhotoDao().getAll().map { it.toBackupV3() },
                ),
            )
        }
        val existingPhotoFilenames = backup.data.stopPhotos
            .map { it.filename }
            .filterTo(mutableSetOf()) { File(photosDirectory, it).isFile }
        return backup.withAvailablePhotos(existingPhotoFilenames)
    }

    private suspend fun prepareImport(file: File): PreparedImport {
        val archive = BackupArchive.read(file)
        val decoded = decode(archive.jsonPayload)
        val validCountryIso2 = database.countryDao().getAllIso2().toSet()
        validator.validate(backup = decoded, validCountryIso2 = validCountryIso2)
        val sanitized = decoded.withAvailablePhotos(archive.photoEntryNames.keys)
        validator.validate(backup = sanitized, validCountryIso2 = validCountryIso2)
        return PreparedImport(backup = sanitized, archive = archive)
    }

    private suspend fun replaceDatabaseRows(backup: AtlasBackupV3) {
        database.stopPhotoDao().deleteAll()
        database.flightDao().deleteAll()
        database.itineraryDao().deleteAll()
        database.excursionDao().deleteAll()
        database.tripStopDao().deleteAll()
        database.tripDao().deleteAll()
        database.countryLogDao().deleteAll()
        database.countryUserStateDao().deleteAll()

        database.countryUserStateDao().upsertAll(backup.data.countryUserStates.map { it.toEntity() })
        database.countryLogDao().upsertAll(backup.data.countryLogs.map { it.toEntity() })
        database.tripDao().upsertAll(backup.data.trips.map { it.toEntity() })
        database.tripStopDao().upsertAll(backup.data.tripStops.map { it.toEntity() })
        backup.data.itineraries.forEach { database.itineraryDao().upsertItinerary(it.toEntity()) }
        database.itineraryDao().upsertGroups(backup.data.itineraryGroups.map { it.toEntity() })
        database.flightDao().upsertAll(backup.data.flights.map { it.toEntity() })
        backup.data.excursions.forEach { database.excursionDao().upsertExcursion(it.toEntity()) }
        backup.data.excursionStops.forEach { database.excursionDao().upsertStop(it.toEntity()) }
        database.stopPhotoDao().upsertAll(backup.data.stopPhotos.map { it.toEntity() })
    }

    private fun decode(rawJson: String): AtlasBackupV3 =
        try {
            json.decodeFromString(AtlasBackupV3.serializer(), rawJson)
        } catch (_: SerializationException) {
            throw BackupValidationException("El fitxer no és una còpia JSON vàlida d'Atlas.")
        } catch (_: IllegalArgumentException) {
            throw BackupValidationException("El fitxer no és una còpia JSON vàlida d'Atlas.")
        }

    private fun createWorkingDirectory(label: String): File {
        val parent = photosDirectory.parentFile
            ?: throw BackupValidationException("No s'ha pogut preparar la restauració de fotos.")
        if (!parent.exists() && !parent.mkdirs()) {
            throw BackupValidationException("No s'ha pogut preparar la restauració de fotos.")
        }
        val directory = File(parent, ".atlas-photos-$label-${UUID.randomUUID()}")
        if (!directory.mkdir()) {
            throw BackupValidationException("No s'ha pogut preparar la restauració de fotos.")
        }
        return directory
    }

    private fun replacePhotoDirectory(stagingDirectory: File): PhotoDirectorySwap {
        val previousDirectory = if (photosDirectory.exists()) {
            createUnusedWorkingPath("previous").also { previous ->
                moveDirectory(photosDirectory, previous)
            }
        } else {
            null
        }

        try {
            moveDirectory(stagingDirectory, photosDirectory)
        } catch (error: Throwable) {
            if (photosDirectory.exists()) {
                runCatching { deleteRecursivelyChecked(photosDirectory) }
                    .exceptionOrNull()
                    ?.let(error::addSuppressed)
            }
            previousDirectory?.let { previous ->
                runCatching { moveDirectory(previous, photosDirectory) }
                    .exceptionOrNull()
                    ?.let(error::addSuppressed)
            }
            throw error
        }
        return PhotoDirectorySwap(previousDirectory = previousDirectory)
    }

    private fun rollbackPhotoDirectory(swap: PhotoDirectorySwap) {
        deleteRecursivelyChecked(photosDirectory)
        swap.previousDirectory?.let { previous ->
            moveDirectory(previous, photosDirectory)
        }
    }

    private fun createUnusedWorkingPath(label: String): File {
        val parent = photosDirectory.parentFile
            ?: throw BackupValidationException("No s'ha pogut preparar la restauració de fotos.")
        return File(parent, ".atlas-photos-$label-${UUID.randomUUID()}")
    }

    private fun moveDirectory(source: File, target: File) {
        try {
            Files.move(source.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(source.toPath(), target.toPath())
        }
    }

    private fun deleteRecursivelyChecked(directory: File) {
        if (directory.exists() && !directory.deleteRecursively()) {
            throw BackupValidationException("No s'ha pogut revertir la restauració de fotos.")
        }
    }

    private fun deleteRecursivelyBestEffort(directory: File) {
        runCatching { directory.deleteRecursively() }
    }

    private fun AtlasBackupV3.toPreview(): BackupImportPreview =
        BackupImportPreview(
            countryUserStateCount = data.countryUserStates.size,
            countryLogCount = data.countryLogs.size,
            tripCount = data.trips.size,
            tripStopCount = data.tripStops.size,
            flightCount = data.flights.size,
            itineraryCount = data.itineraries.size,
            excursionCount = data.excursions.size,
            photoCount = data.stopPhotos.size,
        )

    private data class PreparedImport(
        val backup: AtlasBackupV3,
        val archive: BackupArchiveContents,
    )

    private data class PhotoDirectorySwap(
        val previousDirectory: File?,
    )
}
