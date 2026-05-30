package com.atlas.data.local.mapper

import com.atlas.data.local.entity.CountryLogEntity
import com.atlas.domain.model.CountryLog
import com.atlas.domain.model.CountryLogType
import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.model.FlexibleDateRange

fun CountryLogEntity.toDomain(): CountryLog = CountryLog(
    id = id,
    countryIso2 = countryIso2,
    type = CountryLogType.valueOf(type),
    dateRange = toDateRange(),
    notes = notes,
)

private fun CountryLogEntity.toDateRange(): FlexibleDateRange? {
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
