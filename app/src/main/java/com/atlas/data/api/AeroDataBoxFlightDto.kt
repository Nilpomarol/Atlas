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
    val modeS: String? = null,
    @SerialName("reg") val registration: String? = null,
)

@Serializable
data class AeroDataBoxAircraftDetailsDto(
    val id: Long? = null,
    @SerialName("reg") val registration: String? = null,
    val active: Boolean? = null,
    val serial: String? = null,
    val hexIcao: String? = null,
    val airlineName: String? = null,
    val iataType: String? = null,
    val iataCodeShort: String? = null,
    val icaoCode: String? = null,
    val model: String? = null,
    val modelCode: String? = null,
    val numSeats: Int? = null,
    val rolloutDate: String? = null,
    val firstFlightDate: String? = null,
    val deliveryDate: String? = null,
    val registrationDate: String? = null,
    val typeName: String? = null,
    val numEngines: Int? = null,
    val engineType: String? = null,
    val isFreighter: Boolean? = null,
    val productionLine: String? = null,
    val ageYears: Double? = null,
    val verified: Boolean? = null,
    val image: AeroDataBoxResourceDto? = null,
)

@Serializable
data class AeroDataBoxResourceDto(
    val url: String? = null,
    val webUrl: String? = null,
    val author: String? = null,
    val title: String? = null,
    val license: String? = null,
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
