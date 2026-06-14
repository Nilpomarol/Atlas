package com.atlas.data.backup

data class CloudBackupDocument(
    val documentId: String,
    val displayName: String,
    val lastModified: Long,
)

fun cloudBackupDocumentsIncludingCurrent(
    documents: List<CloudBackupDocument>,
    currentDocument: CloudBackupDocument,
): List<CloudBackupDocument> =
    documents
        .filterNot { it.documentId == currentDocument.documentId }
        .plus(currentDocument)

fun cloudBackupDocumentsToDelete(
    documents: List<CloudBackupDocument>,
    keepCount: Int = 3,
): List<CloudBackupDocument> =
    documents
        .filter { AUTO_BACKUP_FILENAME.matches(it.displayName) }
        .sortedWith(
            compareByDescending<CloudBackupDocument> { it.lastModified }
                .thenByDescending { it.displayName },
        )
        .drop(keepCount.coerceAtLeast(0))

private val AUTO_BACKUP_FILENAME =
    Regex("""atlas-auto-backup-\d{4}-\d{2}-\d{2}-\d{6}-\d{3}\.atlasbackup""")
