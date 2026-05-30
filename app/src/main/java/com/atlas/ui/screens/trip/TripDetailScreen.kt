package com.atlas.ui.screens.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.domain.model.Country
import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.LocationSearchResult
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.presentation.date.FlexibleDateRangeDraftField
import com.atlas.presentation.trip.TripDetailUiState
import com.atlas.presentation.trip.TripStopDraftUiState
import com.atlas.ui.components.date.FlexibleDateRangeField
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// ─────────────────────────────────────────────
// Palette — matches CountryDetailScreen
// ─────────────────────────────────────────────
private val TripBg          = Color(0xFFF1F3F7)
private val TripCard        = Color(0xFFFFFFFF)
private val TripBorder      = Color(0xFFE4E8EF)
private val TripInk         = Color(0xFF111827)
private val TripMuted       = Color(0xFF6B7280)
private val TripAccent      = Color(0xFF024E82)   // trip blue
private val TripAccentLight = Color(0xFFC2D9F0)
private val TripPlanned     = Color(0xFF92400E)
private val TripPlannedLight = Color(0xFFFDE3C8)
private val TripError       = Color(0xFFB91C1C)

private val dateRangeFormatter = FlexibleDateFormatter()

// ─────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────
@Composable
fun TripDetailScreen(
    uiState: TripDetailUiState,
    onBackClick: () -> Unit,
    onDeleteTrip: () -> Unit,
    onEditTripClick: () -> Unit,
    onDismissTripDraft: () -> Unit,
    onTripTitleChanged: (String) -> Unit,
    onTripStatusChanged: (TravelStatus) -> Unit,
    onTripDatePrecisionChanged: (DatePrecision) -> Unit,
    onTripDateFieldChanged: (FlexibleDateRangeDraftField, String) -> Unit,
    onTripNotesChanged: (String) -> Unit,
    onSaveTripDraft: () -> Unit,
    onAddStopClick: () -> Unit,
    onDismissStopDraft: () -> Unit,
    onStopLocationNameChanged: (String) -> Unit,
    onLocationSearchQueryChanged: (String) -> Unit,
    onSearchLocationClick: () -> Unit,
    onLocationSearchResultSelected: (LocationSearchResult) -> Unit,
    onUseManualStopEntryClick: () -> Unit,
    onStopCountryChanged: (String) -> Unit,
    onStopLatitudeChanged: (String) -> Unit,
    onStopLongitudeChanged: (String) -> Unit,
    onStopDatePrecisionChanged: (DatePrecision) -> Unit,
    onStopDateFieldChanged: (FlexibleDateRangeDraftField, String) -> Unit,
    onStopNotesChanged: (String) -> Unit,
    onSaveStopDraft: () -> Unit,
    onEditStop: (TripStop) -> Unit,
    onMoveStopUp: (TripStop) -> Unit,
    onMoveStopDown: (TripStop) -> Unit,
    onDeleteStop: (TripStop) -> Unit,
) {
    var isDeleteTripDialogOpen by remember { mutableStateOf(false) }
    var pendingDeleteStop by remember { mutableStateOf<TripStop?>(null) }
    var isReorderMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TripBg),
    ) {
        // ── Top bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                onClick = onBackClick,
                shape = RoundedCornerShape(100.dp),
                color = TripCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, TripBorder),
                shadowElevation = 0.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Torna",
                        modifier = Modifier.size(14.dp),
                        tint = TripInk,
                    )
                    Text(
                        text = "Enrere",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = TripInk,
                    )
                }
            }
        }

        val trip = uiState.trip
        if (trip == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Carregant el viatge...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TripMuted,
                )
            }
        } else {
            TripDetailContent(
                uiState = uiState,
                trip = trip,
                isReorderMode = isReorderMode,
                onReorderModeChanged = { isReorderMode = it },
                onEditTripClick = onEditTripClick,
                onDeleteTrip = { isDeleteTripDialogOpen = true },
                onAddStopClick = onAddStopClick,
                onEditStop = onEditStop,
                onMoveStopUp = onMoveStopUp,
                onMoveStopDown = onMoveStopDown,
                onDeleteStop = { pendingDeleteStop = it },
            )
        }
    }

    // ── Dialogs ──
    if (uiState.tripDraft.isOpen) {
        TripEditorDialog(
            draft = uiState.tripDraft,
            onDismiss = onDismissTripDraft,
            onTitleChanged = onTripTitleChanged,
            onStatusChanged = onTripStatusChanged,
            onDatePrecisionChanged = onTripDatePrecisionChanged,
            onDateFieldChanged = onTripDateFieldChanged,
            onNotesChanged = onTripNotesChanged,
            onSave = onSaveTripDraft,
        )
    }

    if (uiState.stopDraft.isOpen) {
        TripStopDialog(
            draft = uiState.stopDraft,
            countries = uiState.countries,
            onDismiss = onDismissStopDraft,
            onLocationNameChanged = onStopLocationNameChanged,
            onLocationSearchQueryChanged = onLocationSearchQueryChanged,
            onSearchLocationClick = onSearchLocationClick,
            onLocationSearchResultSelected = onLocationSearchResultSelected,
            onUseManualEntryClick = onUseManualStopEntryClick,
            onCountryChanged = onStopCountryChanged,
            onLatitudeChanged = onStopLatitudeChanged,
            onLongitudeChanged = onStopLongitudeChanged,
            onDatePrecisionChanged = onStopDatePrecisionChanged,
            onDateFieldChanged = onStopDateFieldChanged,
            onNotesChanged = onStopNotesChanged,
            onSave = onSaveStopDraft,
        )
    }

    if (isDeleteTripDialogOpen && uiState.trip != null) {
        ConfirmDeleteDialog(
            title = "Eliminar viatge?",
            body = "S'eliminarà \"${uiState.trip.title}\" i totes les seves parades. Aquesta acció no es pot desfer.",
            onDismiss = { isDeleteTripDialogOpen = false },
            onConfirm = { isDeleteTripDialogOpen = false; onDeleteTrip() },
        )
    }

    pendingDeleteStop?.let { stop ->
        ConfirmDeleteDialog(
            title = "Eliminar parada?",
            body = "S'eliminarà \"${stop.locationName}\" del viatge. Aquesta acció no es pot desfer.",
            onDismiss = { pendingDeleteStop = null },
            onConfirm = { pendingDeleteStop = null; onDeleteStop(stop) },
        )
    }
}

