package com.atlas.ui.rework.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.atlas.presentation.dashboard.DashboardUiState
import com.atlas.ui.rework.components.ReworkFloatingCard
import com.atlas.ui.rework.foundation.AtlasReworkTheme
import com.atlas.ui.rework.screens.home.ReworkHomeScreen

@Composable
fun AtlasReworkNavHost(
    navController: NavHostController,
    homeState: DashboardUiState,
    selectedCountryIso2: String?,
    onCountrySelected: (String) -> Unit,
    onCountrySelectionCleared: () -> Unit,
    captureExpanded: Boolean,
    onCaptureRequested: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = ReworkDestination.Home.route,
        modifier = Modifier.fillMaxSize(),
    ) {
        composable(ReworkDestination.Home.route) {
            ReworkHomeScreen(
                state = homeState,
                selectedCountryIso2 = selectedCountryIso2,
                onCountrySelected = onCountrySelected,
                onCountrySelectionCleared = onCountrySelectionCleared,
                captureExpanded = captureExpanded,
                onCaptureRequested = onCaptureRequested,
            )
        }
        ReworkDestination.entries.filterNot { it == ReworkDestination.Home }.forEach { destination ->
            composable(destination.route) { ReworkPlaceholder(destination) }
        }
    }
}

@Composable
private fun ReworkPlaceholder(destination: ReworkDestination) {
    val colors = AtlasReworkTheme.colors
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        ReworkFloatingCard(modifier = Modifier.padding(horizontal = 28.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(destination.label, style = AtlasReworkTheme.typography.display, color = colors.ink)
                Text(
                    "Aquesta destinació formarà part del pròxim tall vertical.",
                    style = AtlasReworkTheme.typography.body,
                    color = colors.inkMuted,
                )
            }
        }
    }
}
