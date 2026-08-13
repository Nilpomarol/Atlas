package com.atlas.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atlas.domain.model.Airport
import com.atlas.domain.model.Country
import com.atlas.domain.model.CountryLog
import com.atlas.domain.model.CountryStatsScope
import com.atlas.domain.model.CountryTrackingState
import com.atlas.domain.model.Flight
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.model.Itinerary
import com.atlas.domain.model.ItineraryGroup
import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.model.StopPhoto
import com.atlas.domain.model.StopType
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop
import com.atlas.domain.repository.AircraftTypeRepository
import com.atlas.domain.repository.AirportRepository
import com.atlas.domain.repository.CountryRepository
import com.atlas.domain.repository.FlightRepository
import com.atlas.domain.repository.ItineraryRepository
import com.atlas.domain.repository.CountryStatsScopePreferencesRepository
import com.atlas.domain.repository.StopPhotoRepository
import com.atlas.domain.repository.TripRepository
import com.atlas.domain.service.CountryStateDerivationService
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.domain.util.utcAwareDelayMinutes
import com.atlas.domain.util.utcAwareDurationMinutes
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(
    countryRepository: CountryRepository,
    tripRepository: TripRepository,
    flightRepository: FlightRepository,
    itineraryRepository: ItineraryRepository,
    countryStatsScopePreferencesRepository: CountryStatsScopePreferencesRepository,
    airportRepository: AirportRepository,
    stopPhotoRepository: StopPhotoRepository,
    private val aircraftTypeRepository: AircraftTypeRepository,
    private val countryStateDerivationService: CountryStateDerivationService,
    private val flexibleDateFormatter: FlexibleDateFormatter,
) : ViewModel() {
    private val countryData = combine(
        countryRepository.observeTrackableCountries(),
        countryRepository.observeUserStates(),
        countryRepository.observeCountryLogs(),
    ) { countries, userStates, logs ->
        CountryData(countries, userStates.associateBy { it.countryIso2 }, logs)
    }

    private val tripStopsFlow = tripRepository.observeTripStops()
    private val tripsFlow = tripRepository.observeTrips()

    private val tripData = combine(
        tripsFlow,
        tripStopsFlow,
    ) { trips, stops ->
        TripData(trips, stops)
    }

    private val flightData = combine(
        flightRepository.observeFlights(),
        itineraryRepository.observeAllGroups(),
        itineraryRepository.observeItineraries(),
        airportRepository.observeAirports(),
    ) { flights, groups, itineraries, airports ->
        FlightData(flights, groups, itineraries, airports)
    }

    @Suppress("UNCHECKED_CAST")
    private val tripStopPhotosFlow = tripStopsFlow.flatMapLatest { stops ->
        val ids = stops.map { it.id }
        if (ids.isEmpty()) flowOf(emptyMap())
        else stopPhotoRepository.observeByStopIds(ids, StopType.TRIP_STOP)
            .map { photos -> photos.groupBy { it.stopId } }
    }


    private val sourceData = combine(
        countryData,
        tripData,
        flightData,
        tripStopPhotosFlow,
    ) { countries, trips, flights, photos ->
        StatsSourceData(countries, trips, flights, photos)
    }

    val uiState: StateFlow<StatsUiState> = combine(
        sourceData,
        countryStatsScopePreferencesRepository.observeScope(),
    ) { data, statsScope -> data to statsScope }
        .mapLatest { (data, statsScope) -> buildUiState(data, statsScope) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatsUiState(),
        )

    private suspend fun buildUiState(data: StatsSourceData, statsScope: CountryStatsScope): StatsUiState {
        val allCountries = data.countryData.countries
        val trips = data.tripData.trips
        val tripStops = data.tripData.stops
        val flights = data.flightData.flights
        val itineraryGroups = data.flightData.itineraryGroups
        val itineraries = data.flightData.itineraries
        val airports = data.flightData.airports
        // Groups whose itinerary is linked to a trip are excluded from activity counting —
        // the trip stops already represent that activity. Only standalone itinerary groups count.
        val tripLinkedItineraryIds = itineraries.filter { it.tripId != null }.mapTo(mutableSetOf()) { it.id }
        val standaloneItineraryGroups = itineraryGroups.filter { it.itineraryId !in tripLinkedItineraryIds }
        val photos = data.photos

        val airportsById = airports.associateBy { it.id }
        val countriesByIso2 = allCountries.associateBy { it.iso2 }
        val countryNamesByIso2 = allCountries.associate { it.iso2 to it.nameCa }
        val airportCountryIso2ById = airports.associate { it.id to it.countryIso2 }
        val logsByIso2 = data.countryData.logs.groupBy { it.countryIso2 }
        val stopsByIso2 = tripStops.groupBy { it.countryIso2 }

        val allCountryStates = allCountries.map { country ->
            country to countryStateDerivationService.derive(
                countryIso2 = country.iso2,
                userState = data.countryData.userStatesByIso2[country.iso2],
                logs = logsByIso2[country.iso2].orEmpty(),
                trips = trips,
                tripStops = stopsByIso2[country.iso2].orEmpty(),
                flights = flights,
                itineraryGroups = itineraryGroups,
                airportCountryIso2ById = airportCountryIso2ById,
            )
        }
        val countryStates = allCountryStates.filter { (country, _) -> statsScope.includes(country) }
        val countries = countryStates.map { it.first }

        val livingIso2s = countryStates.filter { it.second.currentlyLiving }.map { it.first.iso2 }.toSet()
        val livedIso2s = countryStates.filter { it.second.lived && !it.second.currentlyLiving }.map { it.first.iso2 }.toSet()
        val visitedIso2s = countryStates.filter { it.second.visited && !it.second.lived }.map { it.first.iso2 }.toSet()
        val plannedIso2s = countryStates.filter { it.second.planned && !it.second.visited && !it.second.lived }.map { it.first.iso2 }.toSet()
        val wishedIso2s = countryStates.filter { it.second.wished && !it.second.planned && !it.second.visited && !it.second.lived }.map { it.first.iso2 }.toSet()

        val visitedCount = countryStates.count { it.second.visited }
        val worldPercentage = if (countries.isEmpty()) 0f else visitedCount.toFloat() / countries.size.toFloat() * 100f
        val currentLivingMapCenter = allCountryStates.firstOrNull { it.second.currentlyLiving }?.first?.toMapPoint()

        val completedTrips = trips.filter { it.status == TravelStatus.COMPLETED }
        val completedFlights = flights.filter { it.status == TravelStatus.COMPLETED }
        val totalFlightMinutes = completedFlights.sumOf { it.utcAwareDurationMinutes()?.coerceAtLeast(0) ?: 0L }
        val flownDistanceKm = flights.sumOf { it.distanceKm ?: 0.0 }
        val tripDays = completedTrips.mapNotNull { it.dayCount() }
        val tripStopsByTripId = tripStops.groupBy { it.tripId }
        // Route lines and main markers follow the main route only; counts include the
        // places visited from those stops.
        val mainStopsByTripId = tripStops.filter { it.parentStopId == null }.groupBy { it.tripId }
        val nestedStops = tripStops.filter { it.parentStopId != null }
        val tripPhotoCountByTripId = buildTripPhotoCountByTripId(trips, tripStopsByTripId, photos)
        val tripCountryCountByTripId = buildTripCountryCountByTripId(trips, tripStopsByTripId)

        val topCountryRanks = buildCountryRanks(
            countries = countries,
            countryStates = countryStates,
            logs = data.countryData.logs,
            tripStops = tripStops,
            flights = flights,
            itineraryGroups = standaloneItineraryGroups,
            airportsById = airportsById,
        )
        val continentStats = buildContinentStats(countries, countryStates)
        val topRoutes = buildTopRoutes(flights, airportsById)
        val topAirports = buildTopAirports(flights, airportsById)
        val topAirlines = buildTopAirlines(flights)
        val topAircraft = resolveTopAircraft(flights)
        val yearStats = buildYearStats(trips, flights, tripStops, airportsById, itineraryGroups)
        val tripMonthStats = buildTripMonthStats(trips)
        val flightMapRoutes = buildFlightMapRoutes(flights, airportsById)
        val tripMapRoutes = buildTripMapRoutes(trips, mainStopsByTripId)
        val tripStopMapMarkers = buildTripStopMarkers(trips, mainStopsByTripId, countryNamesByIso2)
        val excursionStopMapMarkers = buildNestedStopMarkers(nestedStops, countryNamesByIso2)
        val delayBuckets = buildDelayBuckets(flights)
        val topDelays = buildTopDelayRecords(flights, airportsById)
        val topDelayStats = buildTopDelayStats(flights, airportsById)
        val intercontinentalFlights = flights.count { it.isIntercontinental(airportsById, countriesByIso2) }
        val continentalFlights = flights.count { it.hasKnownContinentalPair(airportsById, countriesByIso2) } - intercontinentalFlights

        val moonLoops = flownDistanceKm / MOON_DISTANCE_KM
        val nightFlightCount = flights.count { f ->
            val depStr = f.scheduledDepartureAt ?: return@count false
            if (depStr.length < 13) return@count false
            val hour = depStr.substring(11, 13).toIntOrNull() ?: return@count false
            hour < 6 || hour >= 22
        }
        val flightsWithKnownDep = flights.count { it.scheduledDepartureAt?.length?.let { l -> l >= 13 } == true }
        val dayFlightCount = flightsWithKnownDep - nightFlightCount
        val haulDistances = flights.mapNotNull { it.distanceKm }
        val shortHaulCount = haulDistances.count { it < 1_500.0 }
        val mediumHaulCount = haulDistances.count { it in 1_500.0..4_000.0 }
        val longHaulCount = haulDistances.count { it > 4_000.0 }

        val tripVisuals = trips
            .filter { it.coverPhotoFilename != null || tripStopsByTripId[it.id].orEmpty().any { stop -> stop.latitude != null && stop.longitude != null } }
            .sortedWith(
                compareByDescending<Trip> { it.coverPhotoFilename != null }
                    .thenByDescending { tripPhotoCountByTripId[it.id] ?: 0 }
                    .thenByDescending { it.dateRange?.start?.year ?: it.dateRange?.end?.year ?: 0 },
            )
            .take(12)
            .map { trip ->
                trip.toStatsTripVisual(
                    stops = tripStopsByTripId[trip.id].orEmpty(),
                    countryNamesByIso2 = countryNamesByIso2,
                    photoCount = tripPhotoCountByTripId[trip.id] ?: 0,
                )
            }

        val countryRecords = buildCountryRecords(
            topCountryRanks = topCountryRanks,
            continentStats = continentStats,
            logsByCountryIso2 = logsByIso2,
            stopsByCountryIso2 = stopsByIso2,
            countryNamesByIso2 = countryNamesByIso2,
        )
        val tripRecords = buildTripRecords(
            trips = trips,
            completedTrips = completedTrips,
            tripStopsByTripId = tripStopsByTripId,
            tripCountryCountByTripId = tripCountryCountByTripId,
            tripPhotoCountByTripId = tripPhotoCountByTripId,
        )
        val flightRecords = buildFlightRecords(
            flights = flights,
            airportsById = airportsById,
            topRoutes = topRoutes,
            topAirports = topAirports.take(10),
        )
        val geographicRecords = buildGeographicRecords(tripStops)
        val recordCards = (countryRecords + tripRecords + flightRecords + geographicRecords).distinctBy { it.title to it.detail }

        return StatsUiState(
            totalCountries = countries.size,
            visitedCountries = visitedCount,
            livedCountries = countryStates.count { it.second.lived },
            currentlyLivingCountries = countryStates.count { it.second.currentlyLiving },
            plannedCountries = countryStates.count { it.second.planned },
            wishedCountries = countryStates.count { it.second.wished },
            visitedContinents = countryStates.filter { it.second.visited || it.second.lived }.map { it.first.continent }.distinct().size,
            worldPercentage = worldPercentage,
            completionTier = buildCompletionTier(worldPercentage),
            currentLivingMapCenter = currentLivingMapCenter,
            livingIso2s = livingIso2s,
            livedIso2s = livedIso2s,
            visitedIso2s = visitedIso2s,
            plannedIso2s = plannedIso2s,
            wishedIso2s = wishedIso2s,
            countryNamesByIso2 = countryNamesByIso2,
            travelIdentity = buildTravelIdentity(
                visitedCountries = visitedCount,
                tripCount = trips.size,
                flightCount = flights.size,
                stopCount = tripStops.size,
                uniqueAirportCount = topAirports.size,
                wishedCount = wishedIso2s.size,
            ),
            countryRanks = topCountryRanks.take(10),
            countryStamps = countryStates
                .mapNotNull { (country, state) -> country.toStamp(state) }
                .sortedWith(compareBy<StatsCountryStamp> { it.state.priority }.thenBy { it.name })
                .take(90),
            continentStats = continentStats,
            countryRecords = countryRecords,
            tripCount = trips.size,
            completedTripCount = completedTrips.size,
            plannedTripCount = trips.count { it.status == TravelStatus.PLANNED },
            inProgressTripCount = trips.count { it.status == TravelStatus.IN_PROGRESS },
            tripStopCount = tripStops.size,
            excursionCount = nestedStops.size,
            excursionStopCount = nestedStops.size,
            totalStopCount = tripStops.size,
            daysTraveled = tripDays.sum(),
            avgTripLengthDays = tripDays.takeIf { it.isNotEmpty() }?.average(),
            photoCount = photos.values.sumOf { it.size },
            tripVisuals = tripVisuals,
            tripMonthStats = tripMonthStats,
            tripSeasonStats = buildTripSeasonStats(trips),
            tripRecords = tripRecords,
            flightCount = flights.size,
            completedFlightCount = completedFlights.size,
            plannedFlightCount = flights.count { it.status == TravelStatus.PLANNED },
            inProgressFlightCount = flights.count { it.status == TravelStatus.IN_PROGRESS },
            flownDistanceKm = flownDistanceKm,
            hoursFlown = totalFlightMinutes / 60.0,
            earthLoops = flownDistanceKm / EARTH_CIRCUMFERENCE_KM,
            uniqueAirportCount = topAirports.size,
            uniqueAirlineCount = topAirlines.size,
            aircraftTypeCount = topAircraft.size,
            topRoutes = topRoutes.take(10),
            topAirports = topAirports.take(10),
            topAirlines = topAirlines.take(10),
            topAircraft = topAircraft.take(10),
            flightMapRoutes = flightMapRoutes,
            tripMapRoutes = tripMapRoutes,
            tripStopMapMarkers = tripStopMapMarkers,
            excursionStopMapMarkers = excursionStopMapMarkers,
            delayBuckets = delayBuckets,
            topDelays = topDelays,
            topDelayStats = topDelayStats,
            intercontinentalFlightCount = intercontinentalFlights,
            continentalFlightCount = continentalFlights.coerceAtLeast(0),
            yearStats = yearStats,
            flightRecords = flightRecords,
            moonLoops = moonLoops,
            nightFlightCount = nightFlightCount,
            dayFlightCount = dayFlightCount,
            shortHaulCount = shortHaulCount,
            mediumHaulCount = mediumHaulCount,
            longHaulCount = longHaulCount,
            recordCards = recordCards,
            badges = buildBadges(
                visitedCountries = visitedCount,
                visitedContinents = countryStates.filter { it.second.visited || it.second.lived }.map { it.first.continent }.distinct().size,
                uniqueAirports = topAirports.size,
                uniqueAirlineCount = topAirlines.size,
                uniqueRouteCount = topRoutes.size,
                aircraftTypeCount = topAircraft.size,
                flightCount = flights.size,
                intercontinentalFlightCount = intercontinentalFlights,
                earthLoops = flownDistanceKm / EARTH_CIRCUMFERENCE_KM,
                photoCount = photos.values.sumOf { it.size },
                topRoutes = topRoutes,
                completedTrips = completedTrips.size,
                daysTraveled = tripDays.sum(),
                totalStopCount = tripStops.size,
                worldPercentage = worldPercentage,
                yearStats = yearStats,
                nightFlightCount = nightFlightCount,
                hasNightFlight = flights.any { f ->
                    val dep = f.scheduledDepartureAt?.take(10) ?: return@any false
                    val arr = (f.scheduledArrivalAt ?: f.actualArrivalAt)?.take(10) ?: return@any false
                    arr > dep
                },
                hasBigDelay = flights.any { (it.utcAwareDelayMinutes() ?: 0L) > 180L },
                hasItinerary = itineraryGroups.isNotEmpty(),
                hasUltraLongFlight = flights.any { (it.distanceKm ?: 0.0) >= 6000.0 },
                hasEarlyMorningFlight = flights.any { f ->
                    val dep = f.scheduledDepartureAt ?: return@any false
                    if (dep.length < 13) return@any false
                    val hour = dep.substring(11, 13).toIntOrNull() ?: return@any false
                    hour < 5
                },
                hasLongTrip = completedTrips.any { it.dayCount()?.let { d -> d >= 14 } == true },
            ),
        )
    }

    private suspend fun resolveTopAircraft(flights: List<Flight>): List<StatsAircraftRank> {
        val grouped = flights.mapNotNull { flight ->
            val raw = flight.aircraft?.trim()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            raw.normalizedAircraftLabel() to flight
        }.groupBy({ it.first }, { it.second })

        return grouped.map { (raw, rows) ->
            val resolved = aircraftTypeRepository.resolveAircraftType(raw)
            StatsAircraftRank(
                rawValue = raw,
                displayName = resolved?.displayName ?: raw,
                category = resolved?.category?.toCatalanAircraftCategory(),
                count = rows.size,
                distanceKm = rows.sumOf { it.distanceKm ?: 0.0 },
                imageAssetRef = resolved?.imageAssetRef,
            )
        }.groupBy { it.displayName }
            .map { (_, items) ->
                items.reduce { acc, item ->
                    acc.copy(
                        count = acc.count + item.count,
                        distanceKm = acc.distanceKm + item.distanceKm,
                    )
                }
            }
            .sortedWith(compareByDescending<StatsAircraftRank> { it.count }.thenByDescending { it.distanceKm })
    }

    private fun Trip.toStatsTripVisual(
        stops: List<TripStop>,
        countryNamesByIso2: Map<String, String>,
        photoCount: Int,
    ): StatsTripVisual {
        val sortedStops = stops.sortedBy { it.sortOrder }
        val route = when {
            sortedStops.isEmpty() -> null
            sortedStops.first().locationName == sortedStops.last().locationName -> sortedStops.first().locationName
            else -> "${sortedStops.first().locationName} → ${sortedStops.last().locationName}"
        }
        val countries = sortedStops.map { countryNamesByIso2[it.countryIso2] ?: it.countryIso2 }.distinct()
        return StatsTripVisual(
            tripId = id,
            title = title,
            dateText = dateRange?.let(flexibleDateFormatter::format),
            routeText = route ?: countries.joinToString(", ").ifBlank { null },
            stopCount = sortedStops.size,
            photoCount = photoCount,
            coverPhotoFilename = coverPhotoFilename,
            points = sortedStops.mapNotNull { stop ->
                StatsMapPoint(
                    latitude = stop.latitude ?: return@mapNotNull null,
                    longitude = stop.longitude ?: return@mapNotNull null,
                )
            },
        )
    }

    class Factory(
        private val countryRepository: CountryRepository,
        private val tripRepository: TripRepository,
        private val flightRepository: FlightRepository,
        private val itineraryRepository: ItineraryRepository,
        private val countryStatsScopePreferencesRepository: CountryStatsScopePreferencesRepository,
        private val airportRepository: AirportRepository,
        private val stopPhotoRepository: StopPhotoRepository,
        private val aircraftTypeRepository: AircraftTypeRepository,
        private val countryStateDerivationService: CountryStateDerivationService,
        private val flexibleDateFormatter: FlexibleDateFormatter,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StatsViewModel(
                countryRepository = countryRepository,
                tripRepository = tripRepository,
                flightRepository = flightRepository,
                itineraryRepository = itineraryRepository,
                countryStatsScopePreferencesRepository = countryStatsScopePreferencesRepository,
                airportRepository = airportRepository,
                stopPhotoRepository = stopPhotoRepository,
                aircraftTypeRepository = aircraftTypeRepository,
                countryStateDerivationService = countryStateDerivationService,
                flexibleDateFormatter = flexibleDateFormatter,
            ) as T
    }
}

