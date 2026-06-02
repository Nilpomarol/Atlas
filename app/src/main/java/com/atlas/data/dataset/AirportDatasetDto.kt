package com.atlas.data.dataset

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AirportDatasetDto(
    val version: String,
    val airports: List<AirportDto>,
)

@Serializable
data class AirportDto(
    val id: String,
    val iata: String? = null,
    val icao: String? = null,
    val name: String,
    val city: String? = null,
    @SerialName("country_iso2")
    val countryIso2: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String? = null,
)
