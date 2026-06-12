package com.atlas.ui.screens.countryinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.atlas.presentation.country.CountryInfoUiState
import com.atlas.ui.components.AtlasCard
import com.atlas.ui.components.AtlasSectionLabel
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasPrimary

@Composable
fun CountryInfoScreen(
    uiState: CountryInfoUiState,
    onBackClick: () -> Unit,
) {
    if (uiState.country == null) {
        CountryInfoStatePage(
            onBackClick = onBackClick,
            loading = uiState.isLoading,
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AtlasBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        InfoHero(uiState, onBackClick)

        Column(
            modifier = Modifier.padding(horizontal = 16.dp).padding(top = 18.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            if (uiState.highlights.isNotEmpty()) HighlightsShelf(uiState.highlights)
            if (uiState.kpis.isNotEmpty()) {
                AtlasSectionLabel("Dades clau")
                KpiGrid(uiState.kpis)
            }
            if (uiState.sections.isEmpty()) {
                EmptyInfoCard()
            } else {
                AtlasSectionLabel("Informació per àmbits")
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.sections.forEachIndexed { i, s ->
                        SectionCard(s, defaultExpanded = i == 0, bordersMap = uiState.bordersMap)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CountryInfoStatePage(
    onBackClick: () -> Unit,
    loading: Boolean,
) {
    Column(Modifier.fillMaxSize().background(AtlasBackground)) {
        Box(Modifier.fillMaxWidth().height(72.dp).background(AtlasNavy)) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart).padding(8.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Enrere", tint = Color.White)
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (loading) CircularProgressIndicator(color = AtlasPrimary)
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (loading) "Carregant informació..." else "Informació no disponible",
                style = MaterialTheme.typography.bodyMedium,
                color = AtlasOnSurfaceMuted,
            )
        }
    }
}

@Composable
private fun EmptyInfoCard() {
    AtlasCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Informació no disponible",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AtlasOnSurfaceStrong,
            )
            Text(
                text = "Encara no hi ha dades de país per mostrar.",
                style = MaterialTheme.typography.bodyMedium,
                color = AtlasOnSurfaceMuted,
            )
        }
    }
}