data class StatsUiState(
    val totalCountries: Int = 0,
    val visitedCountries: Int = 0,
    val livedCountries: Int = 0,
    val currentlyLivingCountries: Int = 0,
    val plannedCountries: Int = 0,
    val wishedCountries: Int = 0,
    val visitedContinents: Int = 0,
    val worldPercentage: Float = 0f,
    val completionTier: StatsCompletionTier = StatsCompletionTier("Primer segell", "Comença la col·lecció.", 0f, "10%"),
    val currentLivingMapCenter: StatsMapPoint? = null,
    val livingIso2s: Set<String> = emptySet(),
    val livedIso2s: Set<String> = emptySet(),
    val visitedIso2s: Set<String> = emptySet(),
    val plannedIso2s: Set<String> = emptySet(),
    val wishedIso2s: Set<String> = emptySet(),
    val countryNamesByIso2: Map<String, String> = emptyMap(),
    val travelIdentity: StatsIdentity = StatsIdentity("Atlas en construcció", "Afegeix viatges, països i vols per veure el teu patró."),
    val countryRanks: List<StatsRank> = emptyList(),
    val countryStamps: List<StatsCountryStamp> = emptyList(),
    val continentStats: List<StatsContinent> = emptyList(),
    val countryRecords: List<StatsRecord> = emptyList(),
    val tripCount: Int = 0,
    val completedTripCount: Int = 0,
    val plannedTripCount: Int = 0,
    val inProgressTripCount: Int = 0,
    val tripStopCount: Int = 0,
    val excursionCount: Int = 0,
    val excursionStopCount: Int = 0,
    val totalStopCount: Int = 0,
    val daysTraveled: Int = 0,
    val avgTripLengthDays: Double? = null,
    val photoCount: Int = 0,
    val tripVisuals: List<StatsTripVisual> = emptyList(),
    val tripMonthStats: List<StatsMonthStat> = emptyList(),
    val tripSeasonStats: List<StatsSeasonStat> = emptyList(),
    val tripRecords: List<StatsRecord> = emptyList(),
    val flightCount: Int = 0,
    val completedFlightCount: Int = 0,
    val plannedFlightCount: Int = 0,
    val inProgressFlightCount: Int = 0,
    val flownDistanceKm: Double = 0.0,
    val hoursFlown: Double = 0.0,
    val earthLoops: Double = 0.0,
    val uniqueAirportCount: Int = 0,
    val uniqueAirlineCount: Int = 0,
    val aircraftTypeCount: Int = 0,
    val topRoutes: List<StatsRouteRank> = emptyList(),
    val topAirports: List<StatsAirportRank> = emptyList(),
    val topAirlines: List<StatsAirlineRank> = emptyList(),
    val topAircraft: List<StatsAircraftRank> = emptyList(),
    val flightMapRoutes: List<StatsFlightMapRoute> = emptyList(),
    val tripMapRoutes: List<StatsTripMapRoute> = emptyList(),
    val tripStopMapMarkers: List<StatsMapMarker> = emptyList(),
    val excursionStopMapMarkers: List<StatsMapMarker> = emptyList(),
    val delayBuckets: List<StatsDelayBucket> = emptyList(),
    val topDelays: List<StatsRecord> = emptyList(),
    val intercontinentalFlightCount: Int = 0,
    val continentalFlightCount: Int = 0,
    val yearStats: List<StatsYearStat> = emptyList(),
    val flightRecords: List<StatsRecord> = emptyList(),
    val topDelayStats: List<StatsTopDelay> = emptyList(),
    val moonLoops: Double = 0.0,
    val nightFlightCount: Int = 0,
    val dayFlightCount: Int = 0,
    val shortHaulCount: Int = 0,
    val mediumHaulCount: Int = 0,
    val longHaulCount: Int = 0,
    val recordCards: List<StatsRecord> = emptyList(),
    val badges: List<StatsBadge> = emptyList(),
)

