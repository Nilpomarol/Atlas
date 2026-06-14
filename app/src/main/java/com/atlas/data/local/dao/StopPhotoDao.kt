package com.atlas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.atlas.data.local.entity.StopPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StopPhotoDao {
    @Query("SELECT * FROM stop_photos WHERE stop_id = :stopId AND stop_type = :stopType ORDER BY sort_order ASC")
    fun observeByStop(stopId: String, stopType: String): Flow<List<StopPhotoEntity>>

    @Query("SELECT * FROM stop_photos WHERE stop_id IN (:stopIds) AND stop_type = :stopType ORDER BY sort_order ASC")
    fun observeByStopIds(stopIds: List<String>, stopType: String): Flow<List<StopPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: StopPhotoEntity)

    @Upsert
    suspend fun upsertAll(entities: List<StopPhotoEntity>)

    @Delete
    suspend fun delete(entity: StopPhotoEntity)

    @Query("SELECT * FROM stop_photos WHERE stop_id = :stopId AND stop_type = :stopType ORDER BY sort_order ASC")
    suspend fun getByStop(stopId: String, stopType: String): List<StopPhotoEntity>

    @Query("SELECT * FROM stop_photos ORDER BY created_at ASC, sort_order ASC")
    suspend fun getAll(): List<StopPhotoEntity>

    @Query("DELETE FROM stop_photos WHERE stop_id = :stopId AND stop_type = :stopType")
    suspend fun deleteAllForStop(stopId: String, stopType: String)

    @Query("DELETE FROM stop_photos")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM stop_photos WHERE stop_id = :stopId AND stop_type = :stopType")
    suspend fun countByStop(stopId: String, stopType: String): Int

    @Query("SELECT MAX(sort_order) FROM stop_photos WHERE stop_id = :stopId AND stop_type = :stopType")
    suspend fun getMaxSortOrder(stopId: String, stopType: String): Int?
}
