package com.atlas.domain.repository

import kotlinx.coroutines.flow.Flow

interface ApiKeyRepository {
    /** Emits the stored RapidAPI key, or null / blank string if not set. */
    fun observeRapidApiKey(): Flow<String>
    suspend fun saveRapidApiKey(key: String)
}