// ─────────────────────────────────────────────
// Main content
// ─────────────────────────────────────────────
@Composable
private fun TripDetailContent(
    uiState: TripDetailUiState,
    trip: Trip,
    isReorderMode: Boolean,
    onReorderModeChanged: (Boolean) -> Unit,
    onEditTripClick: () -> Unit,
    onDeleteTrip: () -> Unit,
    onAddStopClick: () -> Unit,
    onEditStop: (TripStop) -> Unit,
    onMoveStopUp: (TripStop) -> Unit,
    onMoveStopDown: (TripStop) -> Unit,
    onDeleteStop: (TripStop) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Spacer(Modifier.height(4.dp))

        // ── Trip header card ──
        TripHeaderCard(
            trip = trip,
            stopCount = uiState.stops.size,
            onEditTripClick = onEditTripClick,
            onDeleteTrip = onDeleteTrip,
        )

        // ── Map preview ──
        TripMapPreview(stops = uiState.stops)

        // ── Stops section ──
        TripStopsSection(
            stops = uiState.stops,
            countries = uiState.countries,
            isReorderMode = isReorderMode,
            onReorderModeChanged = onReorderModeChanged,
            onAddStopClick = onAddStopClick,
            onEditStop = onEditStop,
            onMoveStopUp = onMoveStopUp,
            onMoveStopDown = onMoveStopDown,
            onDeleteStop = onDeleteStop,
        )

        Spacer(Modifier.height(24.dp))
    }
}

