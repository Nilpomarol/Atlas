package com.atlas.ui.screens.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalAirport
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.atlas.presentation.stats.StatsAircraftRank
import com.atlas.presentation.stats.StatsAirlineRank
import com.atlas.presentation.stats.StatsAirportRank
import com.atlas.presentation.stats.BadgeTier
import com.atlas.presentation.stats.StatsBadge
import com.atlas.presentation.stats.StatsCompletionTier
import com.atlas.presentation.stats.StatsContinent
import com.atlas.presentation.stats.StatsCountryStamp
import com.atlas.presentation.stats.StatsCountryState
import com.atlas.presentation.stats.StatsDelayBucket
import com.atlas.presentation.stats.StatsMonthStat
import com.atlas.presentation.stats.StatsRank
import com.atlas.presentation.stats.StatsRecord
import com.atlas.presentation.stats.StatsRouteRank
import com.atlas.presentation.stats.StatsTopDelay
import com.atlas.presentation.stats.StatsTripVisual
import com.atlas.presentation.stats.StatsUiState
import com.atlas.presentation.stats.StatsSeasonStat
import com.atlas.presentation.stats.StatsYearStat
import com.atlas.ui.components.AirlineLogo
import com.atlas.ui.components.AtlasCard
import com.atlas.ui.components.AtlasPage
import com.atlas.ui.components.AtlasSectionLabel
import com.atlas.ui.components.CountryFlag
import com.atlas.ui.components.geo.AtlasGeoCanvas
import com.atlas.ui.components.geo.GeoCoordinate
import com.atlas.ui.components.geo.GeoRouteSegment
import com.atlas.ui.components.geo.GeoViewport
import com.atlas.ui.theme.AtlasCompleted
import com.atlas.ui.theme.AtlasError
import com.atlas.ui.theme.AtlasGold
import com.atlas.ui.theme.AtlasInProgress
import com.atlas.ui.theme.AtlasLived
import com.atlas.ui.theme.AtlasLiving
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasOnSurfaceFaint
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceSoft
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPlanned
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasSurfaceSubtle
import com.atlas.ui.theme.AtlasVisited
import com.atlas.ui.theme.AtlasWished
import java.io.File
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

private val WorldViewport = GeoViewport.World(minLatitudeDeg = -45.0, maxLatitudeDeg = 72.0)
private val NumberFormatter = NumberFormat.getIntegerInstance(Locale("ca", "ES"))

@Composable
fun StatsScreen(uiState: StatsUiState, onTimelineClick: () -> Unit = {}) {
    var selectedTab by rememberSaveable { mutableStateOf(StatsTab.Summary) }

    AtlasPage(contentPadding = PaddingValues(0.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            StatsHeader()
            StatsTabRow(selectedTab = selectedTab, onSelected = { selectedTab = it })
            if (selectedTab == StatsTab.Map) {
                MapTab(uiState = uiState, modifier = Modifier.weight(1f))
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    when (selectedTab) {
                        StatsTab.Summary -> SummaryTab(uiState, onTimelineClick)
                        StatsTab.Map -> Unit
                        StatsTab.Countries -> CountriesTab(uiState)
                        StatsTab.Trips -> TripsTab(uiState)
                        StatsTab.Flights -> FlightsTab(uiState)
                        StatsTab.Badges -> BadgesTab(uiState)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Estadístiques",
                style = MaterialTheme.typography.headlineSmall,
                color = AtlasOnSurfaceStrong,
            )
            Text(
                text = "El teu atlas en xifres, rutes i records visuals.",
                style = MaterialTheme.typography.labelMedium,
                color = AtlasOnSurfaceMuted,
            )
        }
        IconBadge(icon = Icons.Filled.Public, color = AtlasPrimary)
    }
}

@Composable
private fun StatsTabRow(selectedTab: StatsTab, onSelected: (StatsTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        StatsTab.entries.forEach { tab ->
            StatsTabPill(
                label = tab.label,
                selected = selectedTab == tab,
                onClick = { onSelected(tab) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatsTabPill(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(34.dp),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) AtlasNavy else AtlasSurface,
        border = BorderStroke(1.dp, if (selected) AtlasNavy else AtlasOutline),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.Bold,
                color = if (selected) AtlasSurface else AtlasOnSurfaceSoft,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SummaryTab(uiState: StatsUiState, onTimelineClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        WorldHero(uiState)
        NextMilestonesCard(uiState)
        MetricMosaic(uiState)
        Section("Ritme anual") { YearPulseChart(uiState.yearStats, onTimelineClick) }
        if (uiState.tripVisuals.any { it.coverPhotoFilename != null }) {
            Section("Records amb portada") {
                TripVisualRail(uiState.tripVisuals.filter { it.coverPhotoFilename != null })
            }
        }
        RecordsPreview(uiState.recordCards.take(4))
    }
}

@Composable
private fun MapTab(uiState: StatsUiState, modifier: Modifier = Modifier) {
    var filters by remember { mutableStateOf(MapLayerFilters()) }
    Box(modifier = modifier.fillMaxSize()) {
        StatsMapCanvas(
            uiState = uiState,
            filters = filters,
            modifier = Modifier.matchParentSize(),
        )
        MapFilterOverlay(
            filters = filters,
            onFiltersChange = { filters = it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )
    }
}

@Composable
private fun CountriesTab(uiState: StatsUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CountriesHeroCard(uiState)
        Section("Col·lecció de països") {
            CountryFlagGrid(uiState.countryStamps)
        }
        Section("Continents") {
            ContinentProgressCard(uiState.continentStats)
        }
        Section("Països amb més activitat") {
            RankedCountryCard(uiState.countryRanks)
        }
        Section("Rècords de països") {
            RecordGrid(uiState.countryRecords)
        }
    }
}

@Composable
private fun CountriesHeroCard(uiState: StatsUiState) {
    AtlasCard(modifier = Modifier.padding(horizontal = 20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${uiState.visitedCountries}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = AtlasVisited,
                        )
                        Text(
                            text = " / ${uiState.totalCountries}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = AtlasOnSurfaceMuted,
                            modifier = Modifier.padding(bottom = 3.dp),
                        )
                    }
                    Text(
                        text = "PAÏSOS VISITATS",
                        style = MaterialTheme.typography.labelSmall,
                        color = AtlasOnSurfaceMuted,
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "${uiState.worldPercentage.roundToInt()}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = AtlasOnSurfaceStrong,
                    )
                    Text(
                        text = "DEL MÓN",
                        style = MaterialTheme.typography.labelSmall,
                        color = AtlasOnSurfaceMuted,
                    )
                }
            }
            StackedProgressBar(
                primary = uiState.visitedCountries,
                secondary = uiState.plannedCountries,
                total = uiState.totalCountries,
                height = 10.dp,
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                CountriesHeroStat("Viscuts", uiState.livedCountries, AtlasLived, Modifier.weight(1f))
                CountriesHeroStat("Continents", uiState.visitedContinents, AtlasNavy, Modifier.weight(1f))
                CountriesHeroStat("Plans", uiState.plannedCountries, AtlasPlanned, Modifier.weight(1f))
                CountriesHeroStat("Desitjats", uiState.wishedCountries, AtlasWished, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CountriesHeroStat(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
            color = AtlasOnSurfaceMuted,
        )
    }
}

@Composable
private fun TripsTab(uiState: StatsUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Section("Viatges") {
            TripStatusPanel(uiState)
        }
        Section("Postals") {
            TripVisualRail(uiState.tripVisuals)
        }
        Section("Viatges per mes") {
            TripMonthChart(uiState.tripMonthStats)
        }
        Section("Viatges per any") {
            TripYearChart(uiState.yearStats)
        }
        Section("Temporades") {
            TripSeasonCard(uiState.tripSeasonStats)
        }
        Section("Rècords de viatge") {
            RecordGrid(uiState.tripRecords)
        }
    }
}

@Composable
private fun FlightsTab(uiState: StatsUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Section("Bitàcola aèria") {
            FlightHeroPanel(uiState)
        }
        Section("Vols per any") {
            FlightYearChart(uiState.yearStats)
        }
        Section("Dia i nit") {
            NightDayCard(uiState.nightFlightCount, uiState.dayFlightCount)
        }
        Section("Companyies principals") {
            TopAirlineBars(uiState.topAirlines)
        }
        Section("Aeronau") {
            TopAircraftCard(uiState.topAircraft)
        }
        Section("Puntualitat") {
            DelayDistributionCard(uiState.delayBuckets)
        }
        Section("Abast dels vols") {
            FlightScopeCard(uiState)
        }
        Section("Retards destacats") {
            TopDelayChart(uiState.topDelayStats)
        }
        Section("Rutes principals") {
            TopRoutesChart(uiState.topRoutes)
        }
        Section("Aeroports principals") {
            TopAirportsChart(uiState.topAirports)
        }
        Section("Rècords de vol") {
            RecordGrid(uiState.flightRecords)
        }
    }
}

@Composable
private fun BadgesTab(uiState: StatsUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Section("Progrés") {
            BadgeCompletionCard(uiState.badges)
        }
        Section("Totes les insígnies") {
            BadgeGrid(uiState.badges)
        }
    }
}

@Composable
private fun CountryStat(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AtlasOnSurfaceMuted,
        )
    }
}

