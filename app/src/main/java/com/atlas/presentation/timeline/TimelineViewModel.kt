package com.atlas.presentation.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atlas.domain.model.CountryLogType
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.repository.AirportRepository
import com.atlas.domain.repository.CountryRepository
import com.atlas.domain.repository.ItineraryRepository
import com.atlas.domain.repository.TripRepository
import com.atlas.domain.service.FlexibleDateFormatter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class TimelineUiState(
    val years: List<TimelineYear> = emptyList(),
    val isLoading: Boolean = true,
)

data class TimelineYear(
    val year: String,
    val items: List<TimelineItem>,
)

sealed class TimelineItem {
    abstract val sortKey: String

    data class TripItem(
        val id: String,
        val title: String,
        val status: TravelStatus,
        val dateLabel: String,
        val coverPhotoFilename: String?,
        override val sortKey: String,
    ) : TimelineItem()

    data class ItineraryItem(
        val id: String,
        val codeLabel: String,   // IATA codes: "BCN → DOH → NRT"
        val cityLabel: String,   // City names: "Barcelona → Doha → Tòquio" (empty if same as codeLabel)
        val dateLabel: String,
        override val sortKey: String,
    ) : TimelineItem()

    data class LogItem(
        val logId: String,
        val countryIso2: String,
        val countryName: String,
        val flagEmoji: String?,
        val logType: CountryLogType,
        val dateLabel: String,
        override val sortKey: String,
    ) : TimelineItem()
}

class TimelineViewModel(
    tripRepository: TripRepository,
    itineraryRepository: ItineraryRepository,
    countryRepository: CountryRepository,
    airportRepository: AirportRepository,
    private val flexibleDateFormatter: FlexibleDateFormatter,
) : ViewModel() {

    val uiState: StateFlow<TimelineUiState> = combine(
        tripRepository.observeTrips(),
        itineraryRepository.observeItineraries(),
        itineraryRepository.observeAllGroups(),
        countryRepository.observeCountryLogs(),
        combine(
            countryRepository.observeTrackableCountries(),
            airportRepository.observeAirports(),
        ) { countries, airports -> countries to airports },
    ) { trips, itineraries, allGroups, logs, (countries, airports) ->
        val countryByIso2 = countries.associateBy { it.iso2 }
        val airportsById = airports.associateBy { it.id }
        val groupsByItinerary = allGroups.groupBy { it.itineraryId }

        val items = buildList {
            trips.forEach { trip ->
                val sortKey = trip.dateRange?.start?.let { fd ->
                    "${fd.year}-${(fd.month ?: 1).toString().padStart(2, '0')}-${(fd.day ?: 1).toString().padStart(2, '0')}"
                } ?: ""
                add(
                    TimelineItem.TripItem(
                        id = trip.id,
                        title = trip.title,
                        status = trip.status,
                        dateLabel = trip.dateRange?.let { flexibleDateFormatter.format(it) } ?: "",
                        coverPhotoFilename = trip.coverPhotoFilename,
                        sortKey = sortKey,
                    ),
                )
            }

            itineraries.filter { it.tripId == null }.forEach { itinerary ->
                val groups = (groupsByItinerary[itinerary.id] ?: emptyList()).sortedBy { it.sortOrder }
                // Build deduplicated code + city labels, same logic as buildItineraryCodeLabel /
                // buildItineraryRouteLabel in ItineraryDetailScreen.
                val seenIds = mutableSetOf<String>()
                val codeParts = mutableListOf<String>()
                val cityParts = mutableListOf<String>()
                groups.forEach { group ->
                    val flights = group.flights.sortedBy { it.sortOrder ?: Int.MAX_VALUE }
                    val first = flights.firstOrNull() ?: return@forEach
                    val last = flights.lastOrNull() ?: return@forEach
                    listOf(first.originAirportId, last.destinationAirportId).forEach { airportId ->
                        if (seenIds.add(airportId.uppercase())) {
                            val airport = airportsById[airportId]
                            codeParts += airport?.let { it.iata ?: it.icao ?: it.id.uppercase() }
                                ?: airportId.uppercase()
                            cityParts += airport?.let { it.city.takeIf { c -> c.isNotBlank() } ?: it.iata ?: it.icao ?: it.id.uppercase() }
                                ?: airportId.uppercase()
                        }
                    }
                }
                val codeLabel = codeParts.ifEmpty { listOf("Itinerari") }.joinToString(" → ")
                val cityLabel = cityParts.ifEmpty { listOf("Itinerari") }.joinToString(" → ")
                val firstFlight = groups.flatMap { it.flights }.minByOrNull { it.scheduledDepartureAt ?: "" }
                val sortKey = firstFlight?.scheduledDepartureAt?.take(10) ?: ""
                val dateLabel = sortKey.ifEmpty { null }?.let { flexibleDateFormatter.formatIsoDate(it) } ?: ""
                add(
                    TimelineItem.ItineraryItem(
                        id = itinerary.id,
                        codeLabel = codeLabel,
                        cityLabel = if (cityLabel != codeLabel) cityLabel else "",
                        dateLabel = dateLabel,
                        sortKey = sortKey,
                    ),
                )
            }

            logs.forEach { log ->
                val country = countryByIso2[log.countryIso2]
                val sortKey = log.dateRange?.start?.let { fd ->
                    "${fd.year}-${(fd.month ?: 1).toString().padStart(2, '0')}-${(fd.day ?: 1).toString().padStart(2, '0')}"
                } ?: ""
                add(
                    TimelineItem.LogItem(
                        logId = log.id,
                        countryIso2 = log.countryIso2,
                        countryName = country?.nameCa ?: log.countryIso2,
                        flagEmoji = country?.flagEmoji,
                        logType = log.type,
                        dateLabel = log.dateRange?.let { flexibleDateFormatter.format(it) } ?: "",
                        sortKey = sortKey,
                    ),
                )
            }
        }

        val years = items
            .groupBy { if (it.sortKey.length >= 4) it.sortKey.take(4) else null }
            .entries
            .sortedByDescending { (year, _) -> year }
            .map { (year, yearItems) ->
                TimelineYear(
                    year = year ?: "Sense data",
                    items = yearItems.sortedByDescending { it.sortKey },
                )
            }

        TimelineUiState(years = years, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TimelineUiState(),
    )

    class Factory(
        private val tripRepository: TripRepository,
        private val itineraryRepository: ItineraryRepository,
        private val countryRepository: CountryRepository,
        private val airportRepository: AirportRepository,
        private val flexibleDateFormatter: FlexibleDateFormatter,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TimelineViewModel(
                tripRepository,
                itineraryRepository,
                countryRepository,
                airportRepository,
                flexibleDateFormatter,
            ) as T
    }
}
