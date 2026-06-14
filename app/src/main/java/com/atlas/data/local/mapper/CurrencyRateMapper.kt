package com.atlas.data.local.mapper

import com.atlas.data.local.entity.CurrencyRateEntity
import com.atlas.domain.model.CurrencyRate

fun CurrencyRateEntity.toDomain(): CurrencyRate = CurrencyRate(
    currencyCode = currencyCode,
    eurRate = eurRate,
    fetchedAt = fetchedAt,
)
