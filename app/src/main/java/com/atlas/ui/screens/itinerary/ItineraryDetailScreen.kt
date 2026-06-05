package com.atlas.ui.screens.itinerary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.domain.model.Airport
import com.atlas.domain.model.Flight
import com.atlas.domain.model.ItineraryGroup
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.domain.util.utcAwareLayoverDurationMinutesTo
import com.atlas.presentation.flight.FlightEditorDraftUiState
import com.atlas.presentation.flight.FlightListItemUiState
import com.atlas.presentation.itinerary.ItineraryDetailUiState
import com.atlas.ui.components.AtlasPill
import com.atlas.ui.components.FlightCard
import com.atlas.ui.components.tripStatusColors
import com.atlas.ui.screens.flight.FlightEditorDialog
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
import kotlin.math.roundToInt

@Composable
fun ItineraryDetailScreen(
    uiState: ItineraryDetailUiState,
    onBackClick: () -> Unit,
    onTripClick: (String) -> Unit,
    onUnlinkTripClick: () -> Unit,
    onAssignTripClick: () -> Unit,
    onDismissTripPicker: () -> Unit,
    onLinkTrip: (Trip) -> Unit,
    onDeleteItineraryClick: () -> Unit,
    onAddGroupClick: () -> Unit,
    onDeleteGroupClick: (ItineraryGroup) -> Unit,
    onToggleGroupReorderMode: () -> Unit,
    onMoveGroupUp: (ItineraryGroup) -> Unit,
    onMoveGroupDown: (ItineraryGroup) -> Unit,
    onAddFlightToGroupClick: (String) -> Unit,
    onAddExistingFlightToGroupClick: (String) -> Unit,
    onDismissSoloFlightPicker: () -> Unit,
    onAssignSoloFlight: (Flight) -> Unit,
    onRemoveFlightFromItineraryClick: (Flight) -> Unit,
    onDismissRemoveFlightConfirm: () -> Unit,
    onConfirmRemoveFlightFromItinerary: () -> Unit,
    onMoveFlightToGroupClick: (Flight) -> Unit,
    onDismissMoveFlightPicker: () -> Unit,
    onMoveFlightToGroup: (Flight, ItineraryGroup) -> Unit,
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
    onApiFlightNumberChanged: (String) -> Unit,
    onApiSearchDateChanged: (String) -> Unit,
    onSearchByFlightNumber: () -> Unit,
    onApplyApiResult: () -> Unit,
    onManualEntryClick: () -> Unit,
    onBackToSearch: () -> Unit,
    onSaveFlightDraft: () -> Unit,
) {
    var showDeleteItineraryDialog by remember { mutableStateOf(false) }
    var groupToDelete by remember { mutableStateOf<ItineraryGroup?>(null) }
    var flightToDelete by remember { mutableStateOf<Flight?>(null) }
    val routeCodeLabel = remember(uiState.groups, uiState.airports) {
        buildItineraryCodeLabel(uiState.groups, uiState.airports)
    }
    val routeCityLabel = remember(uiState.groups, uiState.airports) {
        buildItineraryRouteLabel(uiState.groups, uiState.airports)
    }

    Column(
        modifier = Modifier.background(AtlasBackground),
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                ItineraryDetailHeader(
                    onBackClick = onBackClick,
                    onDeleteClick = { showDeleteItineraryDialog = true },
                )
            }

            item {
                ItineraryRouteHero(codeLabel = routeCodeLabel, cityLabel = routeCityLabel)
            }

            item {
                ItineraryStatsStrip(groups = uiState.groups)
            }

            item {
                ItineraryGroupsHeader(
                    groupCount = uiState.groups.size,
                    isGroupReorderMode = uiState.isGroupReorderMode,
                    onToggleGroupReorderMode = onToggleGroupReorderMode,
                    onAddGroupClick = onAddGroupClick,
                )
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
                    airports = uiState.airports,
                    isGroupReorderMode = uiState.isGroupReorderMode,
                    isFlightReorderMode = uiState.reorderingFlightsGroupId == group.id,
                    isFirstGroup = uiState.groups.first().id == group.id,
                    isLastGroup = uiState.groups.last().id == group.id,
                    onDeleteGroupClick = { groupToDelete = group },
                    onMoveGroupUp = { onMoveGroupUp(group) },
                    onMoveGroupDown = { onMoveGroupDown(group) },
                    onAddFlightClick = { onAddFlightToGroupClick(group.id) },
                    onAddExistingFlightClick = { onAddExistingFlightToGroupClick(group.id) },
                    hasSoloFlights = uiState.soloFlights.isNotEmpty(),
                    onRemoveFlightFromItineraryClick = onRemoveFlightFromItineraryClick,
                    onMoveFlightToGroupClick = onMoveFlightToGroupClick,
                    hasOtherGroups = uiState.groups.size > 1,
                    onFlightClick = { onFlightClick(it.id) },
                    onToggleFlightReorderMode = { onToggleFlightReorderMode(group.id) },
                    onMoveFlightUp = { flight -> onMoveFlightUp(group.id, flight) },
                    onMoveFlightDown = { flight -> onMoveFlightDown(group.id, flight) },
                )
            }

            item {
                LinkedTripPanel(
                    trip = uiState.linkedTrip,
                    onTripClick = onTripClick,
                    onUnlinkTripClick = onUnlinkTripClick,
                    onAssignTripClick = onAssignTripClick,
                )
            }
        }
    }

    // Solo flight picker dialog
    if (uiState.showSoloFlightPicker) {
        SoloFlightPickerDialog(
            soloFlights = uiState.soloFlights,
            onDismiss = onDismissSoloFlightPicker,
            onFlightSelected = onAssignSoloFlight,
        )
    }

    // Remove flight confirmation dialog
    uiState.flightPendingRemoval?.let { flight ->
        AlertDialog(
            onDismissRequest = onDismissRemoveFlightConfirm,
            title = { Text("Treu del itinerari") },
            text = { Text("Vols treure el vol ${flight.originAirportId.uppercase()} → ${flight.destinationAirportId.uppercase()} d'aquest itinerari? El vol quedarà com a vol independent.") },
            confirmButton = {
                TextButton(onClick = onConfirmRemoveFlightFromItinerary) { Text("Treu") }
            },
            dismissButton = {
                TextButton(onClick = onDismissRemoveFlightConfirm) { Text("Cancel·la") }
            },
        )
    }

    // Move flight to group picker dialog
    uiState.flightPendingMove?.let { flight ->
        MoveFlightToGroupDialog(
            flight = flight,
            groups = uiState.groups,
            airports = uiState.airports,
            onDismiss = onDismissMoveFlightPicker,
            onGroupSelected = { targetGroup -> onMoveFlightToGroup(flight, targetGroup) },
        )
    }

    // Trip picker dialog
    if (uiState.showTripPicker) {
        TripPickerDialog(
            trips = uiState.availableTrips,
            onDismiss = onDismissTripPicker,
            onTripSelected = onLinkTrip,
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
            onApiFlightNumberChanged = onApiFlightNumberChanged,
            onApiSearchDateChanged = onApiSearchDateChanged,
            onSearchByFlightNumber = onSearchByFlightNumber,
            onApplyApiResult = onApplyApiResult,
            onManualEntryClick = onManualEntryClick,
            onBackToSearch = onBackToSearch,
            onSave = onSaveFlightDraft,
        )
    }

    // Delete itinerary confirmation
    if (showDeleteItineraryDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteItineraryDialog = false },
            title = { Text("Elimina itinerari") },
            text = { Text("Vols eliminar aquest itinerari? Els vols associats quedaran sense itinerari assignat. Aquesta acció no es pot desfer.") },
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
            text = { Text("Vols eliminar aquest grup? Els vols del grup quedaran sense grup assignat. Aquesta acció no es pot desfer.") },
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
private fun ItineraryDetailHeader(
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var actionsExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.size(42.dp),
            shape = CircleShape,
            color = AtlasSurface,
            border = BorderStroke(1.dp, AtlasOutline),
            onClick = onBackClick,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Enrere",
                    tint = AtlasOnSurfaceStrong,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
        Box(modifier = Modifier.weight(1f))
        Box {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = AtlasSurface,
                border = BorderStroke(1.dp, AtlasOutline),
            ) {
                IconButton(onClick = { actionsExpanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Accions", tint = AtlasOnSurfaceStrong)
                }
            }
            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme.copy(surfaceContainer = AtlasSurface),
                shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(14.dp)),
            ) {
                DropdownMenu(
                    expanded = actionsExpanded,
                    onDismissRequest = { actionsExpanded = false },
                    modifier = Modifier.width(180.dp),
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Elimina",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = AtlasPrimary,
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Delete, contentDescription = null, tint = AtlasPrimary, modifier = Modifier.size(16.dp))
                        },
                        onClick = { actionsExpanded = false; onDeleteClick() },
                    )
                }
            }
        }
    }
}