// ─────────────────────────────────────────────
// Trip header card
// Title, status badge, dates, summary pills, actions
// ─────────────────────────────────────────────
@Composable
private fun TripHeaderCard(
    trip: Trip,
    stopCount: Int,
    onEditTripClick: () -> Unit,
    onDeleteTrip: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(TripCard)
            .border(1.dp, TripBorder, RoundedCornerShape(22.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Title + status badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = trip.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TripInk,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(12.dp))
            StatusBadge(status = trip.status)
        }

        // Dates + notes
        trip.dateRange?.let { range ->
            LabeledValue(
                label = "DATES",
                value = dateRangeFormatter.format(range),
            )
        }
        trip.notes?.takeIf { it.isNotBlank() }?.let { notes ->
            LabeledValue(label = "NOTES", value = notes)
        }

        // Summary pills: days + stops
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SummaryPill(
                modifier = Modifier.weight(1f),
                label = "DIES",
                value = trip.dayCountText(),
            )
            SummaryPill(
                modifier = Modifier.weight(1f),
                label = "PARADES",
                value = stopCount.toString(),
            )
        }

        // Actions
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onEditTripClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TripAccent,
                    contentColor = Color.White,
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
            ) {
                Text("Edita viatge", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            }
            TextButton(
                onClick = onDeleteTrip,
                colors = ButtonDefaults.textButtonColors(contentColor = TripError),
            ) {
                Text("Elimina", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun StatusBadge(status: TravelStatus) {
    val (bg, fg) = when (status) {
        TravelStatus.COMPLETED   -> TripAccentLight to TripAccent
        TravelStatus.IN_PROGRESS -> Color(0xFFC6EAD8) to Color(0xFF005C38)
        TravelStatus.PLANNED     -> TripPlannedLight to TripPlanned
        TravelStatus.UNKNOWN     -> Color(0xFFE5E7EB) to TripMuted
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(bg)
            .padding(horizontal = 11.dp, vertical = 4.dp),
    ) {
        Text(
            text = status.toCatalanLabel().uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = fg,
            letterSpacing = 0.12.sp,
        )
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = TripMuted,
            letterSpacing = 0.14.sp,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TripInk,
        )
    }
}

@Composable
private fun SummaryPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(TripBg)
            .border(1.dp, TripBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = TripMuted,
            letterSpacing = 0.12.sp,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = TripInk,
        )
    }
}

