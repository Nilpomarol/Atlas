package com.atlas.domain.model

/** A single cached landscape photo reference for a country. */
data class CountryLandscapePhoto(
    val filename: String,
    val author: String?,
    val authorLink: String?,
)

/** The cached set of landscape photos for a country, with the time they were fetched. */
data class CountryLandscapePhotos(
    val countryIso2: String,
    val photos: List<CountryLandscapePhoto>,
    val fetchedAt: String,
)
