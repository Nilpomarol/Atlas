package com.atlas.data.repository

import androidx.room.withTransaction
import com.atlas.data.local.database.AtlasDatabase
import com.atlas.data.local.entity.ExcursionEntity
import com.atlas.data.local.entity.ExcursionStopEntity
import com.atlas.data.local.mapper.toDomain
import com.atlas.data.local.mapper.toEntity
import com.atlas.domain.model.Excursion
import com.atlas.domain.model.ExcursionStop
import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.repository.ExcursionRepository
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExcursionRepositoryImpl(
    private val database: AtlasDatabase,
) : ExcursionRepository {
    private val excursionDao = database.excursionDao()

    override fun observeExcursions(): Flow<List<Excursion>> =
        excursionDao.observeAllWithItems().map { relations -> relations.map { it.toDomain() } }

    override fun observeExcursions(tripId: String): Flow<List<Excursion>> =
        excursionDao.observeByTripIdWithItems(tripId).map { relations -> relations.map { it.toDomain() } }

    override suspend fun createExcursion(
        tripId: String,
        anchorTripStopId: String?,
        title: String,
        notes: String?,
    ) {
        val now = Instant.now().toString()
        excursionDao.upsertExcursion(
            ExcursionEntity(
                id = UUID.randomUUID().toString(),
                tripId = tripId,
                anchorTripStopId = anchorTripStopId,
                title = title,
                notes = notes,
                sortOrder = excursionDao.getMaxExcursionSortOrder(tripId) + 1,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    override suspend fun updateExcursion(excursion: Excursion) {
        val now = Instant.now().toString()
        excursionDao.upsertExcursion(excursion.toEntity(createdAt = now, updatedAt = now))
    }

    override suspend fun deleteExcursion(excursion: Excursion) {
        excursionDao.deleteExcursion(excursion.toEntity(createdAt = "", updatedAt = ""))
    }

    override suspend fun reorderExcursions(excursions: List<Excursion>) {
        database.withTransaction {
            val now = Instant.now().toString()
            excursions.forEachIndexed { index, excursion ->
                excursionDao.updateExcursionSortOrder(
                    id = excursion.id,
                    sortOrder = index,
                    updatedAt = now,
                )
            }
        }
    }

    override suspend fun createExcursionStop(
        excursionId: String,
        locationName: String,
        countryIso2: String,
        latitude: Double?,
        longitude: Double?,
        dateRange: FlexibleDateRange?,
        notes: String?,
    ) {
        val now = Instant.now().toString()
        excursionDao.upsertStop(
            ExcursionStopEntity(
                id = UUID.randomUUID().toString(),
                excursionId = excursionId,
                locationName = locationName,
                countryIso2 = countryIso2,
                latitude = latitude,
                longitude = longitude,
                startYear = dateRange?.start?.year,
                startMonth = dateRange?.start?.month,
                startDay = dateRange?.start?.day,
                endYear = dateRange?.end?.year,
                endMonth = dateRange?.end?.month,
                endDay = dateRange?.end?.day,
                datePrecision = dateRange?.precision?.name,
                notes = notes,
                sortOrder = excursionDao.getMaxStopSortOrder(excursionId) + 1,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    override suspend fun updateExcursionStop(stop: ExcursionStop) {
        val now = Instant.now().toString()
        excursionDao.upsertStop(stop.toEntity(createdAt = now, updatedAt = now))
    }

    override suspend fun deleteExcursionStop(stop: ExcursionStop) {
        excursionDao.deleteStop(stop.toEntity(createdAt = "", updatedAt = ""))
    }

    override suspend fun reorderExcursionStops(stops: List<ExcursionStop>) {
        database.withTransaction {
            val now = Instant.now().toString()
            stops.forEachIndexed { index, stop ->
                excursionDao.updateStopSortOrder(
                    id = stop.id,
                    sortOrder = index,
                    updatedAt = now,
                )
            }
        }
    }
}
