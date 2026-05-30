package com.atlas.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dataset_metadata")
data class DatasetMetadataEntity(
    @PrimaryKey
    @ColumnInfo(name = "key")
    val key: String,
    @ColumnInfo(name = "version")
    val version: String,
    @ColumnInfo(name = "imported_at")
    val importedAt: String,
)
