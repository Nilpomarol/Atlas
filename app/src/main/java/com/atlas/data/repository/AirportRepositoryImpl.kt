package com.atlas.data.repository

import com.atlas.data.local.database.AtlasDatabase
import com.atlas.data.local.mapper.toDomain
import com.atlas.domain.model.Airport
import com.atlas.domain.repository.AirportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AirportRepositoryImpl(
    database: AtlasDatabase,
) : AirportRepository {
    private val airportDao = database.airportDao()

    override fun observeAirports(): Flow<List<Airport>> =
        airportDao.observeAll().map { airports -> airports.map { it.toDomain() } }

    override fun searchAirports(query: String): Flow<List<Airport>> {
        val normalized = query.trim().uppercase()
        return airportDao.search(
            query = "%$normalized%",
            exactQuery = normalized,
            prefixQuery = "$normalized%",
            limit = SEARCH_LIMIT,
        ).map { airports ->
            airports.map { it.toDomain() }
        }
    }

    override suspend fun getAirportById(id: String): Airport? =
        airportDao.getById(id)?.toDomain()

    private companion object {
        const val SEARCH_LIMIT = 30
    }
}
