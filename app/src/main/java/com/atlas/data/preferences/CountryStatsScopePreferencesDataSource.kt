package com.atlas.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.atlas.domain.model.CountryStatsScope
import com.atlas.domain.repository.CountryStatsScopePreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CountryStatsScopePreferencesDataSource(
    private val context: Context,
) : CountryStatsScopePreferencesRepository {
    override fun observeScope(): Flow<CountryStatsScope> =
        context.atlasDataStore.data.map { preferences ->
            CountryStatsScope.fromStorageKey(preferences[COUNTRY_STATS_SCOPE])
        }

    override suspend fun setScope(scope: CountryStatsScope) {
        context.atlasDataStore.edit { preferences ->
            preferences[COUNTRY_STATS_SCOPE] = scope.storageKey
        }
    }

    private companion object {
        val COUNTRY_STATS_SCOPE = stringPreferencesKey("country_stats_scope")
    }
}
