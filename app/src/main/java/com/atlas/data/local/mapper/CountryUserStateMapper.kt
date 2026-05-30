package com.atlas.data.local.mapper

import com.atlas.data.local.entity.CountryUserStateEntity
import com.atlas.domain.model.CountryUserState

fun CountryUserStateEntity.toDomain(): CountryUserState = CountryUserState(
    countryIso2 = countryIso2,
    wished = wished,
    currentlyLiving = currentlyLiving,
)
