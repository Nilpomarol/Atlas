package com.atlas.domain.service

import com.atlas.domain.model.CountryLog
import com.atlas.domain.model.CountryLogType
import com.atlas.domain.model.CountryTrackingState
import com.atlas.domain.model.CountryUserState
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
        val lived = currentlyLiving || hasLivedLog
        val visited = currentlyLiving || hasVisitLog || hasLivedLog || hasVisitedStop

        return CountryTrackingState(
            wished = userState?.wished == true,
            currentlyLiving = currentlyLiving,
            lived = lived,
            visited = visited,
            planned = hasPlannedStop,
            neverVisited = !visited && !lived,
        )
    }
}
