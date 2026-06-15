package com.atlas.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.atlas.domain.repository.CountryMemoriesPreferencesRepository
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CountryMemoriesPreferencesDataSource(
    private val context: Context,
) : CountryMemoriesPreferencesRepository {

    override fun observeVisible(countryIso2: String): Flow<Boolean> =
        context.atlasDataStore.data.map { preferences ->
            preferences[visibleKey(countryIso2)] ?: true
        }

    override suspend fun setVisible(
        countryIso2: String,
        isVisible: Boolean,
    ) {
        context.atlasDataStore.edit { preferences ->
            preferences[visibleKey(countryIso2)] = isVisible
        }
    }

    private companion object {
        fun visibleKey(countryIso2: String) =
            booleanPreferencesKey(
                "country_memories_visible_${countryIso2.uppercase(Locale.ROOT)}",
            )
    }
}
