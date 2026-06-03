package com.atlas.domain.repository

import com.atlas.domain.model.Airline
import kotlinx.coroutines.flow.Flow

interface AirlineRepository {
    suspend fun getAirlineByIata(iata: String): Airline?
    fun searchAirlines(query: String): Flow<List<Airline>>
}
