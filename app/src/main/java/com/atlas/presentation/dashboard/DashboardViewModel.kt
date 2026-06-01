package com.atlas.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.repository.CountryRepository
import com.atlas.domain.repository.TripRepository
import com.atlas.domain.service.CountryStateDerivationService
import com.atlas.domain.service.FlexibleDateFormatter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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
        val countryFlagsByIso2 = countries.associate { it.iso2 to it.flagEmoji }

        val countryStates = countries.map { country ->
            country to countryStateDerivationService.derive(
                countryIso2 = country.iso2,
                userState = userStatesByIso2[country.iso2],
                logs = logsByIso2[country.iso2].orEmpty(),
                trips = trips,
                tripStops = stopsByIso2[country.iso2].orEmpty(),
            )
        }

        val currentlyLivingIso2 = userStates.firstOrNull { it.currentlyLiving }?.countryIso2
        val currentTrip = trips.firstOrNull { it.status == TravelStatus.IN_PROGRESS }
        val nextPlannedTrip = trips.firstOrNull { it.status == TravelStatus.PLANNED }
        val featuredTrip = currentTrip ?: nextPlannedTrip

        DashboardUiState(
            visitedCount = countryStates.count { it.second.visited },
            wishedCount = countryStates.count { it.second.wished },
            plannedCount = countryStates.count { it.second.planned },
            livedCount = countryStates.count { it.second.lived },
            visitedContinentCount = countryStates
                .filter { it.second.visited || it.second.lived }
                .map { it.first.continent }
                .distinct()
                .size,
            tripCount = trips.size,
            flightCount = 0,
            stopCount = tripStops.size,
            trackableCountryCount = countries.size,
            currentlyLivingCountryName = currentlyLivingIso2?.let { countryNamesByIso2[it] },
            featuredTrip = featuredTrip?.toDashboardTrip(
                allStops = tripStops,
                countryNamesByIso2 = countryNamesByIso2,
                countryFlagsByIso2 = countryFlagsByIso2,
            ),
            nextUpTrip = trips
                .firstOrNull { trip ->
                    trip.status == TravelStatus.PLANNED && trip.id != featuredTrip?.id
                }
                ?.toDashboardTrip(
                    allStops = tripStops,
                    countryNamesByIso2 = countryNamesByIso2,
                    countryFlagsByIso2 = countryFlagsByIso2,
                ),
            recentCompletedTrips = trips
                .filter { it.status == TravelStatus.COMPLETED }
                .take(4)
                .map {
                    it.toDashboardTrip(
                        allStops = tripStops,
                        countryNamesByIso2 = countryNamesByIso2,
                        countryFlagsByIso2 = countryFlagsByIso2,
                    )
                },
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState(),
        )

    private fun Trip.toDashboardTrip(
        allStops: List<TripStop>,
        countryNamesByIso2: Map<String, String>,
        countryFlagsByIso2: Map<String, String?>,
    ): DashboardTripUiState {
        val stops = allStops.filter { it.tripId == id }.sortedBy { it.sortOrder }
        val first = stops.firstOrNull()?.locationName
        val last = stops.lastOrNull()?.locationName
        val firstCountryIso2 = stops.firstOrNull()?.countryIso2
        val countryText = stops
            .map { stop -> countryNamesByIso2[stop.countryIso2] ?: stop.countryIso2 }
            .distinct()
            .joinToString(", ")
            .ifBlank { null }
        return DashboardTripUiState(
            title = title,
            status = status,
            dateText = dateRange?.let(flexibleDateFormatter::format),
            dayCount = dayCount(),
            memoryDateText = dateRange?.toMemoryMonthRange(),
            stopCount = stops.size,
            routeText = when {
                first == null -> null
                last == null || first == last -> first
                else -> "$first -> $last"
            },
            countryText = countryText,
            flagText = firstCountryIso2?.let { countryFlagsByIso2[it] }?.takeIf { it.isNotBlank() }
                ?: firstCountryIso2,
        )
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
    val visitedContinentCount: Int = 0,
    val tripCount: Int = 0,
    val flightCount: Int = 0,
    val stopCount: Int = 0,
    val trackableCountryCount: Int = 0,
    val currentlyLivingCountryName: String? = null,
    val featuredTrip: DashboardTripUiState? = null,
    val nextUpTrip: DashboardTripUiState? = null,
    val recentCompletedTrips: List<DashboardTripUiState> = emptyList(),
)

data class DashboardTripUiState(
    val title: String,
    val status: TravelStatus,
    val dateText: String?,
    val dayCount: Int?,
    val memoryDateText: String?,
    val stopCount: Int,
    val routeText: String?,
    val countryText: String?,
    val flagText: String?,
)

private fun Trip.dayCount(): Int? {
    val range = dateRange ?: return null
    val start = range.start?.toLocalDateOrNull() ?: return null
    val end = range.end?.toLocalDateOrNull() ?: start
    return ChronoUnit.DAYS.between(start, end).coerceAtLeast(0).toInt() + 1
}

private fun com.atlas.domain.model.FlexibleDate.toLocalDateOrNull(): LocalDate? {
    val month = month ?: return null
    val day = day ?: return null
    return runCatching { LocalDate.of(year, month, day) }.getOrNull()
}

private fun FlexibleDateRange.toMemoryMonthRange(): String? {
    val startText = start?.toMonthYearText()
    val endText = end?.toMonthYearText()
    return when {
        startText != null && endText != null && startText != endText -> "$startText - $endText"
        startText != null -> startText
        endText != null -> endText
        else -> null
    }
}

private fun FlexibleDate.toMonthYearText(): String =
    month?.let { "${it.shortCatalanMonth()} $year" } ?: year.toString()

private fun Int.shortCatalanMonth(): String = when (this) {
    1 -> "GEN."
    2 -> "FEBR."
    3 -> "MARÇ"
    4 -> "ABR."
    5 -> "MAIG"
    6 -> "JUNY"
    7 -> "JUL."
    8 -> "AG."
    9 -> "SET."
    10 -> "OCT."
    11 -> "NOV."
    12 -> "DES."
    else -> ""
}