data class StatsIdentity(val title: String, val subtitle: String)
data class StatsCompletionTier(val title: String, val detail: String, val progress: Float, val nextTargetLabel: String)
data class StatsRank(val iso2: String?, val title: String, val subtitle: String, val value: Int, val state: StatsCountryState?)
data class StatsCountryStamp(val iso2: String, val name: String, val flag: String?, val label: String, val state: StatsCountryState)
data class StatsContinent(val name: String, val visited: Int, val planned: Int, val total: Int)
data class StatsMapPoint(val latitude: Double, val longitude: Double)
data class StatsTripVisual(
    val tripId: String,
    val title: String,
    val dateText: String?,
    val routeText: String?,
    val stopCount: Int,
    val photoCount: Int,
    val coverPhotoFilename: String?,
    val points: List<StatsMapPoint>,
)
data class StatsRouteRank(val route: String, val count: Int, val distanceKm: Double)
data class StatsAirportRank(val airportId: String, val code: String, val city: String, val countryIso2: String, val count: Int)
data class StatsAirlineRank(val code: String, val count: Int, val distanceKm: Double)
data class StatsAircraftRank(
    val rawValue: String,
    val displayName: String,
    val category: String?,
    val count: Int,
    val distanceKm: Double,
    val imageAssetRef: String?,
)
data class StatsFlightMapRoute(
    val fromLatitude: Double,
    val fromLongitude: Double,
    val toLatitude: Double,
    val toLongitude: Double,
    val count: Int,
    val isPlanned: Boolean = false,
    val fromCode: String = "",
    val toCode: String = "",
)

