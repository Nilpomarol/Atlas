package com.atlas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.atlas.data.local.entity.AirlineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AirlineDao {
    @Query("SELECT * FROM airlines WHERE iata = :iata LIMIT 1")
    suspend fun getByIata(iata: String): AirlineEntity?

    @Query(
        """
        SELECT * FROM airlines
        WHERE iata LIKE :query OR name LIKE :query
        ORDER BY
            CASE
                WHEN iata = :exactQuery THEN 0
                WHEN name LIKE :prefixQuery THEN 1
                ELSE 2
            END,
            name
        LIMIT :limit
        """,
    )
    fun search(
        query: String,
        exactQuery: String,
        prefixQuery: String,
        limit: Int,
    ): Flow<List<AirlineEntity>>

    @Upsert
    suspend fun upsertAll(airlines: List<AirlineEntity>)
}
