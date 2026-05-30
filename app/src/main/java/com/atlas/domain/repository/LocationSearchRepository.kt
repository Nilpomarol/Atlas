package com.atlas.domain.repository

import com.atlas.domain.model.LocationSearchResult

interface LocationSearchRepository {
    suspend fun search(query: String): List<LocationSearchResult>
}
