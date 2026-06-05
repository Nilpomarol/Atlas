package com.atlas.ui.screens.trip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
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
import com.atlas.domain.model.Excursion
import com.atlas.domain.model.ExcursionStop
import com.atlas.domain.model.Itinerary
import com.atlas.domain.model.LocationSearchResult
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop
import com.atlas.domain.model.TripStopSource
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.presentation.date.FlexibleDateRangeDraftField
import com.atlas.presentation.trip.TripDetailUiState
import com.atlas.presentation.trip.ExcursionDraftUiState
import com.atlas.presentation.trip.ExcursionStopDraftUiState
import com.atlas.presentation.trip.TripStopDraftUiState
import com.atlas.ui.components.date.FlexibleDateRangeField
import com.atlas.ui.components.tripStatusColors
import com.atlas.ui.theme.AtlasAccentContainer
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasError
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPlanned
import com.atlas.ui.theme.AtlasPlannedContainer
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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
    onItineraryClick: (String) -> Unit,
    onOpenItineraryPicker: () -> Unit,
    onDismissItineraryPicker: () -> Unit,
    onLinkItinerary: (Itinerary) -> Unit,
    onUnlinkItinerary: () -> Unit,
    onAddStopClick: () -> Unit,
    onDismissStopDraft: () -> Unit,
    onStopLocationNameChanged: (String) -> Unit,
    onLocationSearchQueryChanged: (String) -> Unit,
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
    onAddExcursionClick: (String?) -> Unit,
    onEditExcursion: (Excursion) -> Unit,
    onDeleteExcursion: (Excursion) -> Unit,
    onMoveExcursionUp: (Excursion) -> Unit,
    onMoveExcursionDown: (Excursion) -> Unit,
    onDismissExcursionDraft: () -> Unit,
    onExcursionTitleChanged: (String) -> Unit,
    onExcursionAnchorChanged: (String?) -> Unit,
    onExcursionNotesChanged: (String) -> Unit,
    onSaveExcursionDraft: () -> Unit,
    onAddExcursionStopClick: (String) -> Unit,
    onEditExcursionStop: (ExcursionStop) -> Unit,
    onDeleteExcursionStop: (ExcursionStop) -> Unit,
    onMoveExcursionStopUp: (String, ExcursionStop) -> Unit,
    onMoveExcursionStopDown: (String, ExcursionStop) -> Unit,
    onDismissExcursionStopDraft: () -> Unit,
    onExcursionStopLocationSearchQueryChanged: (String) -> Unit,
    onExcursionStopLocationSearchResultSelected: (LocationSearchResult) -> Unit,
    onUseManualExcursionStopEntryClick: () -> Unit,
    onExcursionStopLocationNameChanged: (String) -> Unit,
    onExcursionStopCountryChanged: (String) -> Unit,
    onExcursionStopLatitudeChanged: (String) -> Unit,
    onExcursionStopLongitudeChanged: (String) -> Unit,
    onExcursionStopDatePrecisionChanged: (DatePrecision) -> Unit,
    onExcursionStopDateFieldChanged: (FlexibleDateRangeDraftField, String) -> Unit,
    onExcursionStopNotesChanged: (String) -> Unit,
    onSaveExcursionStopDraft: () -> Unit,
) {
    var isDeleteTripDialogOpen by remember { mutableStateOf(false) }
    var pendingDeleteStop by remember { mutableStateOf<TripStop?>(null) }
    var pendingDeleteExcursion by remember { mutableStateOf<Excursion?>(null) }
    var pendingDeleteExcursionStop by remember { mutableStateOf<ExcursionStop?>(null) }
    var isReorderMode by remember { mutableStateOf(false) }

    val trip = uiState.trip

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AtlasBackground),
    ) {
        TripDetailTopBar(
            onBackClick = onBackClick,
            onEditTripClick = onEditTripClick,
            onDeleteTripClick = { isDeleteTripDialogOpen = true },
            actionsEnabled = trip != null,
        )

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
                    color = AtlasOnSurfaceMuted,
                )
            }
        } else {
            TripDetailContent(
                uiState = uiState,
                trip = trip,
                isReorderMode = isReorderMode,
                onReorderModeChanged = { isReorderMode = it },
                onItineraryClick = onItineraryClick,
                onOpenItineraryPicker = onOpenItineraryPicker,
                onUnlinkItinerary = onUnlinkItinerary,
                onAddStopClick = onAddStopClick,
                onEditStop = onEditStop,
                onMoveStopUp = onMoveStopUp,
                onMoveStopDown = onMoveStopDown,
                onDeleteStop = { pendingDeleteStop = it },
                onAddExcursionClick = onAddExcursionClick,
                onEditExcursion = onEditExcursion,
                onDeleteExcursion = { pendingDeleteExcursion = it },
                onMoveExcursionUp = onMoveExcursionUp,
                onMoveExcursionDown = onMoveExcursionDown,
                onAddExcursionStopClick = onAddExcursionStopClick,
                onEditExcursionStop = onEditExcursionStop,
                onDeleteExcursionStop = { pendingDeleteExcursionStop = it },
                onMoveExcursionStopUp = onMoveExcursionStopUp,
                onMoveExcursionStopDown = onMoveExcursionStopDown,
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

    if (uiState.isItineraryPickerOpen) {
        ItineraryPickerDialog(
            itineraries = uiState.availableItineraries,
            onDismiss = onDismissItineraryPicker,
            onSelect = onLinkItinerary,
        )
    }

    if (uiState.excursionDraft.isOpen) {
        ExcursionDialog(
            draft = uiState.excursionDraft,
            stops = uiState.stops,
            onDismiss = onDismissExcursionDraft,
            onTitleChanged = onExcursionTitleChanged,
            onAnchorChanged = onExcursionAnchorChanged,
            onNotesChanged = onExcursionNotesChanged,
            onSave = onSaveExcursionDraft,
        )
    }

    if (uiState.excursionStopDraft.isOpen) {
        ExcursionStopDialog(
            draft = uiState.excursionStopDraft,
            countries = uiState.countries,
            onDismiss = onDismissExcursionStopDraft,
            onLocationSearchQueryChanged = onExcursionStopLocationSearchQueryChanged,
            onLocationSearchResultSelected = onExcursionStopLocationSearchResultSelected,
            onUseManualEntryClick = onUseManualExcursionStopEntryClick,
            onLocationNameChanged = onExcursionStopLocationNameChanged,
            onCountryChanged = onExcursionStopCountryChanged,
            onLatitudeChanged = onExcursionStopLatitudeChanged,
            onLongitudeChanged = onExcursionStopLongitudeChanged,
            onDatePrecisionChanged = onExcursionStopDatePrecisionChanged,
            onDateFieldChanged = onExcursionStopDateFieldChanged,
            onNotesChanged = onExcursionStopNotesChanged,
            onSave = onSaveExcursionStopDraft,
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

    pendingDeleteExcursion?.let { excursion ->
        ConfirmDeleteDialog(
            title = "Eliminar excursió?",
            body = "S'eliminarà \"${excursion.title}\" i totes les seves parades. Aquesta acció no es pot desfer.",
            onDismiss = { pendingDeleteExcursion = null },
            onConfirm = { pendingDeleteExcursion = null; onDeleteExcursion(excursion) },
        )
    }

    pendingDeleteExcursionStop?.let { stop ->
        ConfirmDeleteDialog(
            title = "Eliminar parada d'excursió?",
            body = "S'eliminarà \"${stop.locationName}\" de l'excursio. Aquesta acció no es pot desfer.",
            onDismiss = { pendingDeleteExcursionStop = null },
            onConfirm = { pendingDeleteExcursionStop = null; onDeleteExcursionStop(stop) },
        )
    }
}

// ─────────────────────────────────────────────
// Top bar
// ─────────────────────────────────────────────
@Composable
private fun TripDetailTopBar(
    onBackClick: () -> Unit,
    onEditTripClick: () -> Unit,
    onDeleteTripClick: () -> Unit,
    actionsEnabled: Boolean,
) {
    var actionsExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.size(42.dp),
            shape = CircleShape,
            color = AtlasSurface,
            border = BorderStroke(1.dp, AtlasOutline),
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Enrere",
                    tint = AtlasOnSurfaceStrong,
                    modifier = Modifier.size(19.dp),
                )
            }
        }

        Box(modifier = Modifier.weight(1f))

        if (actionsEnabled) {
            Box {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = AtlasSurface,
                    border = BorderStroke(1.dp, AtlasOutline),
                ) {
                    IconButton(onClick = { actionsExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Accions",
                            tint = AtlasOnSurfaceStrong,
                        )
                    }
                }
                MaterialTheme(
                    colorScheme = MaterialTheme.colorScheme.copy(surfaceContainer = AtlasSurface),
                    shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(14.dp)),
                ) {
                    DropdownMenu(
                        expanded = actionsExpanded,
                        onDismissRequest = { actionsExpanded = false },
                        modifier = Modifier.width(190.dp),
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Edita viatge",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = AtlasOnSurfaceStrong,
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = null,
                                    tint = AtlasOnSurfaceStrong,
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                            onClick = { actionsExpanded = false; onEditTripClick() },
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(AtlasOutline),
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Elimina viatge",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = AtlasError,
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = AtlasError,
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                            onClick = { actionsExpanded = false; onDeleteTripClick() },
                        )
                    }
                }
            }
        }
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
    onItineraryClick: (String) -> Unit,
    onOpenItineraryPicker: () -> Unit,
    onUnlinkItinerary: () -> Unit,
    onAddStopClick: () -> Unit,
    onEditStop: (TripStop) -> Unit,
    onMoveStopUp: (TripStop) -> Unit,
    onMoveStopDown: (TripStop) -> Unit,
    onDeleteStop: (TripStop) -> Unit,
    onAddExcursionClick: (String?) -> Unit,
    onEditExcursion: (Excursion) -> Unit,
    onDeleteExcursion: (Excursion) -> Unit,
    onMoveExcursionUp: (Excursion) -> Unit,
    onMoveExcursionDown: (Excursion) -> Unit,
    onAddExcursionStopClick: (String) -> Unit,
    onEditExcursionStop: (ExcursionStop) -> Unit,
    onDeleteExcursionStop: (ExcursionStop) -> Unit,
    onMoveExcursionStopUp: (String, ExcursionStop) -> Unit,
    onMoveExcursionStopDown: (String, ExcursionStop) -> Unit,
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        // Map hero — full width, no horizontal padding
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
        ) {
            TripMapPreview(
                stops = uiState.stops,
                excursions = uiState.excursions,
            )
        }

        // Padded card stack
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            TripIdentityCard(trip = trip)

            TripStatStrip(trip = trip, stopCount = uiState.stops.size)

            LinkedItineraryPanel(
                linkedItinerary = uiState.linkedItinerary,
                availableCount = uiState.availableItineraries.size,
                onItineraryClick = onItineraryClick,
                onOpenItineraryPicker = onOpenItineraryPicker,
                onUnlinkItinerary = onUnlinkItinerary,
            )

            TripStopsSection(
                stops = uiState.stops,
                excursions = uiState.excursions,
                countries = uiState.countries,
                isReorderMode = isReorderMode,
                onReorderModeChanged = onReorderModeChanged,
                onAddStopClick = onAddStopClick,
                onEditStop = onEditStop,
                onMoveStopUp = onMoveStopUp,
                onMoveStopDown = onMoveStopDown,
                onDeleteStop = onDeleteStop,
                onAddExcursionClick = onAddExcursionClick,
                onEditExcursion = onEditExcursion,
                onDeleteExcursion = onDeleteExcursion,
                onMoveExcursionUp = onMoveExcursionUp,
                onMoveExcursionDown = onMoveExcursionDown,
                onAddExcursionStopClick = onAddExcursionStopClick,
                onEditExcursionStop = onEditExcursionStop,
                onDeleteExcursionStop = onDeleteExcursionStop,
                onMoveExcursionStopUp = onMoveExcursionStopUp,
                onMoveExcursionStopDown = onMoveExcursionStopDown,
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────
// Identity card
// ─────────────────────────────────────────────
@Composable
private fun TripIdentityCard(trip: Trip) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = trip.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = AtlasOnSurfaceStrong,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                TripStatusPill(status = trip.status)
            }
            trip.dateRange?.let { range ->
                Text(
                    text = dateRangeFormatter.format(range),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AtlasOnSurfaceMuted,
                )
            }
            trip.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AtlasOnSurfaceMuted,
                )
            }
        }
    }
}

