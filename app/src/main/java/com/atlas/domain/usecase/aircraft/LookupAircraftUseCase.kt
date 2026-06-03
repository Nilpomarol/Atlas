package com.atlas.domain.usecase.aircraft

import com.atlas.domain.model.Aircraft
import com.atlas.domain.model.AircraftApiAircraft
import com.atlas.domain.model.AircraftApiResult
import com.atlas.domain.repository.AircraftApiClient
import com.atlas.domain.repository.AircraftRepository
import com.atlas.domain.repository.ApiKeyRepository
import com.atlas.domain.util.normalizedAircraftRegistration
import java.time.Instant
import kotlinx.coroutines.flow.first

class LookupAircraftUseCase(
    private val aircraftRepository: AircraftRepository,
    private val aircraftApiClient: AircraftApiClient,
    private val apiKeyRepository: ApiKeyRepository,
) {
    suspend operator fun invoke(registration: String?): Aircraft? {
        val normalizedRegistration = registration
            ?.normalizedAircraftRegistration()
            ?.takeIf { it.isNotBlank() }
            ?: return null

        aircraftRepository.getAircraftByRegistration(normalizedRegistration)?.let { cached ->
            return cached.takeIf { it.lastLookupStatus == LOOKUP_SUCCESS }
        }

        val apiKey = apiKeyRepository.observeRapidApiKey().first().trim()
        if (apiKey.isBlank()) return null

        val now = Instant.now().toString()
        return when (val result = aircraftApiClient.lookupAircraftByRegistration(normalizedRegistration, apiKey)) {
            is AircraftApiResult.Success -> {
                val aircraft = result.aircraft.toAircraft(now)
                aircraftRepository.upsertAircraft(aircraft)
                aircraft
            }
            AircraftApiResult.NotFound -> {
                aircraftRepository.upsertLookupStatus(normalizedRegistration, LOOKUP_NOT_FOUND, now)
                null
            }
            AircraftApiResult.RateLimited -> {
                aircraftRepository.upsertLookupStatus(normalizedRegistration, LOOKUP_RATE_LIMITED, now)
                null
            }
            is AircraftApiResult.NetworkError -> {
                aircraftRepository.upsertLookupStatus(normalizedRegistration, LOOKUP_ERROR, now)
                null
            }
        }
    }

    private fun AircraftApiAircraft.toAircraft(fetchedAt: String): Aircraft = Aircraft(
        registration = registration.normalizedAircraftRegistration(),
        aeroDataBoxId = aeroDataBoxId,
        active = active,
        serialNumber = serialNumber,
        hexIcao = hexIcao,
        airlineName = airlineName,
        iataType = iataType,
        iataCodeShort = iataCodeShort,
        icaoCode = icaoCode,
        model = model,
        modelCode = modelCode,
        numSeats = numSeats,
        rolloutDate = rolloutDate,
        firstFlightDate = firstFlightDate,
        deliveryDate = deliveryDate,
        registrationDate = registrationDate,
        typeName = typeName,
        numEngines = numEngines,
        engineType = engineType,
        isFreighter = isFreighter,
        productionLine = productionLine,
        ageYears = ageYears,
        verified = verified,
        imageUrl = imageUrl,
        imageWebUrl = imageWebUrl,
        imageAuthor = imageAuthor,
        imageTitle = imageTitle,
        imageLicense = imageLicense,
        source = "aerodatabox",
        fetchedAt = fetchedAt,
        lastLookupStatus = LOOKUP_SUCCESS,
    )

    private companion object {
        const val LOOKUP_SUCCESS = "SUCCESS"
        const val LOOKUP_NOT_FOUND = "NOT_FOUND"
        const val LOOKUP_RATE_LIMITED = "RATE_LIMITED"
        const val LOOKUP_ERROR = "ERROR"
    }
}
