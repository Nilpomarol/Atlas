package com.atlas.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atlas.domain.model.Airport
import com.atlas.domain.model.Flight
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop
import com.atlas.domain.util.utcAwareDepartureSortKey
import com.atlas.presentation.trip.TripStopMapPoint
import com.atlas.domain.repository.AirportRepository
import com.atlas.domain.repository.CountryRepository
import com.atlas.domain.repository.ExcursionRepository
import com.atlas.domain.repository.FlightRepository
import com.atlas.domain.repository.ItineraryRepository
import com.atlas.domain.repository.TripRepository
import com.atlas.domain.service.CountryStateDerivationService
import com.atlas.domain.service.FlexibleDateFormatter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private val isoDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

class DashboardViewModel(
    countryRepository: CountryRepository,
    tripRepository: TripRepository,
    flightRepository: FlightRepository,
    itineraryRepository: ItineraryRepository,
    excursionRepository: ExcursionRepository,
    airportRepository: AirportRepository,
    countryStateDerivationService: CountryStateDerivationService,
    private val flexibleDateFormatter: FlexibleDateFormatter,
) : ViewModel() {
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

    private val excursionData = excursionRepository.observeExcursions()

    val uiState: StateFlow<DashboardUiState> = combine(
        countryData,
        tripData,
        flightData,
        excursionData,
    ) { (countries, userStates, logs), (trips, tripStops), (flights, itineraryGroups, airports), excursions ->
        val userStatesByIso2 = userStates.associateBy { it.countryIso2 }
        val logsByIso2 = logs.groupBy { it.countryIso2 }
        val stopsByIso2 = tripStops.groupBy { it.countryIso2 }
        val airportCountryIso2ById = airports.associate { it.id to it.countryIso2 }
        val countryNamesByIso2 = countries.associate { it.iso2 to it.nameCa }
        val countryFlagsByIso2 = countries.associate { it.iso2 to it.flagEmoji }

        val countryStates = countries.map { country ->
            country to countryStateDerivationService.derive(
                countryIso2 = country.iso2,
                userState = userStatesByIso2[country.iso2],
                logs = logsByIso2[country.iso2].orEmpty(),
                trips = trips,
                tripStops = stopsByIso2[country.iso2].orEmpty(),
                flights = flights,
                itineraryGroups = itineraryGroups,
                excursions = excursions,
                airportCountryIso2ById = airportCountryIso2ById,
            )
        }

        val currentlyLivingIso2 = userStates.firstOrNull { it.currentlyLiving }?.countryIso2
        val currentTrip = trips.firstOrNull { it.status == TravelStatus.IN_PROGRESS }

        val livingIso2s = countryStates.filter { it.second.currentlyLiving }.mapNotNull { it.first.iso2 }.toSet()
        val livedIso2s = countryStates.filter { it.second.lived && !it.second.currentlyLiving }.mapNotNull { it.first.iso2 }.toSet()
        val visitedIso2s = countryStates.filter { it.second.visited && !it.second.lived }.mapNotNull { it.first.iso2 }.toSet()
        val plannedIso2s = countryStates.filter { it.second.planned && !it.second.visited && !it.second.lived }.mapNotNull { it.first.iso2 }.toSet()
        val wishedIso2s = countryStates.filter { it.second.wished && !it.second.planned && !it.second.visited && !it.second.lived }.mapNotNull { it.first.iso2 }.toSet()

        val visitedCount = countryStates.count { it.second.visited }

        // Country markers for the world map: labeled for living/lived, plain for visited
        val highlightedCountryMarkers = buildList {
            for (country in countries) {
                val iso2 = country.iso2 ?: continue
                val lat = country.latitude ?: continue
                val lng = country.longitude ?: continue
                when {
                    iso2 in livingIso2s -> add(DashboardCountryMarker(iso2, lat, lng, label = country.nameCa))
                    iso2 in livedIso2s -> add(DashboardCountryMarker(iso2, lat, lng, label = country.nameCa))
                    else -> continue
                }
            }
        }

        // Flight dashboard items
        val airportsById = airports.associateBy { it.id }
        val soloFlightItems = flights
            .filter { it.itineraryGroupId == null }
            .map { flight ->
                val origin = airportsById[flight.originAirportId]?.shortLabel() ?: flight.originAirportId.uppercase()
                val destination = airportsById[flight.destinationAirportId]?.shortLabel() ?: flight.destinationAirportId.uppercase()
                DashboardFlightUiState(
                    title = "$origin → $destination",
                    label = "Vol",
                    dateText = flight.scheduledDepartureAt?.toFlightDateText(),
                    sortKey = flight.scheduledDepartureAt,
                    status = flight.status,
                    meta = listOfNotNull(flight.airline, flight.flightNumber).joinToString(" ").ifBlank { null },
                    originCode = origin,
                    destinationCode = destination,
                    airlineIata = flight.airline?.takeIf { it.length in 2..3 },
                    flightNumber = flight.flightNumber?.takeIf { it.isNotBlank() },
                    flightId = flight.id,
                )
            }
        val groupItems = itineraryGroups.mapNotNull { group ->
            val sortedFlights = group.flights
                .sortedWith(compareBy<Flight> { it.sortOrder ?: Int.MAX_VALUE }.thenBy { it.utcAwareDepartureSortKey() ?: "" })
            val firstFlight = sortedFlights.firstOrNull() ?: return@mapNotNull null
            val lastFlight = sortedFlights.lastOrNull() ?: return@mapNotNull null
            val origin = airportsById[firstFlight.originAirportId]?.shortLabel() ?: firstFlight.originAirportId.uppercase()
            val destination = airportsById[lastFlight.destinationAirportId]?.shortLabel() ?: lastFlight.destinationAirportId.uppercase()
            val groupStatus = group.flights.map { it.status }.deriveGroupStatus()
            val flightCount = group.flights.size
            DashboardFlightUiState(
                title = "$origin → $destination",
                label = "Itinerari",
                dateText = firstFlight.scheduledDepartureAt?.toFlightDateText(),
                sortKey = firstFlight.scheduledDepartureAt,
                status = groupStatus,
                meta = if (flightCount == 1) "1 vol" else "$flightCount vols",
                originCode = origin,
                destinationCode = destination,
                airlineIata = firstFlight.airline?.takeIf { it.length in 2..3 },
                flightNumber = firstFlight.flightNumber?.takeIf { it.isNotBlank() },
                itineraryId = group.itineraryId,
            )
        }
        val allFlightItems = soloFlightItems + groupItems
        val distancesKm = flights.mapNotNull { it.distanceKm }
        val avgFlightDistanceKm = if (distancesKm.isEmpty()) null else distancesKm.average()

        // Aggregate stats
        val completedFlights = flights.filter { it.status == TravelStatus.COMPLETED }
        val totalFlightMinutes = completedFlights.sumOf { flight ->
            val dep = flight.actualDepartureUtc ?: flight.scheduledDepartureUtc
                ?: flight.actualDepartureAt ?: flight.scheduledDepartureAt
            val arr = flight.actualArrivalUtc ?: flight.scheduledArrivalUtc
                ?: flight.actualArrivalAt ?: flight.scheduledArrivalAt
            parseDurationMinutes(dep, arr)
        }
        val hoursFlown = totalFlightMinutes / 60.0

        val uniqueAirportCount = flights
            .flatMap { listOfNotNull(it.originAirportId, it.destinationAirportId) }
            .distinct().size

        val uniqueAirlineCount = flights
            .mapNotNull { it.airline?.takeIf { a -> a.isNotBlank() } }
            .distinct().size

        val completedTripDays = trips
            .filter { it.status == TravelStatus.COMPLETED }
            .mapNotNull { it.dayCount() }
        val daysTraveled = completedTripDays.sum()
        val avgTripLengthDays = completedTripDays.takeIf { it.isNotEmpty() }?.average()

        val worldPercentage = if (countries.isNotEmpty()) {
            visitedCount.toFloat() / countries.size.toFloat() * 100f
        } else 0f

        DashboardUiState(
            visitedCount = visitedCount,
            wishedCount = countryStates.count { it.second.wished },
            plannedCount = countryStates.count { it.second.planned },
            livedCount = countryStates.count { it.second.lived },
            livingIso2s = livingIso2s,
            livedIso2s = livedIso2s,
            visitedIso2s = visitedIso2s,
            plannedIso2s = plannedIso2s,
            wishedIso2s = wishedIso2s,
            visitedContinentCount = countryStates
                .filter { it.second.visited || it.second.lived }
                .map { it.first.continent }
                .distinct()
                .size,
            trackableCountryCount = countries.size,
            worldPercentage = worldPercentage,
            tripCount = trips.size,
            flightCount = flights.size,
            flownDistanceKm = flights.sumOf { it.distanceKm ?: 0.0 },
            avgFlightDistanceKm = avgFlightDistanceKm,
            hoursFlown = hoursFlown,
            uniqueAirportCount = uniqueAirportCount,
            uniqueAirlineCount = uniqueAirlineCount,
            daysTraveled = daysTraveled,
            avgTripLengthDays = avgTripLengthDays,
            stopCount = tripStops.size,
            currentlyLivingCountryName = currentlyLivingIso2?.let { countryNamesByIso2[it] },
            highlightedCountryMarkers = highlightedCountryMarkers,
            featuredTrip = currentTrip?.toDashboardTrip(
                allStops = tripStops,
                countryNamesByIso2 = countryNamesByIso2,
                countryFlagsByIso2 = countryFlagsByIso2,
            ),
            upcomingTrips = trips
                .filter { it.status == TravelStatus.PLANNED }
                .take(3)
                .map {
                    it.toDashboardTrip(
                        allStops = tripStops,
                        countryNamesByIso2 = countryNamesByIso2,
                        countryFlagsByIso2 = countryFlagsByIso2,
                    )
                },
            upcomingFlights = allFlightItems
                .filter { it.status == TravelStatus.PLANNED || it.status == TravelStatus.IN_PROGRESS }
                .sortedBy { it.sortKey ?: "" }
                .take(3),
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
            recentFlights = allFlightItems
                .filter { it.status == TravelStatus.COMPLETED }
                .sortedByDescending { it.sortKey ?: "" }
                .take(4),
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
            tripId = id,
            title = title,
            status = status,
            dateText = dateRange?.let(flexibleDateFormatter::format),
            dayCount = dayCount(),
            memoryDateText = dateRange?.toMemoryMonthRange(),
            stopCount = stops.size,
            routeText = when {
                first == null -> null
                last == null || first == last -> first
                else -> "$first → $last"
            },
            countryText = countryText,
            flagText = firstCountryIso2?.let { countryFlagsByIso2[it] }?.takeIf { it.isNotBlank() }
                ?: firstCountryIso2,
            countryIso2s = stops.mapNotNull { it.countryIso2 }.distinct(),
            mapPoints = stops
                .filter { it.isVisible }
                .mapNotNull { stop ->
                    val lat = stop.latitude ?: return@mapNotNull null
                    val lng = stop.longitude ?: return@mapNotNull null
                    TripStopMapPoint(latitude = lat, longitude = lng)
                },
        )
    }

    class Factory(
        private val countryRepository: CountryRepository,
        private val tripRepository: TripRepository,
        private val flightRepository: FlightRepository,
        private val itineraryRepository: ItineraryRepository,
        private val excursionRepository: ExcursionRepository,
        private val airportRepository: AirportRepository,
        private val countryStateDerivationService: CountryStateDerivationService,
        private val flexibleDateFormatter: FlexibleDateFormatter,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DashboardViewModel(
                countryRepository = countryRepository,
                tripRepository = tripRepository,
                flightRepository = flightRepository,
                itineraryRepository = itineraryRepository,
                excursionRepository = excursionRepository,
                airportRepository = airportRepository,
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
    val livingIso2s: Set<String> = emptySet(),
    val livedIso2s: Set<String> = emptySet(),
    val visitedIso2s: Set<String> = emptySet(),
    val plannedIso2s: Set<String> = emptySet(),
    val wishedIso2s: Set<String> = emptySet(),
    val visitedContinentCount: Int = 0,
    val trackableCountryCount: Int = 0,
    val worldPercentage: Float = 0f,
    val tripCount: Int = 0,
    val flightCount: Int = 0,
    val flownDistanceKm: Double = 0.0,
    val avgFlightDistanceKm: Double? = null,
    val hoursFlown: Double = 0.0,
    val uniqueAirportCount: Int = 0,
    val uniqueAirlineCount: Int = 0,
    val daysTraveled: Int = 0,
    val avgTripLengthDays: Double? = null,
    val stopCount: Int = 0,
    val currentlyLivingCountryName: String? = null,
    val highlightedCountryMarkers: List<DashboardCountryMarker> = emptyList(),
    val featuredTrip: DashboardTripUiState? = null,
    val upcomingTrips: List<DashboardTripUiState> = emptyList(),
    val upcomingFlights: List<DashboardFlightUiState> = emptyList(),
    val recentCompletedTrips: List<DashboardTripUiState> = emptyList(),
    val recentFlights: List<DashboardFlightUiState> = emptyList(),
)

data class DashboardTripUiState(
    val tripId: String,
    val title: String,
    val status: TravelStatus,
    val dateText: String?,
    val dayCount: Int?,
    val memoryDateText: String?,
    val stopCount: Int,
    val routeText: String?,
    val countryText: String?,
    val flagText: String?,
    val countryIso2s: List<String> = emptyList(),
    val mapPoints: List<TripStopMapPoint> = emptyList(),
)

data class DashboardFlightUiState(
    val title: String,
    val label: String,
    val dateText: String?,
    val sortKey: String?,
    val status: TravelStatus,
    val meta: String?,
    val originCode: String = "",
    val destinationCode: String = "",
    val airlineIata: String? = null,
    val flightNumber: String? = null,
    val flightId: String? = null,       // non-null for solo flights
    val itineraryId: String? = null,    // non-null for itinerary groups
)

data class DashboardCountryMarker(
    val iso2: String,
    val latitude: Double,
    val longitude: Double,
    val label: String?,
)

private fun Trip.dayCount(): Int? {
    val range = dateRange ?: return null
    val start = range.start?.toLocalDateOrNull() ?: return null
    val end = range.end?.toLocalDateOrNull() ?: start
    return ChronoUnit.DAYS.between(start, end).coerceAtLeast(0).toInt() + 1
}

private fun parseDurationMinutes(depStr: String?, arrStr: String?): Long {
    if (depStr == null || arrStr == null) return 0L
    return try {
        val dep = LocalDateTime.parse(depStr, isoDateTimeFormatter)
        val arr = LocalDateTime.parse(arrStr, isoDateTimeFormatter)
        ChronoUnit.MINUTES.between(dep, arr).coerceAtLeast(0)
    } catch (e: Exception) { 0L }
}

private fun FlexibleDate.toLocalDateOrNull(): LocalDate? {
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

private fun Airport.shortLabel(): String = iata ?: icao ?: city

private fun List<TravelStatus>.deriveGroupStatus(): TravelStatus = when {
    contains(TravelStatus.IN_PROGRESS) -> TravelStatus.IN_PROGRESS
    contains(TravelStatus.COMPLETED) -> TravelStatus.COMPLETED
    contains(TravelStatus.PLANNED) -> TravelStatus.PLANNED
    else -> TravelStatus.UNKNOWN
}

private val dashboardFlightDateFormatter = FlexibleDateFormatter()
private fun String.toFlightDateText(): String? = dashboardFlightDateFormatter.formatIsoDate(this)

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
