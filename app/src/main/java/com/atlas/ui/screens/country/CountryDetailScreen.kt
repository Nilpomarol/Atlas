package com.atlas.ui.screens.country

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.Country
import com.atlas.domain.model.CountryLog
import com.atlas.domain.model.CountryLogType
import com.atlas.domain.model.DatePrecision
import com.atlas.presentation.country.CountryDetailUiState
import com.atlas.presentation.date.FlexibleDateRangeDraftField
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasSurfaceRaised

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
) {
    val style = uiState.trackingState.toStyle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AtlasBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        CountryMapHero(
            country = country,
            style = style,
            trackingState = uiState.trackingState,
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .offset(y = (-42).dp)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            CountryIdentityHeader(
                country = country,
                style = style,
                trackingState = uiState.trackingState,
                detailPills = uiState.detailPills,
            )
            CountryInfoCard(
                country = country,
                trackingState = uiState.trackingState,
                style = style,
            )
            CountryInfoEntryCard(onClick = onInfoClick)
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

@Composable
private fun CountryInfoEntryCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = AtlasSurfaceRaised,
        border = BorderStroke(1.dp, AtlasOutline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(AtlasNavy, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Public,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White,
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Informació del país",
                    style = MaterialTheme.typography.titleSmall,
                    color = AtlasOnSurfaceStrong,
                )
                Text(
                    "Dades, rànquings i estadístiques",
                    style = MaterialTheme.typography.bodySmall,
                    color = AtlasOnSurfaceMuted,
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = AtlasOnSurfaceMuted,
            )
        }
    }
}
