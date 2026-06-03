package com.atlas.presentation.flight

import com.atlas.domain.model.FlightApiPrefill

sealed class FlightApiSearchState {
    object Idle : FlightApiSearchState()
    object Searching : FlightApiSearchState()
    data class Found(val prefill: FlightApiPrefill) : FlightApiSearchState()
    object NotFound : FlightApiSearchState()
    object NoApiKey : FlightApiSearchState()
    object RateLimited : FlightApiSearchState()
    data class Error(val message: String) : FlightApiSearchState()
}