@Composable
private fun ItineraryRouteHero(codeLabel: String, cityLabel: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Itinerari".uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AtlasOnSurfaceMuted,
            )
            RouteLabelRow(
                routeLabel = codeLabel,
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontSize = 27.sp),
                textWeight = FontWeight.Medium,
            )
            if (cityLabel.isNotBlank() && cityLabel != codeLabel) {
                Text(
                    text = cityLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun RouteLabelRow(
    routeLabel: String,
    textStyle: androidx.compose.ui.text.TextStyle,
    textWeight: FontWeight,
    modifier: Modifier = Modifier,
) {
    val stops = routeLabel.split(" → ").filter { it.isNotBlank() }
    if (stops.isEmpty()) {
        Text(
            text = "Sense ruta",
            modifier = modifier,
            style = textStyle,
            fontWeight = textWeight,
            color = AtlasOnSurfaceStrong,
        )
        return
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        stops.forEachIndexed { index, stop ->
            Text(
                text = stop,
                modifier = Modifier.weight(1f, fill = false),
                style = textStyle,
                fontWeight = textWeight,
                color = AtlasOnSurfaceStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (index < stops.lastIndex) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = AtlasPrimary,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
    }
}

@Composable
private fun LinkedTripPanel(
    trip: Trip?,
    onTripClick: (String) -> Unit,
    onUnlinkTripClick: () -> Unit,
    onAssignTripClick: () -> Unit,
) {
    if (trip == null) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onAssignTripClick),
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
                        text = "Viatge vinculat".uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AtlasOnSurfaceMuted,
                    )
                    Text(
                        text = "Sense viatge assignat",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = AtlasOnSurfaceMuted,
                    )
                }
                Text(
                    text = "Assigna",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = AtlasPrimary,
                )
            }
        }
        return
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTripClick(trip.id) },
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
                    text = "Viatge vinculat".uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceMuted,
                )
                Text(
                    text = trip.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
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
private fun ItineraryStatsStrip(groups: List<ItineraryGroup>) {
    val flightCount = groups.sumOf { it.flights.size }
    val distance = groups.flatMap { it.flights }.sumOf { it.distanceKm ?: 0.0 }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Row {
            ItineraryStatCell(
                value = groups.size.toString(),
                label = "Grups",
                modifier = Modifier.weight(1f),
            )
            ItineraryStatCell(
                value = flightCount.toString(),
                label = "Vols",
                modifier = Modifier.weight(1f),
            )
            ItineraryStatCell(
                value = if (distance > 0.0) formatItineraryDistance(distance) else "—",
                label = "Distància",
                modifier = Modifier.weight(1.2f),
            )
        }
    }
}

