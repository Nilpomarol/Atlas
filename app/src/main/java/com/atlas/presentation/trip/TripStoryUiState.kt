package com.atlas.presentation.trip

import com.atlas.domain.model.Country
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.model.Flight
import com.atlas.domain.model.Airport
import com.atlas.domain.model.Itinerary
import com.atlas.domain.model.ItineraryGroup
import com.atlas.domain.model.StopPhoto
import com.atlas.domain.model.StopType
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop
import com.atlas.domain.model.TripStopSource
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.domain.util.utcAwareDepartureSortKey
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class TripStoryUiState(
    val trip: Trip? = null,
    val dateText: String? = null,
    val dayCountText: String = "—",
    val stopCount: Int = 0,
    val countryNames: List<String> = emptyList(),
    val routeText: String? = null,
    val photoCount: Int = 0,
    val mapStops: List<TripStop> = emptyList(),
    val mapNestedStops: List<TripStop> = emptyList(),
    val slides: List<TripStorySlideUiState> = emptyList(),
) {
    val viewerItems: List<PhotoViewerItemUiState>
        get() = slides.mapNotNull { slide ->
            (slide as? TripStorySlideUiState.Photo)?.item
        }
}

sealed interface TripStorySlideUiState {
    val id: String

    data class Title(
        override val id: String,
        val title: String,
        val dateText: String?,
        val dayCountText: String,
        val stopCount: Int,
        val countryCount: Int,
        val photoCount: Int,
    ) : TripStorySlideUiState

    data class Route(
        override val id: String,
        val routeText: String?,
        val countryNames: List<String>,
    ) : TripStorySlideUiState

    data class Place(
        override val id: String,
        val eyebrow: String,
        val title: String,
        val contextText: String?,
        val notes: String?,
        val photoCount: Int,
    ) : TripStorySlideUiState

    data class Photo(
        override val id: String,
        val item: PhotoViewerItemUiState,
        val sectionLabel: String,
    ) : TripStorySlideUiState

    data class Summary(
        override val id: String,
        val title: String,
        val dayCountText: String,
        val stopCount: Int,
        val countryCount: Int,
        val photoCount: Int,
    ) : TripStorySlideUiState
}

internal fun buildTripStoryUiState(
    trip: Trip?,
    stops: List<TripStop>,
    countries: List<Country>,
    itinerary: Itinerary?,
    itineraryGroups: List<ItineraryGroup>,
    stopPhotoMap: Map<String, List<StopPhoto>>,
    airports: List<Airport> = emptyList(),
    dateFormatter: FlexibleDateFormatter = FlexibleDateFormatter(),
): TripStoryUiState {
    if (trip == null) return TripStoryUiState()

    val airportsById = airports.associateBy(Airport::id)
    val countriesByIso = countries.associateBy { it.iso2 }
    val orderedStops = stops.sortedWith(compareBy(TripStop::sortOrder, TripStop::id))
    val mainStops = orderedStops.filter { it.parentStopId == null }
    val mainStopIds = mainStops.mapTo(mutableSetOf(), TripStop::id)
    val childrenByParent = orderedStops.filter { it.parentStopId != null }.groupBy { it.parentStopId }
    val orphanStops = orderedStops.filter { it.parentStopId != null && it.parentStopId !in mainStopIds }
    val nestedStops = orderedStops.filter { it.parentStopId != null }
    val itineraryGroupsById = itineraryGroups.associateBy(ItineraryGroup::id)

    val countryNames = orderedStops.map { it.countryIso2 }
        .filter(String::isNotBlank)
        .distinct()
        .map { iso2 -> countriesByIso[iso2]?.nameCa ?: iso2 }

    val routeText = mainStops
        .joinToString(" → ") { it.storyTitle(itineraryGroupsById, airportsById) }
        .takeIf(String::isNotBlank)

    val dateText = trip.dateRange?.let(dateFormatter::format)
    val dayCountText = trip.dayCountText()
    val slides = buildList {
        add(
            TripStorySlideUiState.Title(
                id = "title-${trip.id}",
                title = trip.title,
                dateText = dateText,
                dayCountText = dayCountText,
                stopCount = mainStops.size,
                countryCount = countryNames.size,
                photoCount = 0,
            ),
        )
        add(
            TripStorySlideUiState.Route(
                id = "route-${trip.id}",
                routeText = routeText,
                countryNames = countryNames,
            ),
        )

        fun MutableList<TripStorySlideUiState>.addStopSlides(stop: TripStop) {
            val stopTitle = stop.storyTitle(itineraryGroupsById, airportsById)
            val stopEyebrow = stop.storyEyebrow()
            val stopPhotos = stopPhotoMap[stop.id].toStoryPhotos(
                stopId = stop.id,
                stopType = StopType.TRIP_STOP,
                tripId = trip.id,
                title = stopTitle,
                contextLabel = stopEyebrow,
                dateText = stop.dateRange?.let(dateFormatter::format),
            )
            add(
                TripStorySlideUiState.Place(
                    id = "stop-${stop.id}",
                    eyebrow = stopEyebrow,
                    title = stopTitle,
                    contextText = stop.contextText(countriesByIso, dateFormatter),
                    notes = stop.notes?.takeIf(String::isNotBlank),
                    photoCount = stopPhotos.size,
                ),
            )
            stopPhotos.forEach { item ->
                add(
                    TripStorySlideUiState.Photo(
                        id = "photo-${item.photo.id}",
                        item = item,
                        sectionLabel = stopTitle,
                    ),
                )
            }
        }

        // Narrative order: each main-route stop, then the places visited from it.
        mainStops.forEach { stop ->
            addStopSlides(stop)
            childrenByParent[stop.id].orEmpty().forEach { child -> addStopSlides(child) }
        }
        orphanStops.forEach { stop -> addStopSlides(stop) }
    }

    val photoCount = slides.count { it is TripStorySlideUiState.Photo }
    val finalizedSlides = slides.map { slide ->
        if (slide is TripStorySlideUiState.Title) {
            slide.copy(photoCount = photoCount)
        } else {
            slide
        }
    } + TripStorySlideUiState.Summary(
        id = "summary-${trip.id}",
        title = trip.title,
        dayCountText = dayCountText,
        stopCount = mainStops.size,
        countryCount = countryNames.size,
        photoCount = photoCount,
    )

    return TripStoryUiState(
        trip = trip,
        dateText = dateText,
        dayCountText = dayCountText,
        stopCount = mainStops.size,
        countryNames = countryNames,
        routeText = routeText,
        photoCount = photoCount,
        mapStops = mainStops,
        mapNestedStops = nestedStops,
        slides = finalizedSlides,
    )
}

