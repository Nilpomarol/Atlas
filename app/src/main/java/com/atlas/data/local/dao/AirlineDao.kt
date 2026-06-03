package com.atlas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.atlas.data.local.entity.AirlineEntity

@Dao
interface AirlineDao {
    @Query("SELECT * FROM airlines WHERE iata = :iata LIMIT 1")
    suspend fun getByIata(iata: String): AirlineEntity?

    @Upsert
    suspend fun upsertAll(airlines: List<AirlineEntity>)
}
