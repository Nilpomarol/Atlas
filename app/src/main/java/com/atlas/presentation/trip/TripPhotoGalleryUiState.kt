package com.atlas.presentation.trip

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
                    tripId = group.tripId,
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
    val tripId: String? = null,
)

data class TripPhotoGroupUiState(
    val tripId: String,
    val stopId: String,
    val stopType: StopType,
    val title: String,
    val contextLabel: String,
    val countryIso2: String?,
    val dateText: String?,
    val photos: List<StopPhoto>,
)

/**
 * Walks the trip in narrative order: each main-route stop, then the places visited from
 * it. Nested stops whose parent is missing fall back to the end of the walk.
 */
internal fun buildTripPhotoGalleryUiState(
    stops: List<TripStop>,
    stopPhotoMap: Map<String, List<StopPhoto>>,
    dateFormatter: FlexibleDateFormatter = FlexibleDateFormatter(),
): TripPhotoGalleryUiState {
    val ordered = stops.sortedWith(compareBy(TripStop::sortOrder, TripStop::id))
    val mainStops = ordered.filter { it.parentStopId == null }
    val mainStopIds = mainStops.mapTo(mutableSetOf()) { it.id }
    val childrenByParent = ordered
        .filter { it.parentStopId != null }
        .groupBy { it.parentStopId }

    val groups = buildList {
        mainStops.forEach { stop ->
            addStopGroup(stop, stopPhotoMap, dateFormatter)
            childrenByParent[stop.id].orEmpty().forEach { child ->
                addStopGroup(child, stopPhotoMap, dateFormatter)
            }
        }

        ordered
            .filter { it.parentStopId != null && it.parentStopId !in mainStopIds }
            .forEach { orphan -> addStopGroup(orphan, stopPhotoMap, dateFormatter) }
    }

    return TripPhotoGalleryUiState(groups = groups)
}

private fun MutableList<TripPhotoGroupUiState>.addStopGroup(
    stop: TripStop,
    photoMap: Map<String, List<StopPhoto>>,
    dateFormatter: FlexibleDateFormatter,
) {
    val photos = photoMap[stop.id].toOrderedPhotos()
    if (photos.isEmpty()) return

    add(
        TripPhotoGroupUiState(
            tripId = stop.tripId,
            stopId = stop.id,
            stopType = StopType.TRIP_STOP,
            title = stop.displayTitle?.takeIf(String::isNotBlank) ?: stop.locationName,
            contextLabel = stop.contextLabel(),
            countryIso2 = stop.countryIso2.takeIf(String::isNotBlank),
            dateText = stop.dateRange?.let(dateFormatter::format),
            photos = photos,
        ),
    )
}

private fun TripStop.contextLabel(): String = when {
    parentStopId == null -> "PARADA"
    !sideTripLabel.isNullOrBlank() -> "SORTIDA · $sideTripLabel"
    else -> "SORTIDA"
}

private fun List<StopPhoto>?.toOrderedPhotos(): List<StopPhoto> =
    orEmpty().sortedWith(compareBy(StopPhoto::sortOrder, StopPhoto::id))
