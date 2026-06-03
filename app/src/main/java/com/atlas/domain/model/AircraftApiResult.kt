package com.atlas.domain.model

sealed class AircraftApiResult {
    data class Success(val aircraft: AircraftApiAircraft) : AircraftApiResult()
    data object NotFound : AircraftApiResult()
    data object RateLimited : AircraftApiResult()
    data class NetworkError(val message: String) : AircraftApiResult()
}
