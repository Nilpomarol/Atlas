package com.atlas.presentation.country

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atlas.domain.model.Airport
import com.atlas.domain.model.Country
import com.atlas.domain.model.CountryLog
import com.atlas.domain.model.CountryLogType
import com.atlas.domain.model.CountryTrackingState
import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.util.CountryCurrencyCodeMap
import com.atlas.domain.model.CountryLandscapePhotos
import com.atlas.domain.model.CountryStatFact
import com.atlas.domain.model.CurrencyRate
import com.atlas.domain.model.Excursion
import com.atlas.domain.model.Flight
import com.atlas.domain.model.Itinerary
import com.atlas.domain.model.ItineraryGroup
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop
import com.atlas.domain.repository.AirportRepository
import com.atlas.domain.repository.CountryLandscapePhotoRepository
import com.atlas.domain.repository.CountryRepository
import com.atlas.domain.repository.CountryStatRepository
import com.atlas.domain.repository.CurrencyRateRepository
import com.atlas.domain.repository.ExcursionRepository
import com.atlas.domain.repository.FlightRepository
import com.atlas.domain.repository.ItineraryRepository
import com.atlas.domain.repository.TripRepository
import com.atlas.domain.service.CountryStateDerivationService
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.domain.util.utcAwareDepartureSortKey
import com.atlas.domain.validation.FlexibleDateValidator
import com.atlas.presentation.date.FlexibleDateRangeDraftField
import com.atlas.presentation.date.FlexibleDateRangeDraftUiState
import com.atlas.presentation.date.updateField
import com.atlas.domain.usecase.country.AddCountryLogUseCase
import com.atlas.domain.usecase.country.DeleteCountryLogUseCase
import com.atlas.domain.usecase.country.SetCurrentlyLivingCountryUseCase
import com.atlas.domain.usecase.country.ToggleWishedCountryUseCase
import com.atlas.domain.usecase.country.UpdateCountryLogUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

