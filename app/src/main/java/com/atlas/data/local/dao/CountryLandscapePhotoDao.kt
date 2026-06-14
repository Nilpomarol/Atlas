package com.atlas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.atlas.data.local.entity.CountryLandscapePhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryLandscapePhotoDao {
    @Query("SELECT * FROM country_landscape_photos WHERE country_iso2 = :iso2 LIMIT 1")
    fun observe(iso2: String): Flow<CountryLandscapePhotoEntity?>

    @Query("SELECT * FROM country_landscape_photos WHERE country_iso2 = :iso2 LIMIT 1")
    suspend fun getByIso2(iso2: String): CountryLandscapePhotoEntity?

    @Upsert
    suspend fun upsert(photo: CountryLandscapePhotoEntity)
}
