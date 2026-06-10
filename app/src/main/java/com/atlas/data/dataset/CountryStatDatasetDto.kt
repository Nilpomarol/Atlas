package com.atlas.data.dataset

import kotlinx.serialization.Serializable

@Serializable
data class CountryStatDatasetDto(
    val version: String,
    val countries: List<CountryStatCountryDto>,
)

@Serializable
data class CountryStatCountryDto(
    val iso2: String,
    val facts: List<CountryStatFactDto>,
)

@Serializable
data class CountryStatFactDto(
    val category: String,
    val key: String,
    val labelCa: String,
    val value: String,
    val unit: String? = null,
    val year: Int? = null,
    val rank: Int? = null,
    val rankTotal: Int? = null,
    val tier: String? = null,
    val sortOrder: Int = 0,
)
