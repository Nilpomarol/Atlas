package com.atlas.domain.service

import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.model.FlexibleDateRange
import org.junit.Assert.assertEquals
import org.junit.Test

class FlexibleDateFormatterTest {
    private val formatter = FlexibleDateFormatter()

    @Test
    fun formatsYearPrecision() {
        assertEquals(
            "2024",
            formatter.format(
                FlexibleDate(
                    year = 2024,
                    month = null,
                    day = null,
                    precision = DatePrecision.YEAR,
                ),
            ),
        )
    }

    @Test
    fun formatsMonthPrecision() {
        assertEquals(
            "06-2024",
            formatter.format(
                FlexibleDate(
                    year = 2024,
                    month = 6,
                    day = null,
                    precision = DatePrecision.MONTH,
                ),
            ),
        )
    }

    @Test
    fun formatsDayPrecision() {
        assertEquals(
            "09-06-2024",
            formatter.format(
                FlexibleDate(
                    year = 2024,
                    month = 6,
                    day = 9,
                    precision = DatePrecision.DAY,
                ),
            ),
        )
    }

    @Test
    fun formatsDateRange() {
        assertEquals(
            "2023 - 2024",
            formatter.format(
                FlexibleDateRange(
                    start = FlexibleDate(2023, null, null, DatePrecision.YEAR),
                    end = FlexibleDate(2024, null, null, DatePrecision.YEAR),
                    precision = DatePrecision.YEAR,
                ),
            ),
        )
    }
}
