package com.atlas.data.preferences

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.atlas.domain.model.CloudBackupSettings
import com.atlas.domain.repository.CloudBackupPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class CloudBackupPreferencesDataSource(
    private val context: Context,
) : CloudBackupPreferencesRepository {

    override fun observeSettings(): Flow<CloudBackupSettings> =
        context.atlasDataStore.data.map { preferences -> preferences.toSettings() }

    override suspend fun getSettings(): CloudBackupSettings =
        context.atlasDataStore.data.first().toSettings()

    override suspend fun configureFolder(folderUri: String, folderName: String) {
        val previousUri = getSettings().folderUri
        context.atlasDataStore.edit { preferences ->
            preferences[FOLDER_URI] = folderUri
            preferences[FOLDER_NAME] = folderName
            preferences[ENABLED] = true
            preferences.remove(LAST_ERROR)
        }
        if (previousUri != null && previousUri != folderUri) {
            releasePermission(previousUri)
        }
    }

    override suspend fun setEnabled(enabled: Boolean) {
        context.atlasDataStore.edit { preferences ->
            preferences[ENABLED] = enabled
            if (enabled) preferences.remove(LAST_ERROR)
        }
    }

    override suspend fun recordSuccess(completedAt: String) {
        context.atlasDataStore.edit { preferences ->
            preferences[LAST_SUCCESSFUL_BACKUP_AT] = completedAt
            preferences.remove(LAST_ERROR)
        }
    }

    override suspend fun recordFailure(message: String) {
        context.atlasDataStore.edit { preferences ->
            preferences[LAST_ERROR] = message
        }
    }

    override suspend fun clear() {
        val previousUri = getSettings().folderUri
        context.atlasDataStore.edit { preferences ->
            preferences.remove(FOLDER_URI)
            preferences.remove(FOLDER_NAME)
            preferences.remove(ENABLED)
            preferences.remove(LAST_SUCCESSFUL_BACKUP_AT)
            preferences.remove(LAST_ERROR)
        }
        previousUri?.let(::releasePermission)
    }

    private fun releasePermission(rawUri: String) {
        runCatching {
            context.contentResolver.releasePersistableUriPermission(
                Uri.parse(rawUri),
                READ_WRITE_FLAGS,
            )
        }
    }

    private fun androidx.datastore.preferences.core.Preferences.toSettings(): CloudBackupSettings =
        CloudBackupSettings(
            folderUri = this[FOLDER_URI],
            folderName = this[FOLDER_NAME],
            enabled = this[ENABLED] ?: false,
            lastSuccessfulBackupAt = this[LAST_SUCCESSFUL_BACKUP_AT],
            lastError = this[LAST_ERROR],
        )

    private companion object {
        const val READ_WRITE_FLAGS =
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        val FOLDER_URI = stringPreferencesKey("cloud_backup_folder_uri")
        val FOLDER_NAME = stringPreferencesKey("cloud_backup_folder_name")
        val ENABLED = booleanPreferencesKey("cloud_backup_enabled")
        val LAST_SUCCESSFUL_BACKUP_AT = stringPreferencesKey("cloud_backup_last_success_at")
        val LAST_ERROR = stringPreferencesKey("cloud_backup_last_error")
    }
}
