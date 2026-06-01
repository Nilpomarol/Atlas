package com.atlas.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.atlas.presentation.dashboard.DashboardRoute
import com.atlas.presentation.country.CountryDetailRoute
import com.atlas.presentation.country.CountryListRoute
import com.atlas.presentation.settings.SettingsRoute
import com.atlas.presentation.trip.TripDetailRoute
import com.atlas.presentation.trip.TripListRoute
import com.atlas.ui.theme.AtlasAccentContainer
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface

@Composable
fun AtlasNavHost() {
    val navController = rememberNavController()
    val destinations = AtlasDestination.entries

    Scaffold(
        containerColor = AtlasBackground,
        bottomBar = {
            NavigationBar(
                containerColor = AtlasSurface,
                tonalElevation = 0.dp,
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title,
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AtlasPrimary,
                            selectedTextColor = AtlasOnSurfaceStrong,
                            indicatorColor = AtlasAccentContainer,
                            unselectedIconColor = AtlasOnSurfaceMuted,
                            unselectedTextColor = AtlasOnSurfaceMuted,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AtlasDestination.Dashboard.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AtlasDestination.Dashboard.route) {
                DashboardRoute()
            }
            composable(AtlasDestination.Countries.route) {
                CountryListRoute(
                    onCountryClick = { iso2 ->
                        navController.navigate("countries/$iso2")
                    },
                )
            }
            composable(
                route = "countries/{iso2}",
                arguments = listOf(
                    navArgument("iso2") {
                        type = NavType.StringType
                    },
                ),
            ) { backStackEntry ->
                val iso2 = backStackEntry.arguments?.getString("iso2")
                if (iso2 != null) {
                    CountryDetailRoute(
                        iso2 = iso2,
                        onBackClick = navController::popBackStack,
                    )
                }
            }
            composable(AtlasDestination.Trips.route) {
                TripListRoute(
                    onTripClick = { tripId ->
                        navController.navigate("trips/$tripId")
                    },
                )
            }
            composable(
                route = "trips/{tripId}",
                arguments = listOf(
                    navArgument("tripId") {
                        type = NavType.StringType
                    },
                ),
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId")
                if (tripId != null) {
                    TripDetailRoute(
                        tripId = tripId,
                        onBackClick = navController::popBackStack,
                    )
                }
            }
            composable(AtlasDestination.Settings.route) {
                SettingsRoute()
            }
        }
    }
}
