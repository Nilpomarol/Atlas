package com.atlas.domain.repository

sealed interface CountryPhotoApiResult {
    data class Success(
        val imageUrl: String,
        val author: String?,
        val authorLink: String?,
    ) : CountryPhotoApiResult

    data object NoApiKey : CountryPhotoApiResult
    data object NotFound : CountryPhotoApiResult
    data object RateLimited : CountryPhotoApiResult
    data class Error(val message: String) : CountryPhotoApiResult
}

interface CountryPhotoApiClient {
    /** Returns a random landscape photo matching [query]. */
    suspend fun randomPhoto(query: String, apiKey: String): CountryPhotoApiResult
}
