package com.atlas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.atlas.data.local.entity.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY updated_at DESC")
    fun observeTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :id")
    fun observeTrip(id: String): Flow<TripEntity?>

    @Query("SELECT * FROM trips ORDER BY updated_at DESC")
    suspend fun getAll(): List<TripEntity>

    @Upsert
    suspend fun upsert(trip: TripEntity)

    @Upsert
    suspend fun upsertAll(trips: List<TripEntity>)

    @Delete
    suspend fun delete(trip: TripEntity)

    @Query("DELETE FROM trips")
    suspend fun deleteAll()

    @Query("UPDATE trips SET cover_photo_filename = :filename WHERE id = :tripId")
    suspend fun setCoverPhoto(tripId: String, filename: String?)

    @Query("UPDATE trips SET cover_photo_filename = NULL WHERE cover_photo_filename = :filename")
    suspend fun clearCoverPhotoByFilename(filename: String)
}
