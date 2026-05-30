package com.atlas.domain.usecase.country

import com.atlas.domain.repository.CountryRepository

class SetCurrentlyLivingCountryUseCase(
    private val countryRepository: CountryRepository,
) {
    suspend operator fun invoke(countryIso2: String) {
        countryRepository.setCurrentlyLiving(countryIso2)
    }
}