class CountryDetailViewModel(
    countryRepository: CountryRepository,
    tripRepository: TripRepository,
    private val toggleWishedCountryUseCase: ToggleWishedCountryUseCase,
    private val setCurrentlyLivingCountryUseCase: SetCurrentlyLivingCountryUseCase,
    private val addCountryLogUseCase: AddCountryLogUseCase,
    private val updateCountryLogUseCase: UpdateCountryLogUseCase,
    private val deleteCountryLogUseCase: DeleteCountryLogUseCase,
    countryStateDerivationService: CountryStateDerivationService,
    private val flexibleDateValidator: FlexibleDateValidator,
    iso2: String,
    flightRepository: FlightRepository,
    itineraryRepository: ItineraryRepository,
    airportRepository: AirportRepository,
    excursionRepository: ExcursionRepository,
    countryStatRepository: CountryStatRepository,
    private val countryLandscapePhotoRepository: CountryLandscapePhotoRepository,
    currencyRateRepository: CurrencyRateRepository,
) : ViewModel() {
    private val logDraft = MutableStateFlow(CountryLogDraftUiState())

    // The currency a country uses is static reference knowledge; EUR is the user's
    // reference currency, so a EUR→EUR converter is pointless and the card is hidden.
    private val currencyCode: String? =
        CountryCurrencyCodeMap.codeForCountry(iso2)?.takeUnless { it == "EUR" }

    private val baseTrackingData = combine(
        countryRepository.observeUserState(iso2),
        countryRepository.observeCountryLogs(iso2),
        tripRepository.observeTrips(),
        tripRepository.observeTripStops(),
        excursionRepository.observeExcursions(),
    ) { userState, logs, trips, tripStops, excursions ->
        BaseTrackingData(userState, logs, trips, tripStops, excursions)
    }

    private val flightTrackingData = combine(
        flightRepository.observeFlights(),
        itineraryRepository.observeItineraries(),
        itineraryRepository.observeAllGroups(),
        airportRepository.observeAirports(),
    ) { flights, itineraries, itineraryGroups, airports ->
        FlightTrackingData(
            flights = flights,
            itineraries = itineraries,
            itineraryGroups = itineraryGroups,
            airports = airports,
            airportCountryIso2ById = airports.associate { it.id to it.countryIso2 },
        )
    }

    private val countryTrackingState = combine(
        baseTrackingData,
        flightTrackingData,
    ) { base, flightData ->
        countryStateDerivationService.derive(
            countryIso2 = iso2,
            userState = base.userState,
            logs = base.logs,
            trips = base.trips,
            tripStops = base.tripStops.filter { it.countryIso2 == iso2 },
            flights = flightData.flights,
            itineraryGroups = flightData.itineraryGroups,
            excursions = base.excursions,
            airportCountryIso2ById = flightData.airportCountryIso2ById,
        )
    }

    private val countryDetailPills = countryTrackingState.map { trackingState ->
        CountryDetailPillUiState(
            wished = trackingState.wished,
            lived = trackingState.lived,
            currentlyLiving = trackingState.currentlyLiving,
            planned = trackingState.planned,
            visited = trackingState.visited,
        )
    }

    private val countryTripSummaries = combine(
        tripRepository.observeTrips(),
        tripRepository.observeTripStops(),
        excursionRepository.observeExcursions(),
    ) { trips, tripStops, excursions ->
        val tripsById = trips.associateBy { it.id }
        val tripSummaries = tripStops
            .filter { it.countryIso2 == iso2 }
            .groupBy { it.tripId }
            .mapNotNull { (tripId, countryStops) ->
                val allStops = tripStops
                    .filter { it.tripId == tripId }
                    .sortedBy { it.sortOrder }
                tripsById[tripId]?.toCountryTripSummary(
                    countryStops = countryStops,
                    allStops = allStops,
                )
            }
        val excursionSummaries = excursions.flatMap { excursion ->
            val trip = tripsById[excursion.tripId] ?: return@flatMap emptyList()
            excursion.stops
                .filter { it.countryIso2 == iso2 }
                .map { stop ->
                    CountryTripSummaryUiState(
                        tripId = trip.id,
                        title = excursion.title,
                        status = trip.status,
                        dateRangeText = stop.dateRange?.let { FlexibleDateFormatter().format(it) },
                        sortKey = stop.dateRange?.start?.toSortKey(),
                        routeText = excursion.stops.sortedBy { it.sortOrder }.joinToString(" → ") { it.locationName },
                        stopCount = 1,
                        label = "Excursio",
                    )
                }
        }
        tripSummaries + excursionSummaries
    }

    private val countryAirTravelSummaries = flightTrackingData.map { flightData ->
        val airportsById = flightData.airports.associateBy { it.id }
        val unlinkedItineraryIds = flightData.itineraries
            .filter { it.tripId == null }
            .map { it.id }
            .toSet()
        val soloFlightSummaries = flightData.flights
            .filter { it.itineraryGroupId == null }
            .filter { flight ->
                flight.status in setOf(TravelStatus.PLANNED, TravelStatus.COMPLETED) &&
                    flightData.airportCountryIso2ById[flight.destinationAirportId] == iso2
            }
            .map { flight ->
                val origin = airportsById[flight.originAirportId]?.shortLabel() ?: flight.originAirportId.uppercase()
                val destination = airportsById[flight.destinationAirportId]?.shortLabel() ?: flight.destinationAirportId.uppercase()
                CountryAirTravelSummaryUiState(
                    title = "$origin → $destination",
                    label = "Vol",
                    status = flight.status,
                    dateText = flight.scheduledDepartureAt?.toCompactDateText(),
                    sortKey = flight.scheduledDepartureAt,
                    meta = listOfNotNull(flight.airline, flight.flightNumber).joinToString(" ").ifBlank { "Vol individual" },
                )
            }

        val groupDerivations = countryStateDerivationService
            .deriveItineraryGroupCountries(
                groups = flightData.itineraryGroups,
                airportCountryIso2ById = flightData.airportCountryIso2ById,
            )
            .filter { it.countryIso2 == iso2 && it.status in setOf(TravelStatus.PLANNED, TravelStatus.COMPLETED) }
            .associateBy { it.groupId }

        val itinerarySummaries = flightData.itineraryGroups
            .filter { it.itineraryId in unlinkedItineraryIds }
            .filter { groupDerivations.containsKey(it.id) }
            .mapNotNull { group ->
                val derivation = groupDerivations[group.id] ?: return@mapNotNull null
                val sortedFlights = group.flights.sortedWith(
                    compareBy<Flight> { it.sortOrder ?: Int.MAX_VALUE }
                        .thenBy { it.utcAwareDepartureSortKey() ?: "" },
                )
                val firstFlight = sortedFlights.firstOrNull() ?: return@mapNotNull null
                val lastFlight = sortedFlights.lastOrNull() ?: return@mapNotNull null
                val origin = airportsById[firstFlight.originAirportId]?.shortLabel() ?: firstFlight.originAirportId.uppercase()
                val destination = airportsById[lastFlight.destinationAirportId]?.shortLabel() ?: lastFlight.destinationAirportId.uppercase()
                val flightCount = group.flights.size
                CountryAirTravelSummaryUiState(
                    title = "$origin → $destination",
                    label = "Itinerari",
                    status = derivation.status,
                    dateText = firstFlight.scheduledDepartureAt?.toCompactDateText(),
                    sortKey = firstFlight.scheduledDepartureAt,
                    meta = if (flightCount == 1) "1 vol" else "$flightCount vols",
                )
            }

        itinerarySummaries + soloFlightSummaries
    }

    private val historySummaries = combine(
        countryTripSummaries,
        countryAirTravelSummaries,
    ) { tripSummaries, airTravelSummaries ->
        tripSummaries to airTravelSummaries
    }

    private val countryStats = countryStatRepository.observeByCountry(iso2)

    private val currencyRateFlow =
        if (currencyCode == null) flowOf<CurrencyRate?>(null)
        else currencyRateRepository.observeRate(currencyCode)

    // Detail-screen enrichment that sits on top of the core tracking state: the rotating
    // landscape hero photo, headline KPI stats, and the live currency rate.
    private val enrichment = combine(
        countryLandscapePhotoRepository.observePhotos(iso2),
        countryStats,
        currencyRateFlow,
    ) { photos, facts, rate ->
        buildEnrichment(photos, facts, rate)
    }

    val uiState: StateFlow<CountryDetailUiState> = combine(
        combine(
            countryRepository.observeCountry(iso2),
            countryRepository.observeCountryLogs(iso2),
            logDraft,
            countryTrackingState,
            historySummaries,
        ) { country, logs, draft, trackingState, history ->
            CountryDetailUiState(
                country = country,
                logs = logs,
                logDraft = draft,
                trackingState = trackingState,
                tripSummaries = history.first,
                airTravelSummaries = history.second,
            )
        },
        countryDetailPills,
        enrichment,
    ) { uiState, detailPills, enrich ->
        uiState.copy(
            detailPills = detailPills,
            landscapePhotoFilename = enrich.landscapePhotoFilename,
            landscapePhotoAuthor = enrich.landscapePhotoAuthor,
            landscapePhotoAuthorLink = enrich.landscapePhotoAuthorLink,
            kpiStats = enrich.kpiStats,
            currencyCode = enrich.currencyCode,
            currencyName = enrich.currencyName,
            eurRate = enrich.eurRate,
            rateAge = enrich.rateAge,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CountryDetailUiState(),
        )

    init {
        viewModelScope.launch {
            val country = countryRepository.observeCountry(iso2).filterNotNull().first()
            countryLandscapePhotoRepository.refreshIfStale(iso2, country.nameEn ?: country.nameCa)
        }
        currencyCode?.let { code ->
            viewModelScope.launch { currencyRateRepository.refreshIfStale(code) }
        }
    }

    private fun buildEnrichment(
        photos: CountryLandscapePhotos?,
        facts: List<CountryStatFact>,
        rate: CurrencyRate?,
    ): DetailEnrichment {
        // Deterministic daily rotation through the small cached set.
        val today = photos?.photos?.takeIf { it.isNotEmpty() }?.let { list ->
            list[(Instant.now().epochSecond / SECONDS_PER_DAY % list.size).toInt()]
        }
        val factsByKey = facts.associateBy { it.key }
        val kpis = KPI_KEYS.mapNotNull { key ->
            factsByKey[key]?.let { f ->
                KpiStatUi(label = f.labelCa, value = compactStatValue(f.value), unit = f.unit, tier = f.tier)
            }
        }
        return DetailEnrichment(
            landscapePhotoFilename = today?.filename,
            landscapePhotoAuthor = today?.author,
            landscapePhotoAuthorLink = today?.authorLink,
            kpiStats = kpis,
            currencyCode = currencyCode,
            currencyName = factsByKey["currency"]?.value,
            eurRate = rate?.eurRate,
            rateAge = rate?.let { formatRateAge(it.fetchedAt) },
        )
    }

    fun onWishedChanged(wished: Boolean) {
        val countryIso2 = uiState.value.country?.iso2 ?: return
        viewModelScope.launch {
            toggleWishedCountryUseCase(
                countryIso2 = countryIso2,
                wished = wished,
            )
        }
    }

    fun onSetCurrentlyLiving() {
        logDraft.update {
            CountryLogDraftUiState(
                isOpen = true,
                type = CountryLogType.LIVED,
                isLivingFlow = true,
            )
        }
    }

    fun onAddVisitLog() {
        logDraft.update {
            CountryLogDraftUiState(
                isOpen = true,
                type = CountryLogType.VISIT,
            )
        }
    }

    fun onDeleteLog(log: CountryLog) {
        viewModelScope.launch {
            deleteCountryLogUseCase(log)
        }
    }

    fun onEditLog(log: CountryLog) {
        logDraft.update {
            CountryLogDraftUiState.fromLog(log)
        }
    }

    fun onDismissLogDraft() {
        logDraft.update { CountryLogDraftUiState() }
    }

    fun onLogTypeChanged(type: CountryLogType) {
        logDraft.update {
            it.copy(type = type, validationError = null)
        }
    }

    fun onLogPrecisionChanged(precision: DatePrecision) {
        logDraft.update {
            it.copy(
                dateRange = it.dateRange.copy(precision = precision),
                validationError = null,
            )
        }
    }

    fun onLogDraftFieldChanged(
        field: FlexibleDateRangeDraftField,
        value: String,
    ) {
        logDraft.update {
            it.copy(
                dateRange = it.dateRange.updateField(
                    field = field,
                    value = value,
                ),
                validationError = null,
            )
        }
    }

    fun onLogNotesChanged(notes: String) {
        logDraft.update {
            it.copy(notes = notes)
        }
    }

    fun onSaveLogDraft() {
        val countryIso2 = uiState.value.country?.iso2 ?: return
        val draft = logDraft.value
        val type = draft.type
        val dateRange = draft.dateRange.toDateRange()
        val notes = draft.notes.trim().ifBlank { null }

        if (dateRange == null && draft.dateRange.hasAnyInput()) {
            logDraft.update {
                it.copy(validationError = "La data no és vàlida.")
            }
            return
        }

        if (dateRange != null && !flexibleDateValidator.isValid(dateRange)) {
            logDraft.update {
                it.copy(validationError = "La data no és vàlida.")
            }
            return
        }

        viewModelScope.launch {
            if (draft.logId == null) {
                addCountryLogUseCase(
                    countryIso2 = countryIso2,
                    type = type,
                    dateRange = dateRange,
                    notes = notes,
                )
            } else {
                updateCountryLogUseCase(
                    CountryLog(
                        id = draft.logId,
                        countryIso2 = countryIso2,
                        type = type,
                        dateRange = dateRange,
                        notes = notes,
                    ),
                )
            }
            if (draft.isLivingFlow) {
                setCurrentlyLivingCountryUseCase(countryIso2)
            }
            onDismissLogDraft()
        }
    }

    class Factory(
        private val countryRepository: CountryRepository,
        private val tripRepository: TripRepository,
        private val toggleWishedCountryUseCase: ToggleWishedCountryUseCase,
        private val setCurrentlyLivingCountryUseCase: SetCurrentlyLivingCountryUseCase,
        private val addCountryLogUseCase: AddCountryLogUseCase,
        private val updateCountryLogUseCase: UpdateCountryLogUseCase,
        private val deleteCountryLogUseCase: DeleteCountryLogUseCase,
        private val countryStateDerivationService: CountryStateDerivationService,
        private val flexibleDateValidator: FlexibleDateValidator,
        private val iso2: String,
        private val flightRepository: FlightRepository,
        private val itineraryRepository: ItineraryRepository,
        private val airportRepository: AirportRepository,
        private val excursionRepository: ExcursionRepository,
        private val countryStatRepository: CountryStatRepository,
        private val countryLandscapePhotoRepository: CountryLandscapePhotoRepository,
        private val currencyRateRepository: CurrencyRateRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CountryDetailViewModel(
                countryRepository = countryRepository,
                tripRepository = tripRepository,
                toggleWishedCountryUseCase = toggleWishedCountryUseCase,
                setCurrentlyLivingCountryUseCase = setCurrentlyLivingCountryUseCase,
                addCountryLogUseCase = addCountryLogUseCase,
                updateCountryLogUseCase = updateCountryLogUseCase,
                deleteCountryLogUseCase = deleteCountryLogUseCase,
                countryStateDerivationService = countryStateDerivationService,
                flexibleDateValidator = flexibleDateValidator,
                iso2 = iso2,
                flightRepository = flightRepository,
                itineraryRepository = itineraryRepository,
                airportRepository = airportRepository,
                excursionRepository = excursionRepository,
                countryStatRepository = countryStatRepository,
                countryLandscapePhotoRepository = countryLandscapePhotoRepository,
                currencyRateRepository = currencyRateRepository,
            ) as T
        }
    }
}

