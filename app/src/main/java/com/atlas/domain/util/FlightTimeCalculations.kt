package com.atlas.domain.util

import com.atlas.domain.model.Flight
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Returns the number of calendar days between [departureDatetime] and [arrivalDatetime]
 * (both "YYYY-MM-DDTHH:mm" local strings). Returns null when the same day or either is null.
 */
fun dayOffsetBetween(departureDatetime: String?, arrivalDatetime: String?): Int? {
    val depStr = departureDatetime?.substringBefore('T')?.takeIf { it.length == 10 } ?: return null
    val arrStr = arrivalDatetime?.substringBefore('T')?.takeIf { it.length == 10 } ?: return null
    if (depStr == arrStr) return null
    val dep = runCatching { LocalDate.parse(depStr) }.getOrNull() ?: return null
    val arr = runCatching { LocalDate.parse(arrStr) }.getOrNull() ?: return null
    val days = ChronoUnit.DAYS.between(dep, arr).toInt()
    return if (days != 0) days else null
}

fun Flight.utcAwareSortKey(): String? =
    actualDepartureUtc
        ?: scheduledDepartureUtc
        ?: actualDepartureAt
        ?: scheduledDepartureAt
        ?: actualArrivalUtc
        ?: scheduledArrivalUtc
        ?: actualArrivalAt
        ?: scheduledArrivalAt

fun Flight.utcAwareDepartureSortKey(): String? =
    actualDepartureUtc ?: scheduledDepartureUtc ?: actualDepartureAt ?: scheduledDepartureAt

fun Flight.utcAwareDurationMinutes(): Long? =
    minutesBetweenUtcFirst(
        startUtc = actualDepartureUtc ?: scheduledDepartureUtc,
        endUtc = actualArrivalUtc ?: scheduledArrivalUtc,
        startLocal = actualDepartureAt ?: scheduledDepartureAt,
        endLocal = actualArrivalAt ?: scheduledArrivalAt,
    )

fun Flight.utcAwareDelayMinutes(): Long? =
    minutesBetweenUtcFirst(
        startUtc = scheduledArrivalUtc,
        endUtc = actualArrivalUtc,
        startLocal = scheduledArrivalAt,
        endLocal = actualArrivalAt,
    ) ?: minutesBetweenUtcFirst(
        startUtc = scheduledDepartureUtc,
        endUtc = actualDepartureUtc,
        startLocal = scheduledDepartureAt,
        endLocal = actualDepartureAt,
    )

fun Flight.utcAwareDepartureDelayMinutes(): Long? =
    minutesBetweenUtcFirst(
        startUtc = scheduledDepartureUtc,
        endUtc = actualDepartureUtc,
        startLocal = scheduledDepartureAt,
        endLocal = actualDepartureAt,
    )

fun Flight.utcAwareArrivalDelayMinutes(): Long? =
    minutesBetweenUtcFirst(
        startUtc = scheduledArrivalUtc,
        endUtc = actualArrivalUtc,
        startLocal = scheduledArrivalAt,
        endLocal = actualArrivalAt,
    )

fun groupDurationMinutes(firstFlight: Flight, lastFlight: Flight): Long? =
    minutesBetweenUtcFirst(
        startUtc = firstFlight.actualDepartureUtc ?: firstFlight.scheduledDepartureUtc,
        endUtc = lastFlight.actualArrivalUtc ?: lastFlight.scheduledArrivalUtc,
        startLocal = firstFlight.actualDepartureAt ?: firstFlight.scheduledDepartureAt,
        endLocal = lastFlight.actualArrivalAt ?: lastFlight.scheduledArrivalAt,
    )

fun Flight.utcAwareLayoverDurationMinutesTo(next: Flight): Long? =
    minutesBetweenUtcFirst(
        startUtc = actualArrivalUtc ?: scheduledArrivalUtc,
        endUtc = next.actualDepartureUtc ?: next.scheduledDepartureUtc,
        startLocal = actualArrivalAt ?: scheduledArrivalAt,
        endLocal = next.actualDepartureAt ?: next.scheduledDepartureAt,
    )

private fun minutesBetweenUtcFirst(
    startUtc: String?,
    endUtc: String?,
    startLocal: String?,
    endLocal: String?,
): Long? =
    minutesBetweenUtc(startUtc, endUtc) ?: minutesBetweenLocal(startLocal, endLocal)

private fun minutesBetweenUtc(start: String?, end: String?): Long? {
    val startTime = runCatching { Instant.parse(start ?: return null) }.getOrNull() ?: return null
    val endTime = runCatching { Instant.parse(end ?: return null) }.getOrNull() ?: return null
    return Duration.between(startTime, endTime).toMinutes()
}

private fun minutesBetweenLocal(start: String?, end: String?): Long? {
    val startTime = runCatching { LocalDateTime.parse(start ?: return null) }.getOrNull() ?: return null
    val endTime = runCatching { LocalDateTime.parse(end ?: return null) }.getOrNull() ?: return null
    return Duration.between(startTime, endTime).toMinutes()
}
