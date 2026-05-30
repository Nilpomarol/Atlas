package com.atlas.domain.usecase.country

import com.atlas.domain.repository.CountryRepository

class ToggleWishedCountryUseCase(
    private val countryRepository: CountryRepository,
) {
    suspend operator fun invoke(
        countryIso2: String,
        wished: Boolean,
    ) {
        countryRepository.setWished(
            countryIso2 = countryIso2,
            wished = wished,
        )
    }
}
