package com.atlas.domain.repository

import com.atlas.domain.model.CloudBackupSettings
import kotlinx.coroutines.flow.Flow

interface CloudBackupPreferencesRepository {
    fun observeSettings(): Flow<CloudBackupSettings>
    suspend fun getSettings(): CloudBackupSettings
    suspend fun configureFolder(folderUri: String, folderName: String)
    suspend fun setEnabled(enabled: Boolean)
    suspend fun recordSuccess(completedAt: String)
    suspend fun recordFailure(message: String)
    suspend fun clear()
}
