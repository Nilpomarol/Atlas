package com.atlas.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atlas.domain.model.CountryLogType
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.repository.CountryRepository
import com.atlas.domain.repository.TripRepository
import com.atlas.domain.service.CountryStateDerivationService
import com.atlas.domain.service.FlexibleDateFormatter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    countryRepository: CountryRepository,
    tripRepository: TripRepository,
    countryStateDerivationService: CountryStateDerivationService,
    private val flexibleDateFormatter: FlexibleDateFormatter,
) : ViewModel() {
    val uiState: StateFlow<DashboardUiState> = combine(
        countryRepository.observeTrackableCountries(),
        countryRepository.observeUserStates(),
        countryRepository.observeCountryLogs(),
        tripRepository.observeTrips(),
        tripRepository.observeTripStops(),
    ) { countries, userStates, logs, trips, tripStops ->
        val userStatesByIso2 = userStates.associateBy { it.countryIso2 }
        val logsByIso2 = logs.groupBy { it.countryIso2 }
        val stopsByIso2 = tripStops.groupBy { it.countryIso2 }
        val countryNamesByIso2 = countries.associate { it.iso2 to it.nameCa }

        val countryStates = countries.map { country ->
            countryStateDerivationService.derive(
                countryIso2 = country.iso2,
                userState = userStatesByIso2[country.iso2],
                logs = logsByIso2[country.iso2].orEmpty(),
                trips = trips,
                tripStops = stopsByIso2[country.iso2].orEmpty(),
            )
        }

        DashboardUiState(
            visitedCount = countryStates.count { it.visited },
            wishedCount = countryStates.count { it.wished },
            plannedCount = countryStates.count { it.planned },
            livedCount = countryStates.count { it.lived },
            tripCount = trips.size,
            trackableCountryCount = countries.size,
            upcomingTrip = trips
                .firstOrNull { it.status == TravelStatus.PLANNED }
                ?.let { trip ->
                    DashboardTripUiState(
                        title = trip.title,
                        dateText = trip.dateRange?.let(flexibleDateFormatter::format),
                    )
                },
            recentItems = buildRecentItems(
                countryNamesByIso2 = countryNamesByIso2,
                logs = logs.take(3),
                plannedTrips = trips.filter { it.status == TravelStatus.PLANNED }.take(1),
            ),
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState(),
        )

    private fun buildRecentItems(
        countryNamesByIso2: Map<String, String>,
        logs: List<com.atlas.domain.model.CountryLog>,
        plannedTrips: List<com.atlas.domain.model.Trip>,
    ): List<DashboardRecentItemUiState> {
        val logItems = logs.map { log ->
            val countryName = countryNamesByIso2[log.countryIso2] ?: log.countryIso2
            DashboardRecentItemUiState(
                title = log.notes?.takeIf { it.isNotBlank() } ?: countryName,
                subtitle = when (log.type) {
                    CountryLogType.VISIT -> "Visita · $countryName"
                    CountryLogType.LIVED -> "Viscut · $countryName"
                },
                dateText = log.dateRange?.let(flexibleDateFormatter::format),
            )
        }
        val tripItems = plannedTrips.map { trip ->
            DashboardRecentItemUiState(
                title = trip.title,
                subtitle = "Viatge planificat",
                dateText = trip.dateRange?.let(flexibleDateFormatter::format),
            )
        }
        return (logItems + tripItems).take(4)
    }

    class Factory(
        private val countryRepository: CountryRepository,
        private val tripRepository: TripRepository,
        private val countryStateDerivationService: CountryStateDerivationService,
        private val flexibleDateFormatter: FlexibleDateFormatter,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DashboardViewModel(
                countryRepository = countryRepository,
                tripRepository = tripRepository,
                countryStateDerivationService = countryStateDerivationService,
                flexibleDateFormatter = flexibleDateFormatter,
            ) as T
    }
}

data class DashboardUiState(
    val visitedCount: Int = 0,
    val wishedCount: Int = 0,
    val plannedCount: Int = 0,
    val livedCount: Int = 0,
    val tripCount: Int = 0,
    val trackableCountryCount: Int = 0,
    val upcomingTrip: DashboardTripUiState? = null,
    val recentItems: List<DashboardRecentItemUiState> = emptyList(),
)

data class DashboardTripUiState(
    val title: String,
    val dateText: String?,
)

data class DashboardRecentItemUiState(
    val title: String,
    val subtitle: String,
    val dateText: String?,
)
