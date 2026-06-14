package com.atlas.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached EUR-based exchange rate for a single currency. Keyed by ISO 4217 code (not
 * country) because many countries share a currency. [eurRate] is the number of local
 * currency units per 1 EUR.
 */
@Entity(tableName = "currency_rates")
data class CurrencyRateEntity(
    @PrimaryKey
    @ColumnInfo(name = "currency_code")
    val currencyCode: String,
    @ColumnInfo(name = "eur_rate")
    val eurRate: Double,
    @ColumnInfo(name = "fetched_at")
    val fetchedAt: String,
)
