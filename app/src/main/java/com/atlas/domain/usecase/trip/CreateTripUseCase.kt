package com.atlas.domain.usecase.trip

import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.repository.TripRepository

class CreateTripUseCase(
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(
        title: String,
        status: TravelStatus,
        dateRange: FlexibleDateRange?,
        notes: String?,
    ) {
        tripRepository.createTrip(
            title = title.trim(),
            status = status,
            dateRange = dateRange,
            notes = notes?.trim()?.ifBlank { null },
        )
    }
}
