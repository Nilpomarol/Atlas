package com.atlas.data.dataset

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AircraftTypeDatasetDto(
    val version: String,
    @SerialName("aircraft_types")
    val aircraftTypes: List<AircraftTypeDto>,
)

@Serializable
data class AircraftTypeDto(
    val code: String,
    val manufacturer: String,
    val model: String,
    @SerialName("display_name")
    val displayName: String,
    val category: String,
    @SerialName("num_engines")
    val numEngines: Int? = null,
    @SerialName("engine_type")
    val engineType: String? = null,
    @SerialName("image_asset_ref")
    val imageAssetRef: String? = null,
    val aliases: List<String> = emptyList(),
)