data class StatsTripMapRoute(
    val tripId: String,
    val status: TravelStatus,
    val points: List<StatsMapPoint>,
)

data class StatsMapMarker(
    val latitude: Double,
    val longitude: Double,
    val status: TravelStatus,
    val label: String,
    val countryName: String? = null,
)
data class StatsDelayBucket(val label: String, val count: Int)
data class StatsMonthStat(val month: Int, val label: String, val tripCount: Int)
data class StatsYearStat(val year: Int, val tripCount: Int, val flightCount: Int, val countryCount: Int)
data class StatsSeasonStat(val name: String, val emoji: String, val tripCount: Int)
data class StatsRecord(val title: String, val value: String, val detail: String)
data class StatsTopDelay(val route: String, val delayMinutes: Long, val delayLabel: String)
enum class BadgeTier { BRONZE, PLATA, OR, PLATI }

data class StatsBadge(
    val title: String,
    val detail: String,
    val nextGoal: String?,
    val unlocked: Boolean,
    val tier: BadgeTier? = null,
    val progress: Float? = null,
    val completedLevelCount: Int = if (unlocked) 1 else 0,
    val totalLevelCount: Int = 1,
)

enum class StatsCountryState(val priority: Int) {
    Living(0),
    Lived(1),
    Visited(2),
    Planned(3),
    Wished(4),
}

private data class StatsSourceData(
    val countryData: CountryData,
    val tripData: TripData,
    val flightData: FlightData,
    val photos: Map<String, List<StopPhoto>>,
)

private data class CountryData(
    val countries: List<Country>,
    val userStatesByIso2: Map<String, com.atlas.domain.model.CountryUserState>,
    val logs: List<CountryLog>,
)

private data class TripData(val trips: List<Trip>, val stops: List<TripStop>)

private data class FlightData(
    val flights: List<Flight>,
    val itineraryGroups: List<ItineraryGroup>,
    val itineraries: List<Itinerary>,
    val airports: List<Airport>,
)

private const val EARTH_CIRCUMFERENCE_KM = 40_075.0
private const val MOON_DISTANCE_KM = 384_400.0

private fun buildCountryRanks(
    countries: List<Country>,
    countryStates: List<Pair<Country, CountryTrackingState>>,
    logs: List<CountryLog>,
    tripStops: List<TripStop>,
    flights: List<Flight>,
    itineraryGroups: List<ItineraryGroup>,
    airportsById: Map<String, Airport>,
): List<StatsRank> {
    val activity = mutableMapOf<String, Int>()
    // Each log = 1 activity for that country
    logs.forEach { activity[it.countryIso2] = (activity[it.countryIso2] ?: 0) + 1 }
    // Each trip = 1 activity per unique country it visited (multiple stops in same country count once)
    tripStops.groupBy { it.tripId }.forEach { (_, stops) ->
        stops.map { it.countryIso2 }.toSet().forEach { iso2 ->
            activity[iso2] = (activity[iso2] ?: 0) + 1
        }
    }
    // Solo flights (no itinerary group): count both origin and destination
    flights.filter { it.itineraryGroupId == null }.forEach { flight ->
        listOfNotNull(
            airportsById[flight.originAirportId]?.countryIso2,
            airportsById[flight.destinationAirportId]?.countryIso2,
        ).forEach { activity[it] = (activity[it] ?: 0) + 1 }
    }
    // Standalone itinerary group flights (no trip): count only group endpoints, skip layovers
    itineraryGroups.forEach { group ->
        val sorted = group.flights.sortedBy { it.sortOrder ?: Int.MAX_VALUE }
        if (sorted.isNotEmpty()) {
            airportsById[sorted.first().originAirportId]?.countryIso2
                ?.let { activity[it] = (activity[it] ?: 0) + 1 }
            airportsById[sorted.last().destinationAirportId]?.countryIso2
                ?.let { activity[it] = (activity[it] ?: 0) + 1 }
        }
    }
    val stateByIso2 = countryStates.associate { it.first.iso2 to it.second }
    return countries.mapNotNull { country ->
        val value = activity[country.iso2] ?: 0
        if (value == 0) return@mapNotNull null
        val state = stateByIso2[country.iso2]?.toStatsCountryState()
        StatsRank(
            iso2 = country.iso2,
            title = country.nameCa,
            subtitle = state?.toCatalanLabel() ?: country.continent,
            value = value,
            state = state,
        )
    }.sortedWith(compareByDescending<StatsRank> { it.value }.thenBy { it.title })
}

private fun buildContinentStats(
    countries: List<Country>,
    countryStates: List<Pair<Country, CountryTrackingState>>,
): List<StatsContinent> =
    countries.groupBy { it.continent }
        .map { (continent, group) ->
            val groupIso2s = group.map { it.iso2 }.toSet()
            StatsContinent(
                name = continent.toCatalanContinent(),
                visited = countryStates.count { it.first.iso2 in groupIso2s && (it.second.visited || it.second.lived) },
                planned = countryStates.count { it.first.iso2 in groupIso2s && it.second.planned },
                total = group.size,
            )
        }
        .sortedWith(compareByDescending<StatsContinent> { it.visited.toFloat() / it.total.coerceAtLeast(1) }.thenBy { it.name })

