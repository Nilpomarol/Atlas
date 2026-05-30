package com.atlas.domain.model

data class FlexibleDate(
    val year: Int,
    val month: Int?,
    val day: Int?,
    val precision: DatePrecision,
)
