package com.atlas.data.local.mapper

import com.atlas.data.local.entity.StopPhotoEntity
import com.atlas.domain.model.StopPhoto
import com.atlas.domain.model.StopType

fun StopPhotoEntity.toDomain(): StopPhoto = StopPhoto(
    id = id,
    stopId = stopId,
    stopType = StopType.valueOf(stopType),
    filename = filename,
    sortOrder = sortOrder,
    createdAt = createdAt,
)

fun StopPhoto.toEntity(): StopPhotoEntity = StopPhotoEntity(
    id = id,
    stopId = stopId,
    stopType = stopType.name,
    filename = filename,
    sortOrder = sortOrder,
    createdAt = createdAt,
)
