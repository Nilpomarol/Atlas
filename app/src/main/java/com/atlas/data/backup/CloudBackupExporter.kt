package com.atlas.data.backup

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.atlas.domain.repository.BackupRepository
import com.atlas.domain.repository.CloudBackupPreferencesRepository
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

sealed interface CloudBackupExportResult {
    data object Success : CloudBackupExportResult
    data object Disabled : CloudBackupExportResult
    data class Failure(val retryable: Boolean) : CloudBackupExportResult
}

class CloudBackupExporter(
    context: Context,
    private val backupRepository: BackupRepository,
    private val preferencesRepository: CloudBackupPreferencesRepository,
) {
    private val applicationContext = context.applicationContext
    private val exportMutex = Mutex()

    suspend fun export(): CloudBackupExportResult = exportMutex.withLock {
        withContext(Dispatchers.IO) {
            val settings = preferencesRepository.getSettings()
            val rawFolderUri = settings.folderUri
            if (!settings.enabled || rawFolderUri.isNullOrBlank()) {
                return@withContext CloudBackupExportResult.Disabled
            }

            val folderUri = Uri.parse(rawFolderUri)
            var createdDocumentUri: Uri? = null
            try {
                val parentDocumentUri = DocumentsContract.buildDocumentUriUsingTree(
                    folderUri,
                    DocumentsContract.getTreeDocumentId(folderUri),
                )
                val createdAt = Instant.now()
                val filename = AUTO_BACKUP_FORMATTER.format(createdAt)
                createdDocumentUri = DocumentsContract.createDocument(
                    applicationContext.contentResolver,
                    parentDocumentUri,
                    BACKUP_MIME_TYPE,
                    filename,
                ) ?: throw IOException("The document provider did not create the backup file.")

                applicationContext.contentResolver.openOutputStream(createdDocumentUri, "w")?.use { output ->
                    backupRepository.exportBackup(output)
                } ?: throw IOException("The document provider did not open the backup file.")

                runCatching {
                    removeOldAutomaticBackups(
                        folderUri = folderUri,
                        currentDocument = CloudBackupDocument(
                            documentId = DocumentsContract.getDocumentId(createdDocumentUri),
                            displayName = filename,
                            lastModified = createdAt.toEpochMilli(),
                        ),
                    )
                }
                preferencesRepository.recordSuccess(Instant.now().toString())
                CloudBackupExportResult.Success
            } catch (error: CancellationException) {
                createdDocumentUri?.let(::deleteDocumentBestEffort)
                throw error
            } catch (_: SecurityException) {
                createdDocumentUri?.let(::deleteDocumentBestEffort)
                preferencesRepository.recordFailure(
                    "S'ha perdut l'accés a la carpeta. Torna-la a seleccionar.",
                )
                CloudBackupExportResult.Failure(retryable = false)
            } catch (_: Exception) {
                createdDocumentUri?.let(::deleteDocumentBestEffort)
                preferencesRepository.recordFailure(
                    "No s'ha pogut crear la còpia al núvol. Atlas ho tornarà a provar.",
                )
                CloudBackupExportResult.Failure(retryable = true)
            }
        }
    }

    private fun removeOldAutomaticBackups(
        folderUri: Uri,
        currentDocument: CloudBackupDocument,
    ) {
        val documents = cloudBackupDocumentsIncludingCurrent(
            documents = listBackupDocuments(folderUri),
            currentDocument = currentDocument,
        )
        cloudBackupDocumentsToDelete(documents).forEach { document ->
            val documentUri = DocumentsContract.buildDocumentUriUsingTree(
                folderUri,
                document.documentId,
            )
            deleteDocumentBestEffort(documentUri)
        }
    }

    private fun listBackupDocuments(folderUri: Uri): List<CloudBackupDocument> {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            folderUri,
            DocumentsContract.getTreeDocumentId(folderUri),
        )
        return applicationContext.contentResolver.query(
            childrenUri,
            DOCUMENT_PROJECTION,
            null,
            null,
            null,
        )?.use { cursor ->
            val documentIdIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val displayNameIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val lastModifiedIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        CloudBackupDocument(
                            documentId = cursor.getString(documentIdIndex),
                            displayName = cursor.getString(displayNameIndex).orEmpty(),
                            lastModified = if (cursor.isNull(lastModifiedIndex)) {
                                0L
                            } else {
                                cursor.getLong(lastModifiedIndex)
                            },
                        ),
                    )
                }
            }
        }.orEmpty()
    }

    private fun deleteDocumentBestEffort(uri: Uri) {
        runCatching {
            DocumentsContract.deleteDocument(applicationContext.contentResolver, uri)
        }
    }

    private companion object {
        const val BACKUP_MIME_TYPE = "application/zip"
        val AUTO_BACKUP_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("'atlas-auto-backup-'yyyy-MM-dd-HHmmss-SSS'.atlasbackup'")
                .withZone(ZoneId.systemDefault())
        val DOCUMENT_PROJECTION = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        )
    }
}
