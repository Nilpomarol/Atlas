package com.atlas.ui.screens.flight

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.domain.model.Airport
import com.atlas.domain.model.Flight
import com.atlas.domain.util.dayOffsetBetween
import com.atlas.domain.model.Itinerary
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.presentation.flight.FlightListItemUiState
import com.atlas.presentation.flight.FlightListUiState
import com.atlas.presentation.flight.ItineraryGroupRouteUiState
import com.atlas.presentation.flight.ItinerarySummaryUiState
import com.atlas.ui.components.AirlineLogo
import com.atlas.ui.components.AtlasFilterPill
import com.atlas.ui.components.FlightCard
import com.atlas.ui.components.AtlasPage
import com.atlas.ui.components.AtlasPill
import com.atlas.ui.components.AtlasSemanticColors
import com.atlas.ui.components.tripStatusColors
import com.atlas.ui.screens.trip.toCatalanLabel
import com.atlas.ui.theme.AtlasAccentContainer
import com.atlas.ui.theme.AtlasDelay
import com.atlas.ui.theme.AtlasError
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasSurfaceRaised
import com.atlas.ui.theme.AtlasVisited

@Composable
fun FlightListScreen(
    uiState: FlightListUiState,
    onFlightClick: (String) -> Unit,
    onItineraryClick: (String) -> Unit,
    onCreateFlightClick: () -> Unit,
    onCreateItineraryClick: () -> Unit,
    onEditFlightClick: (Flight) -> Unit,
    onDeleteFlightClick: (Flight) -> Unit,
    onDismissDraft: () -> Unit,
    onOriginQueryChanged: (String) -> Unit,
    onOriginSelected: (Airport) -> Unit,
    onDestinationQueryChanged: (String) -> Unit,
    onDestinationSelected: (Airport) -> Unit,
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
    onApiFlightNumberChanged: (String) -> Unit,
    onApiSearchDateChanged: (String) -> Unit,
    onSearchByFlightNumber: () -> Unit,
    onApplyApiResult: () -> Unit,
    onManualEntryClick: () -> Unit,
    onBackToSearch: () -> Unit,
    onSaveDraft: () -> Unit,
) {
    var selectedStatus by remember { mutableStateOf<TravelStatus?>(null) }
    val filteredFlights = uiState.flightItems.filter { selectedStatus == null || it.flight.status == selectedStatus }
    val filteredItineraries = uiState.itineraryItems.filter { selectedStatus == null || it.status == selectedStatus }
    val records = buildList {
        addAll(filteredItineraries.map(FlightListRecord::ItineraryRecord))
        addAll(filteredFlights.map(FlightListRecord::FlightRecord))
    }.sortedWith(compareByDescending<FlightListRecord> { it.sortKey ?: "" }.thenBy { it.title })

    AtlasPage(contentPadding = PaddingValues(0.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            FlightListHeader(
                recordCount = records.size,
                selectedStatus = selectedStatus,
                onSelectedStatusChanged = { selectedStatus = it },
                onCreateFlightClick = onCreateFlightClick,
                onCreateItineraryClick = onCreateItineraryClick,
            )

            if (records.isEmpty()) {
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
                    items(records, key = FlightListRecord::key) { record ->
                        when (record) {
                            is FlightListRecord.FlightRecord -> FlightCard(
                                item = record.item,
                                onClick = { onFlightClick(record.item.flight.id) },
                            )
                            is FlightListRecord.ItineraryRecord -> ItinerarySummaryCard(
                                item = record.item,
                                onClick = { onItineraryClick(record.item.itinerary.id) },
                            )
                        }
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
            airlineResults = uiState.airlineSearchResults,
            onAirlineQueryChanged = onAirlineQueryChanged,
            onAirlineSelected = onAirlineSelected,
            onFlightNumberChanged = onFlightNumberChanged,
            onAircraftChanged = onAircraftChanged,
            onAircraftRegistrationChanged = onAircraftRegistrationChanged,
            onNotesChanged = onNotesChanged,
            onApiFlightNumberChanged = onApiFlightNumberChanged,
            onApiSearchDateChanged = onApiSearchDateChanged,
            onSearchByFlightNumber = onSearchByFlightNumber,
            onApplyApiResult = onApplyApiResult,
            onManualEntryClick = onManualEntryClick,
            onBackToSearch = onBackToSearch,
            onSave = onSaveDraft,
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
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AtlasNavy, contentColor = AtlasSurface),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(17.dp))
                    Text(text = "Vol", modifier = Modifier.padding(start = 4.dp))
                }
                Button(
                    onClick = onCreateItineraryClick,
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AtlasAccentContainer, contentColor = AtlasPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Filled.FlightTakeoff, contentDescription = null, modifier = Modifier.size(17.dp))
                    Text(text = "Itinerari", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AtlasFilterPill(
                label = "Tots",
                selected = selectedStatus == null,
                onClick = { onSelectedStatusChanged(null) },
                modifier = Modifier.weight(1f),
            )
            listOf(TravelStatus.PLANNED, TravelStatus.IN_PROGRESS, TravelStatus.COMPLETED).forEach { status ->
                val colors = status.tripStatusColors()
                AtlasFilterPill(
                    label = status.toCatalanLabel(),
                    selected = selectedStatus == status,
                    onClick = { onSelectedStatusChanged(status) },
                    modifier = Modifier.weight(1f),
                    selectedContainerColor = colors.container,
                    selectedContentColor = colors.foreground,
                )
            }
        }
    }
}

