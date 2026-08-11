package com.atlas.ui.rework.screens.home

import androidx.compose.runtime.Immutable
import com.atlas.presentation.dashboard.DashboardUiState

@Immutable
data class HomeShowcaseScenario(
    val id: String,
    val label: String,
    val description: String,
    val state: DashboardUiState,
    val selectedCountryIso2: String? = null,
    val achievementTitle: String? = null,
)