@Composable
private fun TripStatusPill(status: TravelStatus) {
    val colors = status.tripStatusColors()
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(colors.container)
            .padding(horizontal = 11.dp, vertical = 4.dp),
    ) {
        Text(
            text = status.toCatalanLabel().uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = colors.foreground,
            letterSpacing = 0.12.sp,
        )
    }
}

// ─────────────────────────────────────────────
// Stat strip
// ─────────────────────────────────────────────
@Composable
private fun TripStatStrip(trip: Trip, stopCount: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Row {
            TripStatCell(
                value = trip.dayCountText(),
                label = "Dies",
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(52.dp)
                    .align(Alignment.CenterVertically)
                    .background(AtlasOutline),
            )
            TripStatCell(
                value = stopCount.toString(),
                label = "Parades",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TripStatCell(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 14.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 23.sp),
            fontWeight = FontWeight.Medium,
            color = AtlasOnSurfaceStrong,
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

// ─────────────────────────────────────────────
// Linked itinerary panel
// ─────────────────────────────────────────────
@Composable
private fun LinkedItineraryPanel(
    linkedItinerary: Itinerary?,
    availableCount: Int,
    onItineraryClick: (String) -> Unit,
    onOpenItineraryPicker: () -> Unit,
    onUnlinkItinerary: () -> Unit,
) {
    if (linkedItinerary == null) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = availableCount > 0, onClick = onOpenItineraryPicker),
            shape = RoundedCornerShape(18.dp),
            color = AtlasSurface,
            border = BorderStroke(1.dp, AtlasOutline),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Link,
                    contentDescription = null,
                    tint = AtlasOnSurfaceMuted,
                    modifier = Modifier.size(19.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Itinerari vinculat".uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AtlasOnSurfaceMuted,
                    )
                    Text(
                        text = "Sense itinerari assignat",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = AtlasOnSurfaceMuted,
                    )
                }
                if (availableCount > 0) {
                    Text(
                        text = "Assigna",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = AtlasPrimary,
                    )
                }
            }
        }
        return
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItineraryClick(linkedItinerary.id) },
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Link,
                contentDescription = null,
                tint = AtlasOnSurfaceMuted,
                modifier = Modifier.size(19.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Itinerari vinculat".uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceMuted,
                )
                Text(
                    text = linkedItinerary.title.ifBlank { "Itinerari" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            TextButton(onClick = onUnlinkItinerary) {
                Text("Desvincula", color = AtlasPrimary)
            }
        }
    }
}

