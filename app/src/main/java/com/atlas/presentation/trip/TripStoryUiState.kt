package com.atlas.presentation.trip

import com.atlas.domain.model.Country
import com.atlas.domain.model.Excursion
import com.atlas.domain.model.ExcursionStop
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.model.Itinerary
import com.atlas.domain.model.ItineraryGroup
import com.atlas.domain.model.StopPhoto
import com.atlas.domain.model.StopType
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop
import com.atlas.domain.service.FlexibleDateFormatter
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
    val itinerary: TripStoryItineraryUiState? = null,
    val stops: List<TripStoryStopUiState> = emptyList(),
    val unanchoredExcursions: List<TripStoryExcursionUiState> = emptyList(),
) {
    val viewerItems: List<PhotoViewerItemUiState>
        get() = stops.flatMap { stop ->
            stop.photos + stop.excursions.flatMap { excursion ->
                excursion.stops.flatMap(TripStoryExcursionStopUiState::photos)
            }
        } + unanchoredExcursions.flatMap { excursion ->
            excursion.stops.flatMap(TripStoryExcursionStopUiState::photos)
        }
}

data class TripStoryItineraryUiState(
    val title: String,
    val notes: String?,
    val groupSummary: String?,
)

data class TripStoryStopUiState(
    val id: String,
    val title: String,
    val contextText: String?,
    val notes: String?,
    val photos: List<PhotoViewerItemUiState>,
    val excursions: List<TripStoryExcursionUiState>,
)

data class TripStoryExcursionUiState(
    val id: String,
    val title: String,
    val notes: String?,
    val stops: List<TripStoryExcursionStopUiState>,
) {
    val photoCount: Int
        get() = stops.sumOf { it.photos.size }
}

data class TripStoryExcursionStopUiState(
    val id: String,
    val title: String,
    val contextText: String?,
    val notes: String?,
    val photos: List<PhotoViewerItemUiState>,
)

internal fun buildTripStoryUiState(
    trip: Trip?,
    stops: List<TripStop>,
    countries: List<Country>,
    excursions: List<Excursion>,
    itinerary: Itinerary?,
    itineraryGroups: List<ItineraryGroup>,
    tripStopPhotoMap: Map<String, List<StopPhoto>>,
    excursionStopPhotoMap: Map<String, List<StopPhoto>>,
    dateFormatter: FlexibleDateFormatter = FlexibleDateFormatter(),
): TripStoryUiState {
    if (trip == null) return TripStoryUiState()

    val countriesByIso = countries.associateBy { it.iso2 }
    val orderedStops = stops.sortedWith(compareBy(TripStop::sortOrder, TripStop::id))
    val orderedExcursions = excursions.sortedWith(compareBy(Excursion::sortOrder, Excursion::id))
    val stopIds = orderedStops.mapTo(mutableSetOf(), TripStop::id)
    val anchoredExcursions = orderedExcursions.groupBy(Excursion::anchorTripStopId)

    val storyStops = orderedStops.map { stop ->
        TripStoryStopUiState(
            id = stop.id,
            title = stop.displayTitle?.takeIf(String::isNotBlank) ?: stop.locationName,
            contextText = stop.contextText(countriesByIso, dateFormatter),
            notes = stop.notes?.takeIf(String::isNotBlank),
            photos = tripStopPhotoMap[stop.id].toStoryPhotos(
                stopId = stop.id,
                stopType = StopType.TRIP_STOP,
                tripId = trip.id,
                title = stop.displayTitle?.takeIf(String::isNotBlank) ?: stop.locationName,
                contextLabel = "PARADA",
                dateText = stop.dateRange?.let(dateFormatter::format),
            ),
            excursions = anchoredExcursions[stop.id].orEmpty().map { excursion ->
                excursion.toStoryExcursion(
                    trip = trip,
                    countriesByIso = countriesByIso,
                    photoMap = excursionStopPhotoMap,
                    dateFormatter = dateFormatter,
                )
            },
        )
    }

    val unanchored = orderedExcursions
        .filter { it.anchorTripStopId == null || it.anchorTripStopId !in stopIds }
        .map { excursion ->
            excursion.toStoryExcursion(
                trip = trip,
                countriesByIso = countriesByIso,
                photoMap = excursionStopPhotoMap,
                dateFormatter = dateFormatter,
            )
        }

    val countryNames = (orderedStops.map { it.countryIso2 } + excursions.flatMap { excursion ->
        excursion.stops.map(ExcursionStop::countryIso2)
    })
        .filter(String::isNotBlank)
        .distinct()
        .map { iso2 -> countriesByIso[iso2]?.nameCa ?: iso2 }

    val linkedGroups = itineraryGroups
        .filter { group -> itinerary != null && group.itineraryId == itinerary.id }
        .sortedWith(compareBy(ItineraryGroup::sortOrder, ItineraryGroup::id))

    return TripStoryUiState(
        trip = trip,
        dateText = trip.dateRange?.let(dateFormatter::format),
        dayCountText = trip.dayCountText(),
        stopCount = orderedStops.size,
        countryNames = countryNames,
        routeText = orderedStops.joinToString(" → ") { it.displayTitle?.takeIf(String::isNotBlank) ?: it.locationName }
            .takeIf(String::isNotBlank),
        photoCount = storyStops.sumOf { stop ->
            stop.photos.size + stop.excursions.sumOf(TripStoryExcursionUiState::photoCount)
        } + unanchored.sumOf(TripStoryExcursionUiState::photoCount),
        itinerary = itinerary?.let {
            TripStoryItineraryUiState(
                title = it.title,
                notes = it.notes?.takeIf(String::isNotBlank),
                groupSummary = linkedGroups.takeIf(List<ItineraryGroup>::isNotEmpty)
                    ?.joinToString(" · ") { group -> group.title?.takeIf(String::isNotBlank) ?: "Tram ${group.sortOrder + 1}" },
            )
        },
        stops = storyStops,
        unanchoredExcursions = unanchored,
    )
}

private fun Excursion.toStoryExcursion(
    trip: Trip,
    countriesByIso: Map<String, Country>,
    photoMap: Map<String, List<StopPhoto>>,
    dateFormatter: FlexibleDateFormatter,
): TripStoryExcursionUiState =
    TripStoryExcursionUiState(
        id = id,
        title = title,
        notes = notes?.takeIf(String::isNotBlank),
        stops = stops.sortedWith(compareBy(ExcursionStop::sortOrder, ExcursionStop::id))
            .map { stop ->
                TripStoryExcursionStopUiState(
                    id = stop.id,
                    title = stop.locationName,
                    contextText = stop.contextText(countriesByIso, dateFormatter),
                    notes = stop.notes?.takeIf(String::isNotBlank),
                    photos = photoMap[stop.id].toStoryPhotos(
                        stopId = stop.id,
                        stopType = StopType.EXCURSION_STOP,
                        tripId = trip.id,
                        title = stop.locationName,
                        contextLabel = "EXCURSIÓ · $title",
                        dateText = stop.dateRange?.let(dateFormatter::format),
                    ),
                )
            },
    )

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

private fun TripStop.contextText(
    countriesByIso: Map<String, Country>,
    dateFormatter: FlexibleDateFormatter,
): String? = listOfNotNull(
    countriesByIso[countryIso2]?.nameCa ?: countryIso2.takeIf(String::isNotBlank),
    dateRange?.let(dateFormatter::format),
).joinToString(" · ").takeIf(String::isNotBlank)

private fun ExcursionStop.contextText(
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
