package com.atlas.data.backup

import com.atlas.domain.model.StopType

fun AtlasBackupV3.withAvailablePhotos(availableFilenames: Set<String>): AtlasBackupV3 {
    val availableRows = data.stopPhotos.filter { it.filename in availableFilenames }
    val tripIdByTripStopId = data.tripStops.associate { it.id to it.tripId }
    val tripIdByExcursionId = data.excursions.associate { it.id to it.tripId }
    val tripIdByExcursionStopId = data.excursionStops.mapNotNull { stop ->
        tripIdByExcursionId[stop.excursionId]?.let { tripId -> stop.id to tripId }
    }.toMap()

    val coverFilenamesByTripId = availableRows.mapNotNull { photo ->
        val tripId = when (photo.stopType) {
            StopType.TRIP_STOP.name -> tripIdByTripStopId[photo.stopId]
            StopType.EXCURSION_STOP.name -> tripIdByExcursionStopId[photo.stopId]
            else -> null
        }
        tripId?.let { it to photo.filename }
    }.groupBy(
        keySelector = { it.first },
        valueTransform = { it.second },
    ).mapValues { (_, filenames) -> filenames.toSet() }

    return copy(
        data = data.copy(
            trips = data.trips.map { trip ->
                trip.copy(
                    coverPhotoFilename = trip.coverPhotoFilename
                        ?.takeIf { it in coverFilenamesByTripId[trip.id].orEmpty() },
                )
            },
            stopPhotos = availableRows,
        ),
    )
}
