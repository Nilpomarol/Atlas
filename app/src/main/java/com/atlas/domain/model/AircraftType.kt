package com.atlas.domain.model

data class AircraftType(
    val code: String,
    val manufacturer: String,
    val model: String,
    val displayName: String,
    val category: String,
    val numEngines: Int?,
    val engineType: String?,
    val imageAssetRef: String?,
)
