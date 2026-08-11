package com.atlas.ui.rework.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.atlas.domain.model.TravelStatus
import com.atlas.presentation.dashboard.DashboardFlightUiState
import com.atlas.presentation.dashboard.DashboardTripUiState
import com.atlas.presentation.dashboard.DashboardUiState
import com.atlas.ui.rework.capture.ReworkCaptureButton
import com.atlas.ui.rework.components.AtlasWordmark
import com.atlas.ui.rework.components.ReworkFloatingCard
import com.atlas.ui.rework.foundation.AtlasReworkTheme
import com.atlas.ui.rework.map.AtlasWorldLandAspectRatio
import com.atlas.ui.rework.map.AtlasWorldMap
import com.atlas.ui.rework.map.AtlasWorldMapCountries
import kotlin.math.roundToInt

@Composable
fun ReworkHomeScreen(
    state: DashboardUiState,
    selectedCountryIso2: String?,
    onCountrySelected: (String) -> Unit,
    onCountrySelectionCleared: () -> Unit,
    captureExpanded: Boolean,
    onCaptureRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AtlasReworkTheme.colors
    val density = LocalDensity.current
    var headerBottomPx by remember { mutableIntStateOf(0) }
    var mapCameraResetRequest by remember { mutableIntStateOf(0) }
    var showcasePickerVisible by remember { mutableStateOf(false) }
    var selectedShowcase by remember { mutableStateOf<HomeShowcaseScenario?>(null) }
    var showcaseSelectedCountryIso2 by remember { mutableStateOf<String?>(null) }
    val displayedState = selectedShowcase?.state ?: state
    val displayedCountryIso2 = if (selectedShowcase != null) showcaseSelectedCountryIso2 else selectedCountryIso2
    val activeTrip = displayedState.featuredTrip?.takeIf { it.status == TravelStatus.IN_PROGRESS }
    val activeFlight = displayedState.upcomingFlights.firstOrNull { it.status == TravelStatus.IN_PROGRESS }
    val upcoming = displayedState.upcomingTrips.firstOrNull()
    val achievementTitle = selectedShowcase?.achievementTitle

    BoxWithConstraints(modifier.fillMaxSize().background(colors.mapWater)) {
        val landTop = with(density) { headerBottomPx.toDp() } + 8.dp
        val landBottom = landTop + (maxWidth - 16.dp) * AtlasWorldLandAspectRatio

        AtlasWorldMap(
            countries = AtlasWorldMapCountries(
                living = displayedState.livingIso2s,
                lived = displayedState.livedIso2s,
                visited = displayedState.visitedIso2s,
                planned = displayedState.plannedIso2s,
                wished = displayedState.wishedIso2s,
            ),
            initialLandTopPx = with(density) { landTop.toPx() },
            cameraResetRequest = mapCameraResetRequest,
            modifier = Modifier.fillMaxSize(),
            onCountrySelected = { iso2 ->
                if (selectedShowcase != null) showcaseSelectedCountryIso2 = iso2 else onCountrySelected(iso2)
            },
            onSelectionCleared = {
                if (selectedShowcase != null) showcaseSelectedCountryIso2 = null else onCountrySelectionCleared()
            },
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .onGloballyPositioned { coordinates ->
                    headerBottomPx = (coordinates.positionInRoot().y + coordinates.size.height).roundToInt()
                }
                .padding(horizontal = AtlasReworkTheme.dimensions.screenPadding)
                .padding(top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AtlasWordmark()
            Spacer(Modifier.weight(1f))
            MapControl(Icons.Rounded.Layers, "Capes") {
                if (HomeShowcaseCatalog.enabled) showcasePickerVisible = true
            }
            Spacer(Modifier.width(8.dp))
            MapControl(Icons.Rounded.MyLocation, "Centra el mapa") {
                mapCameraResetRequest += 1
            }
        }

        ReworkCaptureButton(
            expanded = captureExpanded,
            onClick = onCaptureRequested,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .zIndex(2f)
                .navigationBarsPadding()
                .padding(end = 22.dp, bottom = 92.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = landBottom, bottom = 112.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AtlasReworkTheme.dimensions.screenPadding),
        ) {
            activeFlight?.let { flight ->
                ActiveFlightCard(flight)
                Spacer(Modifier.height(AtlasReworkTheme.dimensions.cardGap))
            }
            activeTrip?.let { trip ->
                ActiveTripCard(trip)
                Spacer(Modifier.height(AtlasReworkTheme.dimensions.cardGap))
            }
            achievementTitle?.let { title ->
                AchievementCard(title)
                Spacer(Modifier.height(AtlasReworkTheme.dimensions.cardGap))
            }

            displayedCountryIso2?.let { iso2 ->
                ReworkFloatingCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("PAÍS SELECCIONAT", style = AtlasReworkTheme.typography.label, color = colors.accent)
                            Text(iso2, style = AtlasReworkTheme.typography.title)
                        }
                        Text("Obre el país  →", style = AtlasReworkTheme.typography.data, color = colors.inkMuted)
                    }
                }
                Spacer(Modifier.height(AtlasReworkTheme.dimensions.cardGap))
            }

            TrackingCard(displayedState)

            Spacer(Modifier.height(AtlasReworkTheme.dimensions.cardGap))
            if (upcoming != null) {
                JourneyCard(upcoming)
            } else {
                EmptyJourneyCard()
            }

            val recentTrip = displayedState.recentCompletedTrips.firstOrNull()
            val recentFlight = displayedState.recentFlights.firstOrNull()
            if (recentTrip != null || recentFlight != null) {
                Spacer(Modifier.height(AtlasReworkTheme.dimensions.cardGap))
                RecentCard(recentTrip, recentFlight)
            }

            Spacer(Modifier.height(AtlasReworkTheme.dimensions.cardGap))
            TravelArchiveCard(displayedState)
        }

        if (showcasePickerVisible) {
            HomeShowcasePicker(
                scenarios = HomeShowcaseCatalog.scenarios,
                selectedScenarioId = selectedShowcase?.id,
                onSelect = { scenario ->
                    selectedShowcase = scenario
                    showcaseSelectedCountryIso2 = scenario?.selectedCountryIso2
                    showcasePickerVisible = false
                    mapCameraResetRequest += 1
                },
                onDismiss = { showcasePickerVisible = false },
            )
        }
    }
}

