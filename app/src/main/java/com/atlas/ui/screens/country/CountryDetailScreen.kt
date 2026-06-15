package com.atlas.ui.screens.country

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.Country
import com.atlas.domain.model.CountryLog
import com.atlas.domain.model.CountryLogType
import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.StopPhoto
import com.atlas.presentation.country.CountryDetailUiState
import com.atlas.presentation.date.FlexibleDateRangeDraftField
import com.atlas.ui.components.PhotoViewerDialog
import com.atlas.ui.theme.AtlasBackground

@Composable
fun CountryDetailScreen(
    uiState: CountryDetailUiState,
    onBackClick: () -> Unit,
    onTripClick: (String) -> Unit,
    onInfoClick: () -> Unit,
    onWishedChanged: (Boolean) -> Unit,
    onSetCurrentlyLiving: () -> Unit,
    onAddVisitLog: () -> Unit,
    onEditLog: (CountryLog) -> Unit,
    onDeleteLog: (CountryLog) -> Unit,
    onDismissLogDraft: () -> Unit,
    onLogTypeChanged: (CountryLogType) -> Unit,
    onLogPrecisionChanged: (DatePrecision) -> Unit,
    onLogDraftFieldChanged: (FlexibleDateRangeDraftField, String) -> Unit,
    onLogNotesChanged: (String) -> Unit,
    onSaveLogDraft: () -> Unit,
) {
    val country = uiState.country
    var selectedMemoryPhotoId by rememberSaveable { mutableStateOf<String?>(null) }

    if (country == null) {
        LoadingScreen(onBackClick)
    } else {
        CountryDetailContent(
            country = country,
            uiState = uiState,
            onBackClick = onBackClick,
            onTripClick = onTripClick,
            onInfoClick = onInfoClick,
            onWishedChanged = onWishedChanged,
            onSetCurrentlyLiving = onSetCurrentlyLiving,
            onAddVisitLog = onAddVisitLog,
            onEditLog = onEditLog,
            onDeleteLog = onDeleteLog,
            onMemoryPhotoClick = { photo -> selectedMemoryPhotoId = photo.id },
        )
    }

    selectedMemoryPhotoId?.let { photoId ->
        PhotoViewerDialog(
            items = uiState.memories.viewerItems,
            initialPhotoId = photoId,
            coverPhotoFilename = null,
            onDismiss = { selectedMemoryPhotoId = null },
            onOpenSource = { item ->
                item.tripId?.let { tripId ->
                    selectedMemoryPhotoId = null
                    onTripClick(tripId)
                }
            },
        )
    }

    if (uiState.logDraft.isOpen) {
        CountryLogDialog(
            draft = uiState.logDraft,
            onDismiss = onDismissLogDraft,
            onTypeChanged = onLogTypeChanged,
            onPrecisionChanged = onLogPrecisionChanged,
            onFieldChanged = onLogDraftFieldChanged,
            onNotesChanged = onLogNotesChanged,
            onSave = onSaveLogDraft,
        )
    }
}

@Composable
private fun LoadingScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AtlasBackground)
            .padding(20.dp),
    ) {
        BackPill(onBackClick)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Carregant el país...", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun CountryDetailContent(
    country: Country,
    uiState: CountryDetailUiState,
    onBackClick: () -> Unit,
    onTripClick: (String) -> Unit,
    onInfoClick: () -> Unit,
    onWishedChanged: (Boolean) -> Unit,
    onSetCurrentlyLiving: () -> Unit,
    onAddVisitLog: () -> Unit,
    onEditLog: (CountryLog) -> Unit,
    onDeleteLog: (CountryLog) -> Unit,
    onMemoryPhotoClick: (StopPhoto) -> Unit,
) {
    val style = uiState.trackingState.toStyle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AtlasBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        CountryDetailHero(
            country = country,
            trackingState = uiState.trackingState,
            detailPills = uiState.detailPills,
            photoFilename = uiState.landscapePhotoFilename,
            photoAuthor = uiState.landscapePhotoAuthor,
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, top = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            CountryStatsCard(stats = uiState.kpiStats, onInfoClick = onInfoClick)
            CountryCurrencyCard(
                currencyCode = uiState.currencyCode,
                currencyName = uiState.currencyName,
                eurRate = uiState.eurRate,
                rateAge = uiState.rateAge,
            )
            CountryMapCard(country = country, style = style)
            if (uiState.memories.groups.isNotEmpty()) {
                CountryMemoriesSection(
                    memories = uiState.memories,
                    onTripClick = onTripClick,
                    onPhotoClick = onMemoryPhotoClick,
                )
            }
            CountryQuickActions(
                trackingState = uiState.trackingState,
                onWishedChanged = onWishedChanged,
                onSetCurrentlyLiving = onSetCurrentlyLiving,
                onAddVisitLog = onAddVisitLog,
            )
            CountryHistorySection(
                logs = uiState.logs,
                tripSummaries = uiState.tripSummaries,
                airTravelSummaries = uiState.airTravelSummaries,
                trackingState = uiState.trackingState,
                style = style,
                onTripClick = onTripClick,
                onEditLog = onEditLog,
                onDeleteLog = onDeleteLog,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

