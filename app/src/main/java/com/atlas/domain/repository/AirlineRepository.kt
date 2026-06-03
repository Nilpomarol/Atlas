package com.atlas.domain.repository

import com.atlas.domain.model.Airline

interface AirlineRepository {
    suspend fun getAirlineByIata(iata: String): Airline?
}
