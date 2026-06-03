package com.atlas.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "aircraft",
    indices = [
        Index(value = ["hex_icao"]),
        Index(value = ["icao_code"]),
        Index(value = ["model_code"]),
        Index(value = ["last_lookup_status"]),
    ],
)
data class AircraftEntity(
    @PrimaryKey
    @ColumnInfo(name = "registration")
    val registration: String,
    @ColumnInfo(name = "aerodatabox_id")
    val aeroDataBoxId: Long?,
    @ColumnInfo(name = "active")
    val active: Boolean?,
    @ColumnInfo(name = "serial_number")
    val serialNumber: String?,
    @ColumnInfo(name = "hex_icao")
    val hexIcao: String?,
    @ColumnInfo(name = "airline_name")
    val airlineName: String?,
    @ColumnInfo(name = "iata_type")
    val iataType: String?,
    @ColumnInfo(name = "iata_code_short")
    val iataCodeShort: String?,
    @ColumnInfo(name = "icao_code")
    val icaoCode: String?,
    @ColumnInfo(name = "model")
    val model: String?,
    @ColumnInfo(name = "model_code")
    val modelCode: String?,
    @ColumnInfo(name = "num_seats")
    val numSeats: Int?,
    @ColumnInfo(name = "rollout_date")
    val rolloutDate: String?,
    @ColumnInfo(name = "first_flight_date")
    val firstFlightDate: String?,
    @ColumnInfo(name = "delivery_date")
    val deliveryDate: String?,
    @ColumnInfo(name = "registration_date")
    val registrationDate: String?,
    @ColumnInfo(name = "type_name")
    val typeName: String?,
    @ColumnInfo(name = "num_engines")
    val numEngines: Int?,
    @ColumnInfo(name = "engine_type")
    val engineType: String?,
    @ColumnInfo(name = "is_freighter")
    val isFreighter: Boolean?,
    @ColumnInfo(name = "production_line")
    val productionLine: String?,
    @ColumnInfo(name = "age_years")
    val ageYears: Double?,
    @ColumnInfo(name = "verified")
    val verified: Boolean?,
    @ColumnInfo(name = "image_url")
    val imageUrl: String?,
    @ColumnInfo(name = "image_web_url")
    val imageWebUrl: String?,
    @ColumnInfo(name = "image_author")
    val imageAuthor: String?,
    @ColumnInfo(name = "image_title")
    val imageTitle: String?,
    @ColumnInfo(name = "image_license")
    val imageLicense: String?,
    @ColumnInfo(name = "source")
    val source: String,
    @ColumnInfo(name = "fetched_at")
    val fetchedAt: String,
    @ColumnInfo(name = "last_lookup_status")
    val lastLookupStatus: String,
)