private data class BaseTrackingData(
    val userState: com.atlas.domain.model.CountryUserState?,
    val logs: List<CountryLog>,
    val trips: List<Trip>,
    val tripStops: List<TripStop>,
    val excursions: List<Excursion>,
)

private data class FlightTrackingData(
    val flights: List<Flight>,
    val itineraries: List<Itinerary>,
    val itineraryGroups: List<ItineraryGroup>,
    val airports: List<Airport>,
    val airportCountryIso2ById: Map<String, String>,
)

data class CountryDetailUiState(
    val country: Country? = null,
    val logs: List<CountryLog> = emptyList(),
    val tripSummaries: List<CountryTripSummaryUiState> = emptyList(),
    val airTravelSummaries: List<CountryAirTravelSummaryUiState> = emptyList(),
    val logDraft: CountryLogDraftUiState = CountryLogDraftUiState(),
    val trackingState: CountryTrackingState = CountryTrackingState.Empty,
    val detailPills: CountryDetailPillUiState = CountryDetailPillUiState(),
    val landscapePhotoFilename: String? = null,
    val landscapePhotoAuthor: String? = null,
    val landscapePhotoAuthorLink: String? = null,
    val kpiStats: List<KpiStatUi> = emptyList(),
    val currencyCode: String? = null,
    val currencyName: String? = null,
    val eurRate: Double? = null,
    val rateAge: String? = null,
)

