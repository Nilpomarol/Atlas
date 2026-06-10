package com.atlas.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "country_stat_facts",
    primaryKeys = ["country_iso2", "category", "key"],
    indices = [
        Index(value = ["country_iso2"]),
    ],
)
data class CountryStatFactEntity(
    @ColumnInfo(name = "country_iso2")
    val countryIso2: String,
    @ColumnInfo(name = "category")
    val category: String,
    @ColumnInfo(name = "key")
    val key: String,
    @ColumnInfo(name = "label_ca")
    val labelCa: String,
    @ColumnInfo(name = "value")
    val value: String,
    @ColumnInfo(name = "unit")
    val unit: String? = null,
    @ColumnInfo(name = "year")
    val year: Int? = null,
    @ColumnInfo(name = "rank")
    val rank: Int? = null,
    @ColumnInfo(name = "rank_total")
    val rankTotal: Int? = null,
    @ColumnInfo(name = "tier")
    val tier: String? = null,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,
)
