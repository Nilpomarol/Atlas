package com.atlas.domain.validation

import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.model.FlexibleDateRange
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FlexibleDateValidatorTest {
    private val validator = FlexibleDateValidator()

    @Test
    fun yearPrecisionRequiresOnlyYear() {
        assertTrue(
            validator.isValid(
                FlexibleDate(
                    year = 2024,
                    month = null,
                    day = null,
                    precision = DatePrecision.YEAR,
                ),
            ),
        )

        assertFalse(
            validator.isValid(
                FlexibleDate(
                    year = 2024,
                    month = 6,
                    day = null,
                    precision = DatePrecision.YEAR,
                ),
            ),
        )
    }

    @Test
    fun monthPrecisionRequiresYearAndMonthOnly() {
        assertTrue(
            validator.isValid(
                FlexibleDate(
                    year = 2024,
                    month = 6,
                    day = null,
                    precision = DatePrecision.MONTH,
                ),
            ),
        )

        assertFalse(
            validator.isValid(
                FlexibleDate(
                    year = 2024,
                    month = null,
                    day = null,
                    precision = DatePrecision.MONTH,
                ),
            ),
        )

        assertFalse(
            validator.isValid(
                FlexibleDate(
                    year = 2024,
                    month = 6,
                    day = 12,
                    precision = DatePrecision.MONTH,
                ),
            ),
        )
    }

    @Test
    fun dayPrecisionRequiresValidFullDate() {
        assertTrue(
            validator.isValid(
                FlexibleDate(
                    year = 2024,
                    month = 2,
                    day = 29,
                    precision = DatePrecision.DAY,
                ),
            ),
        )

        assertFalse(
            validator.isValid(
                FlexibleDate(
                    year = 2023,
                    month = 2,
                    day = 29,
                    precision = DatePrecision.DAY,
                ),
            ),
        )
    }

    @Test
    fun rangeRequiresMatchingPrecision() {
        assertTrue(
            validator.isValid(
                FlexibleDateRange(
                    start = FlexibleDate(2024, 1, null, DatePrecision.MONTH),
                    end = FlexibleDate(2024, 6, null, DatePrecision.MONTH),
                    precision = DatePrecision.MONTH,
                ),
            ),
        )

        assertFalse(
            validator.isValid(
                FlexibleDateRange(
                    start = FlexibleDate(2024, null, null, DatePrecision.YEAR),
                    end = FlexibleDate(2024, 6, null, DatePrecision.MONTH),
                    precision = null,
                ),
            ),
        )
    }

    @Test
    fun rangeRejectsEndBeforeStart() {
        assertFalse(
            validator.isValid(
                FlexibleDateRange(
                    start = FlexibleDate(2025, null, null, DatePrecision.YEAR),
                    end = FlexibleDate(2024, null, null, DatePrecision.YEAR),
                    precision = DatePrecision.YEAR,
                ),
            ),
        )
    }
}
