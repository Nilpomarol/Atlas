package com.atlas.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "countries",
    foreignKeys = [
        ForeignKey(
            entity = CountryEntity::class,
            parentColumns = ["iso2"],
            childColumns = ["parent_iso2"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["iso3"], unique = true),
        Index(value = ["name_ca"]),
        Index(value = ["continent"]),
        Index(value = ["type"]),
        Index(value = ["is_trackable"]),
        Index(value = ["parent_iso2"]),
    ],
)
data class CountryEntity(
    @PrimaryKey
    @ColumnInfo(name = "iso2")
    val iso2: String,
    @ColumnInfo(name = "iso3")
    val iso3: String?,
    @ColumnInfo(name = "name_ca")
    val nameCa: String,
    @ColumnInfo(name = "name_en")
    val nameEn: String?,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "parent_iso2")
    val parentIso2: String?,
    @ColumnInfo(name = "is_un_member")
    val isUnMember: Boolean,
    @ColumnInfo(name = "is_observer_state")
    val isObserverState: Boolean,
    @ColumnInfo(name = "is_trackable")
    val isTrackable: Boolean,
    @ColumnInfo(name = "continent")
    val continent: String,
    @ColumnInfo(name = "subregion")
    val subregion: String?,
    @ColumnInfo(name = "flag_emoji")
    val flagEmoji: String?,
    @ColumnInfo(name = "flag_asset")
    val flagAsset: String?,
    @ColumnInfo(name = "latitude")
    val latitude: Double?,
    @ColumnInfo(name = "longitude")
    val longitude: Double?,
)