@Composable
private fun WorldHero(uiState: StatsUiState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AtlasGeoCanvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.72f)
                .clipToBounds(),
            viewport = WorldViewport,
            highlightColorByIso2 = uiState.highlightColors(),
            highlightFillAlpha = 1f,
            highlightStrokeAlpha = 1f,
            markers = emptyList(),
            mapPaddingDp = 6f,
        )
        AtlasCard(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            contentPadding = PaddingValues(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RingMetric(
                    value = uiState.worldPercentage / 100f,
                    centerText = "${uiState.worldPercentage.roundToInt()}%",
                    label = "del món",
                    color = AtlasPrimary,
                )
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${uiState.visitedCountries}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = AtlasVisited,
                            )
                            Text(
                                text = "/${uiState.totalCountries}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = AtlasOnSurfaceMuted,
                            )
                        }
                        Text(
                            text = "països visitats",
                            style = MaterialTheme.typography.bodySmall,
                            color = AtlasOnSurfaceMuted,
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CountryStat("Viscuts", uiState.livedCountries, AtlasLived, Modifier.weight(1f))
                        CountryStat("Planejats", uiState.plannedCountries, AtlasPlanned, Modifier.weight(1f))
                        CountryStat("Desitjats", uiState.wishedCountries, AtlasWished, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun NextMilestonesCard(uiState: StatsUiState) {
    val upcoming = (
        uiState.badges.filter { it.tier != null && it.nextGoal != null } +
        uiState.badges.filter { !it.unlocked }
    ).distinctBy { it.title }.take(3)
    if (upcoming.isEmpty()) return

    AtlasCard(modifier = Modifier.padding(horizontal = 20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "PROPERES FITES",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AtlasOnSurfaceMuted,
            )
            upcoming.forEach { badge ->
                val accentColor = badge.tier?.color() ?: AtlasOnSurfaceSoft
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (badge.tier != null) Icons.Filled.EmojiEvents else Icons.Filled.Flag,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(17.dp),
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = badge.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AtlasOnSurfaceStrong,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = if (badge.nextGoal != null) "${badge.detail} → ${badge.nextGoal}" else badge.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = AtlasOnSurfaceMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (badge.progress != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(accentColor.copy(alpha = 0.18f)),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth((badge.progress ?: 0f).coerceIn(0f, 1f))
                                        .height(5.dp)
                                        .background(accentColor),
                                )
                            }
                        }
                    }
                    if (badge.tier != null) {
                        Text(
                            text = badge.tier.label(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricMosaic(uiState: StatsUiState) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            CompactMetric("Viatges", uiState.tripCount.formatInt(), Icons.Filled.Luggage, AtlasPrimary, Modifier.weight(1f))
            CompactMetric("Vols", uiState.flightCount.formatInt(), Icons.Filled.FlightTakeoff, AtlasPlanned, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            CompactMetric("Km volats", uiState.flownDistanceKm.formatKm(), Icons.Filled.Public, AtlasVisited, Modifier.weight(1f))
            CompactMetric("Aeroports", uiState.uniqueAirportCount.formatInt(), Icons.Filled.LocalAirport, AtlasGold, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            CompactMetric("Dies de ruta", uiState.daysTraveled.formatInt(), Icons.Filled.CalendarMonth, AtlasLived, Modifier.weight(1f))
            CompactMetric("Fotos", uiState.photoCount.formatInt(), Icons.Filled.CameraAlt, AtlasWished, Modifier.weight(1f))
        }
    }
}

@Composable
private fun CompactMetric(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(88.dp),
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, color.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier.padding(11.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                color = AtlasOnSurfaceMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MapFilterOverlay(
    filters: MapLayerFilters,
    onFiltersChange: (MapLayerFilters) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (expanded) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AtlasSurface.copy(alpha = 0.97f),
                border = BorderStroke(1.dp, AtlasOutline),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Row {
                        FilterToggleRow("Vols completats", AtlasPrimary, filters.completedFlights, Modifier.weight(1f)) {
                            onFiltersChange(filters.copy(completedFlights = !filters.completedFlights))
                        }
                        FilterToggleRow("Vols planificats", AtlasPlanned, filters.plannedFlights, Modifier.weight(1f)) {
                            onFiltersChange(filters.copy(plannedFlights = !filters.plannedFlights))
                        }
                    }
                    Row {
                        FilterToggleRow("Parades de viatge", AtlasVisited, filters.tripStops, Modifier.weight(1f)) {
                            onFiltersChange(filters.copy(tripStops = !filters.tripStops))
                        }
                        FilterToggleRow("Parades d'excursió", AtlasLived, filters.excursionStops, Modifier.weight(1f)) {
                            onFiltersChange(filters.copy(excursionStops = !filters.excursionStops))
                        }
                    }
                    Row {
                        FilterToggleRow("Països visitats", AtlasVisited, filters.countriesVisited, Modifier.weight(1f)) {
                            onFiltersChange(filters.copy(countriesVisited = !filters.countriesVisited))
                        }
                        FilterToggleRow("Plans / Desig", AtlasWished, filters.countriesPlanned, Modifier.weight(1f)) {
                            onFiltersChange(filters.copy(countriesPlanned = !filters.countriesPlanned))
                        }
                    }
                }
            }
        }
        Surface(
            onClick = { expanded = !expanded },
            shape = RoundedCornerShape(12.dp),
            color = AtlasSurface.copy(alpha = 0.97f),
            border = BorderStroke(1.dp, AtlasOutline),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Filled.Layers, contentDescription = null, tint = AtlasOnSurfaceSoft, modifier = Modifier.size(16.dp))
                Text(
                    text = if (expanded) "Tanca" else "Capes",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceSoft,
                )
            }
        }
    }
}

