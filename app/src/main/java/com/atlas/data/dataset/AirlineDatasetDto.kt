package com.atlas.data.dataset

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AirlineDatasetDto(
    val version: String,
    val airlines: List<AirlineDto>,
)

@Serializable
data class AirlineDto(
    val iata: String,
    val icao: String? = null,
    val name: String,
    @SerialName("country_iso2")
    val countryIso2: String? = null,
)
