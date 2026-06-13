package com.atlas.domain.model

data class CountryPhoto(
    val countryIso2: String,
    val filename: String,
    val sourceUrl: String?,
    val author: String?,
    val authorLink: String?,
    val fetchedAt: String,
)