@Composable
private fun MapLegendItem(
    color: Color,
    count: Int,
    label: String,
    modifier: Modifier = Modifier,
    outlined: Boolean = false,
) {
    val colors = AtlasReworkTheme.colors
    Column(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 1.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Text(
            count.toString(),
            style = AtlasReworkTheme.typography.title,
            color = if (outlined) colors.mapBorder else color,
        )
        Text(label, style = AtlasReworkTheme.typography.label, color = colors.inkMuted, maxLines = 2)
    }
}

@Composable
private fun MapLegendVerticalRule() {
    val colors = AtlasReworkTheme.colors
    Box(Modifier.width(1.dp).height(46.dp).background(colors.border))
}

@Composable
private fun TravelArchiveCard(state: DashboardUiState) {
    val colors = AtlasReworkTheme.colors
    val hasTravelHistory = state.tripCount > 0 || state.flightCount > 0 || state.stopCount > 0
    ReworkFloatingCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(
                if (hasTravelHistory) "EL TEU ARXIU" else "COMENÇA EL TEU ARXIU",
                style = AtlasReworkTheme.typography.label,
                color = colors.accent,
            )
            Text(
                if (hasTravelHistory) "La teva història en moviment" else "Cada lloc pot convertir-se en record",
                style = AtlasReworkTheme.typography.title,
                color = colors.ink,
            )
            if (!hasTravelHistory) {
                Text(
                    "Registra una visita, prepara un viatge o afegeix el teu primer vol.",
                    style = AtlasReworkTheme.typography.body,
                    color = colors.inkMuted,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                ArchiveMetric(state.tripCount.toString(), "viatges")
                ArchiveMetric(state.flightCount.toString(), "vols")
                ArchiveMetric(state.daysTraveled.toString(), "dies")
                ArchiveMetric(state.stopCount.toString(), "parades")
            }
        }
    }
}

