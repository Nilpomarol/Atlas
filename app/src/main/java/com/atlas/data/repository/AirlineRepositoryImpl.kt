package com.atlas.data.repository

import com.atlas.data.local.database.AtlasDatabase
import com.atlas.data.local.mapper.toDomain
import com.atlas.domain.model.Airline
import com.atlas.domain.repository.AirlineRepository

class AirlineRepositoryImpl(
    database: AtlasDatabase,
) : AirlineRepository {
    private val airlineDao = database.airlineDao()

    override suspend fun getAirlineByIata(iata: String): Airline? =
        airlineDao.getByIata(iata)?.toDomain()
}
