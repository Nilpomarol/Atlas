package com.atlas.domain.repository

import com.atlas.domain.model.AircraftType

interface AircraftTypeRepository {
    suspend fun resolveAircraftType(rawValue: String): AircraftType?
}
