package com.atlas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.atlas.data.local.entity.CountryLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryLogDao {
    @Query("SELECT * FROM country_logs ORDER BY created_at DESC")
    fun observeAll(): Flow<List<CountryLogEntity>>

    @Query("SELECT * FROM country_logs WHERE country_iso2 = :countryIso2 ORDER BY created_at DESC")
    fun observeByCountryIso2(countryIso2: String): Flow<List<CountryLogEntity>>

    @Query("SELECT * FROM country_logs ORDER BY created_at DESC")
    suspend fun getAll(): List<CountryLogEntity>

    @Upsert
    suspend fun upsert(log: CountryLogEntity)

    @Upsert
    suspend fun upsertAll(logs: List<CountryLogEntity>)

    @Delete
    suspend fun delete(log: CountryLogEntity)

    @Query("DELETE FROM country_logs")
    suspend fun deleteAll()
}
