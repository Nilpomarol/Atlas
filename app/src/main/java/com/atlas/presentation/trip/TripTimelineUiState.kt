package com.atlas.presentation.trip

import com.atlas.domain.model.Airport
import com.atlas.domain.model.ItineraryGroup
import com.atlas.domain.model.TripStop
import com.atlas.domain.model.TripStopSource
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.domain.util.utcAwareDepartureSortKey

/**
 * One row of the trip's journey. Places and flight legs share a single sequence, so the
 * timeline reads in the order the trip was lived rather than as separate lists.
 */
sealed interface TripTimelineEntry {

    /** A main-route stop, with the places visited from it. */
    data class Place(
        val stop: TripStop,
        val number: Int,
        val sideTrips: List<TripStop>,
        val dateText: String?,
    ) : TripTimelineEntry

    /**
     * A flight leg, derived from an itinerary group. Not editable as a stop — opening it
     * opens the flight.
     */
    data class Leg(
        val stopId: String,
        val itineraryGroupId: String?,
        val title: String,
        val dateText: String?,
    ) : TripTimelineEntry
}

/**
 * Builds the journey in chronological order.
 *
 * Generated flight legs carry `sortOrder = 10_000 + index`, which appends them after
 * every manual stop, so a mid-trip flight would otherwise land at the end. Ordering
 * therefore prefers a real date: a stop's own date, or a leg's first departure.
 *
 * Undated entries inherit the date of the last dated entry before them in `sortOrder`,
 * which keeps a manually ordered route intact instead of pushing every undated stop to
 * one end.
 */
fun buildTripTimeline(
    stops: List<TripStop>,
    groups: List<ItineraryGroup> = emptyList(),
    airportsById: Map<String, Airport> = emptyMap(),
    dateFormatter: FlexibleDateFormatter = FlexibleDateFormatter(),
): List<TripTimelineEntry> {
    val ordered = stops.sortedWith(compareBy(TripStop::sortOrder, TripStop::id))
    val sideTripsByParent = ordered.filter { it.parentStopId != null }.groupBy { it.parentStopId }
    val groupsById = groups.associateBy { it.id }

    var placeNumber = 0
    val rows = ordered
        .filter { it.parentStopId == null }
        .map { stop ->
            if (stop.source == TripStopSource.ITINERARY_GROUP) {
                val group = stop.itineraryGroupId?.let(groupsById::get)
                Row(
                    entry = TripTimelineEntry.Leg(
                        stopId = stop.id,
                        itineraryGroupId = stop.itineraryGroupId,
                        title = stop.legTitle(group, airportsById),
                        dateText = group?.departureDateText(dateFormatter),
                    ),
                    dateKey = group?.departureSortKey(),
                    sortOrder = stop.sortOrder,
                )
            } else {
                placeNumber += 1
                Row(
                    entry = TripTimelineEntry.Place(
                        stop = stop,
                        number = placeNumber,
                        sideTrips = sideTripsByParent[stop.id].orEmpty()
                            .sortedWith(compareBy(TripStop::sortOrder, TripStop::id)),
                        dateText = dateFormatter.formatTripPill(stop.dateRange),
                    ),
                    dateKey = stop.dateRange?.start?.sortKey(),
                    sortOrder = stop.sortOrder,
                )
            }
        }

    // Carry the last known date forward so undated stops hold their manual position.
    var carried = ""
    val keyed = rows.map { row ->
        val key = row.dateKey?.also { carried = it } ?: carried
        key to row
    }

    return keyed
        .sortedWith(compareBy({ it.first }, { it.second.sortOrder }))
        .map { it.second.entry }
}

private data class Row(
    val entry: TripTimelineEntry,
    val dateKey: String?,
    val sortOrder: Int,
)

private fun TripStop.legTitle(
    group: ItineraryGroup?,
    airportsById: Map<String, Airport>,
): String {
    displayTitle?.takeIf { it.isNotBlank() }?.let { return it }
    val flights = group?.flights.orEmpty().sortedWith(
        compareBy({ it.sortOrder ?: Int.MAX_VALUE }, { it.utcAwareDepartureSortKey() ?: "" }),
    )
    val origin = flights.firstOrNull()?.originAirportId
    val destination = flights.lastOrNull()?.destinationAirportId
    if (origin == null || destination == null) return locationName
    val from = airportsById[origin]?.city?.takeIf(String::isNotBlank) ?: origin
    val to = airportsById[destination]?.city?.takeIf(String::isNotBlank) ?: destination
    return "$from → $to"
}

private fun ItineraryGroup.departureSortKey(): String? = flights
    .mapNotNull { it.utcAwareDepartureSortKey() }
    .minOrNull()
    ?.take(10)

private fun ItineraryGroup.departureDateText(formatter: FlexibleDateFormatter): String? =
    departureSortKey()?.let(formatter::formatIsoDate)

private fun com.atlas.domain.model.FlexibleDate.sortKey(): String {
    val month = (month ?: 1).toString().padStart(2, '0')
    val day = (day ?: 1).toString().padStart(2, '0')
    return "${year.toString().padStart(4, '0')}-$month-$day"
}