// ─────────────────────────────────────────────
// Stops section
// ─────────────────────────────────────────────
@Composable
private fun TripStopsSection(
    stops: List<TripStop>,
    countries: List<Country>,
    isReorderMode: Boolean,
    onReorderModeChanged: (Boolean) -> Unit,
    onAddStopClick: () -> Unit,
    onEditStop: (TripStop) -> Unit,
    onMoveStopUp: (TripStop) -> Unit,
    onMoveStopDown: (TripStop) -> Unit,
    onDeleteStop: (TripStop) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Parades",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TripInk,
                )
                if (stops.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(TripInk)
                            .padding(horizontal = 9.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = stops.size.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Button(
                    onClick = onAddStopClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TripInk,
                        contentColor = Color.White,
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
                ) {
                    Icon(Icons.Filled.Add, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Afegeix", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }

                if (stops.isNotEmpty()) {
                    IconButton(
                        onClick = { onReorderModeChanged(!isReorderMode) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isReorderMode) Color(0xFF005C38) else TripCard)
                            .border(
                                width = 1.dp,
                                color = if (isReorderMode) Color(0xFF005C38) else TripBorder,
                                shape = RoundedCornerShape(12.dp),
                            ),
                    ) {
                        Icon(
                            imageVector = if (isReorderMode) Icons.Filled.Check else Icons.Filled.Menu,
                            contentDescription = if (isReorderMode) "Acaba de reordenar" else "Reordena parades",
                            tint = if (isReorderMode) Color.White else TripMuted,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }

        if (stops.isEmpty()) {
            EmptyStopsState(onAddStopClick = onAddStopClick)
        } else {
            stops.forEachIndexed { index, stop ->
                TripStopRow(
                    stop = stop,
                    position = index + 1,
                    countryName = countries.firstOrNull { it.iso2 == stop.countryIso2 }?.nameCa
                        ?: stop.countryIso2,
                    canMoveUp = index > 0,
                    canMoveDown = index < stops.lastIndex,
                    isReorderMode = isReorderMode,
                    onEditStop = onEditStop,
                    onMoveStopUp = onMoveStopUp,
                    onMoveStopDown = onMoveStopDown,
                    onDeleteStop = onDeleteStop,
                )
            }
        }
    }
}

@Composable
private fun EmptyStopsState(onAddStopClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(TripCard)
            .border(1.dp, TripBorder, RoundedCornerShape(18.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Sense parades encara.",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.ExtraBold,
            color = TripInk,
        )
        Text(
            text = "Afegeix els llocs del viatge en ordre. Després els podràs reordenar.",
            style = MaterialTheme.typography.bodyMedium,
            color = TripMuted,
        )
        Spacer(Modifier.height(2.dp))
        Button(
            onClick = onAddStopClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TripAccent,
                contentColor = Color.White,
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text("Afegeix la primera parada", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
        }
    }
}

// ─────────────────────────────────────────────
// Stop row
// ─────────────────────────────────────────────
@Composable
private fun TripStopRow(
    stop: TripStop,
    position: Int,
    countryName: String,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    isReorderMode: Boolean,
    onEditStop: (TripStop) -> Unit,
    onMoveStopUp: (TripStop) -> Unit,
    onMoveStopDown: (TripStop) -> Unit,
    onDeleteStop: (TripStop) -> Unit,
) {
    val hasCoords = stop.hasCoordinates()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = TripCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, TripBorder),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Position + icon stacked
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (hasCoords) TripAccentLight else TripBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (hasCoords) TripAccent else TripMuted,
                    )
                }
                // Position bubble
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = 4.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(TripInk),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = position.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 9.sp,
                    )
                }
            }

            // Body
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = stop.locationName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TripInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    TypeBadge(
                        label = if (hasCoords) "MAPA" else "MANUAL",
                        color = if (hasCoords) TripAccent else TripPlanned,
                        background = if (hasCoords) TripAccentLight else TripPlannedLight,
                    )
                    Text(
                        text = countryName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TripMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = buildStopMeta(stop),
                    style = MaterialTheme.typography.bodySmall,
                    color = TripMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Actions
            if (isReorderMode) {
                Column {
                    IconButton(
                        onClick = { onMoveStopUp(stop) },
                        enabled = canMoveUp,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.Filled.KeyboardArrowUp,
                            "Mou amunt",
                            tint = if (canMoveUp) TripInk else TripBorder,
                        )
                    }
                    IconButton(
                        onClick = { onMoveStopDown(stop) },
                        enabled = canMoveDown,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            "Mou avall",
                            tint = if (canMoveDown) TripInk else TripBorder,
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    SmallActionButton("Edita") { onEditStop(stop) }
                    SmallActionButton("Elimina") { onDeleteStop(stop) }
                }
            }
        }
    }
}

@Composable
private fun TypeBadge(label: String, color: Color, background: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = color,
        )
    }
}