private fun buildTripPhotoCountByTripId(
    trips: List<Trip>,
    tripStopsByTripId: Map<String, List<TripStop>>,
    photos: Map<String, List<StopPhoto>>,
): Map<String, Int> = trips.associate { trip ->
    trip.id to tripStopsByTripId[trip.id].orEmpty().sumOf { stop ->
        photos[stop.id].orEmpty().size
    }
}

private fun buildTripCountryCountByTripId(
    trips: List<Trip>,
    tripStopsByTripId: Map<String, List<TripStop>>,
): Map<String, Int> = trips.associate { trip ->
    trip.id to tripStopsByTripId[trip.id].orEmpty().mapTo(mutableSetOf()) { it.countryIso2 }.size
}

private fun buildTopRoutes(flights: List<Flight>, airportsById: Map<String, Airport>): List<StatsRouteRank> =
    flights.groupBy { flight ->
        val origin = airportsById[flight.originAirportId]?.shortCode() ?: flight.originAirportId
        val destination = airportsById[flight.destinationAirportId]?.shortCode() ?: flight.destinationAirportId
        "$origin → $destination"
    }.map { (route, rows) ->
        StatsRouteRank(route = route, count = rows.size, distanceKm = rows.sumOf { it.distanceKm ?: 0.0 })
    }.sortedWith(compareByDescending<StatsRouteRank> { it.count }.thenByDescending { it.distanceKm })

private fun buildTopAirports(flights: List<Flight>, airportsById: Map<String, Airport>): List<StatsAirportRank> {
    val counts = mutableMapOf<String, Int>()
    flights.forEach { flight ->
        counts[flight.originAirportId] = (counts[flight.originAirportId] ?: 0) + 1
        counts[flight.destinationAirportId] = (counts[flight.destinationAirportId] ?: 0) + 1
    }
    return counts.mapNotNull { (airportId, count) ->
        val airport = airportsById[airportId] ?: return@mapNotNull null
        StatsAirportRank(airportId = airportId, code = airport.shortCode(), city = airport.city, countryIso2 = airport.countryIso2, count = count)
    }.sortedWith(compareByDescending<StatsAirportRank> { it.count }.thenBy { it.code })
}

private fun buildTopAirlines(flights: List<Flight>): List<StatsAirlineRank> =
    flights.mapNotNull { flight ->
        val code = flight.airline?.trim()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        code.uppercase() to flight
    }.groupBy({ it.first }, { it.second })
        .map { (code, rows) ->
            StatsAirlineRank(code = code, count = rows.size, distanceKm = rows.sumOf { it.distanceKm ?: 0.0 })
        }
        .sortedWith(compareByDescending<StatsAirlineRank> { it.count }.thenByDescending { it.distanceKm })

private fun buildFlightMapRoutes(flights: List<Flight>, airportsById: Map<String, Airport>): List<StatsFlightMapRoute> {
    val isPlanned = { f: Flight -> f.status == TravelStatus.PLANNED }
    return flights
        .groupBy { Triple(it.originAirportId, it.destinationAirportId, isPlanned(it)) }
        .mapNotNull { (triple, rows) ->
            val origin = airportsById[triple.first] ?: return@mapNotNull null
            val destination = airportsById[triple.second] ?: return@mapNotNull null
            StatsFlightMapRoute(
                fromLatitude = origin.latitude,
                fromLongitude = origin.longitude,
                toLatitude = destination.latitude,
                toLongitude = destination.longitude,
                count = rows.size,
                isPlanned = triple.third,
                fromCode = origin.shortCode(),
                toCode = destination.shortCode(),
            )
        }
        .sortedByDescending { it.count }
        .take(120)
}

private fun buildTripMapRoutes(trips: List<Trip>, tripStopsByTripId: Map<String, List<TripStop>>): List<StatsTripMapRoute> =
    trips.mapNotNull { trip ->
        val points = tripStopsByTripId[trip.id]
            .orEmpty()
            .sortedBy { it.sortOrder }
            .mapNotNull { stop ->
                val lat = stop.latitude ?: return@mapNotNull null
                val lng = stop.longitude ?: return@mapNotNull null
                StatsMapPoint(latitude = lat, longitude = lng)
            }
        if (points.size < 2) {
            null
        } else {
            StatsTripMapRoute(
                tripId = trip.id,
                status = trip.status,
                points = points,
            )
        }
    }

private fun buildTripStopMarkers(
    trips: List<Trip>,
    tripStopsByTripId: Map<String, List<TripStop>>,
    countryNamesByIso2: Map<String, String>,
): List<StatsMapMarker> =
    trips.flatMap { trip ->
        tripStopsByTripId[trip.id].orEmpty().mapNotNull { stop ->
            val lat = stop.latitude ?: return@mapNotNull null
            val lng = stop.longitude ?: return@mapNotNull null
            StatsMapMarker(
                latitude = lat,
                longitude = lng,
                status = trip.status,
                label = stop.locationName.takeIf { it.isNotBlank() } ?: "Parada",
                countryName = countryNamesByIso2[stop.countryIso2],
            )
        }
    }

private fun buildNestedStopMarkers(
    nestedStops: List<TripStop>,
    countryNamesByIso2: Map<String, String>,
): List<StatsMapMarker> =
    nestedStops.mapNotNull { stop ->
        val lat = stop.latitude ?: return@mapNotNull null
        val lng = stop.longitude ?: return@mapNotNull null
        StatsMapMarker(
            latitude = lat,
            longitude = lng,
            status = TravelStatus.COMPLETED,
            countryName = countryNamesByIso2[stop.countryIso2],
            label = stop.locationName.takeIf { it.isNotBlank() } ?: "Sortida",
        )
    }

private fun buildYearStats(
    trips: List<Trip>,
    flights: List<Flight>,
    tripStops: List<TripStop>,
    airportsById: Map<String, Airport>,
    itineraryGroups: List<  ItineraryGroup>,
): List<StatsYearStat> {
    val tripYearById = trips.associate { trip -> trip.id to trip.dateRange?.primaryYear() }
    val tripCounts = trips.mapNotNull { it.dateRange?.primaryYear() }.groupingBy { it }.eachCount()
    val flightCounts = flights.mapNotNull { it.primaryYear() }.groupingBy { it }.eachCount()
    val countriesByYear = mutableMapOf<Int, MutableSet<String>>()

    // Trip stops: prefer the stop's own date if set, fall back to trip year.
    tripStops.forEach { stop ->
        val year = stop.dateRange?.primaryYear() ?: tripYearById[stop.tripId] ?: return@forEach
        countriesByYear.getOrPut(year) { mutableSetOf() }.add(stop.countryIso2)
    }

    // Standalone flights: only count the destination country (not origin, no layovers).
    flights.filter { it.itineraryGroupId == null }.forEach { flight ->
        val year = flight.primaryYear() ?: return@forEach
        airportsById[flight.destinationAirportId]?.countryIso2
            ?.let { countriesByYear.getOrPut(year) { mutableSetOf() }.add(it) }
    }

    // Itinerary groups: only count the final destination of each group (skips layover airports).
    itineraryGroups.forEach { group ->
        val lastFlight = group.flights.maxByOrNull { it.sortOrder ?: Int.MAX_VALUE } ?: return@forEach
        val year = lastFlight.primaryYear() ?: return@forEach
        airportsById[lastFlight.destinationAirportId]?.countryIso2
            ?.let { countriesByYear.getOrPut(year) { mutableSetOf() }.add(it) }
    }

    return (tripCounts.keys + flightCounts.keys + countriesByYear.keys)
        .sorted()
        .map { year ->
            StatsYearStat(
                year = year,
                tripCount = tripCounts[year] ?: 0,
                flightCount = flightCounts[year] ?: 0,
                countryCount = countriesByYear[year]?.size ?: 0,
            )
        }
}

