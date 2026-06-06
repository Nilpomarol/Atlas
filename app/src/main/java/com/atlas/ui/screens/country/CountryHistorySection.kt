package com.atlas.ui.screens.country

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.CountryLog
import com.atlas.domain.model.CountryLogType
import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.model.FlexibleDateRange
import com.atlas.domain.model.TravelStatus
import com.atlas.presentation.country.CountryAirTravelSummaryUiState
import com.atlas.presentation.country.CountryTripSummaryUiState
import com.atlas.ui.components.AtlasSectionTitle
import com.atlas.ui.theme.AtlasError
import com.atlas.ui.theme.AtlasLived
import com.atlas.ui.theme.AtlasLivedContainer
import com.atlas.ui.theme.AtlasOnSurfaceFaint
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasTrip
import com.atlas.ui.theme.AtlasTripContainer
import com.atlas.ui.theme.AtlasVisit
import com.atlas.ui.theme.AtlasVisitContainer
import com.atlas.ui.theme.AtlasVisited
import com.atlas.ui.theme.AtlasVisitedContainer

@Composable
fun CountryHistorySection(
    logs: List<CountryLog>,
    tripSummaries: List<CountryTripSummaryUiState>,
    airTravelSummaries: List<CountryAirTravelSummaryUiState>,
    style: CountryDetailStyle,
    onTripClick: (String) -> Unit,
    onEditLog: (CountryLog) -> Unit,
    onDeleteLog: (CountryLog) -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalCount = logs.size + tripSummaries.size + airTravelSummaries.size
    var pendingDeleteLog by remember { mutableStateOf<CountryLog?>(null) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AtlasSectionTitle(
            title = "Historial",
            action = {
                Text(
                    text = "PER QUÈ",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceMuted,
                )
            },
        )

        if (totalCount == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AtlasSurface, RoundedCornerShape(16.dp))
                    .border(1.dp, AtlasOutline, RoundedCornerShape(16.dp))
                    .padding(16.dp),
            ) {
                Text(
                    "Encara no hi ha registres, viatges ni vols.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AtlasOnSurfaceMuted,
                )
            }
        } else {
            Column {
                tripSummaries.forEachIndexed { index, trip ->
                    val meta = listOfNotNull(
                        trip.routeText?.takeIf { it.isNotBlank() },
                        trip.status.toHistoryStatus(),
                    ).joinToString(" · ")
                    TimelineItem(
                        isLast = index == tripSummaries.lastIndex && airTravelSummaries.isEmpty() && logs.isEmpty(),
                        color = AtlasTrip,
                        icon = Icons.Filled.Route,
                        dateText = trip.dateRangeText?.toHistoryDateText() ?: "Sense data",
                        title = trip.title,
                        meta = meta,
                        pillLabel = trip.label.uppercase(),
                        pillForeground = AtlasTrip,
                        pillBackground = AtlasTripContainer,
                        onClick = { onTripClick(trip.tripId) },
                    )
                }
                airTravelSummaries.forEachIndexed { index, airTravel ->
                    val meta = listOfNotNull(
                        airTravel.meta.takeIf { it.isNotBlank() },
                        airTravel.status.toHistoryStatus(),
                    ).joinToString(" · ")
                    TimelineItem(
                        isLast = index == airTravelSummaries.lastIndex && logs.isEmpty(),
                        color = AtlasVisited,
                        icon = Icons.Filled.Flight,
                        dateText = airTravel.dateText?.toHistoryDateText() ?: "Sense data",
                        title = airTravel.title,
                        meta = meta,
                        pillLabel = airTravel.label.uppercase(),
                        pillForeground = AtlasVisited,
                        pillBackground = AtlasVisitedContainer,
                    )
                }
                logs.forEachIndexed { index, log ->
                    val isLived = log.type == CountryLogType.LIVED
                    TimelineItem(
                        isLast = index == logs.lastIndex,
                        color = if (isLived) AtlasLived else AtlasVisit,
                        icon = if (isLived) Icons.Filled.Home else Icons.Filled.Place,
                        dateText = log.dateRange?.toHistoryDateText() ?: "Sense data",
                        title = log.notes?.takeIf { it.isNotBlank() } ?: log.type.toCatalanLabel(),
                        meta = log.historyMeta(),
                        pillLabel = if (isLived) "RESIDÈNCIA" else "VISITA",
                        pillForeground = if (isLived) AtlasLived else AtlasVisit,
                        pillBackground = if (isLived) AtlasLivedContainer else AtlasVisitContainer,
                        onEdit = { onEditLog(log) },
                        onDelete = { pendingDeleteLog = log },
                    )
                }
            }
        }
    }

    pendingDeleteLog?.let { log ->
        ConfirmDeleteLogDialog(
            log = log,
            onDismiss = { pendingDeleteLog = null },
            onConfirm = {
                pendingDeleteLog = null
                onDeleteLog(log)
            },
        )
    }
}

