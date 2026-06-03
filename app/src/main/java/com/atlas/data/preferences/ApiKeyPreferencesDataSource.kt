package com.atlas.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.atlas.domain.repository.ApiKeyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "atlas_prefs")

class ApiKeyPreferencesDataSource(
    private val context: Context,
) : ApiKeyRepository {

    override fun observeRapidApiKey(): Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[RAPID_API_KEY] ?: ""
        }

    override suspend fun saveRapidApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[RAPID_API_KEY] = key.trim()
        }
    }

    private companion object {
        val RAPID_API_KEY = stringPreferencesKey("rapid_api_key")
    }
}
