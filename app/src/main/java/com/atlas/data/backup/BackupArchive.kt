package com.atlas.data.backup

import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FilterOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

data class BackupArchiveContents(
    val jsonPayload: String,
    val photoEntryNames: Map<String, String>,
    val isZip: Boolean,
)

object BackupArchive {
    const val JSON_ENTRY_NAME = "atlas-backup.json"
    private const val PHOTOS_PREFIX = "photos/"
    private const val MAX_JSON_BYTES = 32L * 1024L * 1024L
    private const val MAX_PHOTO_BYTES = 25L * 1024L * 1024L
    private const val MAX_TOTAL_UNCOMPRESSED_BYTES = 2L * 1024L * 1024L * 1024L
    private const val MAX_PHOTO_ENTRIES = 50_000

    fun write(
        output: OutputStream,
        jsonPayload: String,
        photoFiles: Map<String, File>,
    ) {
        val jsonBytes = jsonPayload.toByteArray(StandardCharsets.UTF_8)
        requireBackup(jsonBytes.size.toLong() <= MAX_JSON_BYTES) {
            "Les dades de la còpia són massa grans."
        }
        requireBackup(photoFiles.size <= MAX_PHOTO_ENTRIES) {
            "La còpia conté massa fotos."
        }
        var totalSize = jsonBytes.size.toLong()
        photoFiles.forEach { (filename, file) ->
            requireValidPhotoFilename(filename)
            if (file.isFile) {
                requireBackup(file.length() <= MAX_PHOTO_BYTES) {
                    "La còpia conté una foto massa gran."
                }
                totalSize += file.length()
                requireBackup(totalSize <= MAX_TOTAL_UNCOMPRESSED_BYTES) {
                    "La còpia és massa gran."
                }
            }
        }

        ZipOutputStream(BufferedOutputStream(NonClosingOutputStream(output))).use { zip ->
            zip.putNextEntry(ZipEntry(JSON_ENTRY_NAME))
            zip.write(jsonBytes)
            zip.closeEntry()

            photoFiles.toSortedMap().forEach { (filename, file) ->
                if (!file.isFile) return@forEach
                zip.putNextEntry(ZipEntry("$PHOTOS_PREFIX$filename"))
                file.inputStream().buffered().use { input -> input.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    fun read(file: File): BackupArchiveContents {
        requireBackup(file.isFile) { "No s'ha pogut obrir el fitxer de còpia." }
        return try {
            if (hasZipMagic(file)) readZip(file) else readLegacyJson(file)
        } catch (error: BackupValidationException) {
            throw error
        } catch (_: IOException) {
            throw BackupValidationException("No s'ha pogut llegir el fitxer de còpia.")
        }
    }

    fun extractPhotos(
        archiveFile: File,
        contents: BackupArchiveContents,
        filenames: Set<String>,
        targetDirectory: File,
    ) {
        extractPhotos(
            archiveFile = archiveFile,
            contents = contents,
            filenames = filenames,
            targetDirectory = targetDirectory,
            maximumTotalUncompressedBytes = MAX_TOTAL_UNCOMPRESSED_BYTES,
        )
    }

    internal fun extractPhotos(
        archiveFile: File,
        contents: BackupArchiveContents,
        filenames: Set<String>,
        targetDirectory: File,
        maximumTotalUncompressedBytes: Long,
    ) {
        requireBackup(targetDirectory.mkdirs() || targetDirectory.isDirectory) {
            "No s'ha pogut preparar la restauració de fotos."
        }
        if (!contents.isZip || filenames.isEmpty()) return
        requireBackup(filenames.all { it in contents.photoEntryNames }) {
            "La còpia no conté totes les fotos que declara."
        }
        var totalExtractedBytes = contents.jsonPayload.toByteArray(StandardCharsets.UTF_8).size.toLong()
        requireBackup(totalExtractedBytes <= maximumTotalUncompressedBytes) {
            "La còpia és massa gran."
        }

        try {
            ZipFile(archiveFile).use { zip ->
                filenames.forEach { filename ->
                    requireValidPhotoFilename(filename)
                    val entryName = contents.photoEntryNames.getValue(filename)
                    val entry = zip.getEntry(entryName)
                        ?: throw BackupValidationException("La còpia no conté totes les fotos que declara.")
                    val outputFile = File(targetDirectory, filename)
                    zip.getInputStream(entry).buffered().use { input ->
                        outputFile.outputStream().buffered().use { output ->
                            val remainingBytes = maximumTotalUncompressedBytes - totalExtractedBytes
                            requireBackup(remainingBytes > 0) { "La còpia és massa gran." }
                            val copiedBytes = copyWithLimit(
                                input = input,
                                output = output,
                                limit = minOf(MAX_PHOTO_BYTES, remainingBytes),
                            )
                            totalExtractedBytes += copiedBytes
                        }
                    }
                }
            }
        } catch (error: BackupValidationException) {
            throw error
        } catch (_: IOException) {
            throw BackupValidationException("El fitxer de còpia ZIP està malmès.")
        }
    }

    private fun readZip(file: File): BackupArchiveContents {
        try {
            ZipFile(file).use { zip ->
                var jsonEntry: ZipEntry? = null
                val photoEntries = linkedMapOf<String, String>()
                val seenNames = mutableSetOf<String>()
                var totalDeclaredSize = 0L
                var photoCount = 0
                val entries = zip.entries()

                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val name = entry.name
                    requireBackup(seenNames.add(name)) { "La còpia conté entrades ZIP duplicades." }
                    requireBackup(!name.contains('\\') && !name.startsWith('/') && ".." !in name.split('/')) {
                        "La còpia conté una ruta ZIP no vàlida."
                    }

                    if (entry.isDirectory) {
                        requireBackup(name == PHOTOS_PREFIX) { "La còpia conté una carpeta ZIP desconeguda." }
                        continue
                    }

                    when {
                        name == JSON_ENTRY_NAME -> {
                            requireBackup(jsonEntry == null) { "La còpia conté més d'un fitxer de dades." }
                            requireDeclaredSize(entry, MAX_JSON_BYTES)
                            jsonEntry = entry
                        }
                        name.startsWith(PHOTOS_PREFIX) -> {
                            val filename = name.removePrefix(PHOTOS_PREFIX)
                            requireBackup('/' !in filename && filename.isNotEmpty()) {
                                "La còpia conté una ruta de foto no vàlida."
                            }
                            requireValidPhotoFilename(filename)
                            requireDeclaredSize(entry, MAX_PHOTO_BYTES)
                            requireBackup(photoEntries.put(filename, name) == null) {
                                "La còpia conté fotos duplicades."
                            }
                            photoCount += 1
                            requireBackup(photoCount <= MAX_PHOTO_ENTRIES) {
                                "La còpia conté massa fotos."
                            }
                        }
                        else -> throw BackupValidationException("La còpia conté una entrada ZIP desconeguda.")
                    }

                    if (entry.size >= 0) {
                        totalDeclaredSize += entry.size
                        requireBackup(totalDeclaredSize <= MAX_TOTAL_UNCOMPRESSED_BYTES) {
                            "La còpia és massa gran."
                        }
                    }
                }

                val requiredJsonEntry = jsonEntry
                    ?: throw BackupValidationException("La còpia no conté $JSON_ENTRY_NAME.")
                val jsonPayload = zip.getInputStream(requiredJsonEntry).buffered().use { input ->
                    readUtf8WithLimit(input, MAX_JSON_BYTES)
                }
                return BackupArchiveContents(
                    jsonPayload = jsonPayload,
                    photoEntryNames = photoEntries,
                    isZip = true,
                )
            }
        } catch (error: BackupValidationException) {
            throw error
        } catch (_: ZipException) {
            throw BackupValidationException("El fitxer de còpia ZIP està malmès.")
        }
    }

    private fun readLegacyJson(file: File): BackupArchiveContents {
        requireBackup(file.length() <= MAX_JSON_BYTES) { "El fitxer de còpia és massa gran." }
        val jsonPayload = file.inputStream().buffered().use { input ->
            readUtf8WithLimit(input, MAX_JSON_BYTES)
        }
        return BackupArchiveContents(
            jsonPayload = jsonPayload,
            photoEntryNames = emptyMap(),
            isZip = false,
        )
    }

    private fun hasZipMagic(file: File): Boolean =
        FileInputStream(file).use { input ->
            val magic = ByteArray(4)
            input.read(magic) == magic.size &&
                magic[0] == 'P'.code.toByte() &&
                magic[1] == 'K'.code.toByte() &&
                magic[2] == 3.toByte() &&
                magic[3] == 4.toByte()
        }

    private fun readUtf8WithLimit(input: InputStream, limit: Long): String {
        val output = ByteArrayOutputStream()
        copyWithLimit(input, output, limit)
        return output.toString(StandardCharsets.UTF_8.name())
    }

    private fun copyWithLimit(input: InputStream, output: OutputStream, limit: Long): Long {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            requireBackup(total <= limit) { "La còpia conté un fitxer massa gran." }
            output.write(buffer, 0, read)
        }
        return total
    }

    private fun requireDeclaredSize(entry: ZipEntry, limit: Long) {
        if (entry.size >= 0) {
            requireBackup(entry.size <= limit) { "La còpia conté un fitxer massa gran." }
        }
    }

    private fun requireValidPhotoFilename(filename: String) {
        requireBackup(filename.endsWith(".jpg", ignoreCase = true)) {
            "La còpia conté un nom de foto no vàlid."
        }
        requireBackup(runCatching { UUID.fromString(filename.dropLast(4)) }.isSuccess) {
            "La còpia conté un nom de foto no vàlid."
        }
    }

    private inline fun requireBackup(condition: Boolean, message: () -> String) {
        if (!condition) throw BackupValidationException(message())
    }

    private class NonClosingOutputStream(output: OutputStream) : FilterOutputStream(output) {
        override fun close() {
            flush()
        }
    }
}
