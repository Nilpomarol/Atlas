package com.atlas.domain.repository

import com.atlas.domain.model.Airport
import kotlinx.coroutines.flow.Flow

interface AirportRepository {
    fun searchAirports(query: String): Flow<List<Airport>>
}