@Composable
private fun TimelineItem(
    isLast: Boolean,
    color: Color,
    icon: ImageVector,
    dateText: String,
    title: String,
    meta: String,
    pillLabel: String,
    pillForeground: Color,
    pillBackground: Color,
    onClick: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
    ) {
        // Timeline left column: circle icon + vertical connecting line
        Column(
            modifier = Modifier
                .width(44.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White,
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .padding(vertical = 4.dp)
                        .background(AtlasOutline),
                )
            }
        }

        // Content card
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 12.dp)
                .background(AtlasSurface, RoundedCornerShape(14.dp))
                .border(1.dp, AtlasOutline, RoundedCornerShape(14.dp))
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Date + type pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = dateText,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(pillBackground, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = pillLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = pillForeground,
                    )
                }
            }
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceStrong,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            // Meta
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            // Edit / delete — log items only
            if (onEdit != null || onDelete != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    onEdit?.let {
                        IconButton(onClick = it, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edita",
                                modifier = Modifier.size(14.dp),
                                tint = AtlasOnSurfaceFaint,
                            )
                        }
                    }
                    onDelete?.let {
                        IconButton(onClick = it, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Elimina",
                                modifier = Modifier.size(14.dp),
                                tint = AtlasOnSurfaceFaint,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Date helpers ────────────────────────────────────────────────────────────

private fun TravelStatus.toHistoryStatus(): String? = when (this) {
    TravelStatus.PLANNED -> "planificat"
    TravelStatus.IN_PROGRESS -> "en curs"
    TravelStatus.COMPLETED -> "completat"
    TravelStatus.UNKNOWN -> null
}

private fun CountryLog.historyMeta(): String {
    if (type != CountryLogType.LIVED) return type.toCatalanLabel()
    val range = dateRange ?: return "Període viscut"
    if (range.start != null && range.end == null) {
        return "Des de ${range.start.toHistoryDateText()}"
    }
    return range.yearDurationText() ?: "Període viscut"
}

private fun FlexibleDateRange.toHistoryDateText(): String = when {
    start != null && end != null -> {
        val startText = start.toHistoryDateText()
        val endText = end.toHistoryDateText()
        if (startText == endText) startText else "$startText – $endText"
    }
    start != null -> start.toHistoryDateText()
    end != null -> "Fins ${end.toHistoryDateText()}"
    else -> "Sense data"
}

private fun FlexibleDateRange.yearDurationText(): String? {
    val startYear = start?.year ?: return null
    val endYear = end?.year ?: return null
    val years = (endYear - startYear).coerceAtLeast(1)
    return "${years.toCatalanCount()} ${if (years == 1) "any" else "anys"} de residència"
}

private fun FlexibleDate.toHistoryDateText(): String = when (precision) {
    DatePrecision.DAY -> "${day ?: ""} ${month.shortMonth()} ${year.twoDigitYear()}"
    DatePrecision.MONTH -> "${month.shortMonth()} ${year.twoDigitYear()}"
    DatePrecision.YEAR -> year.toString()
}

private fun String.toHistoryDateText(): String {
    val parts = split(" - ")
    if (parts.size == 2) {
        val start = parts[0].singleDateToHistoryText()
        val end = parts[1].singleDateToHistoryText()
        return if (start == end) start else "$start – $end"
    }
    return singleDateToHistoryText()
}

private fun String.singleDateToHistoryText(): String {
    val parts = split("-")
    return when (parts.size) {
        3 -> "${parts[0]} ${parts[1].toIntOrNull().shortMonth()} ${parts[2].toIntOrNull()?.twoDigitYear() ?: parts[2]}"
        2 -> "${parts[0].toIntOrNull().shortMonth()} ${parts[1].toIntOrNull()?.twoDigitYear() ?: parts[1]}"
        else -> this
    }
}

private fun Int?.shortMonth(): String = when (this) {
    1 -> "gen."
    2 -> "febr."
    3 -> "març"
    4 -> "abr."
    5 -> "maig"
    6 -> "juny"
    7 -> "jul."
    8 -> "ag."
    9 -> "set."
    10 -> "oct."
    11 -> "nov."
    12 -> "des."
    else -> ""
}

private fun Int.twoDigitYear(): String = (this % 100).toString().padStart(2, '0')

private fun Int.toCatalanCount(): String = when (this) {
    1 -> "Un"
    2 -> "Dos"
    3 -> "Tres"
    4 -> "Quatre"
    5 -> "Cinc"
    6 -> "Sis"
    7 -> "Set"
    8 -> "Vuit"
    9 -> "Nou"
    10 -> "Deu"
    else -> toString()
}

// ─── Dialogs ─────────────────────────────────────────────────────────────────

@Composable
private fun ConfirmDeleteLogDialog(
    log: CountryLog,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = AtlasSurface,
        title = {
            Text(
                text = "Eliminar registre?",
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceStrong,
            )
        },
        text = {
            Text(
                text = "S'eliminarà ${log.notes?.takeIf { it.isNotBlank() } ?: log.type.toCatalanLabel()}. Aquesta acció no es pot desfer.",
                color = AtlasOnSurfaceMuted,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Elimina",
                    fontWeight = FontWeight.ExtraBold,
                    color = AtlasError,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel·la")
            }
        },
    )
}
