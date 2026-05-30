package com.atlas.domain.model

data class CountryUserState(
    val countryIso2: String,
    val wished: Boolean,
    val currentlyLiving: Boolean,
)
