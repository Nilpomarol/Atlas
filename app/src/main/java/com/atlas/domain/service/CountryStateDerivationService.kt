package com.atlas.domain.service

import com.atlas.domain.model.CountryLog
import com.atlas.domain.model.CountryLogType
import com.atlas.domain.model.CountryTrackingState
import com.atlas.domain.model.CountryUserState
import com.atlas.domain.model.Excursion
import com.atlas.domain.model.Flight
import com.atlas.domain.model.ItineraryGroup
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop

class CountryStateDerivationService {
    fun derive(
        countryIso2: String? = null,
        userState: CountryUserState?,
        logs: List<CountryLog> = emptyList(),
        trips: List<Trip> = emptyList(),
        tripStops: List<TripStop> = emptyList(),
        flights: List<Flight> = emptyList(),
        itineraryGroups: List<ItineraryGroup> = emptyList(),
        excursions: List<Excursion> = emptyList(),
        airportCountryIso2ById: Map<String, String> = emptyMap(),
    ): CountryTrackingState {
        val currentlyLiving = userState?.currentlyLiving == true
        val hasLivedLog = logs.any { it.type == CountryLogType.LIVED }
        val hasVisitLog = logs.any { it.type == CountryLogType.VISIT }
        val tripsById = trips.associateBy { it.id }
        val matchingStops = tripStops.filter { stop ->
            countryIso2 == null || stop.countryIso2 == countryIso2
        }
        val hasPlannedStop = matchingStops.any { stop ->
            tripsById[stop.tripId]?.status == TravelStatus.PLANNED
        }
        val hasVisitedStop = matchingStops.any { stop ->
            val status = tripsById[stop.tripId]?.status
            status == TravelStatus.IN_PROGRESS || status == TravelStatus.COMPLETED
        }
        val matchingExcursionStops = excursions.flatMap { excursion ->
            excursion.stops.map { stop -> excursion to stop }
        }.filter { (_, stop) ->
            countryIso2 == null || stop.countryIso2 == countryIso2
        }
        val hasPlannedExcursionStop = matchingExcursionStops.any { (excursion, _) ->
            tripsById[excursion.tripId]?.status == TravelStatus.PLANNED
        }
        val hasVisitedExcursionStop = matchingExcursionStops.any { (excursion, _) ->
            val status = tripsById[excursion.tripId]?.status
            status == TravelStatus.IN_PROGRESS || status == TravelStatus.COMPLETED
        }
        val soloFlights = flights.filter { it.itineraryGroupId == null }
        val hasPlannedSoloFlight = soloFlights.any { flight ->
            flight.status == TravelStatus.PLANNED &&
                airportCountryIso2ById[flight.destinationAirportId] == countryIso2
        }
        val hasVisitedSoloFlight = soloFlights.any { flight ->
            flight.status == TravelStatus.COMPLETED &&
                airportCountryIso2ById[flight.destinationAirportId] == countryIso2
        }
        val itineraryDerivedStatuses = deriveItineraryGroupCountries(
            groups = itineraryGroups,
            airportCountryIso2ById = airportCountryIso2ById,
        )
        val hasPlannedItineraryGroup = itineraryDerivedStatuses.any { derived ->
            derived.countryIso2 == countryIso2 && derived.status == TravelStatus.PLANNED
        }
        val hasVisitedItineraryGroup = itineraryDerivedStatuses.any { derived ->
            derived.countryIso2 == countryIso2 && derived.status == TravelStatus.COMPLETED
        }
        val lived = currentlyLiving || hasLivedLog
        val visited = currentlyLiving ||
            hasVisitLog ||
            hasLivedLog ||
            hasVisitedStop ||
            hasVisitedExcursionStop ||
            hasVisitedSoloFlight ||
            hasVisitedItineraryGroup

        return CountryTrackingState(
            wished = userState?.wished == true,
            currentlyLiving = currentlyLiving,
            lived = lived,
            visited = visited,
            planned = hasPlannedStop || hasPlannedExcursionStop || hasPlannedSoloFlight || hasPlannedItineraryGroup,
            neverVisited = !visited && !lived,
        )
    }

    fun deriveItineraryGroupCountries(
        groups: List<ItineraryGroup>,
        airportCountryIso2ById: Map<String, String>,
    ): List<ItineraryGroupCountryDerivation> {
        val groupsByItinerary = groups.groupBy { it.itineraryId }
        return groupsByItinerary.values.flatMap { itineraryGroups ->
            val sortedGroups = itineraryGroups.sortedBy { it.sortOrder }
            sortedGroups.mapIndexedNotNull { index, group ->
                val status = group.effectiveStatus() ?: return@mapIndexedNotNull null
                val flight = group.derivedFlight(
                    isLastGroup = index == sortedGroups.lastIndex,
                    isOnlyGroup = sortedGroups.size == 1,
                ) ?: return@mapIndexedNotNull null
                val airportId = if (sortedGroups.size == 1 || index != sortedGroups.lastIndex) {
                    flight.destinationAirportId
                } else {
                    flight.originAirportId
                }
                ItineraryGroupCountryDerivation(
                    groupId = group.id,
                    itineraryId = group.itineraryId,
                    countryIso2 = airportCountryIso2ById[airportId] ?: return@mapIndexedNotNull null,
                    status = status,
                )
            }
        }
    }

    private fun ItineraryGroup.effectiveStatus(): TravelStatus? =
        status ?: when {
            flights.isEmpty() -> null
            flights.any { it.status == TravelStatus.COMPLETED } -> TravelStatus.COMPLETED
            flights.all { it.status == TravelStatus.PLANNED } -> TravelStatus.PLANNED
            else -> null
        }

    private fun ItineraryGroup.derivedFlight(
        isLastGroup: Boolean,
        isOnlyGroup: Boolean,
    ): Flight? {
        val sortedFlights = flights.sortedWith(
            compareBy<Flight> { it.sortOrder ?: Int.MAX_VALUE }
                .thenBy { it.scheduledDepartureAt ?: "" },
        )
        return if (isOnlyGroup || !isLastGroup) {
            sortedFlights.lastOrNull()
        } else {
            sortedFlights.firstOrNull()
        }
    }

    data class ItineraryGroupCountryDerivation(
        val groupId: String,
        val itineraryId: String,
        val countryIso2: String,
        val status: TravelStatus,
    )
}
