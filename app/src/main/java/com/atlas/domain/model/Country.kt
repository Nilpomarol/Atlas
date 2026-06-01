package com.atlas.domain.model

data class Country(
    val iso2: String,
    val iso3: String?,
    val nameCa: String,
    val nameEn: String?,
    val type: CountryType,
    val parentIso2: String?,
    val isUnMember: Boolean,
    val isObserverState: Boolean,
    val isTrackable: Boolean,
    val continent: String,
    val subregion: String?,
    val flagEmoji: String?,
    val flagAsset: String?,
    val latitude: Double?,
    val longitude: Double?,
    val capitalNameCa: String?,
    val capitalNameEn: String?,
    val capitalLatitude: Double?,
    val capitalLongitude: Double?,
)
