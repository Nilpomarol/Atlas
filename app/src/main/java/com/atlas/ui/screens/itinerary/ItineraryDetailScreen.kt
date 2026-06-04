package com.atlas.ui.screens.itinerary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.Airport
import com.atlas.domain.model.Flight
import com.atlas.domain.model.ItineraryGroup
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.presentation.flight.FlightEditorDraftUiState
import com.atlas.presentation.itinerary.GroupEditorDraft
import com.atlas.presentation.itinerary.ItineraryDetailUiState
import com.atlas.presentation.itinerary.ItineraryEditorDraft
import com.atlas.ui.components.AtlasPill
import com.atlas.ui.components.tripStatusColors
import com.atlas.ui.screens.flight.FlightEditorDialog
import com.atlas.ui.screens.trip.toCatalanLabel
import com.atlas.ui.theme.AtlasAccentContainer
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasSurfaceSubtle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryDetailScreen(
    uiState: ItineraryDetailUiState,
    onBackClick: () -> Unit,
    onTripClick: (String) -> Unit,
    onUnlinkTripClick: () -> Unit,
    onEditItineraryClick: () -> Unit,
    onDeleteItineraryClick: () -> Unit,
    onDismissItineraryDraft: () -> Unit,
    onItineraryTitleChanged: (String) -> Unit,
    onItineraryNotesChanged: (String) -> Unit,
    onSaveItineraryDraft: () -> Unit,
    onAddGroupClick: () -> Unit,
    onEditGroupClick: (ItineraryGroup) -> Unit,
    onDeleteGroupClick: (ItineraryGroup) -> Unit,
    onDismissGroupDraft: () -> Unit,
    onGroupTitleChanged: (String) -> Unit,
    onGroupStatusChanged: (TravelStatus?) -> Unit,
    onSaveGroupDraft: () -> Unit,
    onToggleGroupReorderMode: () -> Unit,
    onMoveGroupUp: (ItineraryGroup) -> Unit,
    onMoveGroupDown: (ItineraryGroup) -> Unit,
    onAddFlightToGroupClick: (String) -> Unit,
    onFlightClick: (String) -> Unit,
    onEditFlightClick: (Flight) -> Unit,
    onDeleteFlightClick: (Flight) -> Unit,
    onToggleFlightReorderMode: (String) -> Unit,
    onMoveFlightUp: (String, Flight) -> Unit,
    onMoveFlightDown: (String, Flight) -> Unit,
    onDismissFlightDraft: () -> Unit,
    onFlightOriginQueryChanged: (String) -> Unit,
    onFlightOriginSelected: (Airport) -> Unit,
    onFlightDestinationQueryChanged: (String) -> Unit,
    onFlightDestinationSelected: (Airport) -> Unit,
    onFlightStatusChanged: (TravelStatus) -> Unit,
    onScheduledDepartureAtChanged: (String) -> Unit,
    onScheduledArrivalAtChanged: (String) -> Unit,
    onActualDepartureAtChanged: (String) -> Unit,
    onActualArrivalAtChanged: (String) -> Unit,
    onAirlineQueryChanged: (String) -> Unit,
    onAirlineSelected: (com.atlas.domain.model.Airline) -> Unit,
    onFlightNumberChanged: (String) -> Unit,
    onAircraftChanged: (String) -> Unit,
    onAircraftRegistrationChanged: (String) -> Unit,
    onFlightNotesChanged: (String) -> Unit,
    onSaveFlightDraft: () -> Unit,
) {
    var showDeleteItineraryDialog by remember { mutableStateOf(false) }
    var groupToDelete by remember { mutableStateOf<ItineraryGroup?>(null) }
    var flightToDelete by remember { mutableStateOf<Flight?>(null) }

    Column {
        TopAppBar(
            title = {
                Text(
                    text = uiState.itinerary?.title ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Enrere")
                }
            },
            actions = {
                IconButton(onClick = onEditItineraryClick) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edita itinerari", tint = AtlasOnSurfaceMuted)
                }
                IconButton(onClick = { showDeleteItineraryDialog = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Elimina itinerari", tint = AtlasOnSurfaceMuted)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AtlasBackground),
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                LinkedTripCard(
                    trip = uiState.linkedTrip,
                    onTripClick = onTripClick,
                    onUnlinkTripClick = onUnlinkTripClick,
                )
            }

            // Groups header row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Grups",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AtlasOnSurfaceStrong,
                        modifier = Modifier.weight(1f),
                    )
                    if (uiState.groups.size > 1) {
                        TextButton(onClick = onToggleGroupReorderMode) {
                            Text(
                                text = if (uiState.isGroupReorderMode) "Fet" else "Reordena",
                                style = MaterialTheme.typography.labelMedium,
                                color = AtlasPrimary,
                            )
                        }
                    }
                    if (!uiState.isGroupReorderMode) {
                        Button(
                            onClick = onAddGroupClick,
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AtlasAccentContainer,
                                contentColor = AtlasPrimary,
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(text = "Grup", modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }

            if (uiState.groups.isEmpty()) {
                item {
                    Text(
                        text = "Afegeix un grup per organitzar els vols d'aquest itinerari.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AtlasOnSurfaceMuted,
                    )
                }
            }

            items(uiState.groups, key = { it.id }) { group ->
                ItineraryGroupCard(
                    group = group,
                    isGroupReorderMode = uiState.isGroupReorderMode,
                    isFlightReorderMode = uiState.reorderingFlightsGroupId == group.id,
                    isFirstGroup = uiState.groups.first().id == group.id,
                    isLastGroup = uiState.groups.last().id == group.id,
                    onEditGroupClick = { onEditGroupClick(group) },
                    onDeleteGroupClick = { groupToDelete = group },
                    onMoveGroupUp = { onMoveGroupUp(group) },
                    onMoveGroupDown = { onMoveGroupDown(group) },
                    onAddFlightClick = { onAddFlightToGroupClick(group.id) },
                    onFlightClick = { onFlightClick(it.id) },
                    onEditFlightClick = onEditFlightClick,
                    onDeleteFlightClick = { flightToDelete = it },
                    onToggleFlightReorderMode = { onToggleFlightReorderMode(group.id) },
                    onMoveFlightUp = { flight -> onMoveFlightUp(group.id, flight) },
                    onMoveFlightDown = { flight -> onMoveFlightDown(group.id, flight) },
                )
            }
        }
    }

    // Itinerary editor dialog
    if (uiState.itineraryDraft.isOpen) {
        ItineraryEditorDialog(
            draft = uiState.itineraryDraft,
            onDismiss = onDismissItineraryDraft,
            onTitleChanged = onItineraryTitleChanged,
            onNotesChanged = onItineraryNotesChanged,
            onSave = onSaveItineraryDraft,
        )
    }

    // Group editor dialog
    if (uiState.groupDraft.isOpen) {
        GroupEditorDialog(
            draft = uiState.groupDraft,
            onDismiss = onDismissGroupDraft,
            onTitleChanged = onGroupTitleChanged,
            onStatusChanged = onGroupStatusChanged,
            onSave = onSaveGroupDraft,
        )
    }

    // Flight editor dialog
    if (uiState.flightDraft.isOpen) {
        FlightEditorDialog(
            draft = uiState.flightDraft,
            originResults = uiState.originSearchResults,
            destinationResults = uiState.destinationSearchResults,
            onDismiss = onDismissFlightDraft,
            onOriginQueryChanged = onFlightOriginQueryChanged,
            onOriginSelected = onFlightOriginSelected,
            onDestinationQueryChanged = onFlightDestinationQueryChanged,
            onDestinationSelected = onFlightDestinationSelected,
            onStatusChanged = onFlightStatusChanged,
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
            onNotesChanged = onFlightNotesChanged,
            onSave = onSaveFlightDraft,
        )
    }

    // Delete itinerary confirmation
    if (showDeleteItineraryDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteItineraryDialog = false },
            title = { Text("Elimina itinerari") },
            text = { Text("Vols eliminar «${uiState.itinerary?.title ?: ""}»? Els vols associats quedaran sense itinerari assignat. Aquesta acció no es pot desfer.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteItineraryClick()
                    showDeleteItineraryDialog = false
                }) { Text("Elimina") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteItineraryDialog = false }) { Text("Cancel·la") }
            },
        )
    }

    // Delete group confirmation
    groupToDelete?.let { group ->
        AlertDialog(
            onDismissRequest = { groupToDelete = null },
            title = { Text("Elimina grup") },
            text = { Text("Vols eliminar el grup «${group.title ?: "Sense títol"}»? Els vols del grup quedaran sense grup assignat. Aquesta acció no es pot desfer.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteGroupClick(group)
                    groupToDelete = null
                }) { Text("Elimina") }
            },
            dismissButton = {
                TextButton(onClick = { groupToDelete = null }) { Text("Cancel·la") }
            },
        )
    }

    // Delete flight confirmation
    flightToDelete?.let { flight ->
        AlertDialog(
            onDismissRequest = { flightToDelete = null },
            title = { Text("Elimina vol") },
            text = { Text("Vols eliminar aquest vol? Aquesta acció no es pot desfer.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteFlightClick(flight)
                    flightToDelete = null
                }) { Text("Elimina") }
            },
            dismissButton = {
                TextButton(onClick = { flightToDelete = null }) { Text("Cancel·la") }
            },
        )
    }
}

