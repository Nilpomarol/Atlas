package com.atlas.domain.repository

import com.atlas.domain.model.FlightApiResult

/** Abstraction over the external flight data API. */
interface FlightApiClient {
    suspend fun lookup(flightNumber: String, date: String, apiKey: String): FlightApiResult
}
