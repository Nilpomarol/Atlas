package com.atlas.domain.repository

import com.atlas.domain.model.CountryStatsScope
import kotlinx.coroutines.flow.Flow

interface CountryStatsScopePreferencesRepository {
    fun observeScope(): Flow<CountryStatsScope>
    suspend fun setScope(scope: CountryStatsScope)
}
