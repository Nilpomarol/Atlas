package com.atlas.domain.repository

import com.atlas.domain.model.Country
import com.atlas.domain.model.CountryLog
import com.atlas.domain.model.CountryLogType
import com.atlas.domain.model.CountryUserState
import com.atlas.domain.model.FlexibleDateRange
import kotlinx.coroutines.flow.Flow

interface CountryRepository {
    fun observeTrackableCountries(): Flow<List<Country>>
    fun observeCountry(iso2: String): Flow<Country?>
    fun observeCountryLogs(): Flow<List<CountryLog>>
    fun observeCountryLogs(countryIso2: String): Flow<List<CountryLog>>
    fun observeUserStates(): Flow<List<CountryUserState>>
    fun observeUserState(countryIso2: String): Flow<CountryUserState?>
    suspend fun addCountryLog(
        countryIso2: String,
        type: CountryLogType,
        dateRange: FlexibleDateRange?,
        notes: String?,
    )
    suspend fun updateCountryLog(log: CountryLog)
    suspend fun deleteCountryLog(log: CountryLog)
    suspend fun setWished(countryIso2: String, wished: Boolean)
    suspend fun setCurrentlyLiving(countryIso2: String)
}
