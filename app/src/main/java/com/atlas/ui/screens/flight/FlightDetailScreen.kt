package com.atlas.ui.screens.flight

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.atlas.domain.model.Aircraft
import com.atlas.domain.model.AircraftType
import com.atlas.domain.model.Airport
import com.atlas.domain.model.Flight
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.domain.util.dayOffsetBetween
import com.atlas.domain.util.utcAwareDelayMinutes
import com.atlas.domain.util.utcAwareDurationMinutes
import com.atlas.presentation.flight.FlightDetailUiState
import com.atlas.ui.components.AirlineLogo
import com.atlas.ui.components.AtlasCard
import com.atlas.ui.components.AtlasPill
import com.atlas.ui.components.AtlasSemanticColors
import com.atlas.ui.components.geo.FlightRouteGeoMap
import com.atlas.ui.components.tripStatusColors
import com.atlas.ui.screens.country.BackPill
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasCompleted
import com.atlas.ui.theme.AtlasDelay
import com.atlas.ui.theme.AtlasError
import com.atlas.ui.theme.AtlasGold
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasSurfaceSubtle
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

@Composable
fun FlightDetailScreen(
    uiState: FlightDetailUiState,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onDismissDraft: () -> Unit,
    onOriginQueryChanged: (String) -> Unit,
    onOriginSelected: (com.atlas.domain.model.Airport) -> Unit,
    onDestinationQueryChanged: (String) -> Unit,
    onDestinationSelected: (com.atlas.domain.model.Airport) -> Unit,
    onStatusChanged: (TravelStatus) -> Unit,
    onScheduledDepartureAtChanged: (String) -> Unit,
    onScheduledArrivalAtChanged: (String) -> Unit,
    onActualDepartureAtChanged: (String) -> Unit,
    onActualArrivalAtChanged: (String) -> Unit,
    onAirlineQueryChanged: (String) -> Unit,
    onAirlineSelected: (com.atlas.domain.model.Airline) -> Unit,
    onFlightNumberChanged: (String) -> Unit,
    onAircraftChanged: (String) -> Unit,
    onAircraftRegistrationChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSaveDraft: () -> Unit,
) {
    val flight = uiState.flight
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (flight == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AtlasBackground)
                .padding(20.dp),
        ) {
            BackPill(onBackClick = onBackClick, modifier = Modifier.padding(top = 16.dp))
        }
        return
    }

    val statusColors = flight.status.tripStatusColors()
    var timeDisplayMode by remember { mutableStateOf(TimeDisplayMode.Local) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AtlasBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        FlightHero(
            flight = flight,
            originAirport = uiState.originAirport,
            destinationAirport = uiState.destinationAirport,
            prevContextAirport = uiState.prevContextAirport,
            nextContextAirport = uiState.nextContextAirport,
            statusColors = statusColors,
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AtlasBackground)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            FlightIdentityCard(
                flight = flight,
                originAirport = uiState.originAirport,
                destinationAirport = uiState.destinationAirport,
                statusColors = statusColors,
                groupPositionLabel = uiState.groupPositionLabel,
            )

            FlightStatStrip(flight = flight)

            FlightDatesCard(
                flight = flight,
                originAirport = uiState.originAirport,
                destinationAirport = uiState.destinationAirport,
                displayMode = timeDisplayMode,
                onDisplayModeChanged = { timeDisplayMode = it },
            )

            val hasAircraftInfo = flight.aircraft != null ||
                flight.aircraftRegistration != null ||
                uiState.resolvedAircraftType != null ||
                uiState.resolvedAircraft != null
            if (hasAircraftInfo) {
                AircraftVisualCard(
                    flight = flight,
                    resolvedAircraftType = uiState.resolvedAircraftType,
                    resolvedAircraft = uiState.resolvedAircraft,
                )
            }

            FlightDataCard(
                flight = flight,
                resolvedAirlineName = uiState.resolvedAirlineName,
                resolvedAircraftType = uiState.resolvedAircraftType,
                resolvedAircraft = uiState.resolvedAircraft,
            )

            if (!flight.notes.isNullOrBlank()) {
                FlightNotesCard(flight.notes)
            }

            FlightActions(
                onEditClick = onEditClick,
                onDeleteClick = { showDeleteDialog = true },
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Elimina vol") },
            text = { Text("Vols eliminar aquest vol? Aquesta acció no es pot desfer.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteConfirmed()
                }) { Text("Elimina") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel·la") }
            },
        )
    }

    if (uiState.draft.isOpen) {
        FlightEditorDialog(
            draft = uiState.draft,
            originResults = uiState.originSearchResults,
            destinationResults = uiState.destinationSearchResults,
            onDismiss = onDismissDraft,
            onOriginQueryChanged = onOriginQueryChanged,
            onOriginSelected = onOriginSelected,
            onDestinationQueryChanged = onDestinationQueryChanged,
            onDestinationSelected = onDestinationSelected,
            onStatusChanged = onStatusChanged,
            onScheduledDepartureAtChanged = onScheduledDepartureAtChanged,
            onScheduledArrivalAtChanged = onScheduledArrivalAtChanged,
            onActualDepartureAtChanged = onActualDepartureAtChanged,
            onActualArrivalAtChanged = onActualArrivalAtChanged,
            airlineResults = uiState.airlineSearchResults,
            onAirlineQueryChanged = onAirlineQueryChanged,
            onAirlineSelected = onAirlineSelected,
            onFlightNumberChanged = onFlightNumberChanged,
            onAircraftChanged = onAircraftChanged,
            onAircraftRegistrationChanged = onAircraftRegistrationChanged,
            onNotesChanged = onNotesChanged,
            // Edit always starts in form step; search callbacks are never triggered
            onApiFlightNumberChanged = {},
            onApiSearchDateChanged = {},
            onSearchByFlightNumber = {},
            onApplyApiResult = {},
            onManualEntryClick = {},
            onBackToSearch = {},
            onSave = onSaveDraft,
        )
    }
}

