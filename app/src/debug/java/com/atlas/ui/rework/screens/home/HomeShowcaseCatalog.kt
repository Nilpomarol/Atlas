package com.atlas.ui.rework.screens.home

import com.atlas.domain.model.TravelStatus
import com.atlas.presentation.dashboard.DashboardFlightUiState
import com.atlas.presentation.dashboard.DashboardTripUiState
import com.atlas.presentation.dashboard.DashboardUiState

object HomeShowcaseCatalog {
    val enabled: Boolean = true

    val scenarios: List<HomeShowcaseScenario> = listOf(
        HomeShowcaseScenario(
            id = "empty",
            label = "Buit",
            description = "Usuari nou sense activitat",
            state = DashboardUiState(trackableCountryCount = 195),
        ),
        HomeShowcaseScenario(
            id = "established",
            label = "Viatger habitual",
            description = "Mapa poblat, pròxim viatge i records",
            state = establishedTravelerState(),
        ),
        HomeShowcaseScenario(
            id = "active_trip",
            label = "Viatge actiu",
            description = "Montanya en curs",
            state = establishedTravelerState().copy(
                featuredTrip = trip(
                    id = "demo-active-trip",
                    title = "Montanya",
                    status = TravelStatus.IN_PROGRESS,
                    dateText = "12–23 ag. 2026",
                    routeText = "Ljubljana · Dolomites · Venècia",
                    countryText = "Eslovènia · Àustria · Itàlia",
                    stopCount = 6,
                ),
                upcomingTrips = emptyList(),
                recentCompletedTrips = emptyList(),
                recentFlights = emptyList(),
            ),
        ),
        HomeShowcaseScenario(
            id = "active_flight",
            label = "Vol actiu",
            description = "Vol cap a Tòquio en curs",
            state = establishedTravelerState().copy(
                featuredTrip = null,
                upcomingTrips = emptyList(),
                upcomingFlights = listOf(
                    DashboardFlightUiState(
                        title = "Barcelona → Tòquio",
                        label = "Vol",
                        dateText = "Avui · 14.35",
                        sortKey = "2026-08-11T14:35",
                        status = TravelStatus.IN_PROGRESS,
                        meta = "JL 48",
                        originCode = "BCN",
                        destinationCode = "HND",
                        originCity = "Barcelona",
                        destinationCity = "Tòquio",
                        airlineIata = "JL",
                        flightNumber = "48",
                        flightId = "demo-active-flight",
                    ),
                ),
                recentCompletedTrips = emptyList(),
                recentFlights = emptyList(),
            ),
        ),
        HomeShowcaseScenario(
            id = "selected_country",
            label = "País seleccionat",
            description = "Portugal seleccionat al mapa",
            state = establishedTravelerState(),
            selectedCountryIso2 = "PT",
        ),
        HomeShowcaseScenario(
            id = "achievement",
            label = "Assoliment recent",
            description = "Moment de celebració destacat",
            state = establishedTravelerState().copy(
                upcomingTrips = emptyList(),
                recentCompletedTrips = emptyList(),
                recentFlights = emptyList(),
            ),
            achievementTitle = "Cinc continents",
        ),
    )
}

private fun establishedTravelerState(): DashboardUiState {
    val living = setOf("ES")
    val lived = setOf("DE")
    val visited = setOf(
        "PT", "FR", "IT", "GB", "IE", "NL", "BE", "CH", "AT", "CZ",
        "PL", "DK", "SE", "NO", "FI", "EE", "GR", "HR", "SI", "MA",
        "US", "CA", "MX", "AR", "CL", "PE", "TH", "VN", "SG", "AU",
        "ZA", "EG", "TR",
    )
    return DashboardUiState(
        visitedCount = living.size + lived.size + visited.size,
        wishedCount = 3,
        plannedCount = 2,
        livedCount = 2,
        livingIso2s = living,
        livedIso2s = lived,
        visitedIso2s = visited,
        plannedIso2s = setOf("JP", "IS"),
        wishedIso2s = setOf("NZ", "NP", "ID"),
        visitedContinentCount = 6,
        trackableCountryCount = 195,
        worldPercentage = 17.9f,
        tripCount = 18,
        flightCount = 42,
        flownDistanceKm = 61_842.0,
        hoursFlown = 96.5,
        uniqueAirportCount = 32,
        uniqueAirlineCount = 11,
        daysTraveled = 146,
        stopCount = 73,
        currentlyLivingCountryName = "Espanya",
        upcomingTrips = listOf(
            trip(
                id = "demo-upcoming-trip",
                title = "Montanya",
                status = TravelStatus.PLANNED,
                dateText = "12–23 ag. 2026",
                routeText = "Ljubljana · Dolomites · Venècia",
                countryText = "Eslovènia · Àustria · Itàlia",
                stopCount = 6,
            ),
        ),
        recentCompletedTrips = listOf(
            trip(
                id = "demo-recent-trip",
                title = "Itàlia del nord",
                status = TravelStatus.COMPLETED,
                dateText = "Abr. 2026",
                routeText = "Milà · Como · Verona",
                countryText = "Itàlia",
                stopCount = 4,
                memoryDateText = "Abr. 2026",
            ),
        ),
    )
}

private fun trip(
    id: String,
    title: String,
    status: TravelStatus,
    dateText: String,
    routeText: String,
    countryText: String,
    stopCount: Int,
    memoryDateText: String? = null,
): DashboardTripUiState = DashboardTripUiState(
    tripId = id,
    title = title,
    status = status,
    dateText = dateText,
    dayCount = 12,
    memoryDateText = memoryDateText,
    stopCount = stopCount,
    routeText = routeText,
    countryText = countryText,
    flagText = null,
    countryIso2s = emptyList(),
)
