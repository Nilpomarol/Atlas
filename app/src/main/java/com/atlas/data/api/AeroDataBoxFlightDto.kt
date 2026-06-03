package com.atlas.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AeroDataBoxFlightDto(
    val number: String? = null,
    val departure: AeroDataBoxMovementDto? = null,
    val arrival: AeroDataBoxMovementDto? = null,
    val airline: AeroDataBoxAirlineDto? = null,
    val aircraft: AeroDataBoxAircraftDto? = null,
)

@Serializable
data class AeroDataBoxMovementDto(
    val airport: AeroDataBoxAirportDto? = null,
    val scheduledTime: AeroDataBoxTimeDto? = null,
)

@Serializable
data class AeroDataBoxAirportDto(
    val iata: String? = null,
    val icao: String? = null,
    val name: String? = null,
)

@Serializable
data class AeroDataBoxTimeDto(
    val utc: String? = null,
    val local: String? = null,
)

@Serializable
data class AeroDataBoxAirlineDto(
    val iata: String? = null,
    val icao: String? = null,
    val name: String? = null,
)

@Serializable
data class AeroDataBoxAircraftDto(
    val model: String? = null,
    @SerialName("reg") val registration: String? = null,
)

/**
 * Parses an AeroDataBox local time string to "YYYY-MM-DDTHH:mm".
 * Input examples: "2024-06-01 10:25+02:00"  or  "2024-06-01T10:25+02:00"
 */
fun parseAeroDataBoxLocalTime(raw: String?): String? {
    if (raw == null) return null
    // Normalise space→T, then take the first 16 chars (YYYY-MM-DDTHH:mm)
    val normalised = raw.replace(" ", "T")
    return if (normalised.length >= 16) normalised.take(16) else null
}
