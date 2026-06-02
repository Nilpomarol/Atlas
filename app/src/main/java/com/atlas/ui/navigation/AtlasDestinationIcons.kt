package com.atlas.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

val AtlasDestination.icon: ImageVector
    get() = when (this) {
        AtlasDestination.Dashboard -> Icons.Filled.Home
        AtlasDestination.Countries -> Icons.Filled.Public
        AtlasDestination.Trips -> Icons.Filled.Work
        AtlasDestination.Flights -> Icons.Filled.Flight
        AtlasDestination.Settings -> Icons.Filled.Settings
    }
