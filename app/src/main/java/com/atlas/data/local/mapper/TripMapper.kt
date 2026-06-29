package com.atlas.data.local.mapper

import com.atlas.data.local.entity.TripEntity
import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip

fun TripEntity.toDomain(): Trip = Trip(
    id = id,
    title = title,
    status = TravelStatus.valueOf(status),
    dateRange = toDateRange(),
    notes = notes,
    coverPhotoFilename = coverPhotoFilename,
    isQuickTrip = isQuickTrip,
)

private fun TripEntity.toDateRange(): FlexibleDateRange? {
    val precision = datePrecision?.let { DatePrecision.valueOf(it) }
    val start = toFlexibleDate(
        year = startYear,
        month = startMonth,
        day = startDay,
        precision = precision,
    )
    val end = toFlexibleDate(
        year = endYear,
        month = endMonth,
        day = endDay,
        precision = precision,
    )

    return if (start == null && end == null) {
        null
    } else {
        FlexibleDateRange(
            start = start,
            end = end,
            precision = precision,
        )
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