@Composable
private fun FilterToggleRow(label: String, color: Color, enabled: Boolean, modifier: Modifier = Modifier, onToggle: () -> Unit) {
    Row(
        modifier = modifier
            .clickable(onClick = onToggle)
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(if (enabled) color else AtlasOnSurfaceFaint),
        )
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) AtlasOnSurfaceStrong else AtlasOnSurfaceMuted,
        )
        if (enabled) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
        } else {
            Spacer(Modifier.size(13.dp))
        }
    }
}

@Composable
private fun FlightMapCanvas(
    uiState: StatsUiState,
    viewport: GeoViewport,
    modifier: Modifier = Modifier,
    showMarkers: Boolean,
    routeAlpha: Float,
) {
    val routes = uiState.flightMapRoutes.map {
        GeoRouteSegment(
            from = GeoCoordinate(it.fromLatitude, it.fromLongitude),
            to = GeoCoordinate(it.toLatitude, it.toLongitude),
            color = if (it.isPlanned) AtlasPlanned else AtlasPrimary,
            alpha = if (it.isPlanned) 0.55f else 1.0f,
            strokeWidthDp = 1.2f,
            showGlow = false,
        )
    }
    AtlasGeoCanvas(
        modifier = modifier,
        viewport = viewport,
        routeSegments = routes,
        markers = emptyList(),
        highlightColorByIso2 = uiState.highlightColors(),
        highlightFillAlpha = if (showMarkers) 0.38f else 0.46f,
        mapPaddingDp = 8f,
    )
}

@Composable
private fun FlightHeroPanel(uiState: StatsUiState) {
    AtlasCard(contentPadding = PaddingValues(0.dp)) {
        Column {
            FlightMapCanvas(
                uiState = uiState,
                viewport = WorldViewport,
                showMarkers = false,
                routeAlpha = 0.70f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(188.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
            )
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RingMetric(
                    value = (uiState.earthLoops / 3.0).toFloat().coerceIn(0f, 1f),
                    centerText = String.format(Locale("ca", "ES"), "%.1f×", uiState.earthLoops),
                    label = "voltes",
                    color = AtlasPrimary,
                    size = 88.dp,
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    BigStatLine(uiState.flownDistanceKm.formatKm(), "quilòmetres volats", AtlasPrimary)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        BigStatLine("${uiState.hoursFlown.roundToInt().formatInt()} h", "hores a l'aire", AtlasPlanned)
                        Text(
                            text = "${uiState.uniqueAirlineCount} companyies · ${uiState.aircraftTypeCount} tipus d'avió",
                            style = MaterialTheme.typography.labelSmall,
                            color = AtlasOnSurfaceMuted,
                        )
                    }
                }
            }
            if (uiState.moonLoops >= 0.01) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AtlasOutline))
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("🌕", style = MaterialTheme.typography.headlineMedium)
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        val moonValue = if (uiState.moonLoops >= 1.0)
                            String.format(Locale("ca", "ES"), "%.2f×", uiState.moonLoops)
                        else
                            "${(uiState.moonLoops * 100).roundToInt()}%"
                        val moonLabel = if (uiState.moonLoops >= 1.0)
                            "de la distància fins a la Lluna"
                        else
                            "del camí fins a la Lluna"
                        Text(
                            text = moonValue,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = AtlasGold,
                        )
                        Text(
                            text = moonLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = AtlasOnSurfaceMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TripStatusPanel(uiState: StatsUiState) {
    AtlasCard {
        Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                BigStatusNumber(uiState.completedTripCount, "Completats", AtlasCompleted, Modifier.weight(1f))
                BigStatusNumber(uiState.inProgressTripCount, "En curs", AtlasInProgress, Modifier.weight(1f))
                BigStatusNumber(uiState.plannedTripCount, "Plans", AtlasPlanned, Modifier.weight(1f))
            }
            ProgressBarRow("Parades totals", uiState.totalStopCount, max(uiState.totalStopCount, 1), AtlasPrimary)
            ProgressBarRow("Viatges amb fotos", uiState.tripVisuals.count { it.photoCount > 0 }, max(uiState.tripCount, 1), AtlasWished)
            uiState.avgTripLengthDays?.let {
                Text(
                    text = "Durada mitjana: ${it.roundToInt()} dies · ${uiState.daysTraveled.formatInt()} dies registrats",
                    style = MaterialTheme.typography.labelMedium,
                    color = AtlasOnSurfaceMuted,
                )
            }
        }
    }
}

@Composable
private fun BigStatusNumber(value: Int, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(value.formatInt(), style = MaterialTheme.typography.headlineSmall, color = color)
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceMuted, maxLines = 1)
    }
}

@Composable
private fun TripVisualRail(trips: List<StatsTripVisual>) {
    if (trips.isEmpty()) {
        EmptyStatCard("Encara no hi ha postals visuals.")
        return
    }
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        trips.forEach { TripVisualCard(it) }
    }
}