@Composable
private fun SmallActionButton(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = TripMuted,
            containerColor = TripBg,
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        modifier = Modifier.height(28.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────
// Stop dialog
// ─────────────────────────────────────────────
@Composable
private fun TripStopDialog(
    draft: TripStopDraftUiState,
    countries: List<Country>,
    onDismiss: () -> Unit,
    onLocationNameChanged: (String) -> Unit,
    onLocationSearchQueryChanged: (String) -> Unit,
    onSearchLocationClick: () -> Unit,
    onLocationSearchResultSelected: (LocationSearchResult) -> Unit,
    onUseManualEntryClick: () -> Unit,
    onCountryChanged: (String) -> Unit,
    onLatitudeChanged: (String) -> Unit,
    onLongitudeChanged: (String) -> Unit,
    onDatePrecisionChanged: (DatePrecision) -> Unit,
    onDateFieldChanged: (FlexibleDateRangeDraftField, String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    val hasCoordinates = draft.latitude.isNotBlank() && draft.longitude.isNotBlank()
    val selectedCountry = countries.firstOrNull { it.iso2 == draft.countryIso2 }
    val showManualFields = draft.isManualEntryVisible

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = TripCard,
        title = {
            Text(
                text = if (draft.isEditing) "Edita parada" else "Afegeix parada",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TripInk,
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // ── Location search ──
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = TripAccent,
                        modifier = Modifier.size(16.dp),
                    )
                    DialogSectionLabel("Cerca lloc")
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = draft.locationSearchQuery,
                        onValueChange = onLocationSearchQueryChanged,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Nom o adreça", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        shape = RoundedCornerShape(14.dp),
                    )
                    Button(
                        onClick = onSearchLocationClick,
                        enabled = !draft.isSearchingLocation,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TripAccent,
                            contentColor = Color.White,
                        ),
                        contentPadding = PaddingValues(horizontal = 13.dp, vertical = 8.dp),
                    ) {
                        Text("Cerca", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }

                if (draft.isSearchingLocation) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = TripAccent,
                        )
                        Text(
                            "Cercant llocs...",
                            style = MaterialTheme.typography.bodySmall,
                            color = TripMuted,
                        )
                    }
                }

                draft.locationSearchError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TripError,
                    )
                }

                if (draft.locationSearchResults.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(TripBg)
                            .border(1.dp, TripBorder, RoundedCornerShape(12.dp)),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        draft.locationSearchResults.forEach { result ->
                            LocationSearchResultRow(
                                result = result,
                                onClick = { onLocationSearchResultSelected(result) },
                            )
                        }
                    }
                }

                if (draft.locationName.isNotBlank()) {
                    SelectedLocationSummary(
                        locationName = draft.locationName,
                        countryName = selectedCountry?.nameCa ?: draft.countryIso2,
                        hasCoordinates = hasCoordinates,
                        showEditDetails = !showManualFields,
                        onEditDetailsClick = onUseManualEntryClick,
                    )
                }

                if (!showManualFields && draft.locationName.isBlank()) {
                    TextButton(
                        onClick = onUseManualEntryClick,
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = TripAccent),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    ) {
                        Text("Entrada manual", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }

                if (showManualFields) {
                    // ── Manual entry ──
                    DialogSectionLabel("Detalls manuals")
                    OutlinedTextField(
                        value = draft.locationName,
                        onValueChange = onLocationNameChanged,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Nom del lloc", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        shape = RoundedCornerShape(14.dp),
                    )
                    CountryDropdown(
                        countries = countries,
                        selectedIso2 = draft.countryIso2,
                        onCountryChanged = onCountryChanged,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = draft.latitude,
                            onValueChange = onLatitudeChanged,
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("Latitud", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            placeholder = { Text("Opcional", color = TripMuted) },
                            shape = RoundedCornerShape(14.dp),
                        )
                        OutlinedTextField(
                            value = draft.longitude,
                            onValueChange = onLongitudeChanged,
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("Longitud", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            placeholder = { Text("Opcional", color = TripMuted) },
                            shape = RoundedCornerShape(14.dp),
                        )
                    }
                }
                Text(
                    text = "Dades de cerca © OpenStreetMap contributors",
                    style = MaterialTheme.typography.labelSmall,
                    color = TripMuted,
                )

                DialogSectionLabel("Data")
                FlexibleDateRangeField(
                    draft = draft.dateRange,
                    onPrecisionChanged = onDatePrecisionChanged,
                    onFieldChanged = onDateFieldChanged,
                    showHint = false,
                )
                OutlinedTextField(
                    value = draft.notes,
                    onValueChange = onNotesChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Notes", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    shape = RoundedCornerShape(14.dp),
                    minLines = 1,
                    maxLines = 3,
                )
                draft.validationError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TripError,
                    )
                }
            }
        },
        confirmButton = {
            CompactTripDialogActionButton(onClick = onSave) {
                Text("Desa", fontWeight = FontWeight.ExtraBold, color = TripAccent)
            }
        },
        dismissButton = {
            CompactTripDialogActionButton(onClick = onDismiss) {
                Text("Cancel·la", fontWeight = FontWeight.Bold, color = TripMuted)
            }
        },
    )
}

