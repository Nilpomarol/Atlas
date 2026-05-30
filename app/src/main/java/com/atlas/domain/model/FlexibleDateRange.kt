package com.atlas.domain.model

data class FlexibleDateRange(
    val start: FlexibleDate?,
    val end: FlexibleDate?,
    val precision: DatePrecision?,
)
