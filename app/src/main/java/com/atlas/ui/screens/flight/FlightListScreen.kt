package com.atlas.ui.screens.flight

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.Airport
import com.atlas.domain.model.Flight
import com.atlas.domain.model.Itinerary
import com.atlas.domain.model.TravelStatus
import com.atlas.presentation.flight.FlightEditorDraftUiState
import com.atlas.presentation.flight.FlightListItemUiState
import com.atlas.presentation.flight.FlightListUiState
import com.atlas.presentation.flight.ItinerarySummaryUiState
import com.atlas.ui.components.AtlasPage
import com.atlas.ui.components.AtlasPill
import com.atlas.ui.components.AtlasSemanticColors
import com.atlas.ui.components.tripStatusColors
import com.atlas.ui.screens.itinerary.ItineraryEditorDialog
import com.atlas.ui.screens.trip.toCatalanLabel
import com.atlas.ui.theme.AtlasAccentContainer
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface

@Composable
fun FlightListScreen(
    uiState: FlightListUiState,
    onFlightClick: (String) -> Unit,
    onItineraryClick: (String) -> Unit,
    onCreateFlightClick: () -> Unit,
    onCreateItineraryClick: () -> Unit,
    onEditFlightClick: (Flight) -> Unit,
    onDeleteFlightClick: (Flight) -> Unit,
    onEditItineraryClick: (Itinerary) -> Unit,
    onDeleteItineraryClick: (Itinerary) -> Unit,
    onDismissDraft: () -> Unit,
    onDismissItineraryDraft: () -> Unit,
    onOriginQueryChanged: (String) -> Unit,
    onOriginSelected: (Airport) -> Unit,
    onDestinationQueryChanged: (String) -> Unit,
    onDestinationSelected: (Airport) -> Unit,
    onStatusChanged: (TravelStatus) -> Unit,
    onScheduledDepartureAtChanged: (String) -> Unit,
    onScheduledArrivalAtChanged: (String) -> Unit,
    onActualDepartureAtChanged: (String) -> Unit,
    onActualArrivalAtChanged: (String) -> Unit,
    onAirlineChanged: (String) -> Unit,
    onFlightNumberChanged: (String) -> Unit,
    onAircraftChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onApiFlightNumberChanged: (String) -> Unit,
    onApiSearchDateChanged: (String) -> Unit,
    onSearchByFlightNumber: () -> Unit,
    onApplyApiResult: () -> Unit,
    onSaveDraft: () -> Unit,
    onItineraryTitleChanged: (String) -> Unit,
    onItineraryNotesChanged: (String) -> Unit,
    onSaveItineraryDraft: () -> Unit,
) {
    var selectedStatus by remember { mutableStateOf<TravelStatus?>(null) }
    val filteredFlights = uiState.flightItems.filter { item ->
        selectedStatus == null || item.flight.status == selectedStatus
    }
    val visibleRecordCount = filteredFlights.size + uiState.itineraryItems.size
    var flightToDelete by remember { mutableStateOf<Flight?>(null) }
    var itineraryToDelete by remember { mutableStateOf<Itinerary?>(null) }

    AtlasPage(contentPadding = PaddingValues(0.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            FlightListHeader(
                recordCount = visibleRecordCount,
                selectedStatus = selectedStatus,
                onSelectedStatusChanged = { selectedStatus = it },
                onCreateFlightClick = onCreateFlightClick,
                onCreateItineraryClick = onCreateItineraryClick,
            )

            if (visibleRecordCount == 0) {
                EmptyFlightList(
                    onCreateFlightClick = onCreateFlightClick,
                    onCreateItineraryClick = onCreateItineraryClick,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.itineraryItems, key = { "itinerary-${it.itinerary.id}" }) { item ->
                        ItinerarySummaryCard(
                            item = item,
                            onClick = { onItineraryClick(item.itinerary.id) },
                            onEditClick = { onEditItineraryClick(item.itinerary) },
                            onDeleteClick = { itineraryToDelete = item.itinerary },
                        )
                    }
                    items(filteredFlights, key = { it.flight.id }) { item ->
                        FlightCard(
                            item = item,
                            onClick = { onFlightClick(item.flight.id) },
                            onEditClick = { onEditFlightClick(item.flight) },
                            onDeleteClick = { flightToDelete = item.flight },
                        )
                    }
                }
            }
        }
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
            onAirlineChanged = onAirlineChanged,
            onFlightNumberChanged = onFlightNumberChanged,
            onAircraftChanged = onAircraftChanged,
            onNotesChanged = onNotesChanged,
            apiSearchCallbacks = FlightApiSearchCallbacks(
                onApiFlightNumberChanged = onApiFlightNumberChanged,
                onApiSearchDateChanged = onApiSearchDateChanged,
                onSearchByFlightNumber = onSearchByFlightNumber,
                onApplyApiResult = onApplyApiResult,
            ),
            onSave = onSaveDraft,
        )
    }

    if (uiState.itineraryDraft.isOpen) {
        ItineraryEditorDialog(
            draft = uiState.itineraryDraft,
            onDismiss = onDismissItineraryDraft,
            onTitleChanged = onItineraryTitleChanged,
            onNotesChanged = onItineraryNotesChanged,
            onSave = onSaveItineraryDraft,
        )
    }

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

    itineraryToDelete?.let { itinerary ->
        AlertDialog(
            onDismissRequest = { itineraryToDelete = null },
            title = { Text("Elimina itinerari") },
            text = { Text("Vols eliminar aquest itinerari? Els grups i els vols associats quedaran sense assignar. Aquesta acció no es pot desfer.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteItineraryClick(itinerary)
                    itineraryToDelete = null
                }) { Text("Elimina") }
            },
            dismissButton = {
                TextButton(onClick = { itineraryToDelete = null }) { Text("Cancel·la") }
            },
        )
    }
}

