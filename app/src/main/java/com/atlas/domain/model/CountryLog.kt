package com.atlas.domain.model

data class CountryLog(
    val id: String,
    val countryIso2: String,
    val type: CountryLogType,
    val dateRange: FlexibleDateRange?,
    val notes: String?,
)
