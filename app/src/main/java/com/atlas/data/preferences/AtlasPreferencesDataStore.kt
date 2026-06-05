package com.atlas.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

internal val Context.atlasDataStore: DataStore<Preferences> by preferencesDataStore(name = "atlas_prefs")
