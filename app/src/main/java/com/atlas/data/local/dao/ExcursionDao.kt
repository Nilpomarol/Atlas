package com.atlas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.atlas.data.local.entity.ExcursionEntity
import com.atlas.data.local.entity.ExcursionStopEntity
import com.atlas.data.local.entity.ExcursionWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface ExcursionDao {
    @Transaction
    @Query("SELECT * FROM excursions ORDER BY trip_id ASC, sort_order ASC")
    fun observeAllWithItems(): Flow<List<ExcursionWithItems>>

    @Transaction
    @Query("SELECT * FROM excursions WHERE trip_id = :tripId ORDER BY sort_order ASC")
    fun observeByTripIdWithItems(tripId: String): Flow<List<ExcursionWithItems>>

    @Query("SELECT COALESCE(MAX(sort_order), -1) FROM excursions WHERE trip_id = :tripId")
    suspend fun getMaxExcursionSortOrder(tripId: String): Int

    @Query("SELECT COALESCE(MAX(sort_order), -1) FROM excursion_stops WHERE excursion_id = :excursionId")
    suspend fun getMaxStopSortOrder(excursionId: String): Int

    @Query("SELECT * FROM excursions ORDER BY trip_id ASC, sort_order ASC")
    suspend fun getAll(): List<ExcursionEntity>

    @Query("SELECT * FROM excursion_stops ORDER BY excursion_id ASC, sort_order ASC")
    suspend fun getAllStops(): List<ExcursionStopEntity>

    @Query("DELETE FROM excursions")
    suspend fun deleteAll()

    @Upsert
    suspend fun upsertExcursion(excursion: ExcursionEntity)

    @Delete
    suspend fun deleteExcursion(excursion: ExcursionEntity)

    @Upsert
    suspend fun upsertStop(stop: ExcursionStopEntity)

    @Delete
    suspend fun deleteStop(stop: ExcursionStopEntity)

    @Query("UPDATE excursions SET sort_order = :sortOrder, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateExcursionSortOrder(id: String, sortOrder: Int, updatedAt: String)

    @Query("UPDATE excursion_stops SET sort_order = :sortOrder, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateStopSortOrder(id: String, sortOrder: Int, updatedAt: String)
}
