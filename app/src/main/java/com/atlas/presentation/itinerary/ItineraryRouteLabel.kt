package com.atlas.presentation.itinerary

import com.atlas.domain.model.Airport
import com.atlas.domain.model.ItineraryGroup

/**
 * Builds an itinerary's display title from its route: the de-duplicated chain of airport
 * codes across its ordered groups (e.g. "BCN → DOH → NRT"). This is the canonical itinerary
 * title — the stored `Itinerary.title` field is no longer populated — and is shared by the
 * itinerary detail hero, the itinerary list, the timeline, and the trip itinerary panels so
 * they all read the same.
 */
fun itineraryCodeLabel(groups: List<ItineraryGroup>, airports: List<Airport>): String =
    buildRouteLabel(groups, airports) { it.displayCode() }

/** Like [itineraryCodeLabel] but uses city names where available (e.g. "Barcelona → Doha → Tòquio"). */
fun itineraryCityLabel(groups: List<ItineraryGroup>, airports: List<Airport>): String =
    buildRouteLabel(groups, airports) { it.shortLabel() }

private fun buildRouteLabel(
    groups: List<ItineraryGroup>,
    airports: List<Airport>,
    label: (Airport) -> String,
): String {
    val orderedGroups = groups.sortedBy { it.sortOrder }
    val seen = mutableSetOf<String>()
    val route = mutableListOf<String>()

    fun addAirportIfNew(airportId: String) {
        val normalized = airportId.uppercase()
        if (!seen.add(normalized)) return
        route += airports.findAirport(airportId)?.let(label) ?: normalized
    }

    orderedGroups.forEach { group ->
        val flights = group.flights.sortedBy { it.sortOrder ?: Int.MAX_VALUE }
        val first = flights.firstOrNull() ?: return@forEach
        val last = flights.lastOrNull() ?: return@forEach
        addAirportIfNew(first.originAirportId)
        addAirportIfNew(last.destinationAirportId)
    }

    return route.ifEmpty { listOf("Sense ruta") }.joinToString(" → ")
}

private fun List<Airport>.findAirport(value: String): Airport? {
    val normalized = value.uppercase()
    return firstOrNull {
        it.id.equals(value, ignoreCase = true) ||
            it.iata?.equals(normalized, ignoreCase = true) == true ||
            it.icao?.equals(normalized, ignoreCase = true) == true
    }
}

private fun Airport.displayCode(): String = iata ?: icao ?: id.uppercase()

private fun Airport.shortLabel(): String =
    city.takeIf { it.isNotBlank() } ?: iata ?: icao ?: id.uppercase()
