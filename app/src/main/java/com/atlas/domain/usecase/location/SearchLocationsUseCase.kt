package com.atlas.domain.usecase.location

import com.atlas.domain.model.LocationSearchResult
import com.atlas.domain.repository.LocationSearchRepository

class SearchLocationsUseCase(
    private val locationSearchRepository: LocationSearchRepository,
) {
    suspend operator fun invoke(query: String): List<LocationSearchResult> =
        locationSearchRepository.search(query.trim())
}