// ─────────────────────────────────────────────
// Itinerary picker dialog
// ─────────────────────────────────────────────
@Composable
private fun ItineraryPickerDialog(
    itineraries: List<Itinerary>,
    onDismiss: () -> Unit,
    onSelect: (Itinerary) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = AtlasSurface,
        title = {
            Text(
                text = "Vincula itinerari",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceStrong,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (itineraries.isEmpty()) {
                    Text(
                        text = "No hi ha itineraris sense viatge.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AtlasOnSurfaceMuted,
                    )
                } else {
                    itineraries.forEach { itinerary ->
                        Surface(
                            onClick = { onSelect(itinerary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = AtlasBackground,
                            border = BorderStroke(1.dp, AtlasOutline),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = itinerary.title.ifBlank { "Itinerari" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AtlasOnSurfaceStrong,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel·la", fontWeight = FontWeight.Bold, color = AtlasOnSurfaceMuted)
            }
        },
    )
}

// ─────────────────────────────────────────────
// Stops section
// ─────────────────────────────────────────────
@Composable
private fun TripStopsSection(
    stops: List<TripStop>,
    excursions: List<Excursion>,
    countries: List<Country>,
    isReorderMode: Boolean,
    onReorderModeChanged: (Boolean) -> Unit,
    onAddStopClick: () -> Unit,
    onEditStop: (TripStop) -> Unit,
    onMoveStopUp: (TripStop) -> Unit,
    onMoveStopDown: (TripStop) -> Unit,
    onDeleteStop: (TripStop) -> Unit,
    onAddExcursionClick: (String?) -> Unit,
    onEditExcursion: (Excursion) -> Unit,
    onDeleteExcursion: (Excursion) -> Unit,
    onMoveExcursionUp: (Excursion) -> Unit,
    onMoveExcursionDown: (Excursion) -> Unit,
    onAddExcursionStopClick: (String) -> Unit,
    onEditExcursionStop: (ExcursionStop) -> Unit,
    onDeleteExcursionStop: (ExcursionStop) -> Unit,
    onMoveExcursionStopUp: (String, ExcursionStop) -> Unit,
    onMoveExcursionStopDown: (String, ExcursionStop) -> Unit,
) {
    val excursionsByAnchor = excursions.groupBy { it.anchorTripStopId }
    val totalTimelineItems = stops.size + excursions.size

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
                    color = AtlasOnSurfaceStrong,
                )
                if (totalTimelineItems > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(AtlasOnSurfaceStrong)
                            .padding(horizontal = 9.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = totalTimelineItems.toString(),
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
                        containerColor = AtlasNavy,
                        contentColor = Color.White,
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
                ) {
                    Icon(Icons.Filled.Add, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Afegeix", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }

                if (totalTimelineItems > 0) {
                    IconButton(
                        onClick = { onReorderModeChanged(!isReorderMode) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isReorderMode) AtlasPrimary else AtlasSurface)
                            .border(
                                width = 1.dp,
                                color = if (isReorderMode) AtlasPrimary else AtlasOutline,
                                shape = RoundedCornerShape(12.dp),
                            ),
                    ) {
                        Icon(
                            imageVector = if (isReorderMode) Icons.Filled.Check else Icons.Filled.Menu,
                            contentDescription = if (isReorderMode) "Acaba de reordenar" else "Reordena parades",
                            tint = if (isReorderMode) Color.White else AtlasOnSurfaceMuted,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }

        if (stops.isEmpty() && excursions.isEmpty()) {
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
                excursionsByAnchor[stop.id].orEmpty().forEach { excursion ->
                    val excursionIndex = excursions.indexOf(excursion)
                    Box(modifier = Modifier.padding(start = 28.dp)) {
                        ExcursionCard(
                            excursion = excursion,
                            anchorName = stop.locationName,
                            countries = countries,
                            canMoveUp = excursionIndex > 0,
                            canMoveDown = excursionIndex in 0 until excursions.lastIndex,
                            isReorderMode = isReorderMode,
                            onEditExcursion = onEditExcursion,
                            onDeleteExcursion = onDeleteExcursion,
                            onMoveExcursionUp = onMoveExcursionUp,
                            onMoveExcursionDown = onMoveExcursionDown,
                            onAddExcursionStopClick = onAddExcursionStopClick,
                            onEditExcursionStop = onEditExcursionStop,
                            onDeleteExcursionStop = onDeleteExcursionStop,
                            onMoveExcursionStopUp = onMoveExcursionStopUp,
                            onMoveExcursionStopDown = onMoveExcursionStopDown,
                        )
                    }
                }
            }
            excursionsByAnchor[null].orEmpty().forEach { excursion ->
                val excursionIndex = excursions.indexOf(excursion)
                ExcursionCard(
                    excursion = excursion,
                    anchorName = null,
                    countries = countries,
                    canMoveUp = excursionIndex > 0,
                    canMoveDown = excursionIndex in 0 until excursions.lastIndex,
                    isReorderMode = isReorderMode,
                    onEditExcursion = onEditExcursion,
                    onDeleteExcursion = onDeleteExcursion,
                    onMoveExcursionUp = onMoveExcursionUp,
                    onMoveExcursionDown = onMoveExcursionDown,
                    onAddExcursionStopClick = onAddExcursionStopClick,
                    onEditExcursionStop = onEditExcursionStop,
                    onDeleteExcursionStop = onDeleteExcursionStop,
                    onMoveExcursionStopUp = onMoveExcursionStopUp,
                    onMoveExcursionStopDown = onMoveExcursionStopDown,
                )
            }
            TextButton(
                onClick = { onAddExcursionClick(null) },
                colors = ButtonDefaults.textButtonColors(contentColor = AtlasPrimary),
            ) {
                Icon(Icons.Filled.Add, null, Modifier.size(14.dp))
                Spacer(Modifier.width(5.dp))
                Text("Afegeix excursió", fontWeight = FontWeight.ExtraBold)
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
            .background(AtlasSurface)
            .border(1.dp, AtlasOutline, RoundedCornerShape(18.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Sense parades encara.",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.ExtraBold,
            color = AtlasOnSurfaceStrong,
        )
        Text(
            text = "Afegeix els llocs del viatge en ordre. Després els podràs reordenar.",
            style = MaterialTheme.typography.bodyMedium,
            color = AtlasOnSurfaceMuted,
        )
        Spacer(Modifier.height(2.dp))
        Button(
            onClick = onAddStopClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AtlasPrimary,
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
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
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
                        .background(if (hasCoords) AtlasAccentContainer else AtlasBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (hasCoords) AtlasPrimary else AtlasOnSurfaceMuted,
                    )
                }
                // Position bubble
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = 4.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(AtlasOnSurfaceStrong),
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
                    text = stop.displayTitle ?: stop.locationName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    TypeBadge(
                        label = if (stop.source == TripStopSource.ITINERARY_GROUP) {
                            "ITINERARI"
                        } else if (hasCoords) {
                            "MAPA"
                        } else {
                            "MANUAL"
                        },
                        color = if (stop.source == TripStopSource.ITINERARY_GROUP || hasCoords) AtlasPrimary else AtlasPlanned,
                        background = if (stop.source == TripStopSource.ITINERARY_GROUP || hasCoords) {
                            AtlasAccentContainer
                        } else {
                            AtlasPlannedContainer
                        },
                    )
                    Text(
                        text = countryName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasOnSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = buildStopMeta(stop),
                    style = MaterialTheme.typography.bodySmall,
                    color = AtlasOnSurfaceMuted,
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
                            tint = if (canMoveUp) AtlasOnSurfaceStrong else AtlasOutline,
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
                            tint = if (canMoveDown) AtlasOnSurfaceStrong else AtlasOutline,
                        )
                    }
                }
            } else {
                if (stop.source == TripStopSource.MANUAL) {
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
private fun ExcursionCard(
    excursion: Excursion,
    anchorName: String?,
    countries: List<Country>,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    isReorderMode: Boolean,
    onEditExcursion: (Excursion) -> Unit,
    onDeleteExcursion: (Excursion) -> Unit,
    onMoveExcursionUp: (Excursion) -> Unit,
    onMoveExcursionDown: (Excursion) -> Unit,
    onAddExcursionStopClick: (String) -> Unit,
    onEditExcursionStop: (ExcursionStop) -> Unit,
    onDeleteExcursionStop: (ExcursionStop) -> Unit,
    onMoveExcursionStopUp: (String, ExcursionStop) -> Unit,
    onMoveExcursionStopDown: (String, ExcursionStop) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AtlasSurface)
            .border(1.dp, AtlasOutline, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = excursion.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = anchorName?.let { "Ancorada a $it" } ?: "Sense parada d'ancoratge",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                if (isReorderMode) {
                    Row {
                        IconButton(onClick = { onMoveExcursionUp(excursion) }, enabled = canMoveUp, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Filled.KeyboardArrowUp, "Mou amunt", tint = if (canMoveUp) AtlasOnSurfaceStrong else AtlasOutline)
                        }
                        IconButton(onClick = { onMoveExcursionDown(excursion) }, enabled = canMoveDown, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Filled.KeyboardArrowDown, "Mou avall", tint = if (canMoveDown) AtlasOnSurfaceStrong else AtlasOutline)
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        SmallActionButton("Edita") { onEditExcursion(excursion) }
                        SmallActionButton("Elimina") { onDeleteExcursion(excursion) }
                    }
                }
            }
        }

        if (excursion.stops.isEmpty()) {
            Text(
                text = "Encara no hi ha parades d'excursio.",
                style = MaterialTheme.typography.bodySmall,
                color = AtlasOnSurfaceMuted,
            )
        } else {
            excursion.stops.sortedBy { it.sortOrder }.forEachIndexed { index, stop ->
                val countryName = countries.firstOrNull { it.iso2 == stop.countryIso2 }?.nameCa ?: stop.countryIso2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = AtlasPrimary,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stop.locationName, fontWeight = FontWeight.ExtraBold, color = AtlasOnSurfaceStrong, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            text = listOfNotNull(
                                countryName,
                                stop.dateRange?.let { dateRangeFormatter.format(it) },
                            ).joinToString(" · "),
                            color = AtlasOnSurfaceMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (isReorderMode) {
                        IconButton(
                            onClick = { onMoveExcursionStopUp(excursion.id, stop) },
                            enabled = index > 0,
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(Icons.Filled.KeyboardArrowUp, "Mou amunt", tint = if (index > 0) AtlasOnSurfaceStrong else AtlasOutline)
                        }
                        IconButton(
                            onClick = { onMoveExcursionStopDown(excursion.id, stop) },
                            enabled = index < excursion.stops.lastIndex,
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(Icons.Filled.KeyboardArrowDown, "Mou avall", tint = if (index < excursion.stops.lastIndex) AtlasOnSurfaceStrong else AtlasOutline)
                        }
                    } else {
                        SmallActionButton("Edita") { onEditExcursionStop(stop) }
                        SmallActionButton("Elimina") { onDeleteExcursionStop(stop) }
                    }
                }
            }
        }

        if (!isReorderMode) {
            TextButton(onClick = { onAddExcursionStopClick(excursion.id) }) {
                Text("Afegeix parada d'excursio", fontWeight = FontWeight.ExtraBold, color = AtlasPrimary)
            }
        }
    }
}

@Composable
private fun SmallActionButton(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = AtlasOnSurfaceMuted,
            containerColor = AtlasBackground,
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
private fun ExcursionDialog(
    draft: ExcursionDraftUiState,
    stops: List<TripStop>,
    onDismiss: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onAnchorChanged: (String?) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = AtlasSurface,
        title = {
            Text(
                text = if (draft.excursionId == null) "Afegeix excursió" else "Edita excursio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceStrong,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draft.title,
                    onValueChange = onTitleChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Titol", fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(14.dp),
                )
                DialogSectionLabel("Parada d'ancoratge")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AtlasBackground)
                        .border(1.dp, AtlasOutline, RoundedCornerShape(12.dp))
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    TextButton(onClick = { onAnchorChanged(null) }) {
                        Text(
                            text = if (draft.anchorTripStopId == null) "Sense ancoratge seleccionat" else "Sense ancoratge",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (draft.anchorTripStopId == null) AtlasPrimary else AtlasOnSurfaceMuted,
                        )
                    }
                    stops.forEach { stop ->
                        TextButton(onClick = { onAnchorChanged(stop.id) }) {
                            Text(
                                text = stop.displayTitle ?: stop.locationName,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (draft.anchorTripStopId == stop.id) AtlasPrimary else AtlasOnSurfaceStrong,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = draft.notes,
                    onValueChange = onNotesChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Notes", fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(14.dp),
                    minLines = 1,
                    maxLines = 3,
                )
                draft.validationError?.let { error ->
                    Text(error, color = AtlasError, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            CompactTripDialogActionButton(onClick = onSave) {
                Text("Desa", fontWeight = FontWeight.ExtraBold, color = AtlasPrimary)
            }
        },
        dismissButton = {
            CompactTripDialogActionButton(onClick = onDismiss) {
                Text("Cancel.la", fontWeight = FontWeight.Bold, color = AtlasOnSurfaceMuted)
            }
        },
    )
}

@Composable
private fun ExcursionStopDialog(
    draft: ExcursionStopDraftUiState,
    countries: List<Country>,
    onDismiss: () -> Unit,
    onLocationSearchQueryChanged: (String) -> Unit,
    onLocationSearchResultSelected: (LocationSearchResult) -> Unit,
    onUseManualEntryClick: () -> Unit,
    onLocationNameChanged: (String) -> Unit,
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
        containerColor = AtlasSurface,
        title = {
            Text(
                text = if (draft.stopId == null) "Afegeix parada d'excursio" else "Edita parada d'excursio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceStrong,
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // ── Location search ──
                OutlinedTextField(
                    value = draft.locationSearchQuery,
                    onValueChange = onLocationSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Cerca un lloc", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    placeholder = { Text("Nom o adreça...", color = AtlasOnSurfaceMuted) },
                    leadingIcon = {
                        if (draft.isSearchingLocation) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = AtlasPrimary)
                        } else {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = AtlasOnSurfaceMuted, modifier = Modifier.size(18.dp))
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                )

                draft.locationSearchError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasError,
                    )
                }

                if (draft.locationSearchResults.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AtlasBackground)
                            .border(1.dp, AtlasOutline, RoundedCornerShape(12.dp)),
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
                        colors = ButtonDefaults.textButtonColors(contentColor = AtlasPrimary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    ) {
                        Text("Entrada manual", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }

                if (showManualFields) {
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
                            placeholder = { Text("Opcional", color = AtlasOnSurfaceMuted) },
                            shape = RoundedCornerShape(14.dp),
                        )
                        OutlinedTextField(
                            value = draft.longitude,
                            onValueChange = onLongitudeChanged,
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("Longitud", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            placeholder = { Text("Opcional", color = AtlasOnSurfaceMuted) },
                            shape = RoundedCornerShape(14.dp),
                        )
                    }
                }
                Text(
                    text = "Dades de cerca OpenStreetMap contributors",
                    style = MaterialTheme.typography.labelSmall,
                    color = AtlasOnSurfaceMuted,
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
                    label = { Text("Notes", fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(14.dp),
                    minLines = 1,
                    maxLines = 3,
                )
                draft.validationError?.let { error ->
                    Text(error, color = AtlasError, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            CompactTripDialogActionButton(onClick = onSave) {
                Text("Desa", fontWeight = FontWeight.ExtraBold, color = AtlasPrimary)
            }
        },
        dismissButton = {
            CompactTripDialogActionButton(onClick = onDismiss) {
                Text("Cancel.la", fontWeight = FontWeight.Bold, color = AtlasOnSurfaceMuted)
            }
        },
    )
}

@Composable
private fun TripStopDialog(
    draft: TripStopDraftUiState,
    countries: List<Country>,
    onDismiss: () -> Unit,
    onLocationNameChanged: (String) -> Unit,
    onLocationSearchQueryChanged: (String) -> Unit,
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
        containerColor = AtlasSurface,
        title = {
            Text(
                text = if (draft.isEditing) "Edita parada" else "Afegeix parada",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceStrong,
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // ── Location search ──
                OutlinedTextField(
                    value = draft.locationSearchQuery,
                    onValueChange = onLocationSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Cerca un lloc", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    placeholder = { Text("Nom o adreça...", color = AtlasOnSurfaceMuted) },
                    leadingIcon = {
                        if (draft.isSearchingLocation) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = AtlasPrimary)
                        } else {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = AtlasOnSurfaceMuted, modifier = Modifier.size(18.dp))
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                )

                draft.locationSearchError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasError,
                    )
                }

                if (draft.locationSearchResults.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AtlasBackground)
                            .border(1.dp, AtlasOutline, RoundedCornerShape(12.dp)),
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
                        colors = ButtonDefaults.textButtonColors(contentColor = AtlasPrimary),
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
                            placeholder = { Text("Opcional", color = AtlasOnSurfaceMuted) },
                            shape = RoundedCornerShape(14.dp),
                        )
                        OutlinedTextField(
                            value = draft.longitude,
                            onValueChange = onLongitudeChanged,
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("Longitud", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            placeholder = { Text("Opcional", color = AtlasOnSurfaceMuted) },
                            shape = RoundedCornerShape(14.dp),
                        )
                    }
                }
                Text(
                    text = "Dades de cerca © OpenStreetMap contributors",
                    style = MaterialTheme.typography.labelSmall,
                    color = AtlasOnSurfaceMuted,
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
                        color = AtlasError,
                    )
                }
            }
        },
        confirmButton = {
            CompactTripDialogActionButton(onClick = onSave) {
                Text("Desa", fontWeight = FontWeight.ExtraBold, color = AtlasPrimary)
            }
        },
        dismissButton = {
            CompactTripDialogActionButton(onClick = onDismiss) {
                Text("Cancel·la", fontWeight = FontWeight.Bold, color = AtlasOnSurfaceMuted)
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
        color = AtlasOnSurfaceMuted,
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
                .background(AtlasPrimary),
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
                color = AtlasOnSurfaceStrong,
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
                color = AtlasOnSurfaceMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (showEditDetails) {
            TextButton(
                onClick = onEditDetailsClick,
                modifier = Modifier.height(30.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = AtlasPrimary),
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
        colors = ButtonDefaults.textButtonColors(contentColor = AtlasOnSurfaceStrong),
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
                color = AtlasOnSurfaceMuted,
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
        containerColor = AtlasSurface,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceStrong,
            )
        },
        text = {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = AtlasOnSurfaceMuted,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Elimina", fontWeight = FontWeight.ExtraBold, color = AtlasError)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel·la", fontWeight = FontWeight.Bold, color = AtlasOnSurfaceMuted)
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
