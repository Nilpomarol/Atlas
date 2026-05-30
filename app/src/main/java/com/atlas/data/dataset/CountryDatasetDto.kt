package com.atlas.data.dataset

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CountryDatasetDto(
    val version: String,
    val countries: List<CountryDto>,
)

@Serializable
data class CountryDto(
    val iso2: String,
    val iso3: String? = null,
    @SerialName("name_ca")
    val nameCa: String,
    @SerialName("name_en")
    val nameEn: String? = null,
    val type: String,
    @SerialName("parent_iso2")
    val parentIso2: String? = null,
    @SerialName("is_un_member")
    val isUnMember: Boolean,
    @SerialName("is_observer_state")
    val isObserverState: Boolean,
    @SerialName("is_trackable")
    val isTrackable: Boolean,
    val continent: String,
    val subregion: String? = null,
    @SerialName("flag_emoji")
    val flagEmoji: String? = null,
    @SerialName("flag_asset")
    val flagAsset: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)
