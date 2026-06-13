package com.atlas.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "country_photos")
data class CountryPhotoEntity(
    @PrimaryKey
    @ColumnInfo(name = "country_iso2")
    val countryIso2: String,
    @ColumnInfo(name = "filename")
    val filename: String,
    @ColumnInfo(name = "source_url")
    val sourceUrl: String?,
    @ColumnInfo(name = "author")
    val author: String?,
    @ColumnInfo(name = "author_link")
    val authorLink: String?,
    @ColumnInfo(name = "fetched_at")
    val fetchedAt: String,
)
