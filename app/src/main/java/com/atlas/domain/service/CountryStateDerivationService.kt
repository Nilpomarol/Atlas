package com.atlas.domain.service

import com.atlas.domain.model.CountryLog
import com.atlas.domain.model.CountryLogType
import com.atlas.domain.model.CountryTrackingState
import com.atlas.domain.model.CountryUserState
import com.atlas.domain.model.Flight
import com.atlas.domain.model.ItineraryGroup
import com.atlas.domain.model.effectiveStatus
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop
import com.atlas.domain.util.utcAwareDepartureSortKey

class CountryStateDerivationService {
    fun derive(
        countryIso2: String? = null,
        userState: CountryUserState?,
        logs: List<CountryLog> = emptyList(),
        trips: List<Trip> = emptyList(),
        tripStops: List<TripStop> = emptyList(),
        flights: List<Flight> = emptyList(),
        itineraryGroups: List<ItineraryGroup> = emptyList(),
        airportCountryIso2ById: Map<String, String> = emptyMap(),
    ): CountryTrackingState {
        val currentlyLiving = userState?.currentlyLiving == true
        val hasLivedLog = logs.any { it.type == CountryLogType.LIVED }
        val hasVisitLog = logs.any { it.type == CountryLogType.VISIT }
        val tripsById = trips.associateBy { it.id }
        // Nested stops (formerly excursion stops) are ordinary trip stops and count exactly
        // like main-route stops, so no separate side-trip branch is needed here.
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
        val soloFlights = flights.filter { it.itineraryGroupId == null }
        val hasPlannedSoloFlight = soloFlights.any { flight ->
            flight.status == TravelStatus.PLANNED && (
                (flight.destinationCountsForCountryTracking && airportCountryIso2ById[flight.destinationAirportId] == countryIso2) ||
                (flight.originCountsForCountryTracking && airportCountryIso2ById[flight.originAirportId] == countryIso2)
            )
        }
        val hasVisitedSoloFlight = soloFlights.any { flight ->
            flight.status == TravelStatus.COMPLETED && (
                (flight.destinationCountsForCountryTracking && airportCountryIso2ById[flight.destinationAirportId] == countryIso2) ||
                (flight.originCountsForCountryTracking && airportCountryIso2ById[flight.originAirportId] == countryIso2)
            )
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
            hasVisitedSoloFlight ||
            hasVisitedItineraryGroup

        return CountryTrackingState(
            wished = userState?.wished == true,
            currentlyLiving = currentlyLiving,
            lived = lived,
            visited = visited,
            planned = hasPlannedStop || hasPlannedSoloFlight || hasPlannedItineraryGroup,
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
                val usesDestination = sortedGroups.size == 1 || index != sortedGroups.lastIndex
                if (usesDestination && !flight.destinationCountsForCountryTracking) return@mapIndexedNotNull null
                if (!usesDestination && !flight.originCountsForCountryTracking) return@mapIndexedNotNull null
                val airportId = if (usesDestination) {
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


    private fun ItineraryGroup.derivedFlight(
        isLastGroup: Boolean,
        isOnlyGroup: Boolean,
    ): Flight? {
        val sortedFlights = flights.sortedWith(
            compareBy<Flight> { it.sortOrder ?: Int.MAX_VALUE }
                .thenBy { it.utcAwareDepartureSortKey() ?: "" },
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
