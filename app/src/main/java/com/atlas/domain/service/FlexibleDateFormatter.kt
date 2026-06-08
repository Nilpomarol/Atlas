package com.atlas.domain.service

import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.model.FlexibleDateRange
import java.time.LocalDate
import java.time.LocalDateTime

class FlexibleDateFormatter {
    fun format(date: FlexibleDate): String = when (date.precision) {
        DatePrecision.YEAR -> date.year.toString()
        DatePrecision.MONTH -> "${date.month.monthAbbreviation()} ${date.year}"
        DatePrecision.DAY -> "${date.day.requireValue()} ${date.month.monthAbbreviation()} ${date.year}"
    }

    fun format(range: FlexibleDateRange): String = when {
        range.start != null && range.end != null -> "${format(range.start)} - ${format(range.end)}"
        range.start != null -> format(range.start)
        range.end != null -> "Fins ${format(range.end)}"
        else -> "Sense data"
    }

    fun format(date: LocalDate): String =
        "${date.dayOfMonth} ${date.monthValue.monthAbbreviation()} ${date.year}"

    fun format(dateTime: LocalDateTime): String =
        "${format(dateTime.toLocalDate())} · ${dateTime.hour.twoDigits()}:${dateTime.minute.twoDigits()}"

    fun formatTripPill(range: FlexibleDateRange?): String? {
        if (range == null) return null
        val start = range.start
        val end = range.end
        return when {
            start == null -> end?.toMonthYearText()
            end == null -> start.toMonthYearText()
            start.sameMonthAndYear(end) -> end.toMonthYearText()
            start.precision == DatePrecision.YEAR && end.precision == DatePrecision.YEAR -> "${start.year} - ${end.year}"
            else -> "${start.toMonthOnlyText()} - ${end.toMonthYearText()}"
        }
    }

    fun formatIsoDate(value: String): String? =
        runCatching { format(LocalDate.parse(value.take(10))) }.getOrNull()

    fun formatIsoDateTime(value: String): String? =
        runCatching { format(LocalDateTime.parse(value)) }.getOrNull()

    private fun Int?.requireValue(): Int = requireNotNull(this)

    private fun Int?.monthAbbreviation(): String = requireValue().monthAbbreviation()

    private fun Int.monthAbbreviation(): String = when (this) {
        1 -> "gen."
        2 -> "febr."
        3 -> "març"
        4 -> "abr."
        5 -> "maig"
        6 -> "juny"
        7 -> "jul."
        8 -> "ag."
        9 -> "set."
        10 -> "oct."
        11 -> "nov."
        12 -> "des."
        else -> error("Invalid month: $this")
    }

    private fun Int.twoDigits(): String = toString().padStart(2, '0')

    private fun FlexibleDate.toMonthYearText(): String =
        month?.let { "${it.monthAbbreviation()} $year" } ?: year.toString()

    private fun FlexibleDate.toMonthOnlyText(): String =
        month?.monthAbbreviation() ?: year.toString()

    private fun FlexibleDate.sameMonthAndYear(other: FlexibleDate): Boolean =
        year == other.year && month == other.month
}
