package com.atlas.domain.usecase.airport

import com.atlas.domain.model.Airport
import com.atlas.domain.repository.AirportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SearchAirportsUseCase(
    private val airportRepository: AirportRepository,
) {
    operator fun invoke(query: String): Flow<List<Airport>> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.length < MIN_QUERY_LENGTH) return flowOf(emptyList())
        return airportRepository.searchAirports(normalizedQuery)
    }

    private companion object {
        const val MIN_QUERY_LENGTH = 2
    }
}
