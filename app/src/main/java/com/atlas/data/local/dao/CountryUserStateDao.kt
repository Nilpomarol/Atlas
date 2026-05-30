package com.atlas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.atlas.data.local.entity.CountryUserStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryUserStateDao {
    @Query("SELECT * FROM country_user_states")
    fun observeAll(): Flow<List<CountryUserStateEntity>>

    @Query("SELECT * FROM country_user_states WHERE country_iso2 = :countryIso2")
    fun observeByCountryIso2(countryIso2: String): Flow<CountryUserStateEntity?>

    @Query("SELECT * FROM country_user_states WHERE country_iso2 = :countryIso2")
    suspend fun getByCountryIso2(countryIso2: String): CountryUserStateEntity?

    @Query("SELECT * FROM country_user_states ORDER BY country_iso2")
    suspend fun getAll(): List<CountryUserStateEntity>

    @Query("UPDATE country_user_states SET currently_living = 0, updated_at = :updatedAt WHERE currently_living = 1")
    suspend fun clearCurrentlyLiving(updatedAt: String)

    @Upsert
    suspend fun upsert(userState: CountryUserStateEntity)

    @Upsert
    suspend fun upsertAll(userStates: List<CountryUserStateEntity>)

    @Query("DELETE FROM country_user_states")
    suspend fun deleteAll()
}