@Composable
private fun ArchiveMetric(value: String, label: String) {
    val colors = AtlasReworkTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = AtlasReworkTheme.typography.title, color = colors.ink)
        Text(label, style = AtlasReworkTheme.typography.label, color = colors.inkMuted)
    }
}

@Composable
private fun ActiveTripCard(trip: DashboardTripUiState) {
    val colors = AtlasReworkTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AtlasReworkTheme.dimensions.cardRadius),
        color = colors.ink,
        contentColor = colors.surfaceStrong,
        border = BorderStroke(1.dp, colors.accent),
        shadowElevation = 14.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                InProgressBadge("EN CURS")
                Spacer(Modifier.width(10.dp))
                Text(
                    trip.title,
                    style = AtlasReworkTheme.typography.title,
                    color = colors.surfaceStrong,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "${trip.stopCount} parades",
                    style = AtlasReworkTheme.typography.data,
                    color = colors.accent,
                )
            }
            Text(
                trip.countryText?.takeIf { it.isNotBlank() }
                    ?: trip.routeText?.takeIf { it.isNotBlank() }
                    ?: "El teu viatge està passant ara",
                style = AtlasReworkTheme.typography.body,
                color = colors.surfaceStrong.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ActiveFlightCard(flight: DashboardFlightUiState) {
    val colors = AtlasReworkTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AtlasReworkTheme.dimensions.cardRadius),
        color = colors.ink,
        contentColor = colors.surfaceStrong,
        border = BorderStroke(1.dp, colors.accent),
        shadowElevation = 14.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                InProgressBadge("EN VOL")
                Spacer(Modifier.width(10.dp))
                Text(
                    "${flight.originCode}  →  ${flight.destinationCode}",
                    style = AtlasReworkTheme.typography.title,
                    color = colors.surfaceStrong,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
                flight.meta?.let { Text(it, style = AtlasReworkTheme.typography.data, color = colors.mapWater) }
            }
            Text(
                listOfNotNull(flight.originCity, flight.destinationCity, flight.dateText).joinToString("  ·  "),
                style = AtlasReworkTheme.typography.body,
                color = colors.surfaceStrong.copy(alpha = 0.78f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun InProgressBadge(label: String) {
    val colors = AtlasReworkTheme.colors
    Surface(shape = RoundedCornerShape(7.dp), color = colors.accent, contentColor = Color.White) {
        Text(
            label,
            style = AtlasReworkTheme.typography.label,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun AchievementCard(title: String) {
    val colors = AtlasReworkTheme.colors
    ReworkFloatingCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(shape = CircleShape, color = colors.accent, contentColor = Color.White) {
                Text("★", style = AtlasReworkTheme.typography.title, modifier = Modifier.padding(10.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("NOU ASSOLIMENT", style = AtlasReworkTheme.typography.label, color = colors.accent)
                Text(title, style = AtlasReworkTheme.typography.title, color = colors.ink)
                Text("El teu atlas continua creixent.", style = AtlasReworkTheme.typography.body, color = colors.inkMuted)
            }
        }
    }
}

@Composable
private fun MapControl(icon: ImageVector, description: String, onClick: () -> Unit) {
    val colors = AtlasReworkTheme.colors
    Surface(
        modifier = Modifier.size(46.dp),
        shape = CircleShape,
        color = colors.surfaceStrong,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 7.dp,
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = description, tint = colors.ink, modifier = Modifier.size(21.dp))
        }
    }
}

@Composable
private fun TrackingCard(state: DashboardUiState) {
    val colors = AtlasReworkTheme.colors
    val coloredCountryCount = (
        state.livingIso2s +
            state.livedIso2s +
            state.visitedIso2s +
            state.plannedIso2s +
            state.wishedIso2s
        ).size
    val unrecordedCountryCount = (state.trackableCountryCount - coloredCountryCount).coerceAtLeast(0)
    ReworkFloatingCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("EL TEU MAPA", style = AtlasReworkTheme.typography.label, color = colors.inkMuted)
                    Text(
                        if (state.visitedCount == 0) "Comença el teu atlas" else "${state.worldPercentage.roundToInt()}% del món explorat",
                        style = AtlasReworkTheme.typography.title,
                        color = colors.ink,
                    )
                }
                Metric(state.visitedCount.toString(), "territoris")
                Spacer(Modifier.width(18.dp))
                Metric(state.visitedContinentCount.toString(), "continents")
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MapLegendItem(colors.living, state.livingIso2s.size, "Hi vius", Modifier.weight(1f))
                    MapLegendVerticalRule()
                    MapLegendItem(colors.lived, state.livedIso2s.size, "Hi has viscut", Modifier.weight(1f))
                    MapLegendVerticalRule()
                    MapLegendItem(colors.visited, state.visitedIso2s.size, "Visitats", Modifier.weight(1f))
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MapLegendItem(colors.planned, state.plannedIso2s.size, "Planificats", Modifier.weight(1f))
                    MapLegendVerticalRule()
                    MapLegendItem(colors.wished, state.wishedIso2s.size, "Desitjats", Modifier.weight(1f))
                    MapLegendVerticalRule()
                    MapLegendItem(colors.mapLand, unrecordedCountryCount, "Sense registrar", Modifier.weight(1f), outlined = true)
                }
            }
        }
    }
}

