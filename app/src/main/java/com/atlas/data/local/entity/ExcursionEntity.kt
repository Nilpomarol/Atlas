package com.atlas.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "excursions",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["trip_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TripStopEntity::class,
            parentColumns = ["id"],
            childColumns = ["anchor_trip_stop_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["trip_id"]),
        Index(value = ["anchor_trip_stop_id"]),
        Index(value = ["trip_id", "sort_order"]),
    ],
)
data class ExcursionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "trip_id")
    val tripId: String,
    @ColumnInfo(name = "anchor_trip_stop_id")
    val anchorTripStopId: String?,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "notes")
    val notes: String?,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String,
)
