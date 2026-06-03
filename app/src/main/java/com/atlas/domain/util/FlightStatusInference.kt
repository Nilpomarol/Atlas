package com.atlas.domain.util

import com.atlas.domain.model.TravelStatus
import java.time.LocalDate

/**
 * Infers a sensible default flight status from the scheduled departure date.
 *
 *   future  → PLANNED
 *   today   → IN_PROGRESS
 *   past    → COMPLETED
 *   null    → null (caller keeps existing status)
 */
fun inferFlightStatus(scheduledDepartureAt: String?): TravelStatus? {
    val departure = scheduledDepartureAt?.takeIf { it.length >= 10 } ?: return null
    return try {
        val departureDate = LocalDate.parse(departure.take(10))
        val today = LocalDate.now()
        when {
            departureDate.isAfter(today) -> TravelStatus.PLANNED
            departureDate.isBefore(today) -> TravelStatus.COMPLETED
            else -> TravelStatus.IN_PROGRESS
        }
    } catch (_: Exception) {
        null
    }
}
