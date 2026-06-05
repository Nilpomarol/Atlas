package com.atlas.domain.repository

import kotlinx.coroutines.flow.Flow

interface TripMapPreferencesRepository {
    fun observeGeneratedStopsVisible(tripId: String): Flow<Boolean>

    suspend fun setGeneratedStopsVisible(
        tripId: String,
        isVisible: Boolean,
    )
}
