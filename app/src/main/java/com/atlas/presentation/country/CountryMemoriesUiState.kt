package com.atlas.presentation.country

import com.atlas.domain.model.Excursion
import com.atlas.domain.model.StopPhoto
import com.atlas.domain.model.StopType
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.presentation.trip.PhotoViewerItemUiState
import com.atlas.presentation.trip.buildTripPhotoGalleryUiState

data class CountryMemoriesUiState(
    val groups: List<CountryMemoryGroupUiState> = emptyList(),
) {
    val photoCount: Int
        get() = groups.sumOf { it.photos.size }

    val viewerItems: List<PhotoViewerItemUiState>
        get() = groups.flatMap { group ->
            group.photos.map { photo ->
                PhotoViewerItemUiState(
                    photo = photo,
                    stopId = group.stopId,
                    stopType = group.stopType,
                    title = group.locationName,
                    contextLabel = group.viewerContextLabel,
                    dateText = group.stopDateText ?: group.tripDateText,
                    tripId = group.tripId,
                )
            }
        }
}

data class CountryMemoryGroupUiState(
    val tripId: String,
    val tripTitle: String,
    val tripDateText: String?,
    val stopId: String,
    val stopType: StopType,
    val locationName: String,
    val sourceLabel: String,
    val stopDateText: String?,
    val photos: List<StopPhoto>,
) {
    val viewerContextLabel: String
        get() = listOf("VIATGE · $tripTitle", sourceLabel.takeUnless { it == "PARADA" })
            .filterNotNull()
            .joinToString(" · ")
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
    val groups = trips
        .sortedWith(
            compareByDescending<Trip> { it.dateRange?.start?.toCountryMemorySortKey().orEmpty() }
                .thenBy { it.title }
                .thenBy { it.id },
        )
        .flatMap { trip ->
            buildTripPhotoGalleryUiState(
                stops = tripStops.filter { it.tripId == trip.id },
                excursions = excursions.filter { it.tripId == trip.id },
                tripStopPhotoMap = tripStopPhotoMap,
                excursionStopPhotoMap = excursionStopPhotoMap,
                dateFormatter = dateFormatter,
            ).groups
                .filter { group -> group.countryIso2.equals(countryIso2, ignoreCase = true) }
                .map { group ->
                    CountryMemoryGroupUiState(
                        tripId = trip.id,
                        tripTitle = trip.title,
                        tripDateText = trip.dateRange?.let(dateFormatter::format),
                        stopId = group.stopId,
                        stopType = group.stopType,
                        locationName = group.title,
                        sourceLabel = group.contextLabel,
                        stopDateText = group.dateText,
                        photos = group.photos,
                    )
                }
        }

    return CountryMemoriesUiState(groups = groups)
}

private fun com.atlas.domain.model.FlexibleDate.toCountryMemorySortKey(): String {
    val yearPart = year.toString().padStart(4, '0')
    val monthPart = (month ?: 1).toString().padStart(2, '0')
    val dayPart = (day ?: 1).toString().padStart(2, '0')
    return "$yearPart-$monthPart-$dayPart"
}
