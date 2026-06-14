package com.atlas.domain.repository

import com.atlas.domain.model.CountryStatFact
import kotlinx.coroutines.flow.Flow

interface CountryStatRepository {
    fun observeByCountry(iso2: String): Flow<List<CountryStatFact>>

    /** Facts across all countries for the given keys — used to sort the country list. */
    fun observeByKeys(keys: Set<String>): Flow<List<CountryStatFact>>
}
