package com.atlas.ui.rework.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.atlas.app.AtlasAppContainer
import com.atlas.presentation.dashboard.DashboardViewModel
import com.atlas.ui.rework.capture.CaptureContext
import com.atlas.ui.rework.capture.ReworkCaptureButton
import com.atlas.ui.rework.capture.ReworkCaptureOverlay
import com.atlas.ui.rework.components.ReworkNavigationBar
import com.atlas.ui.rework.foundation.AtlasReworkTheme
import com.atlas.ui.rework.navigation.AtlasReworkNavHost
import com.atlas.ui.rework.navigation.ReworkDestination

@Composable
fun AtlasReworkApp(container: AtlasAppContainer) {
    AtlasReworkTheme {
        val navController = rememberNavController()
        val entry by navController.currentBackStackEntryAsState()
        val selectedDestination = ReworkDestination.entries.firstOrNull { it.route == entry?.destination?.route }
            ?: ReworkDestination.Home
        var captureExpanded by remember { mutableStateOf(false) }
        var selectedCountryIso2 by remember { mutableStateOf<String?>(null) }
        // On the country detail screen, capture is pre-scoped to that country (via the FAB).
        val detailCountryIso2 = if (selectedDestination == ReworkDestination.CountryDetail) {
            entry?.arguments?.getString("iso2")
        } else {
            null
        }

        val dashboardViewModel: DashboardViewModel = viewModel(
            factory = DashboardViewModel.Factory(
                countryRepository = container.countryRepository,
                tripRepository = container.tripRepository,
                flightRepository = container.flightRepository,
                itineraryRepository = container.itineraryRepository,
                countryStatsScopePreferencesRepository = container.countryStatsScopePreferencesRepository,
                airportRepository = container.airportRepository,
                countryStateDerivationService = container.countryStateDerivationService,
                flexibleDateFormatter = container.flexibleDateFormatter,
            ),
        )
        val homeState by dashboardViewModel.uiState.collectAsStateWithLifecycle()

        Box(Modifier.fillMaxSize()) {
            AtlasReworkNavHost(
                navController = navController,
                container = container,
                homeState = homeState,
                selectedCountryIso2 = selectedCountryIso2,
                onCountrySelected = { selectedCountryIso2 = it },
                onCountrySelectionCleared = { selectedCountryIso2 = null },
                captureExpanded = captureExpanded,
                onCaptureRequested = { captureExpanded = !captureExpanded },
            )

            ReworkNavigationBar(
                selected = selectedDestination,
                onSelect = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(ReworkDestination.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            )

            if (selectedDestination != ReworkDestination.Home && !captureExpanded) {
                ReworkCaptureButton(
                    expanded = false,
                    onClick = { captureExpanded = true },
                    modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(end = 22.dp, bottom = 92.dp),
                )
            }

            if (captureExpanded) {
                ReworkCaptureOverlay(
                    context = CaptureContext(countryIso2 = detailCountryIso2 ?: selectedCountryIso2),
                    onDismiss = { captureExpanded = false },
                )
            }
        }
    }
}
