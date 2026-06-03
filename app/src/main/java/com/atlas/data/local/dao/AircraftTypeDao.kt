package com.atlas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.atlas.data.local.entity.AircraftTypeEntity

@Dao
interface AircraftTypeDao {
    @Query("SELECT * FROM aircraft_types WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: String): AircraftTypeEntity?

    @Query(
        """
        SELECT * FROM aircraft_types
        WHERE normalized_search_tokens LIKE :tokenPattern
        LIMIT 1
        """,
    )
    suspend fun getByNormalizedToken(tokenPattern: String): AircraftTypeEntity?

    @Upsert
    suspend fun upsertAll(aircraftTypes: List<AircraftTypeEntity>)
}