// ─── Hero ─────────────────────────────────────────────────────────────────────

@Composable
private fun FlightHero(
    flight: Flight,
    originAirport: Airport?,
    destinationAirport: Airport?,
    prevContextAirport: Airport?,
    nextContextAirport: Airport?,
    statusColors: AtlasSemanticColors,
    onBackClick: () -> Unit,
) {
    val originCode = originAirport?.iata ?: originAirport?.icao ?: flight.originAirportId.uppercase()
    val destinationCode = destinationAirport?.iata ?: destinationAirport?.icao ?: flight.destinationAirportId.uppercase()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .clipToBounds(),
    ) {
        FlightRouteGeoMap(
            originAirport = originAirport,
            destinationAirport = destinationAirport,
            prevContextAirport = prevContextAirport,
            nextContextAirport = nextContextAirport,
            statusColors = statusColors,
            modifier = Modifier.fillMaxSize(),
        )

        BackPill(
            onBackClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 16.dp),
        )

        // Route code label — frosted pill for readability over map tiles
        Text(
            text = "$originCode → $destinationCode",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.82f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = AtlasOnSurfaceMuted,
        )
    }
}

// ─── Cards ────────────────────────────────────────────────────────────────────

@Composable
private fun FlightIdentityCard(
    flight: Flight,
    originAirport: Airport?,
    destinationAirport: Airport?,
    statusColors: AtlasSemanticColors,
    groupPositionLabel: String?,
) {
    val originCode = originAirport?.iata ?: originAirport?.icao ?: flight.originAirportId.uppercase()
    val destCode = destinationAirport?.iata ?: destinationAirport?.icao ?: flight.destinationAirportId.uppercase()
    val originCity = originAirport?.city
    val destCity = destinationAirport?.city
    val arrivalDayOffset = dayOffsetBetween(
        departureDatetime = flight.actualDepartureAt ?: flight.scheduledDepartureAt,
        arrivalDatetime = flight.actualArrivalAt ?: flight.scheduledArrivalAt,
    )

    AtlasCard {
        Column(verticalArrangement = Arrangement.spacedBy(17.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                flight.airline?.let { iata ->
                    AirlineLogo(
                        iata = iata,
                        modifier = Modifier
                            .height(36.dp)
                            .widthIn(max = 96.dp),
                    )
                } ?: AirlineFallbackLogo(statusColors.foreground, statusColors.container)
                Text(
                    text = flight.flightNumber?.takeIf { it.isNotBlank() } ?: "Vol",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                AtlasPill(
                    label = statusColors.label,
                    colors = statusColors,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 13.dp, vertical = 7.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                DetailAirportEndpoint(
                    airportCode = originCode,
                    city = originCity,
                    primaryTime = flight.actualDepartureAt?.timePart() ?: flight.scheduledDepartureAt?.timePart(),
                    secondaryTime = flight.actualDepartureAt?.let { flight.scheduledDepartureAt?.timePart() },
                    modifier = Modifier.weight(1f),
                )
                DetailRouteMiddle(
                    distanceKm = flight.distanceKm,
                    color = statusColors.foreground,
                    modifier = Modifier.weight(1.15f),
                )
                DetailAirportEndpoint(
                    airportCode = destCode,
                    city = destCity,
                    primaryTime = flight.actualArrivalAt?.timePart() ?: flight.scheduledArrivalAt?.timePart(),
                    secondaryTime = flight.actualArrivalAt?.let { flight.scheduledArrivalAt?.timePart() },
                    dayOffset = arrivalDayOffset,
                    modifier = Modifier.weight(1f),
                    alignEnd = true,
                )
            }

            groupPositionLabel?.let {
                AtlasPill(
                    label = it,
                    colors = AtlasSemanticColors(
                        foreground = AtlasOnSurfaceMuted,
                        container = AtlasSurface,
                        label = it,
                    ),
                )
            }
        }
    }
}

@Composable
private fun FlightStatStrip(flight: Flight) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, AtlasOutline),
    ) {
        Row {
            FlightStatCell(
                value = flight.durationMinutes()?.toDurationText() ?: "—",
                label = "Durada",
                modifier = Modifier.weight(1f),
            )
            FlightStatCell(
                value = flight.distanceKm?.let(::formatDistanceKm) ?: "—",
                label = "Distància",
                modifier = Modifier.weight(1f),
            )
            FlightStatCell(
                value = flight.delayMinutes()?.toDelayText() ?: "—",
                label = "Retard",
                valueColor = flight.delayMinutes()?.delayColor() ?: AtlasOnSurfaceStrong,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FlightStatCell(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = AtlasOnSurfaceStrong,
) {
    Column(
        modifier = modifier.padding(horizontal = 14.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = AtlasOnSurfaceMuted,
            maxLines = 1,
        )
    }
}

@Composable
private fun FlightDatesCard(
    flight: Flight,
    originAirport: Airport?,
    destinationAirport: Airport?,
    displayMode: TimeDisplayMode,
    onDisplayModeChanged: (TimeDisplayMode) -> Unit,
) {
    val hasScheduled = flight.scheduledDepartureAt != null || flight.scheduledArrivalAt != null
    val hasActual = flight.actualDepartureAt != null || flight.actualArrivalAt != null
    if (!hasScheduled && !hasActual) return

    val originCode = originAirport?.iata ?: originAirport?.icao ?: flight.originAirportId.uppercase()
    val destinationCode = destinationAirport?.iata ?: destinationAirport?.icao ?: flight.destinationAirportId.uppercase()
    val localArrivalDayOffset = dayOffsetBetween(
        departureDatetime = flight.actualDepartureAt ?: flight.scheduledDepartureAt,
        arrivalDatetime = flight.actualArrivalAt ?: flight.scheduledArrivalAt,
    )
    val utcArrivalDayOffset = utcDayOffsetBetween(
        departureInstant = flight.actualDepartureUtc ?: flight.scheduledDepartureUtc,
        arrivalInstant = flight.actualArrivalUtc ?: flight.scheduledArrivalUtc,
    )

    AtlasCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Horari".uppercase(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = AtlasOnSurfaceMuted,
                )
                TimeModeToggle(
                    selected = displayMode,
                    onSelected = onDisplayModeChanged,
                )
            }
            DatetimeRow(
                label = "Sortida · $originCode",
                primaryValue = flight.actualDepartureAt ?: flight.scheduledDepartureAt,
                secondaryValue = flight.actualDepartureAt?.let { flight.scheduledDepartureAt },
                utcPrimaryValue = flight.actualDepartureUtc ?: flight.scheduledDepartureUtc,
                utcSecondaryValue = flight.actualDepartureUtc?.let { flight.scheduledDepartureUtc },
                displayMode = displayMode,
            )
            DatetimeRow(
                label = "Arribada · $destinationCode",
                primaryValue = flight.actualArrivalAt ?: flight.scheduledArrivalAt,
                secondaryValue = flight.actualArrivalAt?.let { flight.scheduledArrivalAt },
                utcPrimaryValue = flight.actualArrivalUtc ?: flight.scheduledArrivalUtc,
                utcSecondaryValue = flight.actualArrivalUtc?.let { flight.scheduledArrivalUtc },
                displayMode = displayMode,
                localDayOffset = localArrivalDayOffset,
                utcDayOffset = utcArrivalDayOffset,
            )
        }
    }
}

