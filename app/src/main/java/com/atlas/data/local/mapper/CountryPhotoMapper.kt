package com.atlas.data.local.mapper

import com.atlas.data.local.entity.CountryPhotoEntity
import com.atlas.domain.model.CountryPhoto

fun CountryPhotoEntity.toDomain(): CountryPhoto = CountryPhoto(
    countryIso2 = countryIso2,
    filename = filename,
    sourceUrl = sourceUrl,
    author = author,
    authorLink = authorLink,
    fetchedAt = fetchedAt,
)
