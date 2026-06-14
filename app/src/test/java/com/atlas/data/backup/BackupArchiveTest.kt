package com.atlas.data.backup

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BackupArchiveTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun zipRoundTripPreservesJsonAndPhotos() {
        val filename = "11111111-1111-1111-1111-111111111111.jpg"
        val photoBytes = byteArrayOf(1, 2, 3, 4)
        val photo = temporaryFolder.newFile(filename).apply { writeBytes(photoBytes) }
        val archive = temporaryFolder.newFile("backup.atlasbackup")
        archive.outputStream().use { output ->
            BackupArchive.write(output, """{"backupVersion":3}""", mapOf(filename to photo))
        }

        val contents = BackupArchive.read(archive)
        val restoredDirectory = temporaryFolder.newFolder("restored")
        BackupArchive.extractPhotos(archive, contents, setOf(filename), restoredDirectory)

        assertTrue(contents.isZip)
        assertEquals("""{"backupVersion":3}""", contents.jsonPayload)
        assertEquals(setOf(filename), contents.photoEntryNames.keys)
        assertArrayEquals(photoBytes, File(restoredDirectory, filename).readBytes())
    }

    @Test
    fun plainJsonIsDetectedAsLegacyBackup() {
        val backup = temporaryFolder.newFile("backup.json").apply {
            writeText("""{"backupVersion":2}""")
        }

        val contents = BackupArchive.read(backup)

        assertFalse(contents.isZip)
        assertEquals("""{"backupVersion":2}""", contents.jsonPayload)
        assertTrue(contents.photoEntryNames.isEmpty())
    }

    @Test
    fun unknownZipEntryIsRejected() {
        val archive = temporaryFolder.newFile("unsafe.atlasbackup")
        ZipOutputStream(archive.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry(BackupArchive.JSON_ENTRY_NAME))
            zip.write("""{"backupVersion":3}""".toByteArray())
            zip.closeEntry()
            zip.putNextEntry(ZipEntry("unexpected.txt"))
            zip.write(byteArrayOf(1))
            zip.closeEntry()
        }

        assertThrows(BackupValidationException::class.java) {
            BackupArchive.read(archive)
        }
    }

    @Test
    fun extractionRejectsActualBytesBeyondCumulativeLimit() {
        val firstFilename = "11111111-1111-1111-1111-111111111111.jpg"
        val secondFilename = "22222222-2222-2222-2222-222222222222.jpg"
        val archive = temporaryFolder.newFile("oversized.atlasbackup")
        archive.outputStream().use { output ->
            BackupArchive.write(
                output = output,
                jsonPayload = "{}",
                photoFiles = mapOf(
                    firstFilename to temporaryFolder.newFile(firstFilename).apply {
                        writeBytes(byteArrayOf(1, 2))
                    },
                    secondFilename to temporaryFolder.newFile(secondFilename).apply {
                        writeBytes(byteArrayOf(3, 4))
                    },
                ),
            )
        }
        val contents = BackupArchive.read(archive)

        assertThrows(BackupValidationException::class.java) {
            BackupArchive.extractPhotos(
                archiveFile = archive,
                contents = contents,
                filenames = setOf(firstFilename, secondFilename),
                targetDirectory = temporaryFolder.newFolder("limited"),
                maximumTotalUncompressedBytes = 5,
            )
        }
    }
}
