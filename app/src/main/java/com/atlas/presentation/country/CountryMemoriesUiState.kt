package com.atlas.presentation.country

import com.atlas.domain.model.Excursion
import com.atlas.domain.model.StopPhoto
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.presentation.trip.PhotoViewerItemUiState
import com.atlas.presentation.trip.buildTripPhotoGalleryUiState

data class CountryMemoriesUiState(
    val trips: List<CountryMemoryTripUiState> = emptyList(),
) {
    val photoCount: Int
        get() = trips.sumOf { it.items.size }

    val viewerItems: List<PhotoViewerItemUiState>
        get() = trips.flatMap(CountryMemoryTripUiState::items)
}

data class CountryMemoryTripUiState(
    val tripId: String,
    val tripTitle: String,
    val tripDateText: String?,
    val items: List<PhotoViewerItemUiState>,
) {
    val locationNames: List<String>
        get() = items.map(PhotoViewerItemUiState::title).distinct()
}

internal fun buildCountryMemoriesUiState(
    countryIso2: String,
    trips: List<Trip>,
    tripStops: List<TripStop>,
    excursions: List<Excursion>,
    tripStopPhotoMap: Map<String, List<StopPhoto>>,
    excursionStopPhotoMap: Map<String, List<StopPhoto>>,
    dateFormatter: FlexibleDateFormatter = FlexibleDateFormatter(),
): CountryMemoriesUiState {
    val memoryTrips = trips
        .sortedWith(
            compareByDescending<Trip> { it.dateRange?.start?.toCountryMemorySortKey().orEmpty() }
                .thenBy { it.title }
                .thenBy { it.id },
        )
        .mapNotNull { trip ->
            val items = buildTripPhotoGalleryUiState(
                stops = tripStops.filter { it.tripId == trip.id },
                excursions = excursions.filter { it.tripId == trip.id },
                tripStopPhotoMap = tripStopPhotoMap,
                excursionStopPhotoMap = excursionStopPhotoMap,
                dateFormatter = dateFormatter,
            ).groups
                .filter { group -> group.countryIso2.equals(countryIso2, ignoreCase = true) }
                .flatMap { group ->
                    group.photos.map { photo ->
                        PhotoViewerItemUiState(
                            photo = photo,
                            stopId = group.stopId,
                            stopType = group.stopType,
                            title = group.title,
                            contextLabel = listOf(
                                "VIATGE · ${trip.title}",
                                group.contextLabel.takeUnless { it == "PARADA" },
                            ).filterNotNull().joinToString(" · "),
                            dateText = group.dateText
                                ?: trip.dateRange?.let(dateFormatter::format),
                            tripId = trip.id,
                        )
                    }
                }

            items.takeIf(List<PhotoViewerItemUiState>::isNotEmpty)?.let {
                CountryMemoryTripUiState(
                    tripId = trip.id,
                    tripTitle = trip.title,
                    tripDateText = trip.dateRange?.let(dateFormatter::format),
                    items = items,
                )
            }
        }

    return CountryMemoriesUiState(trips = memoryTrips)
}

private fun com.atlas.domain.model.FlexibleDate.toCountryMemorySortKey(): String {
    val yearPart = year.toString().padStart(4, '0')
    val monthPart = (month ?: 1).toString().padStart(2, '0')
    val dayPart = (day ?: 1).toString().padStart(2, '0')
    return "$yearPart-$monthPart-$dayPart"
}
