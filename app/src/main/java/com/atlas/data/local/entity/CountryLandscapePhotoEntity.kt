package com.atlas.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cache of up to a handful of landscape photos for a country, used by the country
 * detail hero. Separate from [CountryPhotoEntity] (the portrait photo used by Country
 * Info): different orientation, different cache directory, and a longer refresh window
 * so the small rotating set stays stable for about a month.
 *
 * The photo list is stored as a serialized JSON array in [photosJson] rather than a
 * fixed set of columns, so the cached count can vary (1–5) without schema churn.
 */
@Entity(tableName = "country_landscape_photos")
data class CountryLandscapePhotoEntity(
    @PrimaryKey
    @ColumnInfo(name = "country_iso2")
    val countryIso2: String,
    @ColumnInfo(name = "photos_json")
    val photosJson: String,
    @ColumnInfo(name = "fetched_at")
    val fetchedAt: String,
)
