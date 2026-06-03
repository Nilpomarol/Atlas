package com.atlas.data.local.mapper

import com.atlas.data.local.entity.AirlineEntity
import com.atlas.domain.model.Airline

fun AirlineEntity.toDomain(): Airline = Airline(
    iata = iata,
    icao = icao,
    name = name,
    countryIso2 = countryIso2,
)
