package com.atlas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.atlas.data.local.entity.AirportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AirportDao {
    @Query("SELECT * FROM airports")
    fun observeAll(): Flow<List<AirportEntity>>

    @Query(
        """
        SELECT * FROM airports
        WHERE iata LIKE :query
            OR icao LIKE :query
            OR name LIKE :query
            OR city LIKE :query
            OR country_iso2 LIKE :query
        ORDER BY
            CASE
                WHEN iata = :exactQuery THEN 0
                WHEN city LIKE :prefixQuery THEN 1
                WHEN name LIKE :prefixQuery THEN 2
                ELSE 3
            END,
            city,
            name
        LIMIT :limit
        """,
    )
    fun search(
        query: String,
        exactQuery: String,
        prefixQuery: String,
        limit: Int,
    ): Flow<List<AirportEntity>>

    @Query("SELECT * FROM airports WHERE id = :id")
    suspend fun getById(id: String): AirportEntity?

    @Query("SELECT * FROM airports WHERE iata = :iata LIMIT 1")
    suspend fun getByIata(iata: String): AirportEntity?

    @Upsert
    suspend fun upsertAll(airports: List<AirportEntity>)
}
