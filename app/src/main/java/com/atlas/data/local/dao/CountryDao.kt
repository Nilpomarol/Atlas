package com.atlas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.atlas.data.local.entity.CountryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryDao {
    @Query("SELECT * FROM countries WHERE is_trackable = 1 ORDER BY name_ca")
    fun observeTrackableCountries(): Flow<List<CountryEntity>>

    @Query("SELECT * FROM countries WHERE iso2 = :iso2")
    fun observeCountry(iso2: String): Flow<CountryEntity?>

    @Query("SELECT iso2 FROM countries")
    suspend fun getAllIso2(): List<String>

    @Upsert
    suspend fun upsertAll(countries: List<CountryEntity>)
}