@Composable
private fun DatetimeRow(
    label: String,
    primaryValue: String?,
    secondaryValue: String?,
    utcPrimaryValue: String?,
    utcSecondaryValue: String?,
    displayMode: TimeDisplayMode,
    localDayOffset: Int? = null,
    utcDayOffset: Int? = null,
) {
    val displayedPrimary = when (displayMode) {
        TimeDisplayMode.Local -> primaryValue
        TimeDisplayMode.Utc -> utcPrimaryValue
    } ?: return
    val displayedSecondary = when (displayMode) {
        TimeDisplayMode.Local -> secondaryValue
        TimeDisplayMode.Utc -> utcSecondaryValue
    }
    val displayedDayOffset = when (displayMode) {
        TimeDisplayMode.Local -> localDayOffset
        TimeDisplayMode.Utc -> utcDayOffset
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.ExtraBold,
            color = AtlasOnSurfaceStrong,
        )
        Column(horizontalAlignment = Alignment.End) {
            displayedSecondary?.takeIf { it != displayedPrimary }?.let {
                Text(
                    text = "${formatTimeOnly(it, displayMode)} · ${formatDateOnlyForMode(it, displayMode)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceMuted,
                    textDecoration = TextDecoration.LineThrough,
                )
            }
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = formatTimeOnly(displayedPrimary, displayMode),
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 30.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = (primaryValue.delayAgainst(secondaryValue)).delayColor(),
                )
                displayedDayOffset?.let {
                    Text(
                        text = if (it > 0) "+$it" else "$it",
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = AtlasOnSurfaceMuted,
                    )
                }
            }
            Text(
                text = formatDateOnlyForMode(displayedPrimary, displayMode),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = AtlasOnSurfaceMuted,
            )
        }
    }
}

