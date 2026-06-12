package com.atlas.presentation.country

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atlas.app.AtlasApplication
import com.atlas.ui.screens.countryinfo.CountryInfoScreen

@Composable
fun CountryInfoRoute(
    iso2: String,
    onBackClick: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as AtlasApplication
    val viewModel: CountryInfoViewModel = viewModel(
        key = "country-info-$iso2",
        factory = CountryInfoViewModel.Factory(
            countryRepository = app.container.countryRepository,
            countryStatRepository = app.container.countryStatRepository,
            countryPhotoRepository = app.container.countryPhotoRepository,
            iso2 = iso2,
        ),
    )
    val uiState by viewModel.uiState.collectAsState()

    CountryInfoScreen(
        uiState = uiState,
        onBackClick = onBackClick,
    )
}
