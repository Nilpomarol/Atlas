package com.atlas.domain.repository

import com.atlas.domain.model.CurrencyRate
import kotlinx.coroutines.flow.Flow

interface CurrencyRateRepository {
    fun observeRate(code: String): Flow<CurrencyRate?>

    /**
     * Fetches a fresh EUR-based rate if the cached one is missing or older than the
     * refresh window. A failure (offline, unsupported currency, error) leaves any
     * existing cached rate untouched.
     */
    suspend fun refreshIfStale(code: String)
}
