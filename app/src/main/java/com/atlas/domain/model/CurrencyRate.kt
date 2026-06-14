package com.atlas.domain.model

/** A cached EUR-based exchange rate: [eurRate] local currency units per 1 EUR. */
data class CurrencyRate(
    val currencyCode: String,
    val eurRate: Double,
    val fetchedAt: String,
)
