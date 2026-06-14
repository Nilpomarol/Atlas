package com.atlas.domain.repository

sealed interface CurrencyRateApiResult {
    /** [rate] is the number of local currency units per 1 EUR. */
    data class Success(val rate: Double) : CurrencyRateApiResult

    /** The currency code is not supported by the rate provider. */
    data object NotFound : CurrencyRateApiResult
    data class Error(val message: String) : CurrencyRateApiResult
}

interface CurrencyRateApiClient {
    /** Fetches the EUR-based exchange rate for [currencyCode] (ISO 4217). */
    suspend fun fetchRate(currencyCode: String): CurrencyRateApiResult
}
