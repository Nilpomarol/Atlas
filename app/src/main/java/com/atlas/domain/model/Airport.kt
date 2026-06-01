package com.atlas.domain.model

data class Airport(
    val id: String,
    val iata: String?,
    val icao: String?,
    val name: String,
    val city: String,
    val countryIso2: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String?,
)