/** A headline statistic tile on the country detail screen. */
data class KpiStatUi(
    val label: String,
    val value: String,
    val unit: String?,
    val tier: String?,
)

private data class DetailEnrichment(
    val landscapePhotoFilename: String?,
    val landscapePhotoAuthor: String?,
    val landscapePhotoAuthorLink: String?,
    val kpiStats: List<KpiStatUi>,
    val currencyCode: String?,
    val currencyName: String?,
    val eurRate: Double?,
    val rateAge: String?,
)

data class CountryDetailPillUiState(
    val wished: Boolean = false,
    val lived: Boolean = false,
    val currentlyLiving: Boolean = false,
    val planned: Boolean = false,
    val visited: Boolean = false,
)

data class CountryTripSummaryUiState(
    val tripId: String,
    val title: String,
    val status: TravelStatus,
    val dateRangeText: String?,
    val sortKey: String?,
    val routeText: String?,
    val stopCount: Int,
    val label: String = "Viatge",
)

data class CountryAirTravelSummaryUiState(
    val title: String,
    val label: String,
    val status: TravelStatus,
    val dateText: String?,
    val sortKey: String?,
    val meta: String,
)

data class CountryLogDraftUiState(
    val isOpen: Boolean = false,
    val logId: String? = null,
    val type: CountryLogType = CountryLogType.VISIT,
    val dateRange: FlexibleDateRangeDraftUiState = FlexibleDateRangeDraftUiState(),
    val notes: String = "",
    val validationError: String? = null,
    val isLivingFlow: Boolean = false,
) {
    companion object {
        fun fromLog(log: CountryLog): CountryLogDraftUiState {
            return CountryLogDraftUiState(
                isOpen = true,
                logId = log.id,
                type = log.type,
                dateRange = FlexibleDateRangeDraftUiState.fromDateRange(log.dateRange),
                notes = log.notes.orEmpty(),
            )
        }
    }
}

