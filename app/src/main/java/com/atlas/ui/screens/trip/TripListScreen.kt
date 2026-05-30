package com.atlas.ui.screens.trip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.presentation.date.FlexibleDateRangeDraftField
import com.atlas.presentation.trip.TripListItemUiState
import com.atlas.presentation.trip.TripListUiState

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "Viatges",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "${uiState.tripItems.size} viatges",
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(onClick = onCreateTripClick) {
                Text(text = "Nou")
            }
        }

        if (uiState.tripItems.isEmpty()) {
            EmptyTripList(onCreateTripClick = onCreateTripClick)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(
                    items = uiState.tripItems,
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
private fun EmptyTripList(
    onCreateTripClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Encara no hi ha cap viatge.",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Crea el primer viatge i afegeix-hi parades quan vulguis.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onCreateTripClick) {
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
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = trip.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(10.dp))
                TripStatusBadge(status = trip.status)
            }

            Text(
                text = trip.dateRange?.let { dateRangeFormatter.format(it) } ?: "Sense data",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TripMetaPill(text = "${item.stopCount} ${if (item.stopCount == 1) "parada" else "parades"}")
                item.routeText()?.let { route ->
                    Text(
                        text = route,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun TripStatusBadge(status: TravelStatus) {
    val (background, foreground) = when (status) {
        TravelStatus.PLANNED -> Color(0xFFFDE3C8) to Color(0xFF92400E)
        TravelStatus.IN_PROGRESS -> Color(0xFFC6EAD8) to Color(0xFF005C38)
        TravelStatus.COMPLETED -> Color(0xFFC2D9F0) to Color(0xFF024E82)
        TravelStatus.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = status.toCatalanLabel().uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = foreground,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun TripMetaPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun TripListItemUiState.routeText(): String? {
    val first = firstStopName?.takeIf { it.isNotBlank() } ?: return null
    val last = lastStopName?.takeIf { it.isNotBlank() } ?: return first
    return if (first == last) first else "$first → $last"
}

fun TravelStatus.toCatalanLabel(): String = when (this) {
    TravelStatus.PLANNED -> "Planificat"
    TravelStatus.IN_PROGRESS -> "En curs"
    TravelStatus.COMPLETED -> "Completat"
    TravelStatus.UNKNOWN -> "Desconegut"
}

private val dateRangeFormatter = FlexibleDateFormatter()
