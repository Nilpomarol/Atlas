package com.atlas.domain.repository

import com.atlas.domain.model.AircraftApiResult

interface AircraftApiClient {
    suspend fun lookupAircraftByRegistration(
        registration: String,
        apiKey: String,
    ): AircraftApiResult
}