@Composable
private fun ItineraryStatCell(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
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

@Composable
private fun ItineraryGroupsHeader(
    groupCount: Int,
    isGroupReorderMode: Boolean,
    onToggleGroupReorderMode: () -> Unit,
    onAddGroupClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Grups",
            style = MaterialTheme.typography.titleLarge,
            color = AtlasOnSurfaceStrong,
        )
        Box(
            modifier = Modifier
                .height(1.dp)
                .weight(1f)
                .background(AtlasOutline),
        )
        if (groupCount > 1) {
            TextButton(onClick = onToggleGroupReorderMode, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) {
                Text(
                    text = if (isGroupReorderMode) "Fet" else "Reordena",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceMuted,
                )
            }
        }
        if (!isGroupReorderMode) {
            TextButton(onClick = onAddGroupClick, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) {
                Text(
                    text = "+ Grup",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = AtlasPrimary,
                )
            }
        }
    }
}

@Composable
private fun ItineraryGroupCard(
    group: ItineraryGroup,
    airports: List<Airport>,
    isGroupReorderMode: Boolean,
    isFlightReorderMode: Boolean,
    isFirstGroup: Boolean,
    isLastGroup: Boolean,
    onDeleteGroupClick: () -> Unit,
    onMoveGroupUp: () -> Unit,
    onMoveGroupDown: () -> Unit,
    onAddFlightClick: () -> Unit,
    onAddExistingFlightClick: () -> Unit,
    hasSoloFlights: Boolean,
    onRemoveFlightFromItineraryClick: (Flight) -> Unit,
    onMoveFlightToGroupClick: (Flight) -> Unit,
    hasOtherGroups: Boolean,
    onFlightClick: (Flight) -> Unit,
    onToggleFlightReorderMode: () -> Unit,
    onMoveFlightUp: (Flight) -> Unit,
    onMoveFlightDown: (Flight) -> Unit,
) {
    var actionsExpanded by remember { mutableStateOf(false) }
    val route = group.routeSummary(airports)
    val derivedStatus = group.derivedStatus()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = route.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AtlasOnSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                    ) {
                        RoutePairRow(
                            origin = route.origin,
                            destination = route.destination,
                            modifier = Modifier.weight(1f),
                        )
                        derivedStatus?.let { status ->
                            val colors = status.tripStatusColors()
                            AtlasPill(label = colors.label, colors = colors)
                        }
                    }
                }
                if (isGroupReorderMode) {
                    IconButton(onClick = onMoveGroupUp, enabled = !isFirstGroup, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Puja", tint = if (!isFirstGroup) AtlasPrimary else AtlasOnSurfaceMuted)
                    }
                    IconButton(onClick = onMoveGroupDown, enabled = !isLastGroup, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Baixa", tint = if (!isLastGroup) AtlasPrimary else AtlasOnSurfaceMuted)
                    }
                } else {
                    Box {
                        IconButton(onClick = { actionsExpanded = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Accions del grup", tint = AtlasOnSurfaceMuted, modifier = Modifier.size(18.dp))
                        }
                        MaterialTheme(
                            colorScheme = MaterialTheme.colorScheme.copy(surfaceContainer = AtlasSurface),
                            shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(14.dp)),
                        ) {
                            DropdownMenu(
                                expanded = actionsExpanded,
                                onDismissRequest = { actionsExpanded = false },
                                modifier = Modifier.width(180.dp),
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Elimina grup",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = AtlasPrimary,
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Delete, contentDescription = null, tint = AtlasPrimary, modifier = Modifier.size(16.dp))
                                    },
                                    onClick = { actionsExpanded = false; onDeleteGroupClick() },
                                )
                            }
                        }
                    }
                }
            }

            if (group.flights.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    group.flights.forEachIndexed { index, flight ->
                        GroupFlightCardItem(
                            flight = flight,
                            airports = airports,
                            nextFlight = group.flights.getOrNull(index + 1),
                            isReorderMode = isFlightReorderMode,
                            isFirst = index == 0,
                            isLast = index == group.flights.lastIndex,
                            onClick = { onFlightClick(flight) },
                            onMoveUp = { onMoveFlightUp(flight) },
                            onMoveDown = { onMoveFlightDown(flight) },
                            onRemoveFromItineraryClick = { onRemoveFlightFromItineraryClick(flight) },
                            onMoveToGroupClick = { onMoveFlightToGroupClick(flight) },
                            hasOtherGroups = hasOtherGroups,
                        )
                    }
                }
            }

            if (!isGroupReorderMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (group.flights.size > 1) {
                        TextButton(onClick = onToggleFlightReorderMode) {
                            Text(
                                text = if (isFlightReorderMode) "Fet" else "Reordena vols",
                                style = MaterialTheme.typography.labelMedium,
                                color = AtlasOnSurfaceMuted,
                            )
                        }
                    }
                    if (hasSoloFlights) {
                        TextButton(onClick = onAddExistingFlightClick) {
                            Text(
                                text = "+ Vol existent",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = AtlasOnSurfaceMuted,
                            )
                        }
                    }
                    TextButton(onClick = onAddFlightClick) {
                        Text(text = "+ Afegeix vol", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = AtlasPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupFlightCardItem(
    flight: Flight,
    airports: List<Airport>,
    nextFlight: Flight?,
    isReorderMode: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemoveFromItineraryClick: () -> Unit,
    onMoveToGroupClick: () -> Unit,
    hasOtherGroups: Boolean,
) {
    val layoverText = layoverLabel(flight, nextFlight, airports)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (isReorderMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row {
                    TextButton(
                        onClick = onRemoveFromItineraryClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    ) {
                        Text(
                            text = "Treu",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = AtlasPrimary,
                        )
                    }
                    if (hasOtherGroups) {
                        TextButton(
                            onClick = onMoveToGroupClick,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        ) {
                            Text(
                                text = "Mou →",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = AtlasOnSurfaceMuted,
                            )
                        }
                    }
                }
                Row {
                    IconButton(onClick = onMoveUp, enabled = !isFirst, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Puja vol", tint = if (!isFirst) AtlasPrimary else AtlasOnSurfaceMuted, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onMoveDown, enabled = !isLast, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Baixa vol", tint = if (!isLast) AtlasPrimary else AtlasOnSurfaceMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        FlightCard(
            item = flight.toFlightListItemUiState(airports),
            onClick = onClick,
        )
        if (layoverText != null) {
            Text(
                text = layoverText,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AtlasOnSurfaceMuted,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}


@Composable
private fun SoloFlightPickerDialog(
    soloFlights: List<Flight>,
    onDismiss: () -> Unit,
    onFlightSelected: (Flight) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Afegeix vol existent") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                soloFlights.forEach { flight ->
                    Surface(
                        onClick = { onFlightSelected(flight) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = AtlasBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AtlasOutline),
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Text(
                                text = "${flight.originAirportId.uppercase()} → ${flight.destinationAirportId.uppercase()}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = AtlasOnSurfaceStrong,
                            )
                            val meta = buildList {
                                flight.flightNumber?.takeIf { it.isNotBlank() }?.let { add(it) }
                                (flight.scheduledDepartureAt ?: flight.actualDepartureAt)
                                    ?.substringBefore('T')
                                    ?.let { add(it) }
                            }.joinToString(" · ")
                            if (meta.isNotBlank()) {
                                Text(
                                    text = meta,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AtlasOnSurfaceMuted,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel·la") } },
    )
}

@Composable
private fun MoveFlightToGroupDialog(
    flight: Flight,
    groups: List<ItineraryGroup>,
    airports: List<Airport>,
    onDismiss: () -> Unit,
    onGroupSelected: (ItineraryGroup) -> Unit,
) {
    val otherGroups = groups.filter { it.id != flight.itineraryGroupId }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mou a un altre grup") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                otherGroups.forEach { group ->
                    val route = group.routeSummary(airports)
                    Surface(
                        onClick = { onGroupSelected(group) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = AtlasBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AtlasOutline),
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Text(
                                text = "${route.origin} → ${route.destination}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = AtlasOnSurfaceStrong,
                            )
                            Text(
                                text = route.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = AtlasOnSurfaceMuted,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel·la") } },
    )
}

@Composable
private fun TripPickerDialog(
    trips: List<Trip>,
    onDismiss: () -> Unit,
    onTripSelected: (Trip) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assigna un viatge") },
        text = {
            if (trips.isEmpty()) {
                Text(
                    text = "No hi ha viatges disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AtlasOnSurfaceMuted,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    trips.forEach { trip ->
                        Surface(
                            onClick = { onTripSelected(trip) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = AtlasBackground,
                            border = BorderStroke(1.dp, AtlasOutline),
                        ) {
                            Text(
                                text = trip.title,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = AtlasOnSurfaceStrong,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel·la") } },
    )
}

private val itineraryDateFormatter = FlexibleDateFormatter()

private data class GroupRouteSummary(
    val label: String,
    val origin: String,
    val destination: String,
)

private fun ItineraryGroup.routeSummary(airports: List<Airport>): GroupRouteSummary {
    val orderedFlights = sortedFlights()
    val firstFlight = orderedFlights.firstOrNull()
    val lastFlight = orderedFlights.lastOrNull()
    val origin = firstFlight?.let { airports.findAirport(it.originAirportId) }
    val destination = lastFlight?.let { airports.findAirport(it.destinationAirportId) }
    val groupLabel = listOfNotNull(
        origin?.city,
        destination?.city,
    ).joinToString(" · ").ifBlank { "Grup ${sortOrder + 1}" }
    val originCity = origin?.city ?: firstFlight?.originAirportId?.uppercase()
    val destinationCity = destination?.city ?: lastFlight?.destinationAirportId?.uppercase()
    return GroupRouteSummary(
        label = groupLabel,
        origin = originCity ?: "Origen",
        destination = destinationCity ?: "Destí",
    )
}

private fun ItineraryGroup.derivedStatus(): TravelStatus? {
    val statuses = flights.map { it.status }
    if (statuses.isEmpty()) return null
    return when {
        TravelStatus.IN_PROGRESS in statuses -> TravelStatus.IN_PROGRESS
        TravelStatus.COMPLETED in statuses -> TravelStatus.COMPLETED
        TravelStatus.PLANNED in statuses -> TravelStatus.PLANNED
        else -> null
    }
}

@Composable
private fun RoutePairRow(
    origin: String,
    destination: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = origin,
            modifier = Modifier.weight(1f, fill = false),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            color = AtlasOnSurfaceStrong,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = AtlasPrimary,
            modifier = Modifier.size(17.dp),
        )
        Text(
            text = destination,
            modifier = Modifier.weight(1f, fill = false),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            color = AtlasOnSurfaceStrong,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun buildItineraryRouteLabel(groups: List<ItineraryGroup>, airports: List<Airport>): String {
    val orderedGroups = groups.sortedBy { it.sortOrder }
    val seenAirportIds = mutableSetOf<String>()
    val route = mutableListOf<String>()

    fun addAirportIfNew(airportId: String) {
        val normalizedId = airportId.uppercase()
        if (!seenAirportIds.add(normalizedId)) return
        val label = airports.findAirport(airportId)?.shortLabel() ?: normalizedId
        route += label
    }

    orderedGroups.forEach { group ->
        val flights = group.sortedFlights()
        val firstFlight = flights.firstOrNull() ?: return@forEach
        val lastFlight = flights.lastOrNull() ?: return@forEach
        addAirportIfNew(firstFlight.originAirportId)
        addAirportIfNew(lastFlight.destinationAirportId)
    }

    return route.ifEmpty { listOf("Sense ruta") }.joinToString(" → ")
}

private fun buildItineraryCodeLabel(groups: List<ItineraryGroup>, airports: List<Airport>): String {
    val orderedGroups = groups.sortedBy { it.sortOrder }
    val seenAirportIds = mutableSetOf<String>()
    val route = mutableListOf<String>()

    fun addAirportIfNew(airportId: String) {
        val normalizedId = airportId.uppercase()
        if (!seenAirportIds.add(normalizedId)) return
        val label = airports.findAirport(airportId)?.displayCode() ?: normalizedId
        route += label
    }

    orderedGroups.forEach { group ->
        val flights = group.sortedFlights()
        val firstFlight = flights.firstOrNull() ?: return@forEach
        val lastFlight = flights.lastOrNull() ?: return@forEach
        addAirportIfNew(firstFlight.originAirportId)
        addAirportIfNew(lastFlight.destinationAirportId)
    }

    return route.ifEmpty { listOf("Sense ruta") }.joinToString(" → ")
}

private fun ItineraryGroup.sortedFlights(): List<Flight> =
    flights.sortedBy { it.sortOrder ?: Int.MAX_VALUE }

private fun List<Airport>.findAirport(value: String): Airport? {
    val normalized = value.uppercase()
    return firstOrNull {
        it.id.equals(value, ignoreCase = true) ||
            it.iata?.equals(normalized, ignoreCase = true) == true ||
            it.icao?.equals(normalized, ignoreCase = true) == true
    }
}

private fun Airport.displayCode(): String =
    iata ?: icao ?: id.uppercase()

private fun Airport.shortLabel(): String =
    city?.takeIf { it.isNotBlank() } ?: iata ?: icao ?: id.uppercase()

private fun Flight.durationMinutesTo(next: Flight): Long? =
    utcAwareLayoverDurationMinutesTo(next)

private fun Long.toDurationLabel(): String {
    val absolute = kotlin.math.abs(this)
    val hours = absolute / 60
    val minutes = absolute % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours} h ${minutes} m"
        hours > 0 -> "${hours} h"
        else -> "${minutes} m"
    }
}

private fun layoverLabel(flight: Flight, nextFlight: Flight?, airports: List<Airport>): String? {
    if (nextFlight == null) return null
    val layoverAirport = airports.findAirport(flight.destinationAirportId)
    val city = layoverAirport?.city ?: flight.destinationAirportId.uppercase()
    val duration = flight.durationMinutesTo(nextFlight)?.takeIf { it >= 0 }?.toDurationLabel()
    return if (duration != null) "Escala · $city · $duration" else "Escala · $city"
}

private fun formatItineraryDistance(value: Double): String =
    "%,d km".format(value.roundToInt()).replace(",", ".")

private fun Flight.toFlightListItemUiState(airports: List<Airport>): FlightListItemUiState {
    val origin = airports.findAirport(originAirportId)
    val destination = airports.findAirport(destinationAirportId)
    return FlightListItemUiState(
        flight = this,
        originLabel = origin?.displayCode() ?: originAirportId.uppercase(),
        destinationLabel = destination?.displayCode() ?: destinationAirportId.uppercase(),
        originCity = origin?.city,
        destinationCity = destination?.city,
    )
}
