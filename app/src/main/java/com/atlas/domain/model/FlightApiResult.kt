package com.atlas.domain.model

sealed class FlightApiResult {
    data class Success(val prefill: FlightApiPrefill) : FlightApiResult()
    object NotFound : FlightApiResult()
    object NoApiKey : FlightApiResult()
    object RateLimited : FlightApiResult()
    data class NetworkError(val message: String) : FlightApiResult()
}
