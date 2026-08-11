package com.atlas.ui.rework.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Luggage
import androidx.compose.material.icons.rounded.Public
import androidx.compose.ui.graphics.vector.ImageVector

enum class ReworkDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Home("home", "Inici", Icons.Rounded.Home),
    Countries("countries", "Països", Icons.Rounded.Public),
    Trips("trips", "Viatges", Icons.Rounded.Luggage),
    Flights("flights", "Vols", Icons.Rounded.Flight),
    Progress("progress", "Progrés", Icons.Rounded.BarChart),
}
