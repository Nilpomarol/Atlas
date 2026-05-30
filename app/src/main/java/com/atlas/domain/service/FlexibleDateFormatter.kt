package com.atlas.domain.service

import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.model.FlexibleDateRange

class FlexibleDateFormatter {
    fun format(date: FlexibleDate): String = when (date.precision) {
        DatePrecision.YEAR -> date.year.toString()
        DatePrecision.MONTH -> "${date.month.twoDigits()}-${date.year}"
        DatePrecision.DAY -> "${date.day.twoDigits()}-${date.month.twoDigits()}-${date.year}"
    }

    fun format(range: FlexibleDateRange): String = when {
        range.start != null && range.end != null -> "${format(range.start)} - ${format(range.end)}"
        range.start != null -> format(range.start)
        range.end != null -> "Fins ${format(range.end)}"
        else -> "Sense data"
    }

    private fun Int?.twoDigits(): String = requireNotNull(this).toString().padStart(2, '0')
}
