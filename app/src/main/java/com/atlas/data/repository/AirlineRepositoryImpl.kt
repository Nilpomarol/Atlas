package com.atlas.data.repository

import com.atlas.data.local.database.AtlasDatabase
import com.atlas.data.local.mapper.toDomain
import com.atlas.domain.model.Airline
import com.atlas.domain.repository.AirlineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AirlineRepositoryImpl(
    database: AtlasDatabase,
) : AirlineRepository {
    private val airlineDao = database.airlineDao()

    override suspend fun getAirlineByIata(iata: String): Airline? =
        airlineDao.getByIata(iata)?.toDomain()

    override fun searchAirlines(query: String): Flow<List<Airline>> {
        val upper = query.trim().uppercase()
        return airlineDao.search(
            query = "%$upper%",
            exactQuery = upper,
            prefixQuery = "$upper%",
            limit = SEARCH_LIMIT,
        ).map { list -> list.map { it.toDomain() } }
    }

    private companion object {
        const val SEARCH_LIMIT = 8
    }
}
