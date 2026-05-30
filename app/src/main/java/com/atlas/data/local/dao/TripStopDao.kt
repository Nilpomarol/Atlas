package com.atlas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.atlas.data.local.entity.TripStopEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripStopDao {
    @Query("SELECT * FROM trip_stops ORDER BY trip_id, sort_order")
    fun observeAll(): Flow<List<TripStopEntity>>

    @Query("SELECT * FROM trip_stops WHERE trip_id = :tripId ORDER BY sort_order")
    fun observeByTripId(tripId: String): Flow<List<TripStopEntity>>

    @Query("SELECT * FROM trip_stops ORDER BY trip_id, sort_order")
    suspend fun getAll(): List<TripStopEntity>

    @Query("SELECT COALESCE(MAX(sort_order), -1) FROM trip_stops WHERE trip_id = :tripId")
    suspend fun getMaxSortOrder(tripId: String): Int

    @Upsert
    suspend fun upsert(stop: TripStopEntity)

    @Upsert
    suspend fun upsertAll(stops: List<TripStopEntity>)

    @Query(
        """
        UPDATE trip_stops
        SET location_name = :locationName,
            country_iso2 = :countryIso2,
            latitude = :latitude,
            longitude = :longitude,
            start_year = :startYear,
            start_month = :startMonth,
            start_day = :startDay,
            end_year = :endYear,
            end_month = :endMonth,
            end_day = :endDay,
            date_precision = :datePrecision,
            notes = :notes,
            sort_order = :sortOrder,
            updated_at = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun updateFields(
        id: String,
        locationName: String,
        countryIso2: String,
        latitude: Double?,
        longitude: Double?,
        startYear: Int?,
        startMonth: Int?,
        startDay: Int?,
        endYear: Int?,
        endMonth: Int?,
        endDay: Int?,
        datePrecision: String?,
        notes: String?,
        sortOrder: Int,
        updatedAt: String,
    )

    @Query("UPDATE trip_stops SET sort_order = :sortOrder, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateSortOrder(
        id: String,
        sortOrder: Int,
        updatedAt: String,
    )

    @Delete
    suspend fun delete(stop: TripStopEntity)

    @Query("DELETE FROM trip_stops")
    suspend fun deleteAll()
}
