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
}
