package com.atlas.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.atlas.domain.repository.TripMapPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TripMapPreferencesDataSource(
    private val context: Context,
) : TripMapPreferencesRepository {

    override fun observeGeneratedStopsVisible(tripId: String): Flow<Boolean> =
        context.atlasDataStore.data.map { prefs ->
            prefs[generatedStopsVisibleKey(tripId)] ?: true
        }

    override suspend fun setGeneratedStopsVisible(
        tripId: String,
        isVisible: Boolean,
    ) {
        context.atlasDataStore.edit { prefs ->
            prefs[generatedStopsVisibleKey(tripId)] = isVisible
        }
    }

    private companion object {
        fun generatedStopsVisibleKey(tripId: String) =
            booleanPreferencesKey("trip_map_generated_stops_visible_$tripId")
    }
}
