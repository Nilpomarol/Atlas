package com.atlas.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stop_photos",
    indices = [
        Index(value = ["stop_id", "stop_type"]),
        Index(value = ["sort_order"]),
    ],
)
data class StopPhotoEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "stop_id")
    val stopId: String,
    @ColumnInfo(name = "stop_type")
    val stopType: String,
    @ColumnInfo(name = "filename")
    val filename: String,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
)
