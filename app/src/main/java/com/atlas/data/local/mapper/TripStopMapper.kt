package com.atlas.data.local.mapper

import com.atlas.data.local.entity.TripStopEntity
import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.model.TripStop
import com.atlas.domain.model.TripStopSource

fun TripStopEntity.toDomain(): TripStop = TripStop(
    id = id,
    tripId = tripId,
    locationName = locationName,
    countryIso2 = countryIso2,
    latitude = latitude,
    longitude = longitude,
    dateRange = toDateRange(),
    notes = notes,
    sortOrder = sortOrder,
    source = runCatching { TripStopSource.valueOf(source) }.getOrDefault(TripStopSource.MANUAL),
    itineraryGroupId = itineraryGroupId,
    isVisible = isVisible,
    displayTitle = displayTitle,
)

fun TripStop.toEntity(
    createdAt: String,
    updatedAt: String,
): TripStopEntity = TripStopEntity(
    id = id,
    tripId = tripId,
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
    source = source.name,
    itineraryGroupId = itineraryGroupId,
    isVisible = isVisible,
    displayTitle = displayTitle,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun TripStopEntity.toDateRange(): FlexibleDateRange? {
    val precision = datePrecision?.let { DatePrecision.valueOf(it) }
    val start = toFlexibleDate(startYear, startMonth, startDay, precision)
    val end = toFlexibleDate(endYear, endMonth, endDay, precision)

    return if (start == null && end == null) {
        null
    } else {
        FlexibleDateRange(start = start, end = end, precision = precision)
    }
}

private fun toFlexibleDate(
    year: Int?,
    month: Int?,
    day: Int?,
    precision: DatePrecision?,
): FlexibleDate? {
    if (year == null || precision == null) return null

    return FlexibleDate(
        year = year,
        month = month,
        day = day,
        precision = precision,
    )
}
