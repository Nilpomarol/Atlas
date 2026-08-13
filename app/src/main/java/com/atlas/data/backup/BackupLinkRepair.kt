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
    val claimedTripIds = mutableSetOf<String>()
    var repaired = false

    val itineraries = data.itineraries.map { itinerary ->
        val tripId = itinerary.tripId ?: return@map itinerary
        // A trip holds at most one itinerary. Beyond the first, later links are dropped
        // rather than left to generate route stops the trip page cannot account for.
        val keep = tripId in tripIds && claimedTripIds.add(tripId)
        if (keep) {
            itinerary
        } else {
            repaired = true
            itinerary.copy(tripId = null)
        }
    }

    return if (repaired) copy(data = data.copy(itineraries = itineraries)) else this
}
