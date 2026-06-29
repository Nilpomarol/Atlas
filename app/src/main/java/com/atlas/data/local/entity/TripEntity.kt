package com.atlas.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trips",
    indices = [
        Index(value = ["status"]),
        Index(value = ["title"]),
    ],
)
data class TripEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "status")
    val status: String,
    @ColumnInfo(name = "start_year")
    val startYear: Int?,
    @ColumnInfo(name = "start_month")
    val startMonth: Int?,
    @ColumnInfo(name = "start_day")
    val startDay: Int?,
    @ColumnInfo(name = "end_year")
    val endYear: Int?,
    @ColumnInfo(name = "end_month")
    val endMonth: Int?,
    @ColumnInfo(name = "end_day")
    val endDay: Int?,
    @ColumnInfo(name = "date_precision")
    val datePrecision: String?,
    @ColumnInfo(name = "notes")
    val notes: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String,
    @ColumnInfo(name = "cover_photo_filename")
    val coverPhotoFilename: String? = null,
    @ColumnInfo(name = "is_quick_trip")
    val isQuickTrip: Boolean = false,
)