@Composable
private fun TripVisualCard(trip: StatsTripVisual) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier.width(230.dp),
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(AtlasSurfaceSubtle),
            ) {
                if (trip.coverPhotoFilename != null) {
                    AsyncImage(
                        model = File(context.filesDir, "photos/${trip.coverPhotoFilename}"),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize(),
                    )
                } else {
                    TripMiniMap(trip.points)
                }
                if (trip.photoCount > 0) {
                    Text(
                        text = "${trip.photoCount} fotos",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .background(AtlasSurface.copy(alpha = 0.90f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AtlasOnSurfaceStrong,
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = trip.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = trip.dateText ?: trip.routeText ?: "${trip.stopCount} parades",
                    style = MaterialTheme.typography.bodySmall,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun TripMiniMap(points: List<com.atlas.presentation.stats.StatsMapPoint>) {
    if (points.size < 2) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(AtlasSurfaceSubtle)
            val spacing = 20.dp.toPx()
            var x = spacing
            while (x < size.width) {
                drawLine(AtlasNavy.copy(alpha = 0.08f), Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
                x += spacing
            }
        }
        return
    }
    val coordinates = points.map { GeoCoordinate(it.latitude, it.longitude) }
    AtlasGeoCanvas(
        modifier = Modifier.fillMaxSize(),
        viewport = GeoViewport.FitPoints(coordinates, minLongitudeSpanDegrees = 4.8, minLatitudeSpanDegrees = 3.2),
        routeSegments = coordinates.zipWithNext { from, to -> GeoRouteSegment(from, to, AtlasPrimary, alpha = 0.9f) },
        markers = emptyList(),
    )
}

@Composable
private fun YearPulseChart(yearStats: List<StatsYearStat>, onTimelineClick: () -> Unit) {
    if (yearStats.isEmpty()) {
        EmptyStatCard("Encara no hi ha anys amb activitat.")
        return
    }
    AtlasCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = "Veure cronologia →",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AtlasPrimary,
                    modifier = Modifier.clickable(onClick = onTimelineClick),
                )
            }
            val visible = yearStats.takeLast(10)
            val maxValue = visible.maxOf { max(max(it.tripCount, it.flightCount), it.countryCount) }.coerceAtLeast(1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                visible.forEach { YearStatColumn(stat = it, maxValue = maxValue, modifier = Modifier.weight(1f)) }
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AtlasOnSurfaceMuted.copy(alpha = 0.15f)))
            LegendDots(listOf("Viatges" to AtlasPrimary, "Vols" to AtlasPlanned, "Països" to AtlasVisited))
        }
    }
}

@Composable
private fun YearStatColumn(stat: StatsYearStat, maxValue: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            VerticalStatBar(stat.tripCount, maxValue, AtlasPrimary, "V", Modifier.weight(1f))
            VerticalStatBar(stat.flightCount, maxValue, AtlasPlanned, "F", Modifier.weight(1f))
            VerticalStatBar(stat.countryCount, maxValue, AtlasVisited, "P", Modifier.weight(1f))
        }
        Text(stat.year.toString(), style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceMuted)
    }
}

@Composable
private fun VerticalStatBar(value: Int, maxValue: Int, color: Color, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(value.toString(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = AtlasOnSurfaceMuted)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (value == 0) 8.dp else (90.dp * (value.toFloat() / maxValue)).coerceAtLeast(12.dp))
                .clip(RoundedCornerShape(3.dp))
                .background(color),
        )
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TripMonthChart(monthStats: List<StatsMonthStat>) {
    if (monthStats.isEmpty()) {
        EmptyStatCard("Encara no hi ha dates de viatge per mes.")
        return
    }
    AtlasCard {
        val maxValue = monthStats.maxOf { it.tripCount }.coerceAtLeast(1)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            monthStats.forEach { stat ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(stat.tripCount.toString(), style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceMuted)
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(if (stat.tripCount == 0) 8.dp else (100.dp * (stat.tripCount.toFloat() / maxValue)).coerceAtLeast(12.dp))
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (stat.tripCount == 0) AtlasSurfaceSubtle else AtlasPrimary),
                    )
                    Text(stat.label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = AtlasOnSurfaceMuted)
                }
            }
        }
    }
}

private fun smoothLinePath(points: List<Offset>): Path = Path().apply {
    if (points.isEmpty()) return@apply
    moveTo(points[0].x, points[0].y)
    for (i in 0 until points.size - 1) {
        val p0 = if (i > 0) points[i - 1] else points[i]
        val p1 = points[i]
        val p2 = points[i + 1]
        val p3 = if (i < points.size - 2) points[i + 2] else points[i + 1]
        cubicTo(
            p1.x + (p2.x - p0.x) / 6f,
            p1.y + (p2.y - p0.y) / 6f,
            p2.x - (p3.x - p1.x) / 6f,
            p2.y - (p3.y - p1.y) / 6f,
            p2.x,
            p2.y,
        )
    }
}

@Composable
private fun TripYearChart(yearStats: List<StatsYearStat>) {
    if (yearStats.none { it.tripCount > 0 }) {
        EmptyStatCard("Encara no hi ha viatges amb dates per any.")
        return
    }
    val active = yearStats.filter { it.tripCount > 0 }
    val minYear = active.minOf { it.year }
    val maxYear = active.maxOf { it.year }
    // Fill any gap years between first and last active year
    val data = (minYear..maxYear).map { year ->
        year to (yearStats.find { it.year == year }?.tripCount ?: 0)
    }
    val maxValue = data.maxOf { it.second }.coerceAtLeast(1)

    AtlasCard {
        if (data.size == 1) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = data[0].first.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = AtlasOnSurfaceMuted,
                )
                Text(
                    text = data[0].second.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = AtlasOnSurfaceStrong,
                )
                Text(
                    text = if (data[0].second == 1) "viatge" else "viatges",
                    style = MaterialTheme.typography.labelSmall,
                    color = AtlasOnSurfaceMuted,
                )
            }
            return@AtlasCard
        }

        val textMeasurer = rememberTextMeasurer()
        val sidePadDp = 16.dp
        val chartHeightDp = 140.dp

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeightDp),
        ) {
            val sidePad = sidePadDp.toPx()
            val pointSpacing = (size.width - sidePad * 2) / (data.size - 1)
                val topPad = 24.dp.toPx()   // room for count labels above highest point
                val labelH = 20.dp.toPx()    // room for year labels below line
                val chartBottom = size.height - labelH

                val points = data.mapIndexed { idx, (_, count) ->
                    Offset(
                        x = sidePad + idx * pointSpacing,
                        y = topPad + (1f - count.toFloat() / maxValue) * (chartBottom - topPad),
                    )
                }

                // Soft area fill under the curve
                val areaPath = smoothLinePath(points).also { path ->
                    path.lineTo(points.last().x, chartBottom)
                    path.lineTo(points.first().x, chartBottom)
                    path.close()
                }
                drawPath(areaPath, color = AtlasPrimary.copy(alpha = 0.10f))

                // Smooth line
                drawPath(
                    path = smoothLinePath(points),
                    color = AtlasPrimary,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                )

                val labelStyle = TextStyle(fontSize = 9.sp, color = AtlasOnSurfaceMuted)

                points.forEachIndexed { idx, pt ->
                    val count = data[idx].second
                    if (count > 0) {
                        // Count above dot
                        val countText = count.toString()
                        val countMeasured = textMeasurer.measure(countText, labelStyle)
                        drawText(
                            textMeasurer = textMeasurer,
                            text = countText,
                            topLeft = Offset(
                                x = pt.x - countMeasured.size.width / 2f,
                                y = pt.y - countMeasured.size.height - 4.dp.toPx(),
                            ),
                            style = labelStyle,
                        )
                        // Dot
                        drawCircle(color = AtlasPrimary, radius = 3.5.dp.toPx(), center = pt)
                        drawCircle(color = AtlasSurface, radius = 1.5.dp.toPx(), center = pt)
                    }

                    // Year label below axis
                    val yearText = data[idx].first.toString()
                    val yearMeasured = textMeasurer.measure(yearText, labelStyle)
                    drawText(
                        textMeasurer = textMeasurer,
                        text = yearText,
                        topLeft = Offset(
                            x = pt.x - yearMeasured.size.width / 2f,
                            y = chartBottom + 4.dp.toPx(),
                        ),
                        style = labelStyle,
                    )
                }
        }
    }
}

