package com.atlas.domain.repository

import com.atlas.domain.model.CountryLandscapePhotos
import kotlinx.coroutines.flow.Flow

interface CountryLandscapePhotoRepository {
    fun observePhotos(iso2: String): Flow<CountryLandscapePhotos?>

    /**
     * Fetches a small set of landscape photos if the cached set is missing or older
     * than the refresh window (about a month). The existing set is only replaced once
     * the new photos download successfully, so a failure (offline, no key, rate limit)
     * leaves the cached photos untouched.
     */
    suspend fun refreshIfStale(iso2: String, query: String)
}
