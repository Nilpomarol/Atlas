package com.atlas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.atlas.data.local.entity.ItineraryEntity
import com.atlas.data.local.entity.ItineraryGroupEntity
import com.atlas.data.local.entity.ItineraryGroupWithFlights
import kotlinx.coroutines.flow.Flow

@Dao
interface ItineraryDao {

    @Query("SELECT * FROM itineraries ORDER BY created_at DESC")
    fun observeAll(): Flow<List<ItineraryEntity>>

    @Query("SELECT * FROM itineraries WHERE id = :id")
    fun observeById(id: String): Flow<ItineraryEntity?>

    @Query("SELECT * FROM itineraries WHERE id = :id")
    suspend fun getById(id: String): ItineraryEntity?

    @Transaction
    @Query("SELECT * FROM itinerary_groups WHERE itinerary_id = :itineraryId ORDER BY sort_order ASC")
    fun observeGroupsWithFlights(itineraryId: String): Flow<List<ItineraryGroupWithFlights>>

    @Transaction
    @Query("SELECT * FROM itinerary_groups WHERE itinerary_id = :itineraryId ORDER BY sort_order ASC")
    suspend fun getGroupsWithFlights(itineraryId: String): List<ItineraryGroupWithFlights>

    @Transaction
    @Query("SELECT * FROM itinerary_groups ORDER BY itinerary_id ASC, sort_order ASC")
    fun observeAllGroupsWithFlights(): Flow<List<ItineraryGroupWithFlights>>

    @Query("SELECT * FROM itineraries ORDER BY created_at ASC")
    suspend fun getAll(): List<ItineraryEntity>

    @Query("SELECT * FROM itinerary_groups ORDER BY itinerary_id ASC, sort_order ASC")
    suspend fun getAllGroups(): List<ItineraryGroupEntity>

    @Query("DELETE FROM itineraries")
    suspend fun deleteAll()

    @Upsert
    suspend fun upsertItinerary(itinerary: ItineraryEntity)

    @Delete
    suspend fun deleteItinerary(itinerary: ItineraryEntity)

    @Upsert
    suspend fun upsertGroup(group: ItineraryGroupEntity)

    @Upsert
    suspend fun upsertGroups(groups: List<ItineraryGroupEntity>)

    @Delete
    suspend fun deleteGroup(group: ItineraryGroupEntity)

    @Query("SELECT id FROM itinerary_groups WHERE itinerary_id = :itineraryId")
    suspend fun getGroupIds(itineraryId: String): List<String>

    @Query("UPDATE itinerary_groups SET sort_order = :sortOrder, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateGroupSortOrder(id: String, sortOrder: Int, updatedAt: String)
}