@Composable
private fun TimeModeToggle(
    selected: TimeDisplayMode,
    onSelected: (TimeDisplayMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(AtlasSurfaceSubtle)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        TimeModeToggleOption("Local", TimeDisplayMode.Local, selected, onSelected)
        TimeModeToggleOption("UTC", TimeDisplayMode.Utc, selected, onSelected)
    }
}

@Composable
private fun TimeModeToggleOption(
    label: String,
    value: TimeDisplayMode,
    selected: TimeDisplayMode,
    onSelected: (TimeDisplayMode) -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected == value) AtlasNavy else Color.Transparent)
            .padding(horizontal = 13.dp, vertical = 8.dp)
            .clickable { onSelected(value) },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = if (selected == value) AtlasSurface else AtlasOnSurfaceMuted,
        )
    }
}

@Composable
private fun AirlineFallbackLogo(
    foreground: Color,
    container: Color,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Flight,
            contentDescription = null,
            tint = foreground,
            modifier = Modifier.size(21.dp),
        )
    }
}

@Composable
private fun DetailAirportEndpoint(
    airportCode: String,
    city: String?,
    primaryTime: String?,
    secondaryTime: String?,
    dayOffset: Int? = null,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
    ) {
        Text(
            text = airportCode,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 34.sp),
            fontWeight = FontWeight.SemiBold,
            color = AtlasOnSurfaceStrong,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
        )
        city?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = AtlasOnSurfaceStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        primaryTime?.let { time ->
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = AtlasOnSurfaceStrong,
                )
                dayOffset?.let {
                    Text(
                        text = if (it > 0) "+$it" else "$it",
                        modifier = Modifier.padding(top = 1.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AtlasOnSurfaceMuted,
                    )
                }
            }
        }
        secondaryTime?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = AtlasOnSurfaceMuted,
            )
        }
    }
}

@Composable
private fun DetailRouteMiddle(
    distanceKm: Double?,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = distanceKm?.let(::formatDistanceKm) ?: "Ruta",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = AtlasOnSurfaceMuted,
            maxLines = 1,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 7.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(color.copy(alpha = 0.5f)),
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(AtlasSurface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Flight,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
    }
}

