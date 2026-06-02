package com.atlas.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flights",
    foreignKeys = [
        ForeignKey(
            entity = AirportEntity::class,
            parentColumns = ["id"],
            childColumns = ["origin_airport_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = AirportEntity::class,
            parentColumns = ["id"],
            childColumns = ["destination_airport_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = ItineraryGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["itinerary_group_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["origin_airport_id"]),
        Index(value = ["destination_airport_id"]),
        Index(value = ["status"]),
        Index(value = ["itinerary_group_id"]),
    ],
)
data class FlightEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "origin_airport_id")
    val originAirportId: String,
    @ColumnInfo(name = "destination_airport_id")
    val destinationAirportId: String,
    @ColumnInfo(name = "status")
    val status: String,
    // All datetimes stored as ISO "YYYY-MM-DDTHH:mm", nullable
    @ColumnInfo(name = "scheduled_departure_at")
    val scheduledDepartureAt: String?,
    @ColumnInfo(name = "scheduled_arrival_at")
    val scheduledArrivalAt: String?,
    @ColumnInfo(name = "actual_departure_at")
    val actualDepartureAt: String?,
    @ColumnInfo(name = "actual_arrival_at")
    val actualArrivalAt: String?,
    @ColumnInfo(name = "airline")
    val airline: String?,
    @ColumnInfo(name = "flight_number")
    val flightNumber: String?,
    @ColumnInfo(name = "aircraft")
    val aircraft: String?,
    @ColumnInfo(name = "notes")
    val notes: String?,
    @ColumnInfo(name = "itinerary_group_id")
    val itineraryGroupId: String?,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int?,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String,
)
