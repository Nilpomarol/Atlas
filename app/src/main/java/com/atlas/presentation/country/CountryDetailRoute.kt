package com.atlas.presentation.country

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atlas.app.AtlasApplication
import com.atlas.ui.screens.country.CountryDetailScreen

@Composable
fun CountryDetailRoute(
    iso2: String,
    onBackClick: () -> Unit,
    onTripClick: (String) -> Unit,
    onInfoClick: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as AtlasApplication
    val viewModel: CountryDetailViewModel = viewModel(
        key = "country-detail-$iso2",
        factory = CountryDetailViewModel.Factory(
            countryRepository = app.container.countryRepository,
            tripRepository = app.container.tripRepository,
            toggleWishedCountryUseCase = app.container.toggleWishedCountryUseCase,
            setCurrentlyLivingCountryUseCase = app.container.setCurrentlyLivingCountryUseCase,
            addCountryLogUseCase = app.container.addCountryLogUseCase,
            updateCountryLogUseCase = app.container.updateCountryLogUseCase,
            deleteCountryLogUseCase = app.container.deleteCountryLogUseCase,
            countryStateDerivationService = app.container.countryStateDerivationService,
            flexibleDateValidator = app.container.flexibleDateValidator,
            iso2 = iso2,
            flightRepository = app.container.flightRepository,
            itineraryRepository = app.container.itineraryRepository,
            airportRepository = app.container.airportRepository,
            excursionRepository = app.container.excursionRepository,
            countryStatRepository = app.container.countryStatRepository,
            countryLandscapePhotoRepository = app.container.countryLandscapePhotoRepository,
            currencyRateRepository = app.container.currencyRateRepository,
            stopPhotoRepository = app.container.stopPhotoRepository,
        ),
    )
    val uiState by viewModel.uiState.collectAsState()

    CountryDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onTripClick = onTripClick,
        onInfoClick = onInfoClick,
        onWishedChanged = viewModel::onWishedChanged,
        onSetCurrentlyLiving = viewModel::onSetCurrentlyLiving,
        onAddVisitLog = viewModel::onAddVisitLog,
        onEditLog = viewModel::onEditLog,
        onDeleteLog = viewModel::onDeleteLog,
        onDismissLogDraft = viewModel::onDismissLogDraft,
        onLogTypeChanged = viewModel::onLogTypeChanged,
        onLogPrecisionChanged = viewModel::onLogPrecisionChanged,
        onLogDraftFieldChanged = viewModel::onLogDraftFieldChanged,
        onLogNotesChanged = viewModel::onLogNotesChanged,
        onSaveLogDraft = viewModel::onSaveLogDraft,
    )
}
