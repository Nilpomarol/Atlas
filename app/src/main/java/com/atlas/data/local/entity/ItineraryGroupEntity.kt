package com.atlas.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "itinerary_groups",
    foreignKeys = [
        ForeignKey(
            entity = ItineraryEntity::class,
            parentColumns = ["id"],
            childColumns = ["itinerary_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["itinerary_id"]),
        Index(value = ["sort_order"]),
        Index(value = ["status"]),
    ],
)
data class ItineraryGroupEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "itinerary_id")
    val itineraryId: String,
    @ColumnInfo(name = "title")
    val title: String?,
    @ColumnInfo(name = "status")
    val status: String?,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String,
)
