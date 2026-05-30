package com.atlas.presentation.date

import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.model.FlexibleDateRange

data class FlexibleDateRangeDraftUiState(
    val precision: DatePrecision = DatePrecision.YEAR,
    val startYear: String = "",
    val startMonth: String = "",
    val startDay: String = "",
    val endYear: String = "",
    val endMonth: String = "",
    val endDay: String = "",
) {
    fun hasAnyInput(): Boolean =
        startYear.isNotBlank() ||
            startMonth.isNotBlank() ||
            startDay.isNotBlank() ||
            endYear.isNotBlank() ||
            endMonth.isNotBlank() ||
            endDay.isNotBlank()

    fun toDateRange(): FlexibleDateRange? {
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

    companion object {
        fun fromDateRange(dateRange: FlexibleDateRange?): FlexibleDateRangeDraftUiState {
            val precision = dateRange?.precision ?: DatePrecision.YEAR
            return FlexibleDateRangeDraftUiState(
                precision = precision,
                startYear = dateRange?.start?.year?.toString().orEmpty(),
                startMonth = dateRange?.start?.month?.toString().orEmpty(),
                startDay = dateRange?.start?.day?.toString().orEmpty(),
                endYear = dateRange?.end?.year?.toString().orEmpty(),
                endMonth = dateRange?.end?.month?.toString().orEmpty(),
                endDay = dateRange?.end?.day?.toString().orEmpty(),
            )
        }
    }
}

enum class FlexibleDateRangeDraftField {
    StartYear,
    StartMonth,
    StartDay,
    EndYear,
    EndMonth,
    EndDay,
}

fun FlexibleDateRangeDraftUiState.updateField(
    field: FlexibleDateRangeDraftField,
    value: String,
): FlexibleDateRangeDraftUiState = when (field) {
    FlexibleDateRangeDraftField.StartYear -> copy(startYear = value)
    FlexibleDateRangeDraftField.StartMonth -> copy(startMonth = value)
    FlexibleDateRangeDraftField.StartDay -> copy(startDay = value)
    FlexibleDateRangeDraftField.EndYear -> copy(endYear = value)
    FlexibleDateRangeDraftField.EndMonth -> copy(endMonth = value)
    FlexibleDateRangeDraftField.EndDay -> copy(endDay = value)
}

private fun toFlexibleDate(
    year: String,
    month: String,
    day: String,
    precision: DatePrecision,
): FlexibleDate? {
    if (year.isBlank()) return null

    return FlexibleDate(
        year = year.toIntOrNull() ?: return invalidDate(precision),
        month = when (precision) {
            DatePrecision.YEAR -> null
            DatePrecision.MONTH,
            DatePrecision.DAY -> month.toIntOrNull()
        },
        day = when (precision) {
            DatePrecision.YEAR,
            DatePrecision.MONTH -> null
            DatePrecision.DAY -> day.toIntOrNull()
        },
        precision = precision,
    )
}

private fun invalidDate(precision: DatePrecision): FlexibleDate = FlexibleDate(
    year = -1,
    month = when (precision) {
        DatePrecision.YEAR -> null
        DatePrecision.MONTH,
        DatePrecision.DAY -> -1
    },
    day = when (precision) {
        DatePrecision.YEAR,
        DatePrecision.MONTH -> null
        DatePrecision.DAY -> -1
    },
    precision = precision,
)
