package com.atlas.domain.validation

import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.model.FlexibleDateRange
import java.time.DateTimeException
import java.time.LocalDate

class FlexibleDateValidator {
    fun isValid(date: FlexibleDate): Boolean {
        if (date.year !in MIN_YEAR..MAX_YEAR) return false

        return when (date.precision) {
            DatePrecision.YEAR -> date.month == null && date.day == null
            DatePrecision.MONTH -> date.month != null &&
                date.month in 1..12 &&
                date.day == null
            DatePrecision.DAY -> date.month != null &&
                date.day != null &&
                isValidLocalDate(
                    year = date.year,
                    month = date.month,
                    day = date.day,
                )
        }
    }

    fun isValid(range: FlexibleDateRange): Boolean {
        if (range.start == null && range.end == null) {
            return range.precision == null
        }

        val dates = listOfNotNull(range.start, range.end)
        if (dates.any { !isValid(it) }) return false

        val expectedPrecision = range.precision ?: dates.first().precision
        if (dates.any { it.precision != expectedPrecision }) return false
        if (range.precision != null && dates.any { it.precision != range.precision }) return false

        val start = range.start
        val end = range.end
        if (start != null && end != null) {
            return start.sortKey() <= end.sortKey()
        }

        return true
    }

    private fun isValidLocalDate(
        year: Int,
        month: Int,
        day: Int,
    ): Boolean = try {
        LocalDate.of(year, month, day)
        true
    } catch (_: DateTimeException) {
        false
    }

    private fun FlexibleDate.sortKey(): Int {
        val safeMonth = month ?: 1
        val safeDay = day ?: 1
        return year * 10_000 + safeMonth * 100 + safeDay
    }

    private companion object {
        const val MIN_YEAR = 1
        const val MAX_YEAR = 9999
    }
}
