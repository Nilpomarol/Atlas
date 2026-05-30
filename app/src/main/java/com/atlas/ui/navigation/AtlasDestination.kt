package com.atlas.ui.navigation

enum class AtlasDestination(
    val route: String,
    val title: String,
) {
    Dashboard("dashboard", "Inici"),
    Countries("countries", "Països"),
    Trips("trips", "Viatges"),
    Settings("settings", "Configuració"),
}
