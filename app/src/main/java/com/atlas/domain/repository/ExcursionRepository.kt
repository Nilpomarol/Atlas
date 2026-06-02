package com.atlas.domain.repository

import com.atlas.domain.model.Excursion
import com.atlas.domain.model.ExcursionStop
import com.atlas.domain.model.FlexibleDateRange
import kotlinx.coroutines.flow.Flow

interface ExcursionRepository {
    fun observeExcursions(): Flow<List<Excursion>>
    fun observeExcursions(tripId: String): Flow<List<Excursion>>

    suspend fun createExcursion(
        tripId: String,
        anchorTripStopId: String?,
        title: String,
        notes: String?,
    )

    suspend fun updateExcursion(excursion: Excursion)
    suspend fun deleteExcursion(excursion: Excursion)
    suspend fun reorderExcursions(excursions: List<Excursion>)

    suspend fun createExcursionStop(
        excursionId: String,
        locationName: String,
        countryIso2: String,
        latitude: Double?,
        longitude: Double?,
        dateRange: FlexibleDateRange?,
        notes: String?,
    )

    suspend fun updateExcursionStop(stop: ExcursionStop)
    suspend fun deleteExcursionStop(stop: ExcursionStop)
    suspend fun reorderExcursionStops(stops: List<ExcursionStop>)
}
