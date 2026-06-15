package com.atlas.presentation.trip

import com.atlas.domain.model.Excursion
import com.atlas.domain.model.StopPhoto
import com.atlas.domain.model.StopType
import com.atlas.domain.model.TripStop
import com.atlas.domain.service.FlexibleDateFormatter

data class TripPhotoGalleryUiState(
    val groups: List<TripPhotoGroupUiState> = emptyList(),
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
                    title = group.title,
                    contextLabel = group.contextLabel,
                    dateText = group.dateText,
                )
            }
        }
}

data class PhotoViewerItemUiState(
    val photo: StopPhoto,
    val stopId: String,
    val stopType: StopType,
    val title: String,
    val contextLabel: String,
    val dateText: String?,
)

data class TripPhotoGroupUiState(
    val stopId: String,
    val stopType: StopType,
    val title: String,
    val contextLabel: String,
    val countryIso2: String?,
    val dateText: String?,
    val photos: List<StopPhoto>,
)

internal fun buildTripPhotoGalleryUiState(
    stops: List<TripStop>,
    excursions: List<Excursion>,
    tripStopPhotoMap: Map<String, List<StopPhoto>>,
    excursionStopPhotoMap: Map<String, List<StopPhoto>>,
    dateFormatter: FlexibleDateFormatter = FlexibleDateFormatter(),
): TripPhotoGalleryUiState {
    val groups = buildList {
        val orderedStops = stops.sortedWith(compareBy(TripStop::sortOrder, TripStop::id))
        val orderedExcursions = excursions.sortedWith(compareBy(Excursion::sortOrder, Excursion::id))
        val stopIds = orderedStops.mapTo(mutableSetOf()) { it.id }
        val anchoredExcursions = orderedExcursions.groupBy { it.anchorTripStopId }

        orderedStops.forEach { stop ->
            tripStopPhotoMap[stop.id]
                .toOrderedPhotos()
                .takeIf { it.isNotEmpty() }
                ?.let { photos ->
                    add(
                        TripPhotoGroupUiState(
                            stopId = stop.id,
                            stopType = StopType.TRIP_STOP,
                            title = stop.displayTitle?.takeIf(String::isNotBlank) ?: stop.locationName,
                            contextLabel = "PARADA",
                            countryIso2 = stop.countryIso2.takeIf(String::isNotBlank),
                            dateText = stop.dateRange?.let(dateFormatter::format),
                            photos = photos,
                        ),
                    )
                }

            anchoredExcursions[stop.id].orEmpty().forEach { excursion ->
                addExcursionGroups(excursion, excursionStopPhotoMap, dateFormatter)
            }
        }

        orderedExcursions
            .filter { it.anchorTripStopId == null || it.anchorTripStopId !in stopIds }
            .forEach { excursion ->
                addExcursionGroups(excursion, excursionStopPhotoMap, dateFormatter)
            }
    }

    return TripPhotoGalleryUiState(groups = groups)
}

private fun MutableList<TripPhotoGroupUiState>.addExcursionGroups(
    excursion: Excursion,
    photoMap: Map<String, List<StopPhoto>>,
    dateFormatter: FlexibleDateFormatter,
) {
    excursion.stops
        .sortedWith(compareBy({ it.sortOrder }, { it.id }))
        .forEach { stop ->
            val photos = photoMap[stop.id].toOrderedPhotos()
            if (photos.isEmpty()) return@forEach

            add(
                TripPhotoGroupUiState(
                    stopId = stop.id,
                    stopType = StopType.EXCURSION_STOP,
                    title = stop.locationName,
                    contextLabel = "EXCURSIÓ · ${excursion.title}",
                    countryIso2 = stop.countryIso2.takeIf(String::isNotBlank),
                    dateText = stop.dateRange?.let(dateFormatter::format),
                    photos = photos,
                ),
            )
        }
}

private fun List<StopPhoto>?.toOrderedPhotos(): List<StopPhoto> =
    orEmpty().sortedWith(compareBy(StopPhoto::sortOrder, StopPhoto::id))
