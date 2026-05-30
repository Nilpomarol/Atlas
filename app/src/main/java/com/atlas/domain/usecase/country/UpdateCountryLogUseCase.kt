package com.atlas.domain.usecase.country

import com.atlas.domain.model.CountryLog
import com.atlas.domain.repository.CountryRepository

class UpdateCountryLogUseCase(
    private val countryRepository: CountryRepository,
) {
    suspend operator fun invoke(log: CountryLog) {
        countryRepository.updateCountryLog(log)
    }
}