private fun Trip.toCountryTripSummary(
    countryStops: List<TripStop>,
    allStops: List<TripStop>,
): CountryTripSummaryUiState {
    val first = allStops.firstOrNull()?.locationName
    val last = allStops.lastOrNull()?.locationName
    return CountryTripSummaryUiState(
        tripId = id,
        title = title,
        status = status,
        dateRangeText = dateRange?.let { FlexibleDateFormatter().format(it) },
        sortKey = dateRange?.start?.toSortKey(),
        routeText = when {
            first == null -> null
            last == null || first == last -> first
            else -> "$first → $last"
        },
        stopCount = countryStops.size,
    )
}

private fun Airport.shortLabel(): String = iata ?: icao ?: city

private fun FlexibleDate.toSortKey(): String {
    val y = year.toString().padStart(4, '0')
    val m = (month ?: 1).toString().padStart(2, '0')
    val d = (day ?: 1).toString().padStart(2, '0')
    return "$y-$m-$d"
}

private fun String.toCompactDateText(): String? =
    countryDetailDateFormatter.formatIsoDate(this)

private val countryDetailDateFormatter = FlexibleDateFormatter()

private const val SECONDS_PER_DAY = 86_400L

// Headline stats for the detail screen, in display order.
private val KPI_KEYS = listOf("population", "area", "gdp_per_capita", "hdi")

/** Compact Catalan "freshness" label for a cached rate timestamp, e.g. "fa 3 h". */
private fun formatRateAge(fetchedAt: String): String? {
    val instant = runCatching { Instant.parse(fetchedAt) }.getOrNull() ?: return null
    val minutes = Duration.between(instant, Instant.now()).toMinutes().coerceAtLeast(0)
    return when {
        minutes < 1 -> "ara mateix"
        minutes < 60 -> "fa $minutes min"
        minutes < 1440 -> "fa ${minutes / 60} h"
        else -> {
            val days = minutes / 1440
            if (days == 1L) "fa 1 dia" else "fa $days dies"
        }
    }
}
