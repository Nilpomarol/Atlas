package com.atlas.domain.model

enum class CountryStatsScope(val storageKey: String) {
    UN_195("un_195"),
    UN_PLUS_KOSOVO_TAIWAN_197("un_plus_kosovo_taiwan_197"),
    ALL_ATLAS("all_atlas"),
    ;

    fun includes(country: Country): Boolean {
        if (!country.isTrackable) return false
        return when (this) {
            UN_195 -> country.isUnMember || country.iso2 in UN_195_EXTRA_ISO2
            UN_PLUS_KOSOVO_TAIWAN_197 ->
                country.isUnMember || country.iso2 in UN_197_EXTRA_ISO2
            ALL_ATLAS -> true
        }
    }

    companion object {
        fun fromStorageKey(value: String?): CountryStatsScope =
            values().firstOrNull { it.storageKey == value } ?: ALL_ATLAS
    }
}

private val UN_195_EXTRA_ISO2 = setOf("PS", "VA")
private val UN_197_EXTRA_ISO2 = UN_195_EXTRA_ISO2 + setOf("XK", "TW")
