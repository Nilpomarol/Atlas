package com.atlas.data.backup

/**
 * Folds legacy excursions into nested trip stops, mirroring Room migration 25 → 26.
 *
 * Ids are preserved verbatim so `stop_photos` rows keep resolving; only their
 * `stopType` discriminator changes. Anchored excursions nest under their anchor stop;
 * unanchored ones become main-route stops.
 */
fun collapseLegacyExcursions(
    tripStops: List<BackupTripStopV4>,
    excursions: List<BackupExcursionV2>,
    excursionStops: List<BackupExcursionStopV2>,
): List<BackupTripStopV4> {
    if (excursions.isEmpty() && excursionStops.isEmpty()) return tripStops

    val excursionsById = excursions.associateBy { it.id }
    val stopsByExcursion = excursionStops.groupBy { it.excursionId }

    val converted = excursionStops.mapNotNull { stop ->
        val excursion = excursionsById[stop.excursionId] ?: return@mapNotNull null
        val isFirstOfExcursion = stopsByExcursion[excursion.id]
            ?.minByOrNull { it.sortOrder }
            ?.id == stop.id
        BackupTripStopV4(
            id = stop.id,
            tripId = excursion.tripId,
            parentStopId = excursion.anchorTripStopId,
            sideTripLabel = excursion.title,
            locationName = stop.locationName,
            countryIso2 = stop.countryIso2,
            latitude = stop.latitude,
            longitude = stop.longitude,
            startYear = stop.startYear,
            startMonth = stop.startMonth,
            startDay = stop.startDay,
            endYear = stop.endYear,
            endMonth = stop.endMonth,
            endDay = stop.endDay,
            datePrecision = stop.datePrecision,
            // The excursion's own notes describe the side trip as a whole, so they land
            // once on its first stop rather than repeating on every sibling.
            notes = if (isFirstOfExcursion) {
                joinNotes(stop.notes, excursion.notes)
            } else {
                stop.notes
            },
            sortOrder = (excursion.sortOrder * 100) + stop.sortOrder,
            createdAt = stop.createdAt,
            updatedAt = stop.updatedAt,
        )
    }

    // An excursion with no stops has only its title and notes to lose, so fold that text
    // into the stop it was anchored to.
    val childlessByAnchor = excursions
        .filter { stopsByExcursion[it.id].isNullOrEmpty() && it.anchorTripStopId != null }
        .groupBy { requireNotNull(it.anchorTripStopId) }

    val existing = tripStops.map { stop ->
        val orphaned = childlessByAnchor[stop.id] ?: return@map stop
        val salvaged = orphaned.joinToString("\n\n") { joinNotes(it.title, it.notes).orEmpty() }
        stop.copy(notes = joinNotes(stop.notes, salvaged))
    }

    return existing + converted
}

private fun joinNotes(first: String?, second: String?): String? {
    val head = first?.takeIf { it.isNotBlank() }
    val tail = second?.takeIf { it.isNotBlank() }
    return when {
        head == null -> tail
        tail == null -> head
        else -> "$head\n\n$tail"
    }
}
