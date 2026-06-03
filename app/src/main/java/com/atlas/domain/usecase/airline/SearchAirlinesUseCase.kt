package com.atlas.domain.usecase.airline

import com.atlas.domain.model.Airline
import com.atlas.domain.repository.AirlineRepository
import kotlinx.coroutines.flow.Flow

class SearchAirlinesUseCase(
    private val airlineRepository: AirlineRepository,
) {
    operator fun invoke(query: String): Flow<List<Airline>> =
        airlineRepository.searchAirlines(query)
}
