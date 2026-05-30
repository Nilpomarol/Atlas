package com.atlas.domain.usecase.country

import com.atlas.domain.model.CountryLogType
import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.repository.CountryRepository

class AddCountryLogUseCase(
    private val countryRepository: CountryRepository,
) {
    suspend operator fun invoke(
        countryIso2: String,
        type: CountryLogType,
        dateRange: FlexibleDateRange?,
        notes: String?,
    ) {
        countryRepository.addCountryLog(
            countryIso2 = countryIso2,
            type = type,
            dateRange = dateRange,
            notes = notes,
        )
    }
}
