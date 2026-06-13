package com.atlas.domain.repository

import com.atlas.domain.model.CountryPhoto
import kotlinx.coroutines.flow.Flow

interface CountryPhotoRepository {
    fun observePhoto(iso2: String): Flow<CountryPhoto?>

    /**
     * Fetches a fresh photo if the cached one is missing or older than a day.
     * The old photo is only replaced once a new one downloads successfully, so a
     * failure (offline, no key, rate limit) leaves the existing photo untouched.
     */
    suspend fun refreshIfStale(iso2: String, query: String)
}
