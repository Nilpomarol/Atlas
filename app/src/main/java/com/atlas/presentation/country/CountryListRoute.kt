package com.atlas.presentation.country

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atlas.app.AtlasApplication
import com.atlas.ui.screens.country.CountryListScreen

@Composable
fun CountryListRoute(
    onCountryClick: (String) -> Unit,
) {
    val app = LocalContext.current.applicationContext as AtlasApplication
    val viewModel: CountryListViewModel = viewModel(
        factory = CountryListViewModel.Factory(
            countryRepository = app.container.countryRepository,
            tripRepository = app.container.tripRepository,
            flightRepository = app.container.flightRepository,
            itineraryRepository = app.container.itineraryRepository,
            excursionRepository = app.container.excursionRepository,
            airportRepository = app.container.airportRepository,
            countryStatRepository = app.container.countryStatRepository,
            countryStateDerivationService = app.container.countryStateDerivationService,
        ),
    )
    val uiState by viewModel.uiState.collectAsState()

    CountryListScreen(
        uiState = uiState,
        onCountryClick = onCountryClick,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onFilterSelected = viewModel::onFilterSelected,
        onSortSelected = viewModel::onSortSelected,
        onSortDirectionToggled = viewModel::onSortDirectionToggled,
    )
}
