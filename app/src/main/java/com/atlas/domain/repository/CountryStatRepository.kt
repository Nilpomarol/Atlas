package com.atlas.domain.repository

import com.atlas.domain.model.CountryStatFact
import kotlinx.coroutines.flow.Flow

interface CountryStatRepository {
    fun observeByCountry(iso2: String): Flow<List<CountryStatFact>>
}
