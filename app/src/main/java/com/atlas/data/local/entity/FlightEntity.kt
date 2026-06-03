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
    @ColumnInfo(name = "scheduled_departure_utc")
    val scheduledDepartureUtc: String? = null,
    @ColumnInfo(name = "scheduled_arrival_utc")
    val scheduledArrivalUtc: String? = null,
    @ColumnInfo(name = "actual_departure_utc")
    val actualDepartureUtc: String? = null,
    @ColumnInfo(name = "actual_arrival_utc")
    val actualArrivalUtc: String? = null,
    @ColumnInfo(name = "distance_km")
    val distanceKm: Double? = null,
    @ColumnInfo(name = "airline")
    val airline: String?,
    @ColumnInfo(name = "flight_number")
    val flightNumber: String?,
    @ColumnInfo(name = "aircraft")
    val aircraft: String?,
    @ColumnInfo(name = "notes")
    val notes: String?,
    @ColumnInfo(name = "aircraft_registration")
    val aircraftRegistration: String? = null,
    @ColumnInfo(name = "itinerary_group_id")
    val itineraryGroupId: String?,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int?,
    @ColumnInfo(name = "fetched_from")
    val fetchedFrom: String = "manual",
    @ColumnInfo(name = "external_provider")
    val externalProvider: String? = null,
    @ColumnInfo(name = "external_id")
    val externalId: String? = null,
    @ColumnInfo(name = "destination_counts_for_country_tracking", defaultValue = "1")
    val destinationCountsForCountryTracking: Boolean = true,
    @ColumnInfo(name = "origin_counts_for_country_tracking", defaultValue = "0")
    val originCountsForCountryTracking: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String,
)
