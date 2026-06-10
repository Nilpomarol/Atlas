package com.atlas.data.local.mapper

import com.atlas.data.local.entity.CountryStatFactEntity
import com.atlas.domain.model.CountryStatFact

fun CountryStatFactEntity.toDomain(): CountryStatFact = CountryStatFact(
    countryIso2 = countryIso2,
    category = category,
    key = key,
    labelCa = labelCa,
    value = value,
    unit = unit,
    year = year,
    rank = rank,
    rankTotal = rankTotal,
    tier = tier,
    sortOrder = sortOrder,
)
