package com.atlas.domain.service

import com.atlas.domain.model.Airport
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class FlightDerivedData(
    val scheduledDepartureUtc: String?,
    val scheduledArrivalUtc: String?,
    val actualDepartureUtc: String?,
    val actualArrivalUtc: String?,
    val distanceKm: Double?,
)

class FlightDerivedDataService {
    fun derive(
        originAirport: Airport?,
        destinationAirport: Airport?,
        scheduledDepartureAt: String?,
        scheduledArrivalAt: String?,
        actualDepartureAt: String?,
        actualArrivalAt: String?,
    ): FlightDerivedData =
        FlightDerivedData(
            scheduledDepartureUtc = scheduledDepartureAt.toUtcInstant(originAirport?.timezone),
            scheduledArrivalUtc = scheduledArrivalAt.toUtcInstant(destinationAirport?.timezone),
            actualDepartureUtc = actualDepartureAt.toUtcInstant(originAirport?.timezone),
            actualArrivalUtc = actualArrivalAt.toUtcInstant(destinationAirport?.timezone),
            distanceKm = distanceKm(originAirport, destinationAirport),
        )

    private fun String?.toUtcInstant(timezone: String?): String? {
        if (this.isNullOrBlank() || timezone.isNullOrBlank()) return null
        return try {
            LocalDateTime.parse(this)
                .atZone(ZoneId.of(timezone))
                .toInstant()
                .toString()
        } catch (_: DateTimeException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun distanceKm(originAirport: Airport?, destinationAirport: Airport?): Double? {
        val origin = originAirport ?: return null
        val destination = destinationAirport ?: return null
        val lat1 = origin.latitude.toRadians()
        val lat2 = destination.latitude.toRadians()
        val deltaLat = (destination.latitude - origin.latitude).toRadians()
        val deltaLon = (destination.longitude - origin.longitude).toRadians()
        val a = sin(deltaLat / 2).let { it * it } +
            cos(lat1) * cos(lat2) * sin(deltaLon / 2).let { it * it }
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EarthRadiusKm * c
    }

    private fun Double.toRadians(): Double = this * PI / 180.0

    private companion object {
        const val EarthRadiusKm = 6371.0088
    }
}