@Composable
private fun AircraftVisualCard(
    flight: Flight,
    resolvedAircraftType: AircraftType?,
    resolvedAircraft: Aircraft?,
) {
    val aircraftTitle = resolvedAircraft?.model ?: resolvedAircraftType?.displayName ?: flight.aircraft ?: "Aeronau"
    val aircraftMeta = listOfNotNull(
        resolvedAircraft?.registration ?: flight.aircraftRegistration,
        resolvedAircraftType?.category?.toCatalanAircraftCategory(),
    ).joinToString(" · ")

    AtlasCard {
        Column {
            AircraftImage(
                imageUrl = resolvedAircraft?.imageUrl,
                imageAssetRef = resolvedAircraftType?.imageAssetRef,
                label = aircraftTitle,
            )
            Text(
                text = aircraftTitle,
                modifier = Modifier.padding(top = 14.dp),
                style = MaterialTheme.typography.titleLarge,
                color = AtlasOnSurfaceStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (aircraftMeta.isNotBlank()) {
                Text(
                    text = aircraftMeta,
                    modifier = Modifier.padding(top = 5.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AircraftMetric(
                    value = (resolvedAircraft?.numEngines ?: resolvedAircraftType?.numEngines)?.toString() ?: "—",
                    label = "Motors",
                )
                AircraftMetric(
                    value = (resolvedAircraft?.engineType ?: resolvedAircraftType?.engineType)?.toCatalanEngineType() ?: "—",
                    label = "Tipus",
                )
                AircraftMetric(
                    value = resolvedAircraft?.numSeats?.toString() ?: "—",
                    label = "Seients",
                )
                AircraftMetric(
                    value = resolvedAircraft?.firstFlightDate?.let { formatDateOnly(it).takeLast(4) } ?: "—",
                    label = "Primer vol",
                )
            }
        }
    }
}

@Composable
private fun AircraftImage(
    imageUrl: String?,
    imageAssetRef: String?,
    label: String,
) {
    val imageData = imageUrl?.takeIf { it.isNotBlank() }
        ?: imageAssetRef?.takeIf { it.isNotBlank() }?.let { "file:///android_asset/$it" }

    imageData?.let { source ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AtlasSurfaceSubtle),
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(source)
                    .crossfade(true)
                    .build(),
                contentDescription = label,
                modifier = Modifier.fillMaxSize(),
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Success -> {
                        SubcomposeAsyncImageContent(
                            contentScale = ContentScale.Crop,
                        )
                    }
                    is AsyncImagePainter.State.Error,
                    is AsyncImagePainter.State.Empty -> AircraftImagePlaceholder(label = label)
                    else -> AircraftImagePlaceholder(label = label)
                }
            }
        }
        return
    }

    AircraftImagePlaceholder(label = label)
}

@Composable
private fun AircraftImagePlaceholder(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AtlasSurfaceSubtle),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stripeColor = AtlasOutline.copy(alpha = 0.28f)
            var x = -size.height
            while (x < size.width) {
                drawLine(
                    color = stripeColor,
                    start = androidx.compose.ui.geometry.Offset(x, size.height),
                    end = androidx.compose.ui.geometry.Offset(x + size.height, 0f),
                    strokeWidth = 9.dp.toPx(),
                )
                x += 24.dp.toPx()
            }
        }
        Text(
            text = "IMATGE · $label",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(AtlasSurface.copy(alpha = 0.82f))
                .padding(horizontal = 8.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AtlasOnSurfaceMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AircraftImageLabel(
    label: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "IMATGE · $label",
        modifier = modifier
            .padding(12.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(AtlasSurface.copy(alpha = 0.82f))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = AtlasOnSurfaceMuted,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun AircraftMetric(
    value: String,
    label: String,
) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AtlasOnSurfaceStrong,
            maxLines = 1,
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = AtlasOnSurfaceMuted,
            maxLines = 1,
        )
    }
}

@Composable
private fun FlightDataCard(
    flight: Flight,
    resolvedAirlineName: String?,
    resolvedAircraftType: AircraftType?,
    resolvedAircraft: Aircraft?,
) {
    val rows = buildList {
        flight.flightNumber?.let { add("Número de vol" to it) }
        flight.aircraftRegistration?.let { add("Matrícula" to it) }
        resolvedAircraft?.deliveryDate?.let { add("Lliurament" to formatDateOnly(it)) }
        resolvedAircraft?.ageYears?.let { add("Edat" to "${"%.1f".format(it)} anys") }
        resolvedAircraftType?.category?.let { add("Categoria" to it.toCatalanAircraftCategory()) }
        add("Seguiment destí" to if (flight.destinationCountsForCountryTracking) "Sí" else "No")
        add("Seguiment origen" to if (flight.originCountsForCountryTracking) "Sí" else "No")
    }
    if (rows.isEmpty() && flight.airline == null) return

    AtlasCard {
        Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Text(
                text = "Dades",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceMuted,
            )
            flight.airline?.let { iata ->
                AirlineDataRow(iata = iata)
            }
            rows.forEach { (label, value) ->
                DataRow(label, value)
            }
        }
    }
}