@Composable
private fun TripSeasonCard(seasons: List<StatsSeasonStat>) {
    if (seasons.all { it.tripCount == 0 }) {
        EmptyStatCard("Encara no hi ha viatges amb dates per calcular temporades.")
        return
    }
    val maxCount = seasons.maxOf { it.tripCount }.coerceAtLeast(1)
    val seasonColors = listOf(
        Color(0xFF8FD4A0), // Primavera — soft green
        Color(0xFFF5C04A), // Estiu — warm amber
        Color(0xFFD4845A), // Tardor — terracotta
        Color(0xFF7BB8E8), // Hivern — icy blue
    )
    AtlasCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            seasons.forEachIndexed { index, season ->
                val color = seasonColors.getOrElse(index) { AtlasPrimary }
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(text = season.emoji, fontSize = 16.sp)
                        Text(
                            text = season.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelMedium,
                            color = AtlasOnSurfaceMuted,
                        )
                        Text(
                            text = season.tripCount.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (season.tripCount > 0) color else AtlasOnSurfaceMuted,
                        )
                    }
                    ThickProgress(value = season.tripCount, maxValue = maxCount, color = color)
                }
            }
        }
    }
}

@Composable
private fun FlightYearChart(yearStats: List<StatsYearStat>) {
    if (yearStats.none { it.flightCount > 0 }) {
        EmptyStatCard("Encara no hi ha vols amb dates per any.")
        return
    }
    val active = yearStats.filter { it.flightCount > 0 }
    val minYear = active.minOf { it.year }
    val maxYear = active.maxOf { it.year }
    val data = (minYear..maxYear).map { year ->
        year to (yearStats.find { it.year == year }?.flightCount ?: 0)
    }
    val maxValue = data.maxOf { it.second }.coerceAtLeast(1)

    AtlasCard {
        if (data.size == 1) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(data[0].first.toString(), style = MaterialTheme.typography.labelMedium, color = AtlasOnSurfaceMuted)
                Text(data[0].second.toString(), style = MaterialTheme.typography.headlineSmall, color = AtlasOnSurfaceStrong)
                Text(if (data[0].second == 1) "vol" else "vols", style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceMuted)
            }
            return@AtlasCard
        }

        val textMeasurer = rememberTextMeasurer()
        val sidePadDp = 16.dp
        val chartHeightDp = 140.dp

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeightDp),
        ) {
            val sidePad = sidePadDp.toPx()
            val pointSpacing = (size.width - sidePad * 2) / (data.size - 1)
            val topPad = 24.dp.toPx()
            val labelH = 20.dp.toPx()
            val chartBottom = size.height - labelH

            val points = data.mapIndexed { idx, (_, count) ->
                Offset(
                    x = sidePad + idx * pointSpacing,
                    y = topPad + (1f - count.toFloat() / maxValue) * (chartBottom - topPad),
                )
            }

            val areaPath = smoothLinePath(points).also { path ->
                path.lineTo(points.last().x, chartBottom)
                path.lineTo(points.first().x, chartBottom)
                path.close()
            }
            drawPath(areaPath, color = AtlasPlanned.copy(alpha = 0.10f))

            drawPath(
                path = smoothLinePath(points),
                color = AtlasPlanned,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
            )

            val labelStyle = TextStyle(fontSize = 9.sp, color = AtlasOnSurfaceMuted)

            points.forEachIndexed { idx, pt ->
                val count = data[idx].second
                if (count > 0) {
                    val countText = count.toString()
                    val countMeasured = textMeasurer.measure(countText, labelStyle)
                    drawText(
                        textMeasurer = textMeasurer,
                        text = countText,
                        topLeft = Offset(
                            x = pt.x - countMeasured.size.width / 2f,
                            y = pt.y - countMeasured.size.height - 4.dp.toPx(),
                        ),
                        style = labelStyle,
                    )
                    drawCircle(color = AtlasPlanned, radius = 3.5.dp.toPx(), center = pt)
                    drawCircle(color = AtlasSurface, radius = 1.5.dp.toPx(), center = pt)
                }
                val yearText = data[idx].first.toString()
                val yearMeasured = textMeasurer.measure(yearText, labelStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = yearText,
                    topLeft = Offset(
                        x = pt.x - yearMeasured.size.width / 2f,
                        y = chartBottom + 4.dp.toPx(),
                    ),
                    style = labelStyle,
                )
            }
        }
    }
}

@Composable
private fun NightDayCard(nightCount: Int, dayCount: Int) {
    val total = nightCount + dayCount
    if (total == 0) {
        EmptyStatCard("No hi ha dades d'hora de sortida.")
        return
    }
    AtlasCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "☀️  Diürns",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AtlasOnSurfaceMuted,
                    )
                    Text(
                        text = dayCount.formatInt(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AtlasGold,
                    )
                }
                ThickProgress(value = dayCount, maxValue = total, color = AtlasGold)
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🌙  Nocturns  (22h–06h)",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AtlasOnSurfaceMuted,
                    )
                    Text(
                        text = nightCount.formatInt(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AtlasPlanned,
                    )
                }
                ThickProgress(value = nightCount, maxValue = total, color = AtlasPlanned)
            }
        }
    }
}

