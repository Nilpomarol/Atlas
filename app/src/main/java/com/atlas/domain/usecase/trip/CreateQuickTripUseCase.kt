package com.atlas.domain.usecase.trip

import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.repository.TripRepository

class CreateQuickTripUseCase(
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(
        title: String,
        status: TravelStatus,
        dateRange: FlexibleDateRange?,
        locationName: String,
        countryIso2: String,
        latitude: Double?,
        longitude: Double?,
    ): String =
        tripRepository.createTripWithInitialStop(
            title = title.trim(),
            status = status,
            dateRange = dateRange,
            notes = null,
            isQuickTrip = true,
            locationName = locationName.trim(),
            countryIso2 = countryIso2.trim().uppercase(),
            latitude = latitude,
            longitude = longitude,
            stopNotes = null,
        )
}
