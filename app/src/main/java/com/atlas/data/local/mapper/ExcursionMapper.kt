package com.atlas.data.local.mapper

import com.atlas.data.local.entity.ExcursionEntity
import com.atlas.data.local.entity.ExcursionStopEntity
import com.atlas.data.local.entity.ExcursionWithItems
import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.Excursion
import com.atlas.domain.model.ExcursionStop
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.model.FlexibleDateRange

fun ExcursionWithItems.toDomain(): Excursion = excursion.toDomain(
    stops = stops.sortedBy { it.sortOrder }.map { it.toDomain() },
)

fun ExcursionEntity.toDomain(stops: List<ExcursionStop> = emptyList()): Excursion = Excursion(
    id = id,
    tripId = tripId,
    anchorTripStopId = anchorTripStopId,
    title = title,
    notes = notes,
    sortOrder = sortOrder,
    stops = stops,
)

fun ExcursionStopEntity.toDomain(): ExcursionStop = ExcursionStop(
    id = id,
    excursionId = excursionId,
    locationName = locationName,
    countryIso2 = countryIso2,
    latitude = latitude,
    longitude = longitude,
    dateRange = toFlexibleDateRange(),
    notes = notes,
    sortOrder = sortOrder,
)

fun Excursion.toEntity(
    createdAt: String,
    updatedAt: String,
): ExcursionEntity = ExcursionEntity(
    id = id,
    tripId = tripId,
    anchorTripStopId = anchorTripStopId,
    title = title,
    notes = notes,
    sortOrder = sortOrder,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun ExcursionStop.toEntity(
    createdAt: String,
    updatedAt: String,
): ExcursionStopEntity = ExcursionStopEntity(
    id = id,
    excursionId = excursionId,
    locationName = locationName,
    countryIso2 = countryIso2,
    latitude = latitude,
    longitude = longitude,
    startYear = dateRange?.start?.year,
    startMonth = dateRange?.start?.month,
    startDay = dateRange?.start?.day,
    endYear = dateRange?.end?.year,
    endMonth = dateRange?.end?.month,
    endDay = dateRange?.end?.day,
    datePrecision = dateRange?.precision?.name,
    notes = notes,
    sortOrder = sortOrder,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun ExcursionStopEntity.toFlexibleDateRange(): FlexibleDateRange? {
    val precision = datePrecision?.let { runCatching { DatePrecision.valueOf(it) }.getOrNull() }
    if (precision == null || (startYear == null && endYear == null)) return null
    return FlexibleDateRange(
        start = startYear?.let {
            FlexibleDate(
                year = it,
                month = startMonth,
                day = startDay,
                precision = precision,
            )
        },
        end = endYear?.let {
            FlexibleDate(
                year = it,
                month = endMonth,
                day = endDay,
                precision = precision,
            )
        },
        precision = precision,
    )
}