private fun buildTripMonthStats(trips: List<Trip>): List<StatsMonthStat> {
    val counts = trips.mapNotNull { it.dateRange?.primaryMonth() }.groupingBy { it }.eachCount()
    return (1..12).map { month ->
        StatsMonthStat(month = month, label = month.toCatalanShortMonth(), tripCount = counts[month] ?: 0)
    }
}

private fun buildTripSeasonStats(trips: List<Trip>): List<StatsSeasonStat> {
    val counts = IntArray(4) // 0=Primavera, 1=Estiu, 2=Tardor, 3=Hivern
    trips.forEach { trip ->
        val month = trip.dateRange?.primaryMonth() ?: return@forEach
        val season = when (month) {
            in 3..5 -> 0
            in 6..8 -> 1
            in 9..11 -> 2
            else -> 3  // 12, 1, 2
        }
        counts[season]++
    }
    return listOf(
        StatsSeasonStat("Primavera", "🌸", counts[0]),
        StatsSeasonStat("Estiu", "☀️", counts[1]),
        StatsSeasonStat("Tardor", "🍂", counts[2]),
        StatsSeasonStat("Hivern", "❄️", counts[3]),
    )
}

private fun buildDelayBuckets(flights: List<Flight>): List<StatsDelayBucket> {
    val delays = flights.mapNotNull { it.utcAwareDelayMinutes() }
    return listOf(
        StatsDelayBucket("Abans", delays.count { it < 0 }),
        StatsDelayBucket("Puntual", delays.count { it == 0L }),
        StatsDelayBucket("0-15", delays.count { it in 1..15 }),
        StatsDelayBucket("16-30", delays.count { it in 16..30 }),
        StatsDelayBucket("31-60", delays.count { it in 31..60 }),
        StatsDelayBucket("60+", delays.count { it > 60 }),
    )
}

private fun buildTopDelayRecords(flights: List<Flight>, airportsById: Map<String, Airport>): List<StatsRecord> =
    flights.mapNotNull { flight ->
        val delay = flight.utcAwareDelayMinutes()?.takeIf { it > 0 } ?: return@mapNotNull null
        val origin = airportsById[flight.originAirportId]?.shortCode() ?: flight.originAirportId
        val destination = airportsById[flight.destinationAirportId]?.shortCode() ?: flight.destinationAirportId
        StatsRecord(
            title = "Retard",
            value = delay.toDurationLabel(),
            detail = "$origin → $destination",
        ) to delay
    }.sortedByDescending { it.second }.take(5).map { it.first }

private fun buildTopDelayStats(flights: List<Flight>, airportsById: Map<String, Airport>): List<StatsTopDelay> =
    flights.mapNotNull { flight ->
        val delay = flight.utcAwareDelayMinutes()?.takeIf { it > 0 } ?: return@mapNotNull null
        val origin = airportsById[flight.originAirportId]?.shortCode() ?: flight.originAirportId
        val destination = airportsById[flight.destinationAirportId]?.shortCode() ?: flight.destinationAirportId
        StatsTopDelay("$origin → $destination", delay, delay.toDurationLabel())
    }.sortedByDescending { it.delayMinutes }.take(5)

private fun buildCountryRecords(
    topCountryRanks: List<StatsRank>,
    continentStats: List<StatsContinent>,
    logsByCountryIso2: Map<String, List<CountryLog>>,
    stopsByCountryIso2: Map<String, List<TripStop>>,
    countryNamesByIso2: Map<String, String>,
): List<StatsRecord> {
    val mostLogged = logsByCountryIso2.entries.filter { it.value.size >= 2 }.maxByOrNull { it.value.size }
    val mostStops = stopsByCountryIso2.entries.filter { it.value.size >= 2 }.maxByOrNull { it.value.size }
    return listOfNotNull(
        topCountryRanks.firstOrNull()?.let { StatsRecord("País més actiu", it.title, "${it.value} registres") },
        continentStats.firstOrNull()?.let {
            StatsRecord("Continent més explorat", it.name, "${it.visited}/${it.total} · ${it.percentLabel()}")
        },
        continentStats.lastOrNull()?.takeIf { it.visited == 0 }?.let {
            StatsRecord("Continent per descobrir", it.name, "0 de ${it.total} països")
        },
        continentStats.firstOrNull { it.visited == it.total && it.total > 0 }?.let {
            StatsRecord("Continent complet", it.name, "${it.total} de ${it.total} països visitats")
        },
        mostLogged?.let { (iso2, logs) ->
            StatsRecord("País més viscut", countryNamesByIso2[iso2] ?: iso2, "${logs.size} estades registrades")
        },
        mostStops?.let { (iso2, stops) ->
            StatsRecord("País amb més parades", countryNamesByIso2[iso2] ?: iso2, "${stops.size} parades de viatge")
        },
    )
}

private fun buildTripRecords(
    trips: List<Trip>,
    completedTrips: List<Trip>,
    tripStopsByTripId: Map<String, List<TripStop>>,
    tripCountryCountByTripId: Map<String, Int>,
    tripPhotoCountByTripId: Map<String, Int>,
): List<StatsRecord> {
    val durationRecords = completedTrips.mapNotNull { trip -> trip.dayCount()?.let { trip to it } }
    return listOfNotNull(
        durationRecords.maxByOrNull { it.second }?.let { (trip, days) ->
            StatsRecord("Viatge més llarg", "$days ${if (days == 1) "dia" else "dies"}", trip.title)
        },
        durationRecords.minByOrNull { it.second }?.let { (trip, days) ->
            StatsRecord("Viatge més curt", "$days ${if (days == 1) "dia" else "dies"}", trip.title)
        },
        trips.maxByOrNull { tripStopsByTripId[it.id].orEmpty().size }?.let { trip ->
            val count = tripStopsByTripId[trip.id].orEmpty().size
            if (count > 0) StatsRecord("Més parades", count.toString(), trip.title) else null
        },
        trips.maxByOrNull { tripCountryCountByTripId[it.id] ?: 0 }?.let { trip ->
            val count = tripCountryCountByTripId[trip.id] ?: 0
            if (count > 0) StatsRecord("Més països en un viatge", count.toString(), trip.title) else null
        },
        trips.maxByOrNull { tripPhotoCountByTripId[it.id] ?: 0 }?.let { trip ->
            val count = tripPhotoCountByTripId[trip.id] ?: 0
            if (count > 0) StatsRecord("Viatge més fotografiat", count.toString(), trip.title) else null
        },
        trips.filter { it.dateRange?.start?.year != null }
            .minByOrNull { it.dateRange!!.start!!.year }
            ?.let { trip -> StatsRecord("Primer viatge", trip.dateRange!!.start!!.year.toString(), trip.title) },
    )
}