@Composable
private fun ItinerarySummaryCard(
    item: ItinerarySummaryUiState,
    onClick: () -> Unit,
) {
    val statusColors = item.status.tripStatusColors()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.SyncAlt,
                    contentDescription = null,
                    tint = AtlasNavy,
                    modifier = Modifier.size(19.dp),
                )
                Text(
                    text = "ITINERARI",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.routeLabel,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                AtlasPill(
                    label = statusColors.label,
                    colors = statusColors,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                )
            }

            Column(
                modifier = Modifier.padding(top = 17.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (item.groupRoutes.isEmpty()) {
                    Text(
                        text = "Sense ruta",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AtlasOnSurfaceMuted,
                    )
                } else {
                    item.groupRoutes.forEachIndexed { index, route ->
                        if (index > 0) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AtlasOutline))
                        }
                        ItineraryGroupRoute(route = route)
                    }
                }
            }

            item.linkedTripTitle?.let { tripTitle ->
                Box(modifier = Modifier.fillMaxWidth().padding(top = 14.dp).height(1.dp).background(AtlasOutline))
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = null,
                        tint = AtlasOnSurfaceMuted,
                        modifier = Modifier.size(19.dp),
                    )
                    Text(
                        text = "Vinculat a",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AtlasOnSurfaceMuted,
                    )
                    Text(
                        text = tripTitle,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = AtlasOnSurfaceStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun ItineraryGroupRoute(
    route: ItineraryGroupRouteUiState,
) {
    val arrivalDayOffset = dayOffsetBetween(
        departureDatetime = route.actualDepartureAt ?: route.scheduledDepartureAt,
        arrivalDatetime = route.actualArrivalAt ?: route.scheduledArrivalAt,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        ItineraryEndpoint(
            airportCode = route.originCode,
            city = route.originCity,
            scheduledTime = route.scheduledDepartureAt,
            actualTime = route.actualDepartureAt,
            delayMinutes = route.departureDelayMinutes,
            modifier = Modifier.weight(1f),
        )
        ItineraryLayoverMiddle(
            groupDurationMinutes = route.groupDurationMinutes,
            layoverCities = route.layoverCities,
            modifier = Modifier.weight(1.15f),
        )
        ItineraryEndpoint(
            airportCode = route.destinationCode,
            city = route.destinationCity,
            scheduledTime = route.scheduledArrivalAt,
            actualTime = route.actualArrivalAt,
            delayMinutes = route.arrivalDelayMinutes,
            dayOffset = arrivalDayOffset,
            modifier = Modifier.weight(1f),
            alignEnd = true,
        )
    }
}

@Composable
private fun ItineraryEndpoint(
    airportCode: String,
    city: String?,
    scheduledTime: String?,
    actualTime: String?,
    delayMinutes: Long?,
    dayOffset: Int? = null,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false,
) {
    val displayTime = actualTime ?: scheduledTime
    val hasActual = actualTime != null
    val actualColor = when {
        delayMinutes == null || delayMinutes <= 0 -> AtlasVisited
        delayMinutes < 45 -> AtlasDelay
        else -> AtlasError
    }
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
    ) {
        Text(
            text = airportCode,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 31.sp),
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
        displayTime?.timePart()?.let { time ->
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = if (hasActual) actualColor else AtlasOnSurfaceStrong,
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
        if (hasActual && scheduledTime != null) {
            Text(
                text = scheduledTime.timePart() ?: scheduledTime,
                style = MaterialTheme.typography.labelSmall.copy(
                    textDecoration = TextDecoration.LineThrough,
                ),
                color = AtlasOnSurfaceMuted,
            )
        } else {
            displayTime?.let { value ->
                flightListDateFormatter.formatIsoDate(value)?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = AtlasOnSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun ItineraryLayoverMiddle(
    groupDurationMinutes: Long?,
    layoverCities: List<String>,
    modifier: Modifier = Modifier,
) {
    val layover = layoverCities.joinToString(", ").ifBlank { "Directe" }
    Column(
        modifier = modifier.padding(top = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = groupDurationMinutes?.toLayoverDurationText() ?: "",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = AtlasOnSurfaceStrong,
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
                    .background(AtlasPrimary.copy(alpha = 0.5f)),
            )
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(AtlasSurface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = AtlasPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Text(
            text = layover,
            modifier = Modifier.padding(top = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = AtlasOnSurfaceMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AtlasSurfaceRaised),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Flight,
                contentDescription = null,
                tint = AtlasPrimary,
                modifier = Modifier.size(25.dp),
            )
        }
        Text(
            text = "Encara no hi ha cap vol.",
            style = MaterialTheme.typography.titleMedium,
            color = AtlasOnSurfaceStrong,
        )
        Text(
            text = "Registra vols manuals o crea itineraris per portar l'historial dels trajectes.",
            style = MaterialTheme.typography.bodyMedium,
            color = AtlasOnSurfaceMuted,
            textAlign = TextAlign.Center,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onCreateFlightClick, shape = RoundedCornerShape(13.dp)) {
                Text(text = "Nou vol")
            }
            Button(onClick = onCreateItineraryClick, shape = RoundedCornerShape(13.dp)) {
                Text(text = "Nou itinerari")
            }
        }
    }
}

private sealed class FlightListRecord {
    abstract val key: String
    abstract val title: String
    abstract val sortKey: String?

    data class FlightRecord(val item: FlightListItemUiState) : FlightListRecord() {
        override val key: String = "flight-${item.flight.id}"
        override val title: String = item.originLabel
        override val sortKey: String? = item.sortKey
    }

    data class ItineraryRecord(val item: ItinerarySummaryUiState) : FlightListRecord() {
        override val key: String = "itinerary-${item.itinerary.id}"
        override val title: String = item.itinerary.title
        override val sortKey: String? = item.sortKey
    }
}

private fun String.timePart(): String? =
    substringAfter('T', missingDelimiterValue = "")
        .take(5)
        .takeIf { it.length == 5 }

private fun Long.toLayoverDurationText(): String {
    val hours = this / 60
    val minutes = this % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes} min"
        hours > 0 -> "${hours}h"
        else -> "${minutes} min"
    }
}

private val flightListDateFormatter = FlexibleDateFormatter()
