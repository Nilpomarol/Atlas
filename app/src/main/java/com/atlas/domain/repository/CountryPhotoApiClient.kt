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

/** A single landscape photo returned by a batch search. */
data class ApiLandscapePhoto(
    val imageUrl: String,
    val author: String?,
    val authorLink: String?,
)

sealed interface CountryLandscapePhotosApiResult {
    data class Success(val photos: List<ApiLandscapePhoto>) : CountryLandscapePhotosApiResult

    data object NoApiKey : CountryLandscapePhotosApiResult
    data object NotFound : CountryLandscapePhotosApiResult
    data object RateLimited : CountryLandscapePhotosApiResult
    data class Error(val message: String) : CountryLandscapePhotosApiResult
}

interface CountryPhotoApiClient {
    /** Returns a random portrait photo matching [query]. */
    suspend fun randomPhoto(query: String, apiKey: String): CountryPhotoApiResult

    /** Returns up to [count] landscape photos matching [query], for a rotating cache. */
    suspend fun landscapePhotos(query: String, apiKey: String, count: Int): CountryLandscapePhotosApiResult
}
