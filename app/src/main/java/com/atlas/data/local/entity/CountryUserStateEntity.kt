package com.atlas.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "country_user_states",
    foreignKeys = [
        ForeignKey(
            entity = CountryEntity::class,
            parentColumns = ["iso2"],
            childColumns = ["country_iso2"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["wished"]),
        Index(value = ["currently_living"]),
    ],
)
data class CountryUserStateEntity(
    @PrimaryKey
    @ColumnInfo(name = "country_iso2")
    val countryIso2: String,
    @ColumnInfo(name = "wished")
    val wished: Boolean,
    @ColumnInfo(name = "currently_living")
    val currentlyLiving: Boolean,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String,
)
