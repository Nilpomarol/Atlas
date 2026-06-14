package com.atlas.data.backup

import org.junit.Assert.assertEquals
import org.junit.Test

class CloudBackupRetentionTest {
    @Test
    fun keepsThreeNewestAutomaticBackups() {
        val documents = listOf(
            automaticDocument("2026-01-01-010101-001", 1L),
            automaticDocument("2026-02-01-010101-002", 2L),
            automaticDocument("2026-03-01-010101-003", 3L),
            automaticDocument("2026-04-01-010101-004", 4L),
            automaticDocument("2026-05-01-010101-005", 5L),
        )

        val deleted = cloudBackupDocumentsToDelete(documents)

        assertEquals(
            listOf(
                "atlas-auto-backup-2026-02-01-010101-002.atlasbackup",
                "atlas-auto-backup-2026-01-01-010101-001.atlasbackup",
            ),
            deleted.map { it.displayName },
        )
    }

    @Test
    fun ignoresManualAndUnrelatedFiles() {
        val documents = listOf(
            automaticDocument("2026-05-01-010101-005", 5L),
            automaticDocument("2026-04-01-010101-004", 4L),
            automaticDocument("2026-03-01-010101-003", 3L),
            automaticDocument("2026-02-01-010101-002", 2L),
            CloudBackupDocument(
                documentId = "manual",
                displayName = "atlas-backup-2026-01-01.atlasbackup",
                lastModified = 1L,
            ),
            CloudBackupDocument(
                documentId = "notes",
                displayName = "notes.txt",
                lastModified = 1L,
            ),
        )

        val deleted = cloudBackupDocumentsToDelete(documents)

        assertEquals(
            listOf("atlas-auto-backup-2026-02-01-010101-002.atlasbackup"),
            deleted.map { it.displayName },
        )
    }

    @Test
    fun includesCurrentBackupWhenProviderListingHasNotCaughtUp() {
        val oldDocuments = listOf(
            automaticDocument("2026-03-01-010101-003", 3L),
            automaticDocument("2026-04-01-010101-004", 4L),
            automaticDocument("2026-05-01-010101-005", 5L),
        )
        val currentDocument = automaticDocument("2026-06-01-010101-006", 6L)

        val deleted = cloudBackupDocumentsToDelete(
            cloudBackupDocumentsIncludingCurrent(oldDocuments, currentDocument),
        )

        assertEquals(
            listOf("atlas-auto-backup-2026-03-01-010101-003.atlasbackup"),
            deleted.map { it.displayName },
        )
    }

    @Test
    fun currentBackupMetadataReplacesStaleProviderMetadata() {
        val currentDocument = automaticDocument("2026-06-01-010101-006", 6L)
        val documents = listOf(
            currentDocument.copy(lastModified = 0L),
            automaticDocument("2026-03-01-010101-003", 3L),
            automaticDocument("2026-04-01-010101-004", 4L),
            automaticDocument("2026-05-01-010101-005", 5L),
        )

        val merged = cloudBackupDocumentsIncludingCurrent(documents, currentDocument)
        val deleted = cloudBackupDocumentsToDelete(merged)

        assertEquals(6L, merged.single { it.documentId == currentDocument.documentId }.lastModified)
        assertEquals(
            listOf("atlas-auto-backup-2026-03-01-010101-003.atlasbackup"),
            deleted.map { it.displayName },
        )
    }

    private fun automaticDocument(timestamp: String, lastModified: Long) =
        CloudBackupDocument(
            documentId = timestamp,
            displayName = "atlas-auto-backup-$timestamp.atlasbackup",
            lastModified = lastModified,
        )
}