@Composable
private fun CountryFlagGrid(stamps: List<StatsCountryStamp>) {
    if (stamps.isEmpty()) {
        EmptyStatCard("Cap país registrat encara.")
        return
    }
    val scrollState = rememberScrollState()
    val row1 = stamps.filterIndexed { i, _ -> i % 2 == 0 }
    val row2 = stamps.filterIndexed { i, _ -> i % 2 == 1 }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            row1.forEach { FlagStampCard(it) }
        }
        if (row2.isNotEmpty()) {
            Row(
                modifier = Modifier.horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row2.forEach { FlagStampCard(it) }
            }
        }
    }
}

// Flags that are not standard rectangles (square or irregular shape):
// these need Fit so the full shape is visible; all other flags use Crop to fill the card.
private fun flagContentScale(iso2: String): ContentScale =
    if (iso2.uppercase() in setOf("CH", "VA", "NP")) ContentScale.Fit else ContentScale.Crop

@Composable
private fun FlagStampCard(stamp: StatsCountryStamp) {
    val stateColor = stamp.state.color()
    Surface(
        modifier = Modifier.width(120.dp),
        shape = RoundedCornerShape(12.dp),
        color = AtlasSurface,  // neutral background visible around non-rectangular flags
        border = BorderStroke(1.dp, stateColor.copy(alpha = 0.22f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 2f),
        ) {
            CountryFlag(
                iso2 = stamp.iso2,
                modifier = Modifier.fillMaxSize(),
                contentScale = flagContentScale(stamp.iso2),
            )
            // Gradient scrim + name overlay at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, AtlasNavy.copy(alpha = 0.85f)),
                        )
                    ),
                contentAlignment = Alignment.BottomStart,
            ) {
                Text(
                    text = stamp.name,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = stateColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ContinentProgressCard(continents: List<StatsContinent>) {
    if (continents.isEmpty()) {
        EmptyStatCard("Encara no hi ha continents per mostrar.")
        return
    }
    AtlasCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            continents.forEach { continent ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = continent.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AtlasOnSurfaceStrong,
                        )
                        Text(
                            text = "${continent.visited}/${continent.total}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (continent.visited > 0) AtlasVisited else AtlasOnSurfaceMuted,
                        )
                        Text(
                            text = continent.percentLabel(),
                            style = MaterialTheme.typography.labelSmall,
                            color = AtlasOnSurfaceMuted,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.End,
                        )
                    }
                    StackedProgressBar(
                        primary = continent.visited,
                        secondary = continent.planned,
                        total = continent.total,
                    )
                }
            }
        }
    }
}

@Composable
private fun StackedProgressBar(primary: Int, secondary: Int, total: Int, height: Dp = 16.dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(4.dp))
            .background(AtlasSurfaceSubtle),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(((primary + secondary).toFloat() / total.coerceAtLeast(1)).coerceIn(0f, 1f))
                .height(height)
                .clip(RoundedCornerShape(4.dp))
                .background(AtlasPlanned.copy(alpha = 0.62f)),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth((primary.toFloat() / total.coerceAtLeast(1)).coerceIn(0f, 1f))
                .height(height)
                .clip(RoundedCornerShape(4.dp))
                .background(AtlasVisited),
        )
    }
}

@Composable
private fun RankedCountryCard(items: List<StatsRank>) {
    if (items.isEmpty()) {
        EmptyStatCard("Encara no hi ha prou activitat per ordenar països.")
        return
    }
    val maxValue = items.maxOf { it.value }.coerceAtLeast(1)
    AtlasCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Registres · viatges · vols (sense escales)",
                style = MaterialTheme.typography.labelSmall,
                color = AtlasOnSurfaceMuted,
            )
            items.forEach { item ->
                CountryRankItem(item = item, maxValue = maxValue)
            }
        }
    }
}

@Composable
private fun CountryRankItem(item: StatsRank, maxValue: Int) {
    val accentColor = item.state?.color() ?: AtlasVisited
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .width(48.dp)
                .aspectRatio(3f / 2f)
                .clip(RoundedCornerShape(4.dp))
                .background(AtlasSurface),
        ) {
            item.iso2?.let { iso2 ->
                CountryFlag(iso2 = iso2, modifier = Modifier.fillMaxSize(), contentScale = flagContentScale(iso2))
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.value.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = AtlasOnSurfaceMuted,
                )
            }
            ThickProgress(value = item.value, maxValue = maxValue, color = accentColor)
        }
    }
}

@Composable
private fun RoutesAndAirportsCard(
    routes: List<StatsRouteRank>,
    airports: List<StatsAirportRank>,
) {
    if (routes.isEmpty() && airports.isEmpty()) {
        EmptyStatCard("Encara no hi ha rutes ni aeroports registrats.")
        return
    }
    AtlasCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (routes.isNotEmpty()) {
                Text(
                    text = "Rutes",
                    style = MaterialTheme.typography.labelSmall,
                    color = AtlasOnSurfaceMuted,
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    routes.forEachIndexed { index, route ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text = "${index + 1}",
                                modifier = Modifier.width(18.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = AtlasOnSurfaceMuted,
                                textAlign = TextAlign.Center,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = route.route,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AtlasOnSurfaceStrong,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = route.distanceKm.formatKm(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AtlasOnSurfaceMuted,
                                )
                            }
                            Text(
                                text = "${route.count}×",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AtlasPrimary,
                            )
                        }
                    }
                }
            }
            if (routes.isNotEmpty() && airports.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AtlasOutline))
            }
            if (airports.isNotEmpty()) {
                Text(
                    text = "Aeroports",
                    style = MaterialTheme.typography.labelSmall,
                    color = AtlasOnSurfaceMuted,
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    airports.forEachIndexed { index, airport ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text = "${index + 1}",
                                modifier = Modifier.width(18.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = AtlasOnSurfaceMuted,
                                textAlign = TextAlign.Center,
                            )
                            if (airport.countryIso2.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .width(28.dp)
                                        .aspectRatio(3f / 2f)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(AtlasSurfaceSubtle),
                                ) {
                                    CountryFlag(
                                        iso2 = airport.countryIso2,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = flagContentScale(airport.countryIso2),
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = airport.code,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AtlasOnSurfaceStrong,
                                )
                                Text(
                                    text = airport.city,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AtlasOnSurfaceMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Text(
                                text = airport.count.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AtlasOnSurfaceMuted,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopAirlineBars(airlines: List<StatsAirlineRank>) {
    if (airlines.isEmpty()) {
        EmptyStatCard("Encara no hi ha companyies registrades.")
        return
    }
    AtlasCard {
        val maxValue = airlines.maxOf { it.count }.coerceAtLeast(1)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            airlines.forEach { airline ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (airline.code.length in 2..3) {
                        AirlineLogo(
                            iata = airline.code,
                            modifier = Modifier
                                .width(64.dp)
                                .height(36.dp),
                        )
                    } else {
                        Text(
                            text = airline.code.take(4),
                            modifier = Modifier.width(64.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AtlasOnSurfaceStrong,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = airline.code,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AtlasOnSurfaceStrong,
                            )
                            Text(
                                text = "${airline.count} vols · ${airline.distanceKm.formatKm()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AtlasOnSurfaceMuted,
                            )
                        }
                        ThickProgress(value = airline.count, maxValue = maxValue, color = AtlasPlanned)
                    }
                }
            }
        }
    }
}

@Composable
private fun TopAircraftCard(aircraft: List<StatsAircraftRank>) {
    if (aircraft.isEmpty()) {
        EmptyStatCard("Encara no hi ha models d'avió registrats.")
        return
    }
    val top = aircraft.first()
    val maxCount = aircraft.maxOf { it.count }.coerceAtLeast(1)
    AtlasCard(contentPadding = PaddingValues(0.dp)) {
        Column {
            // Hero image of top aircraft — full width, no crop
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(AtlasSurfaceSubtle),
                contentAlignment = Alignment.Center,
            ) {
                if (top.imageAssetRef != null) {
                    AsyncImage(
                        model = "file:///android_asset/${top.imageAssetRef}",
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                    )
                } else {
                    Icon(
                        Icons.Filled.FlightTakeoff,
                        contentDescription = null,
                        tint = AtlasOnSurfaceMuted,
                        modifier = Modifier.size(36.dp),
                    )
                }
                // Gradient overlay with aircraft name
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, AtlasNavy.copy(alpha = 0.80f)),
                            ),
                        ),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = top.displayName,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${top.count}×",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = AtlasPrimary,
                        )
                    }
                }
            }
            // List of all aircraft
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                aircraft.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = if (item.category != null) "${item.displayName} · ${item.category}" else item.displayName,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AtlasOnSurfaceStrong,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${item.count}×",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AtlasPrimary,
                        )
                    }
                    ThickProgress(value = item.count, maxValue = maxCount, color = AtlasPrimary)
                }
            }
        }
    }
}