@Composable
private fun Metric(value: String, label: String) {
    val colors = AtlasReworkTheme.colors
    Column(horizontalAlignment = Alignment.End) {
        Text(value, style = AtlasReworkTheme.typography.title, color = colors.ink)
        Text(label, style = AtlasReworkTheme.typography.label, color = colors.inkMuted)
    }
}

@Composable
private fun JourneyCard(trip: DashboardTripUiState) {
    val colors = AtlasReworkTheme.colors
    ReworkFloatingCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("PRÒXIM VIATGE", style = AtlasReworkTheme.typography.label, color = colors.accent)
                Text(trip.title, style = AtlasReworkTheme.typography.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    listOfNotNull(trip.dateText, trip.countryText).joinToString("  ·  "),
                    style = AtlasReworkTheme.typography.data,
                    color = colors.inkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text("→", style = AtlasReworkTheme.typography.title, color = colors.accent)
        }
    }
}

@Composable
private fun EmptyJourneyCard() {
    val colors = AtlasReworkTheme.colors
    ReworkFloatingCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text("QUÈ VE DESPRÉS?", style = AtlasReworkTheme.typography.label, color = colors.accent)
            Text("Planifica el teu pròxim viatge", style = AtlasReworkTheme.typography.title)
            Text("Afegeix un viatge o marca un lloc al mapa.", style = AtlasReworkTheme.typography.body, color = colors.inkMuted)
        }
    }
}

@Composable
private fun RecentCard(trip: DashboardTripUiState?, flight: DashboardFlightUiState?) {
    val colors = AtlasReworkTheme.colors
    val title = trip?.title ?: flight?.title.orEmpty()
    val meta = trip?.memoryDateText ?: flight?.dateText
    ReworkFloatingCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("DARRER RECORD", style = AtlasReworkTheme.typography.label, color = colors.inkMuted)
                Text(title, style = AtlasReworkTheme.typography.body.copy(fontWeight = FontWeight.SemiBold), maxLines = 1)
            }
            meta?.let { Text(it, style = AtlasReworkTheme.typography.data, color = colors.inkMuted) }
        }
    }
}
