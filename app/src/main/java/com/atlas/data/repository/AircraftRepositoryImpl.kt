package com.atlas.data.repository

import com.atlas.data.local.database.AtlasDatabase
import com.atlas.data.local.mapper.toDomain
import com.atlas.data.local.mapper.toEntity
import com.atlas.domain.model.Aircraft
import com.atlas.domain.repository.AircraftRepository
import com.atlas.domain.util.normalizedAircraftRegistration

class AircraftRepositoryImpl(
    database: AtlasDatabase,
) : AircraftRepository {
    private val aircraftDao = database.aircraftDao()

    override suspend fun getAircraftByRegistration(registration: String): Aircraft? =
        aircraftDao.getByRegistration(registration.normalizedAircraftRegistration())?.toDomain()

    override suspend fun upsertAircraft(aircraft: Aircraft) {
        aircraftDao.upsert(aircraft.toEntity())
    }

    override suspend fun upsertLookupStatus(registration: String, status: String, fetchedAt: String) {
        aircraftDao.upsert(
            Aircraft(
                registration = registration.normalizedAircraftRegistration(),
                aeroDataBoxId = null,
                active = null,
                serialNumber = null,
                hexIcao = null,
                airlineName = null,
                iataType = null,
                iataCodeShort = null,
                icaoCode = null,
                model = null,
                modelCode = null,
                numSeats = null,
                rolloutDate = null,
                firstFlightDate = null,
                deliveryDate = null,
                registrationDate = null,
                typeName = null,
                numEngines = null,
                engineType = null,
                isFreighter = null,
                productionLine = null,
                ageYears = null,
                verified = null,
                imageUrl = null,
                imageWebUrl = null,
                imageAuthor = null,
                imageTitle = null,
                imageLicense = null,
                source = "aerodatabox",
                fetchedAt = fetchedAt,
                lastLookupStatus = status,
            ).toEntity(),
        )
    }
}
