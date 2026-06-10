package com.atlas.domain.model

data class CountryStatFact(
    val countryIso2: String,
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
