package com.atlas.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "airlines",
    indices = [
        Index(value = ["icao"]),
        Index(value = ["name"]),
    ],
)
data class AirlineEntity(
    @PrimaryKey
    @ColumnInfo(name = "iata")
    val iata: String,
    @ColumnInfo(name = "icao")
    val icao: String?,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "country_iso2")
    val countryIso2: String?,
)
