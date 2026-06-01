package com.atlas.ui.screens.trip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.presentation.date.FlexibleDateRangeDraftField
import com.atlas.presentation.trip.TripListItemUiState
import com.atlas.presentation.trip.TripListUiState
import com.atlas.ui.components.AtlasPage
import com.atlas.ui.components.AtlasPill
import com.atlas.ui.components.tripStatusColors
import com.atlas.ui.theme.AtlasAccentContainer
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasSurfaceRaised
import com.atlas.ui.theme.AtlasSurfaceSubtle

@Composable
fun TripListScreen(
    uiState: TripListUiState,
    onTripClick: (String) -> Unit,
    onCreateTripClick: () -> Unit,
    onDismissDraft: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onStatusChanged: (TravelStatus) -> Unit,
    onDatePrecisionChanged: (com.atlas.domain.model.DatePrecision) -> Unit,
    onDateFieldChanged: (FlexibleDateRangeDraftField, String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSaveDraft: () -> Unit,
) {
    var selectedStatus by remember { mutableStateOf<TravelStatus?>(null) }
    val filteredTrips = uiState.tripItems.filter { item ->
        selectedStatus == null || item.trip.status == selectedStatus
    }

    AtlasPage(contentPadding = PaddingValues(0.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TripListHeader(
                tripCount = filteredTrips.size,
                selectedStatus = selectedStatus,
                onSelectedStatusChanged = { selectedStatus = it },
                onCreateTripClick = onCreateTripClick,
            )

            if (filteredTrips.isEmpty()) {
                EmptyTripList(onCreateTripClick = onCreateTripClick)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 20.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = filteredTrips,
                        key = { item -> item.trip.id },
                    ) { item ->
                        TripCard(
                            item = item,
                            onClick = { onTripClick(item.trip.id) },
                        )
                    }
                }
            }
        }
    }

    if (uiState.draft.isOpen) {
        TripEditorDialog(
            draft = uiState.draft,
            onDismiss = onDismissDraft,
            onTitleChanged = onTitleChanged,
            onStatusChanged = onStatusChanged,
            onDatePrecisionChanged = onDatePrecisionChanged,
            onDateFieldChanged = onDateFieldChanged,
            onNotesChanged = onNotesChanged,
            onSave = onSaveDraft,
        )
    }
}

@Composable
private fun TripListHeader(
    tripCount: Int,
    selectedStatus: TravelStatus?,
    onSelectedStatusChanged: (TravelStatus?) -> Unit,
    onCreateTripClick: () -> Unit,
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
                    text = "Viatges",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AtlasOnSurfaceStrong,
                )
                Text(
                    text = "$tripCount viatges",
                    style = MaterialTheme.typography.labelMedium,
                    color = AtlasOnSurfaceMuted,
                )
            }
            Button(
                onClick = onCreateTripClick,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AtlasAccentContainer,
                    contentColor = AtlasPrimary,
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(text = "Nou", modifier = Modifier.padding(start = 4.dp))
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                TripStatusFilterChip(
                    label = "Tots",
                    selected = selectedStatus == null,
                    onClick = { onSelectedStatusChanged(null) },
                )
            }
            items(TravelStatus.entries) { status ->
                TripStatusFilterChip(
                    label = status.toCatalanLabel(),
                    selected = selectedStatus == status,
                    onClick = { onSelectedStatusChanged(status) },
                )
            }
        }
    }
}

@Composable
private fun TripStatusFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
            )
        },
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
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

@Composable
private fun EmptyTripList(onCreateTripClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Encara no hi ha cap viatge.",
            style = MaterialTheme.typography.titleMedium,
            color = AtlasOnSurfaceStrong,
        )
        Text(
            text = "Crea el primer viatge i afegeix-hi parades quan vulguis.",
            style = MaterialTheme.typography.bodyMedium,
            color = AtlasOnSurfaceMuted,
        )
        Button(
            onClick = onCreateTripClick,
            shape = RoundedCornerShape(999.dp),
        ) {
            Text(text = "Crea viatge")
        }
    }
}

@Composable
private fun TripCard(
    item: TripListItemUiState,
    onClick: () -> Unit,
) {
    val trip = item.trip
    val colors = trip.status.tripStatusColors()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .background(colors.foreground.copy(alpha = 0.76f)),
            ) {
                RouteLineCanvas()
                AtlasPill(
                    label = colors.label,
                    colors = colors.copy(container = AtlasSurface.copy(alpha = 0.9f)),
                    modifier = Modifier.padding(12.dp),
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp),
                ) {
                    Text(
                        text = trip.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = trip.dateRange?.let { dateRangeFormatter.format(it) } ?: "Sense data",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.88f),
                    )
                }
            }
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Map,
                    contentDescription = null,
                    tint = AtlasOnSurfaceMuted,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = item.routeText() ?: "Sense parades",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${item.stopCount} ${if (item.stopCount == 1) "parada" else "parades"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = AtlasOnSurfaceMuted,
                )
            }
        }
    }
}

@Composable
private fun RouteLineCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val p1 = Offset(size.width * 0.16f, size.height * 0.62f)
        val p2 = Offset(size.width * 0.48f, size.height * 0.36f)
        val p3 = Offset(size.width * 0.82f, size.height * 0.52f)
        drawLine(Color.White.copy(alpha = 0.52f), p1, p2, strokeWidth = 2.dp.toPx())
        drawLine(Color.White.copy(alpha = 0.52f), p2, p3, strokeWidth = 2.dp.toPx())
        listOf(p1, p2, p3).forEach {
            drawCircle(Color.White, radius = 4.dp.toPx(), center = it)
        }
    }
}

private fun TripListItemUiState.routeText(): String? {
    val first = firstStopName?.takeIf { it.isNotBlank() } ?: return null
    val last = lastStopName?.takeIf { it.isNotBlank() } ?: return first
    return if (first == last) first else "$first -> $last"
}

fun TravelStatus.toCatalanLabel(): String = when (this) {
    TravelStatus.PLANNED -> "Planificat"
    TravelStatus.IN_PROGRESS -> "En curs"
    TravelStatus.COMPLETED -> "Completat"
    TravelStatus.UNKNOWN -> "Desconegut"
}

private val dateRangeFormatter = FlexibleDateFormatter()