@Composable
private fun DelayDistributionCard(buckets: List<StatsDelayBucket>) {
    if (buckets.isEmpty() || buckets.sumOf { it.count } == 0) {
        EmptyStatCard("Encara no hi ha dades de retard.")
        return
    }
    AtlasCard {
        val maxValue = buckets.maxOf { it.count }.coerceAtLeast(1)
        val total = buckets.sumOf { it.count }.coerceAtLeast(1)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            buckets.forEach { bucket ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = bucket.label,
                        modifier = Modifier.width(54.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = AtlasOnSurfaceMuted,
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        ThickProgress(value = bucket.count, maxValue = maxValue, color = bucket.delayColor())
                    }
                    Text(
                        text = "${bucket.count}",
                        modifier = Modifier.width(26.dp),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = bucket.delayColor(),
                    )
                    Text(
                        text = "${(bucket.count.toFloat() / total * 100).roundToInt()}%",
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = bucket.delayColor(),
                    )
                }
            }
        }
    }
}

@Composable
private fun TopDelayChart(delays: List<StatsTopDelay>) {
    if (delays.isEmpty()) {
        EmptyStatCard("Cap retard significatiu registrat.")
        return
    }
    val maxDelay = delays.maxOf { it.delayMinutes }.coerceAtLeast(1)
    AtlasCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            delays.forEach { item ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = item.route,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AtlasOnSurfaceStrong,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = item.delayLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AtlasError,
                        )
                    }
                    ThickProgress(value = item.delayMinutes.toInt(), maxValue = maxDelay.toInt(), color = AtlasError)
                }
            }
        }
    }
}

