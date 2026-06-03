package com.atlas.domain.usecase.flight

import com.atlas.domain.model.FlightApiResult
import com.atlas.domain.repository.ApiKeyRepository
import com.atlas.domain.repository.FlightApiClient
import kotlinx.coroutines.flow.first

class LookupFlightUseCase(
    private val flightApiClient: FlightApiClient,
    private val apiKeyRepository: ApiKeyRepository,
) {
    suspend operator fun invoke(flightNumber: String, date: String): FlightApiResult {
        val key = apiKeyRepository.observeRapidApiKey().first().trim()
        if (key.isBlank()) return FlightApiResult.NoApiKey
        return flightApiClient.lookup(
            flightNumber = flightNumber.uppercase().replace(" ", ""),
            date = date,
            apiKey = key,
        )
    }
}
