package com.atlas.data.local.mapper

import com.atlas.data.local.entity.AircraftTypeEntity
import com.atlas.domain.model.AircraftType

fun AircraftTypeEntity.toDomain(): AircraftType = AircraftType(
    code = code,
    manufacturer = manufacturer,
    model = model,
    displayName = displayName,
    category = category,
    numEngines = numEngines,
    engineType = engineType,
    imageAssetRef = imageAssetRef,
)
