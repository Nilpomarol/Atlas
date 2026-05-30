package com.atlas.domain.model

data class LocationSearchResult(
    val id: String,
    val name: String,
    val displayName: String,
    val countryIso2: String?,
    val latitude: Double,
    val longitude: Double,
)
