package com.atlas.domain.usecase.excursion

import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.repository.ExcursionRepository

class CreateExcursionStopUseCase(
    private val excursionRepository: ExcursionRepository,
) {
    suspend operator fun invoke(
        excursionId: String,
        locationName: String,
        countryIso2: String,
        latitude: Double?,
        longitude: Double?,
        dateRange: FlexibleDateRange?,
        notes: String?,
    ) {
        excursionRepository.createExcursionStop(
            excursionId = excursionId,
            locationName = locationName.trim(),
            countryIso2 = countryIso2,
            latitude = latitude,
            longitude = longitude,
            dateRange = dateRange,
            notes = notes?.trim()?.ifBlank { null },
        )
    }
}
