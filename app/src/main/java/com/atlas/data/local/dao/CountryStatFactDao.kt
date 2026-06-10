package com.atlas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.atlas.data.local.entity.CountryStatFactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryStatFactDao {
    @Query(
        "SELECT * FROM country_stat_facts WHERE country_iso2 = :iso2 ORDER BY category, sort_order",
    )
    fun observeByCountry(iso2: String): Flow<List<CountryStatFactEntity>>

    @Query("DELETE FROM country_stat_facts")
    suspend fun clear()

    @Upsert
    suspend fun upsertAll(facts: List<CountryStatFactEntity>)
}
