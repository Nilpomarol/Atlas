package com.atlas.domain.usecase.trip

import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.repository.TripRepository

class CreateTripStopUseCase(
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(
        tripId: String,
        /** Set to nest this place under an existing stop; null puts it on the main route. */
        parentStopId: String? = null,
        sideTripLabel: String? = null,
        locationName: String,
        countryIso2: String,
        latitude: Double?,
        longitude: Double?,
        dateRange: FlexibleDateRange?,
        notes: String?,
    ) {
        tripRepository.createTripStop(
            tripId = tripId,
            parentStopId = parentStopId,
            sideTripLabel = sideTripLabel?.trim()?.ifBlank { null },
            locationName = locationName.trim(),
            countryIso2 = countryIso2,
            latitude = latitude,
            longitude = longitude,
            dateRange = dateRange,
            notes = notes?.trim()?.ifBlank { null },
        )
    }
}
