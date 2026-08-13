package com.atlas.domain.model

/**
 * Canonical status of an itinerary group, used for country/territory derivation.
 *
 * An explicitly stored status is source of truth and is never overridden — the product
 * rules require manual status to survive. Without one, the status is derived from the
 * group's flights, conservatively: a group that cannot be resolved returns null and is
 * left out of derivation rather than guessed at.
 *
 * This is the single definition. It previously existed as a private copy inside
 * `CountryStateDerivationService` while two ViewModels each derived their own answer,
 * so one group could report different statuses on different screens.
 */
fun ItineraryGroup.effectiveStatus(): TravelStatus? =
    status ?: when {
        flights.isEmpty() -> null
        flights.any { it.status == TravelStatus.COMPLETED } -> TravelStatus.COMPLETED
        flights.all { it.status == TravelStatus.PLANNED } -> TravelStatus.PLANNED
        else -> null
    }

/**
 * Status for display, which unlike [effectiveStatus] must always resolve to something.
 *
 * A group with a flight under way reads as in progress even though derivation stays
 * conservative about it: showing "en curs" is honest on screen, while counting the
 * destination as visited before arrival would not be.
 */
fun ItineraryGroup.displayStatus(): TravelStatus = when {
    status != null -> status
    flights.any { it.status == TravelStatus.IN_PROGRESS } -> TravelStatus.IN_PROGRESS
    else -> effectiveStatus() ?: TravelStatus.UNKNOWN
}

/** Aggregate display status for a whole itinerary, from its groups' resolved statuses. */
fun List<ItineraryGroup>.itineraryDisplayStatus(): TravelStatus {
    val statuses = map { it.displayStatus() }
    return when {
        statuses.isEmpty() -> TravelStatus.UNKNOWN
        statuses.all { it == TravelStatus.COMPLETED } -> TravelStatus.COMPLETED
        statuses.any { it == TravelStatus.IN_PROGRESS } -> TravelStatus.IN_PROGRESS
        statuses.any { it == TravelStatus.PLANNED } -> TravelStatus.PLANNED
        else -> TravelStatus.UNKNOWN
    }
}
