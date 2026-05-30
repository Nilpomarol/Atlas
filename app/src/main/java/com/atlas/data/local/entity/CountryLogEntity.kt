package com.atlas.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "country_logs",
    foreignKeys = [
        ForeignKey(
            entity = CountryEntity::class,
            parentColumns = ["iso2"],
            childColumns = ["country_iso2"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["country_iso2"]),
        Index(value = ["type"]),
    ],
)
data class CountryLogEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "country_iso2")
    val countryIso2: String,
    @ColumnInfo(name = "type")
    val type: String,
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
)
