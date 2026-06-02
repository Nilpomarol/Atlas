package com.atlas.domain.repository

data class BackupImportPreview(
    val countryUserStateCount: Int,
    val countryLogCount: Int,
    val tripCount: Int,
    val tripStopCount: Int,
    val flightCount: Int = 0,
    val itineraryCount: Int = 0,
    val excursionCount: Int = 0,
)

interface BackupRepository {
    suspend fun exportBackupJson(): String
    suspend fun previewImport(json: String): BackupImportPreview
    suspend fun importBackupJson(json: String): BackupImportPreview
}
