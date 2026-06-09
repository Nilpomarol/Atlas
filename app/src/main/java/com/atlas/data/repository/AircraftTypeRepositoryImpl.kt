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

        aircraftTypeDao.getByCode(normalized)?.toDomain()?.let { return it }
        aircraftTypeDao.getByNormalizedToken("%|$normalized|%")?.toDomain()?.let { return it }

        // Strip a single leading manufacturer letter (e.g. "B777300ER" → "777300ER").
        // Boeing entries are commonly written as "B737", "B777-300ER", etc.
        if (normalized.length > 2 && normalized[0].isLetter() && normalized[1].isDigit()) {
            val stripped = normalized.substring(1)
            aircraftTypeDao.getByCode(stripped)?.toDomain()?.let { return it }
            aircraftTypeDao.getByNormalizedToken("%|$stripped|%")?.toDomain()?.let { return it }
        }

        return null
    }
}
