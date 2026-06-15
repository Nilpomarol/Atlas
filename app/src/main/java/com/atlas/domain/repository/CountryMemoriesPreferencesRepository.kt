package com.atlas.domain.repository

import kotlinx.coroutines.flow.Flow

interface CountryMemoriesPreferencesRepository {
    fun observeVisible(countryIso2: String): Flow<Boolean>

    suspend fun setVisible(
        countryIso2: String,
        isVisible: Boolean,
    )
}
