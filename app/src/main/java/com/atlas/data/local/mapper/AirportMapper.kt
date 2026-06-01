package com.atlas.data.local.mapper

import com.atlas.data.local.entity.AirportEntity
import com.atlas.domain.model.Airport

fun AirportEntity.toDomain(): Airport = Airport(
    id = id,
    iata = iata,
    icao = icao,
    name = name,
    city = city,
    countryIso2 = countryIso2,
    latitude = latitude,
    longitude = longitude,
    timezone = timezone,
)
