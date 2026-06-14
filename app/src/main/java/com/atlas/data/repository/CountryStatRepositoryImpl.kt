package com.atlas.data.repository

import com.atlas.data.local.database.AtlasDatabase
import com.atlas.data.local.mapper.toDomain
import com.atlas.domain.model.CountryStatFact
import com.atlas.domain.repository.CountryStatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CountryStatRepositoryImpl(
    database: AtlasDatabase,
) : CountryStatRepository {
    private val countryStatFactDao = database.countryStatFactDao()

    override fun observeByCountry(iso2: String): Flow<List<CountryStatFact>> =
        countryStatFactDao.observeByCountry(iso2)
            .map { list -> list.map { it.toDomain() } }

    override fun observeByKeys(keys: Set<String>): Flow<List<CountryStatFact>> =
        countryStatFactDao.observeByKeys(keys.toList())
            .map { list -> list.map { it.toDomain() } }
}
