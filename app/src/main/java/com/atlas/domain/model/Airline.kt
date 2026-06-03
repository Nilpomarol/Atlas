package com.atlas.domain.model

data class Airline(
    val iata: String,
    val icao: String?,
    val name: String,
    val countryIso2: String?,
)
