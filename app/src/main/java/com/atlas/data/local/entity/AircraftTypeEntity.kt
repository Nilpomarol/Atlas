package com.atlas.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "aircraft_types",
    indices = [
        Index(value = ["manufacturer"]),
        Index(value = ["model"]),
        Index(value = ["category"]),
    ],
)
data class AircraftTypeEntity(
    @PrimaryKey
    @ColumnInfo(name = "code")
    val code: String,
    @ColumnInfo(name = "manufacturer")
    val manufacturer: String,
    @ColumnInfo(name = "model")
    val model: String,
    @ColumnInfo(name = "display_name")
    val displayName: String,
    @ColumnInfo(name = "category")
    val category: String,
    @ColumnInfo(name = "num_engines")
    val numEngines: Int?,
    @ColumnInfo(name = "engine_type")
    val engineType: String?,
    @ColumnInfo(name = "image_asset_ref")
    val imageAssetRef: String?,
    @ColumnInfo(name = "normalized_search_tokens")
    val normalizedSearchTokens: String,
)
