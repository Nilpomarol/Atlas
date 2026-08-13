package com.atlas.ui.rework.screens.countries

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atlas.app.AtlasAppContainer
import com.atlas.presentation.country.CountryDetailViewModel
import com.atlas.presentation.country.CountryListViewModel

@Composable
fun ReworkCountriesRoute(
    container: AtlasAppContainer,
    onCountryOpened: (String) -> Unit,
) {
    val viewModel: CountryListViewModel = viewModel(
        factory = CountryListViewModel.Factory(
            countryRepository = container.countryRepository,
            tripRepository = container.tripRepository,
            flightRepository = container.flightRepository,
            itineraryRepository = container.itineraryRepository,
            airportRepository = container.airportRepository,
            countryStatRepository = container.countryStatRepository,
            countryStateDerivationService = container.countryStateDerivationService,
        ),
    )
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    ReworkCountriesScreen(
        state = state,
        onSearchChanged = viewModel::onSearchQueryChanged,
        onFilterSelected = viewModel::onFilterSelected,
        onSortSelected = viewModel::onSortSelected,
        onSortDirectionToggled = viewModel::onSortDirectionToggled,
        onCountryOpened = onCountryOpened,
    )
}

@Composable
fun ReworkCountryDetailRoute(
    container: AtlasAppContainer,
    iso2: String,
    onBack: () -> Unit,
) {
    val viewModel: CountryDetailViewModel = viewModel(
        key = "rework-country-$iso2",
        factory = CountryDetailViewModel.Factory(
            countryRepository = container.countryRepository,
            tripRepository = container.tripRepository,
            toggleWishedCountryUseCase = container.toggleWishedCountryUseCase,
            setCurrentlyLivingCountryUseCase = container.setCurrentlyLivingCountryUseCase,
            addCountryLogUseCase = container.addCountryLogUseCase,
            updateCountryLogUseCase = container.updateCountryLogUseCase,
            deleteCountryLogUseCase = container.deleteCountryLogUseCase,
            countryStateDerivationService = container.countryStateDerivationService,
            flexibleDateValidator = container.flexibleDateValidator,
            iso2 = iso2,
            flightRepository = container.flightRepository,
            itineraryRepository = container.itineraryRepository,
            airportRepository = container.airportRepository,
            countryStatRepository = container.countryStatRepository,
            countryLandscapePhotoRepository = container.countryLandscapePhotoRepository,
            currencyRateRepository = container.currencyRateRepository,
            stopPhotoRepository = container.stopPhotoRepository,
            countryMemoriesPreferencesRepository = container.countryMemoriesPreferencesRepository,
        ),
    )
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    ReworkCountryDetailScreen(
        state = state,
        onBack = onBack,
    )
}
