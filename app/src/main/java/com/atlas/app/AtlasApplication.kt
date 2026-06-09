package com.atlas.app

import android.app.Application
import org.maplibre.android.MapLibre

class AtlasApplication : Application() {
    lateinit var container: AtlasAppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
        container = AtlasAppContainer(this)
        container.importInitialData()
        container.refreshTravelStatusesOnStartup()
    }
}
