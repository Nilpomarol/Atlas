package com.atlas.domain.repository

import com.atlas.domain.model.Aircraft

interface AircraftRepository {
    suspend fun getAircraftByRegistration(registration: String): Aircraft?
    suspend fun upsertAircraft(aircraft: Aircraft)
    suspend fun upsertLookupStatus(registration: String, status: String, fetchedAt: String)
}