@Composable
private fun AirlineDataRow(iata: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Companyia",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = AtlasOnSurfaceMuted,
        )
        AirlineLogo(
            iata = iata,
            modifier = Modifier
                .height(32.dp)
                .widthIn(max = 96.dp),
        )
    }
}

@Composable
private fun DataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = AtlasOnSurfaceMuted,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.ExtraBold,
            color = AtlasOnSurfaceStrong,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FlightMetaCard(
    flight: Flight,
    resolvedAirlineName: String?,
    resolvedAircraftType: AircraftType?,
    resolvedAircraft: Aircraft?,
) {
    AtlasCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            flight.airline?.let { iata ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Companyia",
                        style = MaterialTheme.typography.bodySmall,
                        color = AtlasOnSurfaceMuted,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AirlineLogo(
                            iata = iata,
                            modifier = Modifier
                                .height(24.dp)
                                .widthIn(max = 80.dp),
                        )
                        Text(
                            text = resolvedAirlineName ?: iata,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AtlasOnSurfaceStrong,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            flight.flightNumber?.let { MetaRow("Número de vol", it) }
            flight.distanceKm?.let { MetaRow("Distància", formatDistanceKm(it)) }
            flight.aircraft?.let {
                MetaRow("Aeronau", resolvedAircraft?.model ?: resolvedAircraftType?.displayName ?: it)
                resolvedAircraftType?.let { aircraftType ->
                    MetaRow("Categoria", aircraftType.category.toCatalanAircraftCategory())
                }
            }
            resolvedAircraft?.let { aircraft ->
                MetaRow("Matrícula", aircraft.registration)
                aircraft.numSeats?.let { MetaRow("Seients", it.toString()) }
                aircraft.numEngines?.let { MetaRow("Motors", it.toString()) }
                aircraft.engineType?.let { MetaRow("Tipus de motor", it.toCatalanEngineType()) }
                aircraft.firstFlightDate?.let { MetaRow("Primer vol", formatDateOnly(it)) }
                aircraft.deliveryDate?.let { MetaRow("Lliurament", formatDateOnly(it)) }
                aircraft.ageYears?.let { MetaRow("Edat", "${"%.1f".format(it)} anys") }
            } ?: run {
                flight.aircraftRegistration?.let { MetaRow("Matrícula", it) }
            }
            if (resolvedAircraft == null) {
                resolvedAircraftType?.let { aircraftType ->
                aircraftType.numEngines?.let { MetaRow("Motors", it.toString()) }
                aircraftType.engineType?.let { MetaRow("Tipus de motor", it.toCatalanEngineType()) }
                }
            }
        }
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AtlasOnSurfaceMuted,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = AtlasOnSurfaceStrong,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FlightNotesCard(notes: String) {
    AtlasCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Notes",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceMuted,
            )
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
                color = AtlasOnSurfaceStrong,
            )
        }
    }
}

