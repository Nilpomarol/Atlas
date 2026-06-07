package com.atlas.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.atlas.presentation.dashboard.DashboardUiState
import com.atlas.ui.components.AtlasCard
import com.atlas.ui.components.geo.AtlasGeoCanvas
import com.atlas.ui.components.geo.GeoViewport
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasLived
import com.atlas.ui.theme.AtlasLiving
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPlanned
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasVisited
import com.atlas.ui.theme.AtlasWished

private val DashboardWorldViewport = GeoViewport.World(minLatitudeDeg = -45.0, maxLatitudeDeg = 72.0)

@Composable
internal fun DashboardMapHero(uiState: DashboardUiState) {
    val highlightColorByIso2 = remember(
        uiState.livingIso2s, uiState.livedIso2s, uiState.visitedIso2s,
        uiState.plannedIso2s, uiState.wishedIso2s,
    ) {
        buildMap {
            uiState.wishedIso2s.forEach { put(it, AtlasWished) }
            uiState.plannedIso2s.forEach { put(it, AtlasPlanned) }
            uiState.visitedIso2s.forEach { put(it, AtlasVisited) }
            uiState.livedIso2s.forEach { put(it, AtlasLived) }
            uiState.livingIso2s.forEach { put(it, AtlasLiving) }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AtlasBackground)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        AtlasGeoCanvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.85f)
                .clipToBounds(),
            viewport = DashboardWorldViewport,
            highlightColorByIso2 = highlightColorByIso2,
            highlightFillAlpha = 1f,
            highlightStrokeAlpha = 1f,
            markers = emptyList(),
            mapPaddingDp = 6f,
        )
        DashboardHeroStatsCard(
            uiState = uiState,
            modifier = Modifier
                .padding(top = 10.dp)
                .padding(horizontal = 20.dp),
        )
    }
}

@Composable
private fun DashboardHeroStatsCard(uiState: DashboardUiState, modifier: Modifier = Modifier) {
    AtlasCard(
        modifier = modifier,
        contentPadding = PaddingValues(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                HeroPrimaryMetric(
                    modifier = Modifier.weight(1f),
                    value = uiState.visitedCount.toString(),
                    suffix = "/${uiState.trackableCountryCount}",
                    label = "Territoris visitats",
                    accent = AtlasVisited,
                )
                HeroPrimaryMetric(
                    modifier = Modifier.weight(1f),
                    value = "%.0f".format(uiState.worldPercentage),
                    suffix = "%",
                    label = "${uiState.visitedContinentCount}/7 continents",
                    accent = AtlasPrimary,
                    alignEnd = true,
                )
            }
            HeroKpiRow(uiState = uiState)
        }
    }
}

@Composable
private fun HeroPrimaryMetric(
    value: String,
    suffix: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                color = accent,
                maxLines = 1,
            )
            Text(
                text = suffix,
                modifier = Modifier.padding(start = 3.dp, bottom = 6.dp),
                style = MaterialTheme.typography.titleMedium,
                color = AtlasOnSurfaceMuted,
                maxLines = 1,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AtlasOnSurfaceMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun HeroKpiRow(uiState: DashboardUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HeroKpiItem(Modifier.weight(1f), uiState.visitedCount, "Visitats", AtlasVisited)
        HeroKpiItem(Modifier.weight(1f), uiState.livedCount, "Viscuts", AtlasLived)
        HeroKpiItem(Modifier.weight(1f), uiState.plannedCount, "Plans", AtlasPlanned)
        HeroKpiItem(Modifier.weight(1f), uiState.wishedCount, "Desitjats", AtlasWished)
    }
}

@Composable
private fun HeroKpiItem(modifier: Modifier, value: Int, label: String, color: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = AtlasSurface.copy(alpha = 0.76f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.18f)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = AtlasOnSurfaceMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// unused — kept as pre-existing dead code
@Composable
private fun DashboardHeroStats(uiState: DashboardUiState, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = uiState.visitedCount.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = AtlasOnSurfaceStrong,
                )
                Text(
                    text = "/${uiState.trackableCountryCount}",
                    modifier = Modifier.padding(start = 2.dp, bottom = 6.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = AtlasOnSurfaceMuted,
                )
                Text(
                    text = "· ${"%.0f".format(uiState.worldPercentage)}% del món · ${uiState.visitedContinentCount}/7 continents",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineLarge,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                )
            }
            HeroKpiRow(uiState = uiState)
        }
    }
}
