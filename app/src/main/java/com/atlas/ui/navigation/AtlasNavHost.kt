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
import com.atlas.presentation.stats.StatsRoute
import com.atlas.presentation.timeline.TimelineRoute
import com.atlas.presentation.country.CountryDetailRoute
import com.atlas.presentation.country.CountryInfoRoute
import com.atlas.presentation.country.CountryListRoute
import com.atlas.presentation.flight.FlightDetailRoute
import com.atlas.presentation.flight.FlightListRoute
import com.atlas.presentation.itinerary.ItineraryDetailRoute
import com.atlas.presentation.settings.SettingsRoute
import com.atlas.presentation.trip.TripDetailRoute
import com.atlas.presentation.trip.TripListRoute
import com.atlas.presentation.trip.TripStoryRoute
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isStoryRoute = currentDestination?.route == TRIP_STORY_ROUTE

    Scaffold(
        containerColor = AtlasBackground,
        bottomBar = {
            if (!isStoryRoute) {
                NavigationBar(
                    containerColor = AtlasSurface,
                    tonalElevation = 0.dp,
                ) {
                    destinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
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
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AtlasDestination.Dashboard.route,
            modifier = if (isStoryRoute) Modifier else Modifier.padding(innerPadding),
        ) {
            composable(AtlasDestination.Dashboard.route) {
                DashboardRoute(
                    onSettingsClick = { navController.navigate("settings") },
                    onStatsClick = {
                        navController.navigate(AtlasDestination.Stats.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onTripsClick = {
                        navController.navigate(AtlasDestination.Trips.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onFlightsClick = {
                        navController.navigate(AtlasDestination.Flights.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onTripClick = { tripId -> navController.navigate("trips/$tripId") },
                    onFlightClick = { flightId -> navController.navigate("flights/$flightId") },
                    onItineraryClick = { itineraryId -> navController.navigate("itineraries/$itineraryId") },
                )
            }
            composable(AtlasDestination.Stats.route) {
                StatsRoute(onTimelineClick = { navController.navigate("timeline") })
            }
            composable("timeline") {
                TimelineRoute(
                    onBackClick = navController::popBackStack,
                    onTripClick = { tripId -> navController.navigate("trips/$tripId") },
                    onItineraryClick = { itineraryId -> navController.navigate("itineraries/$itineraryId") },
                )
            }
            composable("settings") {
                SettingsRoute(onBackClick = navController::popBackStack)
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
                        onTripClick = { tripId ->
                            navController.navigate("trips/$tripId")
                        },
                        onInfoClick = {
                            navController.navigate("countries/$iso2/info")
                        },
                    )
                }
            }
            composable(
                route = "countries/{iso2}/info",
                arguments = listOf(
                    navArgument("iso2") {
                        type = NavType.StringType
                    },
                ),
            ) { backStackEntry ->
                val iso2 = backStackEntry.arguments?.getString("iso2")
                if (iso2 != null) {
                    CountryInfoRoute(
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
                        onItineraryClick = { itineraryId ->
                            navController.navigate("itineraries/$itineraryId")
                        },
                        onStoryClick = {
                            navController.navigate("trips/$tripId/story")
                        },
                    )
                }
            }
            composable(
                route = TRIP_STORY_ROUTE,
                arguments = listOf(
                    navArgument("tripId") {
                        type = NavType.StringType
                    },
                ),
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId")
                if (tripId != null) {
                    TripStoryRoute(
                        tripId = tripId,
                        onBackClick = navController::popBackStack,
                    )
                }
            }
            composable(AtlasDestination.Flights.route) {
                FlightListRoute(
                    onFlightClick = { flightId ->
                        navController.navigate("flights/$flightId")
                    },
                    onItineraryClick = { itineraryId ->
                        navController.navigate("itineraries/$itineraryId")
                    },
                )
            }
            composable(
                route = "flights/{flightId}",
                arguments = listOf(
                    navArgument("flightId") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val flightId = backStackEntry.arguments?.getString("flightId")
                if (flightId != null) {
                    FlightDetailRoute(
                        flightId = flightId,
                        onBackClick = navController::popBackStack,
                    )
                }
            }
            composable(
                route = "itineraries/{itineraryId}",
                arguments = listOf(
                    navArgument("itineraryId") {
                        type = NavType.StringType
                    },
                ),
            ) { backStackEntry ->
                val itineraryId = backStackEntry.arguments?.getString("itineraryId")
                if (itineraryId != null) {
                    ItineraryDetailRoute(
                        itineraryId = itineraryId,
                        onBackClick = navController::popBackStack,
                        onTripClick = { tripId ->
                            navController.navigate("trips/$tripId")
                        },
                        onFlightClick = { flightId ->
                            navController.navigate("flights/$flightId")
                        },
                    )
                }
            }
        }
    }
}

private const val TRIP_STORY_ROUTE = "trips/{tripId}/story"
