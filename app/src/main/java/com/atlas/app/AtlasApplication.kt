package com.atlas.app

import android.app.Application

class AtlasApplication : Application() {
    lateinit var container: AtlasAppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AtlasAppContainer(this)
        container.importInitialData()
    }
}