private fun List<StopPhoto>?.toStoryPhotos(
    stopId: String,
    stopType: StopType,
    tripId: String,
    title: String,
    contextLabel: String,
    dateText: String?,
): List<PhotoViewerItemUiState> =
    orEmpty()
        .sortedWith(compareBy(StopPhoto::sortOrder, StopPhoto::id))
        .map { photo ->
            PhotoViewerItemUiState(
                photo = photo,
                stopId = stopId,
                stopType = stopType,
                title = title,
                contextLabel = contextLabel,
                dateText = dateText,
                tripId = tripId,
            )
        }

private fun TripStop.storyTitle(
    itineraryGroupsById: Map<String, ItineraryGroup>,
    airportsById: Map<String, Airport>,
): String =
    displayTitle?.takeIf(String::isNotBlank)
        ?: itineraryRouteTitle(itineraryGroupsById, airportsById)
        ?: locationName.takeIf(String::isNotBlank)
        ?: "Tram d'itinerari"

private fun TripStop.storyEyebrow(): String = when {
    source == TripStopSource.ITINERARY_GROUP -> "VOL"
    parentStopId != null && !sideTripLabel.isNullOrBlank() -> "SORTIDA · $sideTripLabel"
    parentStopId != null -> "SORTIDA"
    else -> "PARADA"
}

private fun TripStop.itineraryRouteTitle(
    itineraryGroupsById: Map<String, ItineraryGroup>,
    airportsById: Map<String, Airport>,
): String? {
    if (source != TripStopSource.ITINERARY_GROUP) return null
    val group = itineraryGroupId?.let(itineraryGroupsById::get) ?: return null
    return group.routeDisplayTitle(airportsById)
}

private fun ItineraryGroup.routeDisplayTitle(airportsById: Map<String, Airport>): String? {
    val flights = flights.sortedWith(
        compareBy<Flight> { it.sortOrder ?: Int.MAX_VALUE }
            .thenBy { it.utcAwareDepartureSortKey() ?: "" },
    )
    val origin = flights.firstOrNull()?.originAirportId?.takeIf(String::isNotBlank) ?: return null
    val destination = flights.lastOrNull()?.destinationAirportId?.takeIf(String::isNotBlank) ?: return null
    return "${origin.displayAirportName(airportsById)} → ${destination.displayAirportName(airportsById)}"
}

private fun String.displayAirportName(airportsById: Map<String, Airport>): String =
    airportsById[this]?.city?.takeIf(String::isNotBlank) ?: this

private fun TripStop.contextText(
    countriesByIso: Map<String, Country>,
    dateFormatter: FlexibleDateFormatter,
): String? = listOfNotNull(
    countriesByIso[countryIso2]?.nameCa ?: countryIso2.takeIf(String::isNotBlank),
    dateRange?.let(dateFormatter::format),
).joinToString(" · ").takeIf(String::isNotBlank)

private fun Trip.dayCountText(): String {
    val range = dateRange ?: return "—"
    val start = range.start?.toLocalDateOrNull() ?: return "—"
    val end = range.end?.toLocalDateOrNull() ?: start
    return (ChronoUnit.DAYS.between(start, end).coerceAtLeast(0) + 1).toString()
}

private fun FlexibleDate.toLocalDateOrNull(): LocalDate? {
    val m = month ?: return null
    val d = day ?: return null
    return runCatching { LocalDate.of(year, m, d) }.getOrNull()
}