private fun buildFlightRecords(
    flights: List<Flight>,
    airportsById: Map<String, Airport>,
    topRoutes: List<StatsRouteRank>,
    topAirports: List<StatsAirportRank>,
): List<StatsRecord> {
    val byDuration = flights.mapNotNull { flight -> flight.utcAwareDurationMinutes()?.takeIf { it > 0 }?.let { flight to it } }
    val byDistance = flights.mapNotNull { flight -> flight.distanceKm?.takeIf { it > 0 }?.let { flight to it } }
    return listOfNotNull(
        byDuration.maxByOrNull { it.second }?.let { (flight, minutes) ->
            StatsRecord("Vol més llarg per temps", minutes.toDurationLabel(), flight.routeLabel(airportsById))
        },
        byDuration.minByOrNull { it.second }?.let { (flight, minutes) ->
            StatsRecord("Vol més curt per temps", minutes.toDurationLabel(), flight.routeLabel(airportsById))
        },
        byDistance.maxByOrNull { it.second }?.let { (flight, km) ->
            StatsRecord("Vol més llarg per km", "${km.roundToInt()} km", flight.routeLabel(airportsById))
        },
        byDistance.minByOrNull { it.second }?.let { (flight, km) ->
            StatsRecord("Vol més curt per km", "${km.roundToInt()} km", flight.routeLabel(airportsById))
        },
        topRoutes.firstOrNull()?.takeIf { it.count >= 2 }?.let { StatsRecord("Ruta preferida", "${it.count}×", it.route) },
        topAirports.firstOrNull()?.let { StatsRecord("Aeroport principal", it.code, it.city) },
    )
}

private fun buildGeographicRecords(tripStops: List<TripStop>): List<StatsRecord> {
    val stops = tripStops.map { it.locationName to (it.latitude to it.longitude) }
    val withLat = stops.mapNotNull { (name, coords) -> coords.first?.let { Triple(name, it, coords.second) } }
    val withLng = stops.mapNotNull { (name, coords) -> coords.second?.let { Triple(name, coords.first, it) } }
    return listOfNotNull(
        withLat.maxByOrNull { it.second }?.let { StatsRecord("Parada més al nord", "${it.second.roundToInt()}°", it.first) },
        withLat.minByOrNull { it.second }?.let { StatsRecord("Parada més al sud", "${it.second.roundToInt()}°", it.first) },
        withLng.maxByOrNull { it.third }?.let { StatsRecord("Parada més a l'est", "${it.third.roundToInt()}°", it.first) },
        withLng.minByOrNull { it.third }?.let { StatsRecord("Parada més a l'oest", "${it.third.roundToInt()}°", it.first) },
    )
}

private fun buildTravelIdentity(
    visitedCountries: Int,
    tripCount: Int,
    flightCount: Int,
    stopCount: Int,
    uniqueAirportCount: Int,
    wishedCount: Int,
): StatsIdentity = when {
    flightCount >= tripCount.coerceAtLeast(1) * 2 && uniqueAirportCount >= 10 ->
        StatsIdentity("Connector d'aeroports", "El teu atlas dibuixa més rutes aèries que fronteres.")
    stopCount >= visitedCountries.coerceAtLeast(1) * 3 ->
        StatsIdentity("Col·leccionista de parades", "T'agrada omplir cada país amb llocs concrets.")
    wishedCount > visitedCountries ->
        StatsIdentity("Cartògraf de futurs", "La llista de desitjos ja marca la pròxima aventura.")
    visitedCountries >= 25 ->
        StatsIdentity("Explorador global", "El mapa comença a tenir una veu pròpia.")
    else ->
        StatsIdentity("Atlas en moviment", "Cada registre fa créixer una mica més el teu món.")
}

private fun buildCompletionTier(worldPercentage: Float): StatsCompletionTier = when {
    worldPercentage >= 75f -> StatsCompletionTier("Llegenda mundial", "Tres quartes parts del mapa ja tenen història.", 1f, "100%")
    worldPercentage >= 50f -> StatsCompletionTier("Mig món", "El teu atlas ja pesa com una vida sencera.", worldPercentage / 75f, "75%")
    worldPercentage >= 25f -> StatsCompletionTier("Mapa encès", "Una quarta part del món ja és teva.", worldPercentage / 50f, "50%")
    worldPercentage >= 10f -> StatsCompletionTier("Primer gran cercle", "Ja no és una col·lecció petita.", worldPercentage / 25f, "25%")
    else -> StatsCompletionTier("Primer segell", "El mapa encara té molt espai per sorprendre.", worldPercentage / 10f, "10%")
}

private fun tieredBadge(
    title: String,
    value: Int,
    thresholds: List<Int>,
    detailText: String,
    nextGoalText: (Int) -> String,
): StatsBadge {
    val tierIndex = thresholds.indexOfLast { value >= it }
    val currentTier = if (tierIndex >= 0) BadgeTier.entries[tierIndex] else null
    val nextThreshold = thresholds.getOrNull(tierIndex + 1)
    val prevThreshold = if (tierIndex >= 0) thresholds[tierIndex] else 0
    val progress = when {
        nextThreshold != null -> ((value - prevThreshold).toFloat() / (nextThreshold - prevThreshold)).coerceIn(0f, 1f)
        currentTier != null -> 1f
        else -> (value.toFloat() / thresholds.first()).coerceIn(0f, 1f)
    }
    return StatsBadge(
        title = title,
        detail = detailText,
        nextGoal = nextThreshold?.let(nextGoalText),
        unlocked = currentTier != null,
        tier = currentTier,
        progress = progress,
        completedLevelCount = (tierIndex + 1).coerceAtLeast(0),
        totalLevelCount = thresholds.size.coerceAtLeast(1),
    )
}

private fun binaryBadge(title: String, unlocked: Boolean, goal: String): StatsBadge =
    StatsBadge(
        title = title,
        detail = if (unlocked) "Desbloquejat" else goal,
        nextGoal = null,
        unlocked = unlocked,
        tier = null,
        progress = if (unlocked) 1f else 0f,
    )