@Composable
private fun FlightActions(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = onEditClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AtlasOnSurfaceStrong),
        ) {
            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(text = "Edita", modifier = Modifier.padding(start = 6.dp))
        }
        OutlinedButton(
            onClick = onDeleteClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = androidx.compose.material3.MaterialTheme.colorScheme.error),
        ) {
            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(text = "Elimina", modifier = Modifier.padding(start = 6.dp))
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun formatDatetime(value: String): String {
    return flightDetailDateFormatter.formatIsoDateTime(value) ?: value
}

private fun formatDateOnly(value: String): String {
    return flightDetailDateFormatter.formatIsoDate(value) ?: value
}

private fun formatUtcDatetime(value: String): String {
    return try {
        val dateTime = LocalDateTime.ofInstant(Instant.parse(value), ZoneOffset.UTC)
        flightDetailDateFormatter.format(dateTime)
    } catch (_: Exception) {
        value
    }
}

private fun formatTimeOnly(value: String, displayMode: TimeDisplayMode): String =
    when (displayMode) {
        TimeDisplayMode.Local -> value.timePart() ?: value
        TimeDisplayMode.Utc -> formatUtcTimeOnly(value)
    }

private fun formatDateOnlyForMode(value: String, displayMode: TimeDisplayMode): String =
    when (displayMode) {
        TimeDisplayMode.Local -> formatDateOnly(value)
        TimeDisplayMode.Utc -> formatUtcDateOnly(value)
    }

private fun formatUtcTimeOnly(value: String): String =
    runCatching {
        val dateTime = LocalDateTime.ofInstant(Instant.parse(value), ZoneOffset.UTC)
        "%02d:%02d".format(dateTime.hour, dateTime.minute)
    }.getOrElse { value }

private fun formatUtcDateOnly(value: String): String =
    runCatching {
        val dateTime = LocalDateTime.ofInstant(Instant.parse(value), ZoneOffset.UTC)
        flightDetailDateFormatter.format(dateTime.toLocalDate())
    }.getOrElse { value }

private fun utcDayOffsetBetween(departureInstant: String?, arrivalInstant: String?): Int? {
    val departureDate = runCatching {
        LocalDateTime.ofInstant(Instant.parse(departureInstant ?: return null), ZoneOffset.UTC).toLocalDate()
    }.getOrNull() ?: return null
    val arrivalDate = runCatching {
        LocalDateTime.ofInstant(Instant.parse(arrivalInstant ?: return null), ZoneOffset.UTC).toLocalDate()
    }.getOrNull() ?: return null
    val days = ChronoUnit.DAYS.between(departureDate, arrivalDate).toInt()
    return if (days != 0) days else null
}

private fun formatDistanceKm(value: Double): String =
    "%,d km".format(value.roundToInt()).replace(",", ".")

private fun Flight.durationMinutes(): Long? {
    return utcAwareDurationMinutes()
}

private fun Flight.delayMinutes(): Long? =
    utcAwareDelayMinutes()

private fun String?.delayAgainst(scheduled: String?): Long? =
    minutesBetween(scheduled, this)

private fun minutesBetween(start: String?, end: String?): Long? {
    val startTime = runCatching { LocalDateTime.parse(start ?: return null) }.getOrNull() ?: return null
    val endTime = runCatching { LocalDateTime.parse(end ?: return null) }.getOrNull() ?: return null
    return Duration.between(startTime, endTime).toMinutes()
}

private fun Long.toDurationText(): String {
    val absolute = kotlin.math.abs(this)
    val hours = absolute / 60
    val minutes = absolute % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours} h ${minutes} m"
        hours > 0 -> "${hours} h"
        else -> "${minutes} m"
    }
}

private fun Long.toDelayText(): String {
    val abs = kotlin.math.abs(this)
    val hours = abs / 60
    val minutes = abs % 60
    val body = when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}min"
        hours > 0 -> "${hours}h"
        else -> "${minutes}min"
    }
    return when {
        this > 0 -> "+$body"
        this < 0 -> "-$body"
        else -> "0min"
    }
}

private fun Long?.delayColor(): Color =
    when {
        this == null -> AtlasOnSurfaceStrong
        this > 45 -> AtlasError
        this >  0 -> AtlasDelay
        this <= 0 -> AtlasCompleted
        else -> AtlasOnSurfaceStrong
    }

private fun String.timePart(): String? =
    substringAfter('T', missingDelimiterValue = "")
        .take(5)
        .takeIf { it.length == 5 }

private enum class TimeDisplayMode { Local, Utc }

private val flightDetailDateFormatter = FlexibleDateFormatter()

private fun String.toCatalanAircraftCategory(): String = when (uppercase()) {
    "NARROWBODY" -> "Fuselatge estret"
    "WIDEBODY" -> "Fuselatge ample"
    "REGIONAL" -> "Regional"
    "TURBOPROP" -> "Turbohèlix"
    else -> this
}

private fun String.toCatalanEngineType(): String = when (uppercase()) {
    "JET" -> "Reactor"
    "TURBOPROP" -> "Turbohèlix"
    "PISTON" -> "Pistó"
    "UNKNOWN" -> "Desconegut"
    else -> this
}
