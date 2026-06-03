package com.atlas.data.repository

import com.atlas.data.local.database.AtlasDatabase
import com.atlas.data.local.mapper.toDomain
import com.atlas.data.util.normalizedAircraftToken
import com.atlas.domain.model.AircraftType
import com.atlas.domain.repository.AircraftTypeRepository

class AircraftTypeRepositoryImpl(
    database: AtlasDatabase,
) : AircraftTypeRepository {
    private val aircraftTypeDao = database.aircraftTypeDao()

    override suspend fun resolveAircraftType(rawValue: String): AircraftType? {
        val normalized = rawValue.normalizedAircraftToken()
        if (normalized.isBlank()) return null

        return aircraftTypeDao.getByCode(normalized)?.toDomain()
            ?: aircraftTypeDao.getByNormalizedToken("%|$normalized|%")?.toDomain()
    }
}
