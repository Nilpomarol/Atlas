package com.atlas.data.backup

/**
 * Detaches itineraries whose trip is missing from the archive.
 *
 * Backup validation rejects an itinerary pointing at a trip that does not exist, and
 * until the trip-delete path was fixed the app could produce exactly that state. Such
 * an archive is otherwise complete and safe, so repairing the dangling link is
 * preferable to refusing the whole restore — the alternative is a user whose only
 * backup cannot be imported.
 *
 * The itinerary, its groups, and its flights are all preserved; only the trip link is
 * cleared, leaving the itinerary standalone and re-linkable.
 */
fun AtlasBackupV4.withRepairedTripLinks(): AtlasBackupV4 {
    val tripIds = data.trips.mapTo(mutableSetOf()) { it.id }
    val dangling = data.itineraries.any { it.tripId != null && it.tripId !in tripIds }
    if (!dangling) return this

    return copy(
        data = data.copy(
            itineraries = data.itineraries.map { itinerary ->
                if (itinerary.tripId != null && itinerary.tripId !in tripIds) {
                    itinerary.copy(tripId = null)
                } else {
                    itinerary
                }
            },
        ),
    )
}
