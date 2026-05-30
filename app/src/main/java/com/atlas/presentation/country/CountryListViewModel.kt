package com.atlas.presentation.country

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atlas.domain.model.Country
import com.atlas.domain.model.CountryTrackingState
import com.atlas.domain.repository.CountryRepository
import com.atlas.domain.repository.TripRepository
import com.atlas.domain.service.CountryStateDerivationService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class CountryListViewModel(
    countryRepository: CountryRepository,
    tripRepository: TripRepository,
    countryStateDerivationService: CountryStateDerivationService,
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val selectedFilter = MutableStateFlow(CountryListFilter.All)

    private val countryRows = combine(
        countryRepository.observeTrackableCountries(),
        countryRepository.observeUserStates(),
        countryRepository.observeCountryLogs(),
        tripRepository.observeTrips(),
        tripRepository.observeTripStops(),
    ) { countries, userStates, logs, trips, tripStops ->
        val userStatesByIso2 = userStates.associateBy { it.countryIso2 }
        val logsByIso2 = logs.groupBy { it.countryIso2 }
        val stopsByIso2 = tripStops.groupBy { it.countryIso2 }

        countries.map { country ->
            CountryListItemUiState(
                country = country,
                trackingState = countryStateDerivationService.derive(
                    countryIso2 = country.iso2,
                    userState = userStatesByIso2[country.iso2],
                    logs = logsByIso2[country.iso2].orEmpty(),
                    trips = trips,
                    tripStops = stopsByIso2[country.iso2].orEmpty(),
                ),
            )
        }
    }

    val uiState: StateFlow<CountryListUiState> = combine(
        countryRows,
        searchQuery,
        selectedFilter,
    ) { countryRows, query, filter ->
        val items = countryRows
            .filter { item ->
                item.matchesQuery(query) && filter.matches(item.trackingState)
            }

        CountryListUiState(
            countries = items,
            searchQuery = query,
            selectedFilter = filter,
            totalCountryCount = countryRows.size,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CountryListUiState(),
        )

    fun onSearchQueryChanged(query: String) {
        searchQuery.update { query }
    }

    fun onFilterSelected(filter: CountryListFilter) {
        selectedFilter.update { filter }
    }

    class Factory(
        private val countryRepository: CountryRepository,
        private val tripRepository: TripRepository,
        private val countryStateDerivationService: CountryStateDerivationService,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CountryListViewModel(
                countryRepository = countryRepository,
                tripRepository = tripRepository,
                countryStateDerivationService = countryStateDerivationService,
            ) as T
        }
    }
}

data class CountryListUiState(
    val countries: List<CountryListItemUiState> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: CountryListFilter = CountryListFilter.All,
    val totalCountryCount: Int = 0,
)

data class CountryListItemUiState(
    val country: Country,
    val trackingState: CountryTrackingState,
)

enum class CountryListFilter(
    val label: String,
) {
    All("Tots"),
    Visited("Visitats"),
    Wished("Desitjats"),
    Planned("Planificats"),
    Lived("Viscuts"),
    CurrentlyLiving("Residència actual"),
    NeverVisited("No visitats"),
}

private fun CountryListItemUiState.matchesQuery(query: String): Boolean {
    val trimmedQuery = query.trim()
    if (trimmedQuery.isEmpty()) return true

    return country.nameCa.contains(trimmedQuery, ignoreCase = true) ||
        country.nameEn?.contains(trimmedQuery, ignoreCase = true) == true ||
        country.iso2.contains(trimmedQuery, ignoreCase = true) ||
        country.iso3?.contains(trimmedQuery, ignoreCase = true) == true
}

private fun CountryListFilter.matches(trackingState: CountryTrackingState): Boolean = when (this) {
    CountryListFilter.All -> true
    CountryListFilter.Visited -> trackingState.visited
    CountryListFilter.Wished -> trackingState.wished
    CountryListFilter.Planned -> trackingState.planned
    CountryListFilter.Lived -> trackingState.lived
    CountryListFilter.CurrentlyLiving -> trackingState.currentlyLiving
    CountryListFilter.NeverVisited -> trackingState.neverVisited
}