@Composable
private fun FlightListHeader(
    recordCount: Int,
    selectedStatus: TravelStatus?,
    onSelectedStatusChanged: (TravelStatus?) -> Unit,
    onCreateFlightClick: () -> Unit,
    onCreateItineraryClick: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Vols",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AtlasOnSurfaceStrong,
                )
                Text(
                    text = "$recordCount ${if (recordCount == 1) "registre" else "registres"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = AtlasOnSurfaceMuted,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onCreateFlightClick,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AtlasAccentContainer,
                        contentColor = AtlasPrimary,
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(text = "Vol", modifier = Modifier.padding(start = 4.dp))
                }
                Button(
                    onClick = onCreateItineraryClick,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AtlasAccentContainer,
                        contentColor = AtlasPrimary,
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.FlightTakeoff,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(text = "Itinerari", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedStatus == null,
                    onClick = { onSelectedStatusChanged(null) },
                    label = { Text("Tots", style = MaterialTheme.typography.labelMedium) },
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedStatus == null,
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
            items(TravelStatus.entries) { status ->
                FilterChip(
                    selected = selectedStatus == status,
                    onClick = { onSelectedStatusChanged(status) },
                    label = { Text(status.toCatalanLabel(), style = MaterialTheme.typography.labelMedium) },
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedStatus == status,
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
}

@Composable
private fun ItinerarySummaryCard(
    item: ItinerarySummaryUiState,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.FlightTakeoff,
                    contentDescription = null,
                    tint = AtlasPrimary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = item.itinerary.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceStrong,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                AtlasPill(
                    label = "Itinerari",
                    colors = AtlasSemanticColors(
                        foreground = AtlasPrimary,
                        container = AtlasAccentContainer,
                        label = "Itinerari",
                    ),
                )
            }

            Text(
                text = "${item.groupCount} ${if (item.groupCount == 1) "grup" else "grups"} · ${item.flightCount} ${if (item.flightCount == 1) "vol" else "vols"}",
                style = MaterialTheme.typography.bodySmall,
                color = AtlasOnSurfaceMuted,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (item.routeSummary.isNotBlank()) {
                Text(
                    text = item.routeSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = AtlasOnSurfaceMuted,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!item.itinerary.notes.isNullOrBlank()) {
                Text(
                    text = item.itinerary.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = AtlasOnSurfaceMuted,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edita",
                        tint = AtlasOnSurfaceMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Elimina",
                        tint = AtlasOnSurfaceMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FlightCard(
    item: FlightListItemUiState,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val flight = item.flight
    val colors = flight.status.tripStatusColors()

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Flight,
                    contentDescription = null,
                    tint = colors.foreground,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "${item.originLabel} → ${item.destinationLabel}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceStrong,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                AtlasPill(label = colors.label, colors = colors)
            }

            val meta = buildList {
                flight.scheduledDepartureAt?.let { add(it.take(10)) }
                (item.resolvedAirlineName ?: flight.airline)?.let { add(it) }
                flight.flightNumber?.let { add(it) }
            }.joinToString(" · ")

            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = AtlasOnSurfaceMuted,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edita",
                        tint = AtlasOnSurfaceMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Elimina",
                        tint = AtlasOnSurfaceMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyFlightList(
    onCreateFlightClick: () -> Unit,
    onCreateItineraryClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Encara no hi ha cap vol.",
            style = MaterialTheme.typography.titleMedium,
            color = AtlasOnSurfaceStrong,
        )
        Text(
            text = "Registra vols manuals per portar un historial dels teus trajectes.",
            style = MaterialTheme.typography.bodyMedium,
            color = AtlasOnSurfaceMuted,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onCreateFlightClick, shape = RoundedCornerShape(999.dp)) {
                Text(text = "Nou vol")
            }
            Button(onClick = onCreateItineraryClick, shape = RoundedCornerShape(999.dp)) {
                Text(text = "Nou itinerari")
            }
        }
    }
}

