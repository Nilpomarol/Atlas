package com.atlas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.atlas.data.local.entity.FlightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightDao {
    @Query("SELECT * FROM flights ORDER BY scheduled_departure_at DESC, created_at DESC")
    fun observeAll(): Flow<List<FlightEntity>>

    @Query("SELECT * FROM flights ORDER BY scheduled_departure_at ASC, created_at ASC")
    suspend fun getAll(): List<FlightEntity>

    @Query("DELETE FROM flights")
    suspend fun deleteAll()

    @Query("SELECT * FROM flights WHERE id = :id")
    fun observeById(id: String): Flow<FlightEntity?>

    @Upsert
    suspend fun upsert(flight: FlightEntity)

    @Upsert
    suspend fun upsertAll(flights: List<FlightEntity>)

    @Delete
    suspend fun delete(flight: FlightEntity)

    @Query("UPDATE flights SET sort_order = :sortOrder, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateSortOrder(id: String, sortOrder: Int, updatedAt: String)

    @Query("UPDATE flights SET itinerary_group_id = NULL, sort_order = NULL WHERE itinerary_group_id = :groupId")
    suspend fun clearGroup(groupId: String)

    @Query("UPDATE flights SET itinerary_group_id = NULL, sort_order = NULL WHERE itinerary_group_id IN (SELECT id FROM itinerary_groups WHERE itinerary_id = :itineraryId)")
    suspend fun clearGroupsForItinerary(itineraryId: String)
}
