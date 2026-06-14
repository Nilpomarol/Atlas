package com.atlas.domain.repository

import java.io.File
import java.io.OutputStream

data class BackupImportPreview(
    val countryUserStateCount: Int,
    val countryLogCount: Int,
    val tripCount: Int,
    val tripStopCount: Int,
    val flightCount: Int = 0,
    val itineraryCount: Int = 0,
    val excursionCount: Int = 0,
    val photoCount: Int = 0,
)

interface BackupRepository {
    suspend fun exportBackup(output: OutputStream)
    suspend fun previewImport(file: File): BackupImportPreview
    suspend fun importBackup(file: File): BackupImportPreview
}
