package com.atlas.presentation.country

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atlas.domain.model.Country
import com.atlas.domain.model.CountryStatFact
import com.atlas.domain.model.CountryTrackingState
import com.atlas.domain.repository.AirportRepository
import com.atlas.domain.repository.CountryRepository
import com.atlas.domain.repository.CountryStatRepository
import com.atlas.domain.repository.FlightRepository
import com.atlas.domain.repository.ItineraryRepository
import com.atlas.domain.repository.TripRepository
import com.atlas.domain.service.CountryStateDerivationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class CountryListViewModel(
    countryRepository: CountryRepository,
    tripRepository: TripRepository,
    flightRepository: FlightRepository,
    itineraryRepository: ItineraryRepository,
    airportRepository: AirportRepository,
    countryStatRepository: CountryStatRepository,
    countryStateDerivationService: CountryStateDerivationService,
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val selectedFilter = MutableStateFlow(CountryListFilter.All)
    private val selectedSort = MutableStateFlow(CountrySort.Name)
    // Name defaults to ascending (A→Z); stat sorts default to descending (see onSortSelected).
    private val sortAscending = MutableStateFlow(true)

    private val sortState = combine(selectedSort, sortAscending) { sort, ascending -> sort to ascending }

    // Sortable stat values (numeric + display label) by ISO2 → (key → value).
    private val sortValues = countryStatRepository.observeByKeys(SORT_KEYS)
        .map { facts ->
            facts.groupBy { it.countryIso2 }
                .mapValues { (_, list) -> list.associate { fact -> fact.key to fact.toSortValue() } }
        }

    private val countryData = combine(
        countryRepository.observeTrackableCountries(),
        countryRepository.observeUserStates(),
        countryRepository.observeCountryLogs(),
    ) { countries, userStates, logs ->
        Triple(countries, userStates, logs)
    }

    private val tripData = combine(
        tripRepository.observeTrips(),
        tripRepository.observeTripStops(),
    ) { trips, tripStops ->
        trips to tripStops
    }

    private val flightData = combine(
        flightRepository.observeFlights(),
        itineraryRepository.observeAllGroups(),
        airportRepository.observeAirports(),
    ) { flights, itineraryGroups, airports ->
        Triple(flights, itineraryGroups, airports)
    }

    private val countryRows = combine(
        countryData,
        tripData,
        flightData,
    ) { (countries, userStates, logs), (trips, tripStops), (flights, itineraryGroups, airports) ->
        val userStatesByIso2 = userStates.associateBy { it.countryIso2 }
        val logsByIso2 = logs.groupBy { it.countryIso2 }
        val stopsByIso2 = tripStops.groupBy { it.countryIso2 }
        val airportCountryIso2ById = airports.associate { it.id to it.countryIso2 }

        countries.map { country ->
            CountryListItemUiState(
                country = country,
                trackingState = countryStateDerivationService.derive(
                    countryIso2 = country.iso2,
                    userState = userStatesByIso2[country.iso2],
                    logs = logsByIso2[country.iso2].orEmpty(),
                    trips = trips,
                    tripStops = stopsByIso2[country.iso2].orEmpty(),
                    flights = flights,
                    itineraryGroups = itineraryGroups,
                    airportCountryIso2ById = airportCountryIso2ById,
                ),
            )
        }
    }

    val uiState: StateFlow<CountryListUiState> = combine(
        countryRows,
        searchQuery,
        selectedFilter,
        sortState,
        sortValues,
    ) { countryRows, query, filter, sortState, values ->
        val (sort, ascending) = sortState
        val items = countryRows
            .filter { item ->
                item.matchesQuery(query) && filter.matches(item.trackingState)
            }
        val sorted = sortCountryRows(items, sort, ascending, values)
        // When sorting by a stat, surface that stat's value on each row.
        val labeled = if (sort.statKey != null) {
            sorted.map { it.copy(sortValueLabel = values[it.country.iso2]?.get(sort.statKey)?.label) }
        } else {
            sorted
        }

        CountryListUiState(
            countries = labeled,
            allCountries = countryRows,
            searchQuery = query,
            selectedFilter = filter,
            selectedSort = sort,
            sortAscending = ascending,
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

    fun onSortSelected(sort: CountrySort) {
        selectedSort.update { sort }
        // Sensible default direction per field: names A→Z, stats high→low.
        sortAscending.update { sort.statKey == null }
    }

    fun onSortDirectionToggled() {
        sortAscending.update { !it }
    }

    class Factory(
        private val countryRepository: CountryRepository,
        private val tripRepository: TripRepository,
        private val flightRepository: FlightRepository,
        private val itineraryRepository: ItineraryRepository,
        private val airportRepository: AirportRepository,
        private val countryStatRepository: CountryStatRepository,
        private val countryStateDerivationService: CountryStateDerivationService,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CountryListViewModel(
                countryRepository = countryRepository,
                tripRepository = tripRepository,
                flightRepository = flightRepository,
                itineraryRepository = itineraryRepository,
                airportRepository = airportRepository,
                countryStatRepository = countryStatRepository,
                countryStateDerivationService = countryStateDerivationService,
            ) as T
        }
    }

    private companion object {
        val SORT_KEYS: Set<String> = CountrySort.entries.mapNotNull { it.statKey }.toSet()
    }
}

/** A country-list ordering. [statKey] is the stat fact key it sorts by, or null for name. */
enum class CountrySort(val label: String, val statKey: String?) {
    Name("Nom", null),
    Population("Població", "population"),
    Area("Superfície", "area"),
    Gdp("PIB", "gdp"),
    Hdi("IDH", "hdi"),
}

/** Pure, testable ordering of the (already filtered) country rows. */
internal fun sortCountryRows(
    items: List<CountryListItemUiState>,
    sort: CountrySort,
    ascending: Boolean,
    valuesByIso2: Map<String, Map<String, CountrySortValue>>,
): List<CountryListItemUiState> {
    val key = sort.statKey ?: return if (ascending) {
        items.sortedBy { it.country.nameCa.foldAccents() }
    } else {
        items.sortedByDescending { it.country.nameCa.foldAccents() }
    }
    // Stable sort; countries without the stat always sink to the bottom, both directions.
    return items.sortedWith { a, b ->
        val av = valuesByIso2[a.country.iso2]?.get(key)?.numeric
        val bv = valuesByIso2[b.country.iso2]?.get(key)?.numeric
        when {
            av == null && bv == null -> 0
            av == null -> 1
            bv == null -> -1
            ascending -> av.compareTo(bv)
            else -> bv.compareTo(av)
        }
    }
}

private fun CountryStatFact.toSortValue(): CountrySortValue =
    CountrySortValue(
        numeric = value.toStatDouble(),
        label = compactStatValue(value) + (unit?.let { " $it" } ?: ""),
    )

/** Parses a Catalan-formatted stat value ("38.000.000", "1,2") into a Double. */
private fun String.toStatDouble(): Double? = replace(".", "").replace(",", ".").toDoubleOrNull()

private val DIACRITICS = Regex("\\p{Mn}+")

/**
 * Lowercases and strips diacritics so accented and plain vowels (and ç) compare equal,
 * e.g. "França" → "franca", "Àustria" → "austria". Used for accent-insensitive search
 * and name sorting.
 */
internal fun String.foldAccents(): String =
    java.text.Normalizer.normalize(this, java.text.Normalizer.Form.NFD)
        .replace(DIACRITICS, "")
        .lowercase()

data class CountryListUiState(
    val countries: List<CountryListItemUiState> = emptyList(),
    /** The unfiltered tracking set used by map surfaces and progress summaries. */
    val allCountries: List<CountryListItemUiState> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: CountryListFilter = CountryListFilter.All,
    val selectedSort: CountrySort = CountrySort.Name,
    val sortAscending: Boolean = true,
    val totalCountryCount: Int = 0,
)

data class CountryListItemUiState(
    val country: Country,
    val trackingState: CountryTrackingState,
    /** Display value for the active stat sort, e.g. "38M" — null when sorting by name. */
    val sortValueLabel: String? = null,
)

/** A sortable stat value: [numeric] for ordering, [label] for the row. */
internal data class CountrySortValue(val numeric: Double?, val label: String)

enum class CountryListFilter(
    val label: String,
) {
    All("Tots"),
    Visited("Visitats"),
    Wished("Desitjats"),
    Planned("Planejats"),
    Lived("Viscuts"),
}

private fun CountryListItemUiState.matchesQuery(query: String): Boolean {
    val q = query.trim().foldAccents()
    if (q.isEmpty()) return true

    return country.nameCa.foldAccents().contains(q) ||
        country.nameEn?.foldAccents()?.contains(q) == true ||
        country.iso2.foldAccents().contains(q) ||
        country.iso3?.foldAccents()?.contains(q) == true
}

private fun CountryListFilter.matches(trackingState: CountryTrackingState): Boolean = when (this) {
    CountryListFilter.All -> true
    CountryListFilter.Visited -> trackingState.visited
    CountryListFilter.Wished -> trackingState.wished
    CountryListFilter.Planned -> trackingState.planned
    CountryListFilter.Lived -> trackingState.lived
}