@Composable
private fun TopRoutesChart(routes: List<StatsRouteRank>) {
    if (routes.isEmpty()) {
        EmptyStatCard("Encara no hi ha rutes registrades.")
        return
    }
    val data = routes.take(5)
    val maxCount = data.maxOf { it.count }.coerceAtLeast(1)
    AtlasCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            data.forEach { route ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = route.route,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AtlasOnSurfaceStrong,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        ThickProgress(value = route.count, maxValue = maxCount, color = AtlasPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${route.count}×",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AtlasPrimary,
                        )
                        Text(
                            text = route.distanceKm.formatKm(),
                            style = MaterialTheme.typography.labelSmall,
                            color = AtlasOnSurfaceMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopAirportsChart(airports: List<StatsAirportRank>) {
    if (airports.isEmpty()) {
        EmptyStatCard("Encara no hi ha aeroports registrats.")
        return
    }
    val maxCount = airports.maxOf { it.count }.coerceAtLeast(1)
    AtlasCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            airports.forEach { airport ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (airport.countryIso2.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .aspectRatio(3f / 2f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(AtlasSurfaceSubtle),
                        ) {
                            CountryFlag(
                                iso2 = airport.countryIso2,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = flagContentScale(airport.countryIso2),
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = airport.code,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AtlasOnSurfaceStrong,
                            )
                            Text(
                                text = airport.city,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelSmall,
                                color = AtlasOnSurfaceMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "${airport.count}×",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AtlasPrimary,
                            )
                        }
                        ThickProgress(value = airport.count, maxValue = maxCount, color = AtlasPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun FlightScopeCard(uiState: StatsUiState) {
    val total = uiState.continentalFlightCount + uiState.intercontinentalFlightCount
    val haulTotal = uiState.shortHaulCount + uiState.mediumHaulCount + uiState.longHaulCount
    AtlasCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ProgressBarRow("Intercontinentals", uiState.intercontinentalFlightCount, total.coerceAtLeast(1), AtlasPrimary)
            ProgressBarRow("Continentals", uiState.continentalFlightCount, total.coerceAtLeast(1), AtlasVisited)
            if (haulTotal > 0) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(AtlasOutline).padding(vertical = 2.dp))
                ProgressBarRow("Curt  < 1.500 km", uiState.shortHaulCount, haulTotal, AtlasVisited)
                ProgressBarRow("Mitjà 1.500–4.000 km", uiState.mediumHaulCount, haulTotal, AtlasPlanned)
                ProgressBarRow("Llarg > 4.000 km", uiState.longHaulCount, haulTotal, AtlasPrimary)
            }
        }
    }
}

@Composable
private fun CompletionCard(tier: StatsCompletionTier, worldPercentage: Float) {
    AtlasCard {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            RingMetric(
                value = tier.progress.coerceIn(0f, 1f),
                centerText = "${worldPercentage.roundToInt()}%",
                label = "món",
                color = AtlasGold,
                size = 94.dp,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(tier.title, style = MaterialTheme.typography.titleLarge, color = AtlasOnSurfaceStrong)
                Text(tier.detail, style = MaterialTheme.typography.bodySmall, color = AtlasOnSurfaceMuted)
                Text(
                    text = "Següent fita: ${tier.nextTargetLabel}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AtlasGold,
                )
            }
        }
    }
}

@Composable
private fun RecordsPreview(records: List<StatsRecord>) {
    if (records.isEmpty()) return
    Section("Rècords destacats") {
        RecordGrid(records)
    }
}

@Composable
private fun RecordGrid(records: List<StatsRecord>) {
    if (records.isEmpty()) {
        EmptyStatCard("Els rècords apareixeran quan hi hagi més dades.")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        records.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { RecordCard(it, Modifier.weight(1f)) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RecordCard(record: StatsRecord, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(138.dp),
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasGold.copy(alpha = 0.24f)),
    ) {
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = AtlasGold, modifier = Modifier.size(22.dp))
            Column {
                Text(
                    text = record.value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = record.title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AtlasGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = record.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun BadgeCompletionCard(badges: List<StatsBadge>) {
    val unlocked = badges.count { it.unlocked }
    val total = badges.size.coerceAtLeast(1)
    AtlasCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                Text(
                    text = "$unlocked",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = AtlasGold,
                )
                Text(
                    text = " / $total",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AtlasOnSurfaceMuted,
                    modifier = Modifier.padding(bottom = 3.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "INSÍGNIES\nDESBLOQUEJADES",
                    style = MaterialTheme.typography.labelSmall,
                    color = AtlasOnSurfaceMuted,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(bottom = 3.dp),
                )
            }
            ThickProgress(value = unlocked, maxValue = total, color = AtlasGold)
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                BadgeTier.entries.forEach { tier ->
                    val count = badges.count { it.tier == tier }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(tier.color(), CircleShape),
                        )
                        Text(
                            text = "${tier.label()} · $count",
                            style = MaterialTheme.typography.labelSmall,
                            color = AtlasOnSurfaceMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeGrid(badges: List<StatsBadge>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        badges.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { BadgeCard(it, Modifier.weight(1f)) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BadgeCard(badge: StatsBadge, modifier: Modifier = Modifier) {
    val accentColor = badge.tier?.color() ?: if (badge.unlocked) AtlasPrimary else AtlasOnSurfaceFaint
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = if (badge.unlocked) AtlasSurface else AtlasSurfaceSubtle.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = if (badge.unlocked) 0.28f else 0.18f)),
    ) {
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (badge.tier != null || badge.unlocked) Icons.Filled.EmojiEvents else Icons.Filled.Flag,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp),
                )
                if (badge.tier != null) {
                    Text(
                        text = badge.tier.label().uppercase(),
                        modifier = Modifier
                            .background(accentColor.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = badge.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = badge.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (badge.nextGoal != null) {
                    Text(
                        text = "→ ${badge.nextGoal}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                        color = accentColor.copy(alpha = 0.80f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(AtlasSurfaceSubtle),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((badge.progress ?: 0f).coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(accentColor.copy(alpha = if (badge.unlocked) 1f else 0.55f)),
                )
            }
        }
    }
}

@Composable
private fun RingMetric(
    value: Float,
    centerText: String,
    label: String,
    color: Color,
    size: Dp = 116.dp,
) {
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            val inset = 10.dp.toPx()
            drawCircle(
                color = AtlasOutline.copy(alpha = 0.75f),
                radius = this.size.minDimension / 2f - inset,
                style = stroke,
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * value.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(this.size.width - inset * 2, this.size.height - inset * 2),
                style = stroke,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerText, style = MaterialTheme.typography.titleLarge, color = AtlasOnSurfaceStrong, maxLines = 1)
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceMuted, maxLines = 1)
        }
    }
}

@Composable
private fun BigStatLine(value: String, label: String, color: Color) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AtlasOnSurfaceMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ProgressBarRow(
    label: String,
    value: Int,
    maxValue: Int,
    color: Color,
    showLabel: Boolean = true,
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        if (showLabel) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 1,
                )
                Text(
                    text = value.formatInt(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
            }
        }
        ThickProgress(value = value, maxValue = maxValue, color = color)
    }
}

@Composable
private fun ThickProgress(value: Int, maxValue: Int, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(AtlasSurfaceSubtle),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth((value.toFloat() / maxValue.coerceAtLeast(1)).coerceIn(0f, 1f))
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color),
        )
    }
}

@Composable
private fun LegendDots(items: List<Pair<String, Color>>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { (label, color) ->
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(color))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = AtlasOnSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun EmptyStatCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = AtlasOnSurfaceMuted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AtlasSectionLabel(title)
        content()
    }
}

@Composable
private fun IconBadge(icon: ImageVector, color: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = AtlasSurface, modifier = Modifier.size(21.dp))
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AtlasOutline.copy(alpha = 0.72f)),
    )
}

private fun StatsUiState.highlightColors(): Map<String, Color> = buildMap {
    wishedIso2s.forEach { put(it, AtlasWished) }
    plannedIso2s.forEach { put(it, AtlasPlanned) }
    visitedIso2s.forEach { put(it, AtlasVisited) }
    livedIso2s.forEach { put(it, AtlasLived) }
    livingIso2s.forEach { put(it, AtlasLiving) }
}

private fun StatsCountryState.color(): Color = when (this) {
    StatsCountryState.Living -> AtlasLiving
    StatsCountryState.Lived -> AtlasLived
    StatsCountryState.Visited -> AtlasVisited
    StatsCountryState.Planned -> AtlasPlanned
    StatsCountryState.Wished -> AtlasWished
}

private fun StatsContinent.percentLabel(): String =
    "${(visited.toFloat() / total.coerceAtLeast(1) * 100f).roundToInt()}%"

private fun StatsDelayBucket.delayColor(): Color = when (label) {
    "Abans", "Puntual" -> AtlasVisited
    "0-15" -> AtlasPlanned
    "16-30", "31-60" -> AtlasGold
    else -> AtlasError
}

private fun Int.formatInt(): String = NumberFormatter.format(this)
private fun Double.formatKm(): String = "${NumberFormatter.format(roundToInt())} km"

private fun BadgeTier.color(): Color = when (this) {
    BadgeTier.BRONZE -> Color(0xFFC17F3A)
    BadgeTier.PLATA -> Color(0xFF7B96AF)
    BadgeTier.OR -> AtlasGold
    BadgeTier.PLATI -> Color(0xFF7C6FCD)
}

private fun BadgeTier.label(): String = when (this) {
    BadgeTier.BRONZE -> "Bronze"
    BadgeTier.PLATA -> "Plata"
    BadgeTier.OR -> "Or"
    BadgeTier.PLATI -> "Platí"
}

private enum class StatsTab(val label: String) {
    Summary("Resum"),
    Map("Mapa"),
    Countries("Països"),
    Trips("Viatges"),
    Flights("Vols"),
    Badges("Insígnies"),
}
