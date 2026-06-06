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
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.atlas.domain.model.CountryTrackingState
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
import com.atlas.ui.theme.AtlasLiving
import com.atlas.ui.theme.AtlasLivingContainer
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
    trackingState: CountryTrackingState,
    style: CountryDetailStyle,
    onTripClick: (String) -> Unit,
    onEditLog: (CountryLog) -> Unit,
    onDeleteLog: (CountryLog) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDeleteLog by remember { mutableStateOf<CountryLog?>(null) }

    val rows = buildList {
        if (trackingState.currentlyLiving) {
            add(
                HistoryRow(
                    sortKey = "9999-12-31",
                    color = AtlasLiving,
                    icon = Icons.Filled.Home,
                    dateText = "Ara",
                    title = "Residència actual",
                    meta = "",
                    pillLabel = "VIVINT",
                    pillForeground = AtlasLiving,
                    pillBackground = AtlasLivingContainer,
                ),
            )
        }
        for (trip in tripSummaries) {
            val meta = listOfNotNull(
                trip.routeText?.takeIf { it.isNotBlank() },
                trip.status.toHistoryStatus(),
            ).joinToString(" · ")
            add(
                HistoryRow(
                    sortKey = trip.sortKey ?: "0000-01-01",
                    color = AtlasTrip,
                    icon = Icons.Filled.Route,
                    dateText = trip.dateRangeText?.toHistoryDateText() ?: "Sense data",
                    title = trip.title,
                    meta = meta,
                    pillLabel = trip.label.uppercase(),
                    pillForeground = AtlasTrip,
                    pillBackground = AtlasTripContainer,
                    onClick = { onTripClick(trip.tripId) },
                ),
            )
        }
        for (airTravel in airTravelSummaries) {
            val meta = listOfNotNull(
                airTravel.meta.takeIf { it.isNotBlank() },
                airTravel.status.toHistoryStatus(),
            ).joinToString(" · ")
            add(
                HistoryRow(
                    sortKey = airTravel.sortKey ?: "0000-01-01",
                    color = AtlasVisited,
                    icon = Icons.Filled.Flight,
                    dateText = airTravel.dateText?.toHistoryDateText() ?: "Sense data",
                    title = airTravel.title,
                    meta = meta,
                    pillLabel = "VOL",
                    pillForeground = AtlasVisited,
                    pillBackground = AtlasVisitedContainer,
                ),
            )
        }
        for (log in logs) {
            val isLived = log.type == CountryLogType.LIVED
            add(
                HistoryRow(
                    sortKey = log.dateRange?.start?.toSortKey() ?: "0000-01-01",
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
                ),
            )
        }
    }.sortedByDescending { it.sortKey }

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

        if (rows.isEmpty()) {
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
                rows.forEachIndexed { index, row ->
                    TimelineItem(
                        isLast = index == rows.lastIndex,
                        color = row.color,
                        icon = row.icon,
                        dateText = row.dateText,
                        title = row.title,
                        meta = row.meta,
                        pillLabel = row.pillLabel,
                        pillForeground = row.pillForeground,
                        pillBackground = row.pillBackground,
                        onClick = row.onClick,
                        onEdit = row.onEdit,
                        onDelete = row.onDelete,
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
    var showMenu by remember { mutableStateOf(false) }
    val hasMenu = onEdit != null || onDelete != null

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
                    .size(30.dp)
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
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
            // Date + type pill + optional overflow menu
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
                Spacer(modifier = Modifier.width(6.dp))
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
                if (hasMenu) {
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Més opcions",
                                modifier = Modifier.size(14.dp),
                                tint = AtlasOnSurfaceFaint,
                            )
                        }
                        MaterialTheme(
                            colorScheme = MaterialTheme.colorScheme.copy(surfaceContainer = AtlasSurface),
                            shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(14.dp)),
                        ) {
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.width(160.dp),
                            ) {
                                onEdit?.let { edit ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Edita",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                        },
                                        onClick = { showMenu = false; edit() },
                                    )
                                }
                                if (onEdit != null && onDelete != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(AtlasOutline),
                                    )
                                }
                                onDelete?.let { delete ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Elimina",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AtlasError,
                                            )
                                        },
                                        onClick = { showMenu = false; delete() },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
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
        }
    }
}

// ─── Models ──────────────────────────────────────────────────────────────────

private data class HistoryRow(
    val sortKey: String,
    val color: Color,
    val icon: ImageVector,
    val dateText: String,
    val title: String,
    val meta: String,
    val pillLabel: String,
    val pillForeground: Color,
    val pillBackground: Color,
    val onClick: (() -> Unit)? = null,
    val onEdit: (() -> Unit)? = null,
    val onDelete: (() -> Unit)? = null,
)

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

private fun FlexibleDate.toSortKey(): String {
    val y = year.toString().padStart(4, '0')
    val m = (month ?: 1).toString().padStart(2, '0')
    val d = (day ?: 1).toString().padStart(2, '0')
    return "$y-$m-$d"
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
