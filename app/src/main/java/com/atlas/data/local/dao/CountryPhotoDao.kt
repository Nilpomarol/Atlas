package com.atlas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.atlas.data.local.entity.CountryPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryPhotoDao {
    @Query("SELECT * FROM country_photos WHERE country_iso2 = :iso2 LIMIT 1")
    fun observe(iso2: String): Flow<CountryPhotoEntity?>

    @Query("SELECT * FROM country_photos WHERE country_iso2 = :iso2 LIMIT 1")
    suspend fun getByIso2(iso2: String): CountryPhotoEntity?

    @Upsert
    suspend fun upsert(photo: CountryPhotoEntity)
}
