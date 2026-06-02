package com.atlas.domain.repository

import com.atlas.domain.model.Airport
import kotlinx.coroutines.flow.Flow

interface AirportRepository {
    fun observeAirports(): Flow<List<Airport>>
    fun searchAirports(query: String): Flow<List<Airport>>
    suspend fun getAirportById(id: String): Airport?
}
