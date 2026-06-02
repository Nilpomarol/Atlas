package com.atlas.domain.service

import com.atlas.domain.model.Airport
import com.atlas.domain.model.Flight
import com.atlas.domain.model.GeneratedTripStopDraft
import com.atlas.domain.model.ItineraryGroup

class ItineraryGeneratedStopService {
    fun buildGeneratedStops(
        groups: List<ItineraryGroup>,
        airportsById: Map<String, Airport>,
    ): List<GeneratedTripStopDraft> {
        val sortedGroups = groups.sortedBy { it.sortOrder }
        return sortedGroups.mapIndexedNotNull { index, group ->
            val selectedAirportId = selectedAirportIdForGroup(
                group = group,
                isOnlyGroup = sortedGroups.size == 1,
                isLastGroup = index == sortedGroups.lastIndex,
            ) ?: return@mapIndexedNotNull null
            val airport = airportsById[selectedAirportId] ?: return@mapIndexedNotNull null

            GeneratedTripStopDraft(
                itineraryGroupId = group.id,
                locationName = airport.city,
                countryIso2 = airport.countryIso2,
                latitude = airport.latitude,
                longitude = airport.longitude,
                displayTitle = group.title?.trim()?.ifBlank { null },
                sortOrder = 10_000 + index,
            )
        }
    }

    private fun selectedAirportIdForGroup(
        group: ItineraryGroup,
        isOnlyGroup: Boolean,
        isLastGroup: Boolean,
    ): String? {
        val flights = group.flights.sortedWith(
            compareBy<Flight> { it.sortOrder ?: Int.MAX_VALUE }
                .thenBy { it.scheduledDepartureAt ?: "" },
        )
        val firstFlight = flights.firstOrNull() ?: return null
        val lastFlight = flights.lastOrNull() ?: return null

        return if (isOnlyGroup || !isLastGroup) {
            lastFlight.destinationAirportId
        } else {
            firstFlight.originAirportId
        }
    }
}