private fun buildBadges(
    visitedCountries: Int,
    visitedContinents: Int,
    uniqueAirports: Int,
    uniqueAirlineCount: Int,
    uniqueRouteCount: Int,
    aircraftTypeCount: Int,
    flightCount: Int,
    intercontinentalFlightCount: Int,
    earthLoops: Double,
    photoCount: Int,
    topRoutes: List<StatsRouteRank>,
    completedTrips: Int,
    daysTraveled: Int,
    totalStopCount: Int,
    worldPercentage: Float,
    yearStats: List<StatsYearStat>,
    nightFlightCount: Int,
    hasNightFlight: Boolean,
    hasBigDelay: Boolean,
    hasItinerary: Boolean,
    hasUltraLongFlight: Boolean,
    hasEarlyMorningFlight: Boolean,
    hasLongTrip: Boolean,
): List<StatsBadge> {
    val earthLoopsInt = earthLoops.toInt()
    val earthLoopsDetail = if (earthLoops < 1.0) "${(earthLoops * EARTH_CIRCUMFERENCE_KM).roundToInt()} km" else "${"%.1f".format(earthLoops)}× la Terra"
    val topRouteCount = topRoutes.firstOrNull()?.count ?: 0
    return listOf(
        // Tiered
        tieredBadge("Països visitats", visitedCountries, listOf(10, 25, 50, 100), "$visitedCountries països") { "$it països" },
        tieredBadge("Continents explorats", visitedContinents, listOf(2, 4, 6, 7), "$visitedContinents continents") { "$it continents" },
        tieredBadge("Vols registrats", flightCount, listOf(10, 50, 100, 200), "$flightCount vols") { "$it vols" },
        tieredBadge("Aeroports", uniqueAirports, listOf(10, 25, 50, 100), "$uniqueAirports aeroports") { "$it aeroports" },
        tieredBadge("Quilòmetres volats", earthLoopsInt, listOf(1, 3, 5, 10), earthLoopsDetail) { "${it}× la Terra" },
        tieredBadge("Arxiu fotogràfic", photoCount, listOf(25, 100, 250, 500), "$photoCount fotos") { "$it fotos" },
        tieredBadge("Viatger", completedTrips, listOf(5, 15, 30, 50), "$completedTrips viatges completats") { "$it viatges" },
        tieredBadge("Ruta fidel", topRouteCount, listOf(2, 3, 5, 10), if (topRouteCount > 0) "${topRouteCount}× la mateixa ruta" else "Cap ruta repetida") { "${it}× la mateixa ruta" },
        tieredBadge("Dies de viatge", daysTraveled, listOf(30, 100, 250, 500), "$daysTraveled dies") { "$it dies" },
        tieredBadge("Parades registrades", totalStopCount, listOf(10, 50, 100, 250), "$totalStopCount parades") { "$it parades" },
        tieredBadge("Companyies aèries", uniqueAirlineCount, listOf(5, 10, 20, 40), "$uniqueAirlineCount companyies") { "$it companyies" },
        tieredBadge("Models d'aeronau", aircraftTypeCount, listOf(5, 10, 20, 35), "$aircraftTypeCount models") { "$it models" },
        tieredBadge("Vols nocturns", nightFlightCount, listOf(1, 5, 10, 20), "$nightFlightCount vols nocturns") { "$it vols nocturns" },
        tieredBadge("Rutes úniques", uniqueRouteCount, listOf(5, 15, 30, 50), "$uniqueRouteCount rutes") { "$it rutes" },
        // Binary
        binaryBadge("Primer país", visitedCountries >= 1, "Visita el primer territori"),
        binaryBadge("Primer vol", flightCount >= 1, "Registra el primer vol"),
        binaryBadge("Primer viatge complet", completedTrips >= 1, "Completa el primer viatge"),
        binaryBadge("Vol intercontinental", intercontinentalFlightCount >= 1, "Un vol entre continents"),
        binaryBadge("Mig món", worldPercentage >= 50f, "Visita el 50% del món"),
        binaryBadge("Noctàmbul", hasNightFlight, "Un vol que aterra l'endemà"),
        binaryBadge("Gran retard", hasBigDelay, "Un retard superior a 3 hores"),
        binaryBadge("Any de vols", yearStats.any { it.flightCount >= 12 }, "12 vols en un any natural"),
        binaryBadge("Explorador d'itineraris", hasItinerary, "Crea el primer itinerari"),
        binaryBadge("Volta al món", earthLoops >= 1.0, "Vola la circumferència completa de la Terra"),
        binaryBadge("Tots els continents", visitedContinents >= 7, "Posa el peu a tots els continents"),
        binaryBadge("Ultrallarg", hasUltraLongFlight, "Un vol de més de 6.000 km"),
        binaryBadge("Matiner", hasEarlyMorningFlight, "Un vol que surt entre les 00h i les 05h"),
        binaryBadge("Grand Tour", hasLongTrip, "Un viatge de 14 dies o més"),
    )
}

private fun Trip.dayCount(): Int? {
    val start = dateRange?.start?.toLocalDateOrNull() ?: return null
    val end = dateRange.end?.toLocalDateOrNull() ?: start
    return ChronoUnit.DAYS.between(start, end).coerceAtLeast(0).toInt() + 1
}

private fun FlexibleDateRange.primaryYear(): Int? = start?.year ?: end?.year
private fun FlexibleDateRange.primaryMonth(): Int? = start?.month ?: end?.month

private fun FlexibleDate.toLocalDateOrNull(): LocalDate? {
    val month = month ?: return null
    val day = day ?: return null
    return runCatching { LocalDate.of(year, month, day) }.getOrNull()
}

private fun Flight.primaryYear(): Int? =
    (actualDepartureAt ?: scheduledDepartureAt ?: actualArrivalAt ?: scheduledArrivalAt)
        ?.take(4)
        ?.toIntOrNull()

private fun Flight.routeLabel(airportsById: Map<String, Airport>): String {
    val origin = airportsById[originAirportId]?.shortCode() ?: originAirportId
    val destination = airportsById[destinationAirportId]?.shortCode() ?: destinationAirportId
    return "$origin → $destination"
}

private fun Flight.isIntercontinental(airportsById: Map<String, Airport>, countriesByIso2: Map<String, Country>): Boolean {
    val originContinent = airportsById[originAirportId]?.countryIso2?.let { countriesByIso2[it]?.continent }
    val destinationContinent = airportsById[destinationAirportId]?.countryIso2?.let { countriesByIso2[it]?.continent }
    return originContinent != null && destinationContinent != null && originContinent != destinationContinent
}

private fun Flight.hasKnownContinentalPair(airportsById: Map<String, Airport>, countriesByIso2: Map<String, Country>): Boolean {
    val originContinent = airportsById[originAirportId]?.countryIso2?.let { countriesByIso2[it]?.continent }
    val destinationContinent = airportsById[destinationAirportId]?.countryIso2?.let { countriesByIso2[it]?.continent }
    return originContinent != null && destinationContinent != null
}

private fun Airport.shortCode(): String = iata ?: icao ?: city

private fun Country.toMapPoint(): StatsMapPoint? {
    val lat = latitude ?: capitalLatitude ?: return null
    val lng = longitude ?: capitalLongitude ?: return null
    return StatsMapPoint(latitude = lat, longitude = lng)
}

private fun Country.toStamp(state: CountryTrackingState): StatsCountryStamp? {
    val statsState = state.toStatsCountryState() ?: return null
    return StatsCountryStamp(
        iso2 = iso2,
        name = nameCa,
        flag = flagEmoji?.takeIf { it.isNotBlank() },
        label = statsState.toCatalanLabel(),
        state = statsState,
    )
}

private fun CountryTrackingState.toStatsCountryState(): StatsCountryState? = when {
    currentlyLiving -> StatsCountryState.Living
    lived -> StatsCountryState.Lived
    visited -> StatsCountryState.Visited
    planned -> StatsCountryState.Planned
    wished -> StatsCountryState.Wished
    else -> null
}

private fun StatsCountryState.toCatalanLabel(): String = when (this) {
    StatsCountryState.Living -> "Vivint-hi"
    StatsCountryState.Lived -> "Viscut"
    StatsCountryState.Visited -> "Visitat"
    StatsCountryState.Planned -> "Pla"
    StatsCountryState.Wished -> "Desig"
}

private fun StatsContinent.percentLabel(): String =
    "${(visited.toFloat() / total.coerceAtLeast(1) * 100f).roundToInt()}%"

private fun Long.toDurationLabel(): String {
    val hours = this / 60
    val minutes = this % 60
    return if (hours > 0 && minutes > 0) "${hours} h ${minutes} min" else if (hours > 0) "${hours} h" else "${minutes} min"
}

private fun Double.toDurationLabel(): String = roundToInt().toLong().toDurationLabel()

private fun String.normalizedAircraftLabel(): String =
    trim().replace(Regex("\\s+"), " ").uppercase()

private fun String.toCatalanAircraftCategory(): String = when (uppercase()) {
    "WIDEBODY" -> "Fuselatge ample"
    "NARROWBODY" -> "Fuselatge estret"
    "REGIONAL" -> "Regional"
    "TURBOPROP" -> "Turboprop"
    else -> lowercase().replaceFirstChar { it.uppercase() }
}

private fun String.toCatalanContinent(): String = when (this) {
    "Europe" -> "Europa"
    "Asia" -> "Àsia"
    "North America" -> "Amèrica del Nord"
    "South America" -> "Amèrica del Sud"
    "Africa" -> "Àfrica"
    "Oceania" -> "Oceania"
    "Antarctica" -> "Antàrtida"
    else -> this
}

private fun Int.toCatalanShortMonth(): String = when (this) {
    1 -> "Gen."
    2 -> "Febr."
    3 -> "Març"
    4 -> "Abr."
    5 -> "Maig"
    6 -> "Juny"
    7 -> "Jul."
    8 -> "Ag."
    9 -> "Set."
    10 -> "Oct."
    11 -> "Nov."
    12 -> "Des."
    else -> ""
}