@Composable
private fun LinkedTripCard(
    trip: Trip?,
    onTripClick: (String) -> Unit,
    onUnlinkTripClick: () -> Unit,
) {
    if (trip == null) return

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTripClick(trip.id) },
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Viatge vinculat",
                    style = MaterialTheme.typography.labelMedium,
                    color = AtlasOnSurfaceMuted,
                )
                Text(
                    text = trip.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            TextButton(onClick = onUnlinkTripClick) {
                Text("Desvincula", color = AtlasPrimary)
            }
        }
    }
}

@Composable
private fun ItineraryGroupCard(
    group: ItineraryGroup,
    isGroupReorderMode: Boolean,
    isFlightReorderMode: Boolean,
    isFirstGroup: Boolean,
    isLastGroup: Boolean,
    onEditGroupClick: () -> Unit,
    onDeleteGroupClick: () -> Unit,
    onMoveGroupUp: () -> Unit,
    onMoveGroupDown: () -> Unit,
    onAddFlightClick: () -> Unit,
    onFlightClick: (Flight) -> Unit,
    onEditFlightClick: (Flight) -> Unit,
    onDeleteFlightClick: (Flight) -> Unit,
    onToggleFlightReorderMode: () -> Unit,
    onMoveFlightUp: (Flight) -> Unit,
    onMoveFlightDown: (Flight) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Group header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.title ?: "Grup sense títol",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AtlasOnSurfaceStrong,
                    )
                }
                group.status?.let { status ->
                    val colors = status.tripStatusColors()
                    AtlasPill(label = colors.label, colors = colors)
                }
                if (isGroupReorderMode) {
                    IconButton(onClick = onMoveGroupUp, enabled = !isFirstGroup, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Puja", tint = if (!isFirstGroup) AtlasPrimary else AtlasOnSurfaceMuted)
                    }
                    IconButton(onClick = onMoveGroupDown, enabled = !isLastGroup, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Baixa", tint = if (!isLastGroup) AtlasPrimary else AtlasOnSurfaceMuted)
                    }
                } else {
                    IconButton(onClick = onEditGroupClick, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edita grup", tint = AtlasOnSurfaceMuted, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDeleteGroupClick, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Elimina grup", tint = AtlasOnSurfaceMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Flights list
            if (group.flights.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    group.flights.forEachIndexed { index, flight ->
                        GroupFlightRow(
                            flight = flight,
                            isReorderMode = isFlightReorderMode,
                            isFirst = index == 0,
                            isLast = index == group.flights.lastIndex,
                            onClick = { onFlightClick(flight) },
                            onEditClick = { onEditFlightClick(flight) },
                            onDeleteClick = { onDeleteFlightClick(flight) },
                            onMoveUp = { onMoveFlightUp(flight) },
                            onMoveDown = { onMoveFlightDown(flight) },
                        )
                    }
                }
            }

            // Group footer actions
            if (!isGroupReorderMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (group.flights.size > 1) {
                        TextButton(onClick = onToggleFlightReorderMode) {
                            Text(
                                text = if (isFlightReorderMode) "Fet" else "Reordena vols",
                                style = MaterialTheme.typography.labelMedium,
                                color = AtlasPrimary,
                            )
                        }
                    } else {
                        // Spacer to keep the add button on the right
                        TextButton(onClick = {}, enabled = false) { Text("") }
                    }
                    TextButton(onClick = onAddFlightClick) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = AtlasPrimary)
                        Text(text = "Afegeix vol", style = MaterialTheme.typography.labelMedium, color = AtlasPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupFlightRow(
    flight: Flight,
    isReorderMode: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = AtlasSurfaceSubtle,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isReorderMode, onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Flight,
                contentDescription = null,
                tint = flight.status.tripStatusColors().foreground,
                modifier = Modifier.size(16.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${flight.originAirportId.uppercase()} → ${flight.destinationAirportId.uppercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = AtlasOnSurfaceStrong,
                )
                val meta = buildList {
                    flight.scheduledDepartureAt?.let { itineraryDateFormatter.formatIsoDate(it)?.let(::add) }
                    flight.flightNumber?.let { add(it) }
                }.joinToString(" · ")
                if (meta.isNotBlank()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.labelSmall,
                        color = AtlasOnSurfaceMuted,
                    )
                }
            }
            if (isReorderMode) {
                IconButton(onClick = onMoveUp, enabled = !isFirst, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Puja vol", tint = if (!isFirst) AtlasPrimary else AtlasOnSurfaceMuted, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onMoveDown, enabled = !isLast, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Baixa vol", tint = if (!isLast) AtlasPrimary else AtlasOnSurfaceMuted, modifier = Modifier.size(16.dp))
                }
            } else {
                IconButton(onClick = onEditClick, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edita", tint = AtlasOnSurfaceMuted, modifier = Modifier.size(14.dp))
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Elimina", tint = AtlasOnSurfaceMuted, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun GroupEditorDialog(
    draft: GroupEditorDraft,
    onDismiss: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onStatusChanged: (TravelStatus?) -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (draft.groupId == null) "Nou grup" else "Edita grup") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = draft.title,
                    onValueChange = onTitleChanged,
                    label = { Text("Títol del grup (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Estat (opcional)",
                    style = MaterialTheme.typography.labelMedium,
                    color = AtlasOnSurfaceMuted,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = draft.status == null,
                        onClick = { onStatusChanged(null) },
                        label = { Text("Cap", style = MaterialTheme.typography.labelMedium) },
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = draft.status == null,
                            borderColor = AtlasOutline,
                            selectedBorderColor = AtlasAccentContainer,
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = AtlasSurface,
                            selectedContainerColor = AtlasAccentContainer,
                            labelColor = AtlasOnSurfaceMuted,
                            selectedLabelColor = AtlasPrimary,
                        ),
                    )
                    TravelStatus.entries.forEach { status ->
                        FilterChip(
                            selected = draft.status == status,
                            onClick = { onStatusChanged(status) },
                            label = { Text(status.toCatalanLabel(), style = MaterialTheme.typography.labelMedium) },
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = draft.status == status,
                                borderColor = AtlasOutline,
                                selectedBorderColor = AtlasAccentContainer,
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = AtlasSurface,
                                selectedContainerColor = AtlasAccentContainer,
                                labelColor = AtlasOnSurfaceMuted,
                                selectedLabelColor = AtlasPrimary,
                            ),
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onSave) { Text("Desa") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel·la") } },
    )
}

private val itineraryDateFormatter = FlexibleDateFormatter()
