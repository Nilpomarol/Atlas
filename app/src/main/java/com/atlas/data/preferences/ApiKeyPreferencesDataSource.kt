package com.atlas.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.atlas.domain.repository.ApiKeyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ApiKeyPreferencesDataSource(
    private val context: Context,
) : ApiKeyRepository {

    override fun observeRapidApiKey(): Flow<String> =
        context.atlasDataStore.data.map { prefs ->
            prefs[RAPID_API_KEY] ?: ""
        }

    override suspend fun saveRapidApiKey(key: String) {
        context.atlasDataStore.edit { prefs ->
            prefs[RAPID_API_KEY] = key.trim()
        }
    }

    override fun observeUnsplashKey(): Flow<String> =
        context.atlasDataStore.data.map { prefs ->
            prefs[UNSPLASH_KEY] ?: ""
        }

    override suspend fun saveUnsplashKey(key: String) {
        context.atlasDataStore.edit { prefs ->
            prefs[UNSPLASH_KEY] = key.trim()
        }
    }

    private companion object {
        val RAPID_API_KEY = stringPreferencesKey("rapid_api_key")
        val UNSPLASH_KEY = stringPreferencesKey("unsplash_access_key")
    }
}