@Composable
private fun DialogSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        color = TripMuted,
        letterSpacing = 0.12.sp,
    )
}

@Composable
private fun SelectedLocationSummary(
    locationName: String,
    countryName: String,
    hasCoordinates: Boolean,
    showEditDetails: Boolean,
    onEditDetailsClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFE6F0FA))
            .border(1.dp, Color(0xFFBFD7EE), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(TripAccent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Place,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(
                text = locationName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TripInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = buildString {
                    append(countryName.ifBlank { "País pendent" })
                    append(" · ")
                    append(if (hasCoordinates) "Amb mapa" else "Manual")
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = TripMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (showEditDetails) {
            TextButton(
                onClick = onEditDetailsClick,
                modifier = Modifier.height(30.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = TripAccent),
            ) {
                Text("Edita", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────
// Location search result row
// ─────────────────────────────────────────────
@Composable
private fun LocationSearchResultRow(
    result: LocationSearchResult,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = TripInk),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = result.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = result.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = TripMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ─────────────────────────────────────────────
// Country dropdown
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryDropdown(
    countries: List<Country>,
    selectedIso2: String,
    onCountryChanged: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCountry = countries.firstOrNull { it.iso2 == selectedIso2 }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selectedCountry?.nameCa.orEmpty(),
            onValueChange = {},
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            readOnly = true,
            label = { Text("País o territori", fontWeight = FontWeight.Bold) },
            shape = RoundedCornerShape(14.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            countries.forEach { country ->
                DropdownMenuItem(
                    text = { Text(country.nameCa, fontWeight = FontWeight.SemiBold) },
                    onClick = { onCountryChanged(country.iso2); expanded = false },
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Confirm delete dialog
// ─────────────────────────────────────────────
@Composable
private fun ConfirmDeleteDialog(
    title: String,
    body: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = TripCard,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TripInk,
            )
        },
        text = {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = TripMuted,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Elimina", fontWeight = FontWeight.ExtraBold, color = TripError)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel·la", fontWeight = FontWeight.Bold, color = TripMuted)
            }
        },
    )
}

private fun TripStop.hasCoordinates(): Boolean =
    latitude != null && longitude != null

private fun buildStopMeta(stop: TripStop): String = buildString {
    append(stop.dateRange?.let { dateRangeFormatter.format(it) } ?: "Sense data")
    if (stop.hasCoordinates()) {
        append(" · ")
        append("%.4f, %.4f".format(requireNotNull(stop.latitude), requireNotNull(stop.longitude)))
    }
}

private fun Trip.dayCountText(): String {
    val range = dateRange ?: return "—"
    val start = range.start?.toLocalDateOrNull() ?: return "—"
    val end   = range.end?.toLocalDateOrNull() ?: start
    val days  = ChronoUnit.DAYS.between(start, end).coerceAtLeast(0) + 1
    return days.toString()
}

private fun com.atlas.domain.model.FlexibleDate.toLocalDateOrNull(): LocalDate? {
    val m = month ?: return null
    val d = day   ?: return null
    return runCatching { LocalDate.of(year, m, d) }.getOrNull()
}
