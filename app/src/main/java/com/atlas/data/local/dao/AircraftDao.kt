package com.atlas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.atlas.data.local.entity.AircraftEntity

@Dao
interface AircraftDao {
    @Query("SELECT * FROM aircraft WHERE registration = :registration LIMIT 1")
    suspend fun getByRegistration(registration: String): AircraftEntity?

    @Upsert
    suspend fun upsert(aircraft: AircraftEntity)
}
