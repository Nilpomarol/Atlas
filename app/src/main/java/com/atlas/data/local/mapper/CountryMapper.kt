package com.atlas.data.local.mapper

import com.atlas.data.local.entity.CountryEntity
import com.atlas.domain.model.Country
import com.atlas.domain.model.CountryType

fun CountryEntity.toDomain(): Country = Country(
    iso2 = iso2,
    iso3 = iso3,
    nameCa = nameCa,
    nameEn = nameEn,
    type = CountryType.valueOf(type),
    parentIso2 = parentIso2,
    isUnMember = isUnMember,
    isObserverState = isObserverState,
    isTrackable = isTrackable,
    continent = continent,
    subregion = subregion,
    flagEmoji = flagEmoji,
    flagAsset = flagAsset,
    latitude = latitude,
    longitude = longitude,
)

fun Country.toEntity(): CountryEntity = CountryEntity(
    iso2 = iso2,
    iso3 = iso3,
    nameCa = nameCa,
    nameEn = nameEn,
    type = type.name,
    parentIso2 = parentIso2,
    isUnMember = isUnMember,
    isObserverState = isObserverState,
    isTrackable = isTrackable,
    continent = continent,
    subregion = subregion,
    flagEmoji = flagEmoji,
    flagAsset = flagAsset,
    latitude = latitude,
    longitude = longitude,
)
