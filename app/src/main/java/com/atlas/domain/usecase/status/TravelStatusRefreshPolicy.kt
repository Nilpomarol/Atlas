package com.atlas.domain.usecase.status

import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.model.Flight
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import java.time.LocalDate

object TravelStatusRefreshPolicy {
    fun inferredTripStatus(dateRange: FlexibleDateRange?, today: LocalDate): TravelStatus {
        val range = dateRange ?: return TravelStatus.PLANNED
        val startDate = range.start?.startBoundary()
        val endDate = (range.end ?: range.start)?.endBoundary()

        if (endDate != null && today.isAfter(endDate)) {
            return TravelStatus.COMPLETED
        }

        if (startDate != null && !today.isBefore(startDate)) {
            return TravelStatus.IN_PROGRESS
        }

        return TravelStatus.PLANNED
    }

    fun refreshedTripStatus(trip: Trip, today: LocalDate): TravelStatus? {
        if (trip.status != TravelStatus.PLANNED && trip.status != TravelStatus.IN_PROGRESS) return null

        val range = trip.dateRange ?: return null
        return when (inferredTripStatus(range, today)) {
            TravelStatus.COMPLETED -> TravelStatus.COMPLETED
            TravelStatus.IN_PROGRESS -> if (trip.status == TravelStatus.PLANNED) {
                TravelStatus.IN_PROGRESS
            } else {
                null
            }
            else -> null
        }
    }

    fun refreshedFlightStatus(flight: Flight, today: LocalDate): TravelStatus? {
        if (flight.status != TravelStatus.PLANNED && flight.status != TravelStatus.IN_PROGRESS) return null

        val departureDate = flight.scheduledDepartureAt?.toIsoDate() ?: return null
        val arrivalDate = flight.scheduledArrivalAt?.toIsoDate()

        if (arrivalDate != null && today.isAfter(arrivalDate)) {
            return TravelStatus.COMPLETED
        }

        if (flight.status == TravelStatus.PLANNED && !today.isBefore(departureDate)) {
            return TravelStatus.IN_PROGRESS
        }

        return null
    }

    private fun FlexibleDate.startBoundary(): LocalDate? = runCatching {
        when (precision) {
            DatePrecision.YEAR -> LocalDate.of(year, 1, 1)
            DatePrecision.MONTH -> LocalDate.of(year, month ?: return null, 1)
            DatePrecision.DAY -> LocalDate.of(year, month ?: return null, day ?: return null)
        }
    }.getOrNull()

    private fun FlexibleDate.endBoundary(): LocalDate? = runCatching {
        when (precision) {
            DatePrecision.YEAR -> LocalDate.of(year, 12, 31)
            DatePrecision.MONTH -> {
                val start = LocalDate.of(year, month ?: return null, 1)
                start.withDayOfMonth(start.lengthOfMonth())
            }
            DatePrecision.DAY -> LocalDate.of(year, month ?: return null, day ?: return null)
        }
    }.getOrNull()

    private fun String.toIsoDate(): LocalDate? {
        if (length < 10) return null
        return runCatching { LocalDate.parse(take(10)) }.getOrNull()
    }
}
