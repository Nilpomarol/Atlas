package com.atlas.ui.screens.country

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.geometry.Offset
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
import com.atlas.presentation.country.CountryTripSummaryUiState
import com.atlas.ui.components.AtlasPill
import com.atlas.ui.components.AtlasSectionTitle
import com.atlas.ui.components.tripStatusColors
import com.atlas.ui.theme.AtlasLived
import com.atlas.ui.theme.AtlasLivedContainer
import com.atlas.ui.theme.AtlasError
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasTrip
import com.atlas.ui.theme.AtlasTripContainer
import com.atlas.ui.theme.AtlasVisit
import com.atlas.ui.theme.AtlasVisitContainer

@Composable
fun CountryHistorySection(
    logs: List<CountryLog>,
    tripSummaries: List<CountryTripSummaryUiState>,
    style: CountryDetailStyle,
    onTripClick: (String) -> Unit,
    onEditLog: (CountryLog) -> Unit,
    onDeleteLog: (CountryLog) -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalCount = logs.size + tripSummaries.size
    var pendingDeleteLog by remember { mutableStateOf<CountryLog?>(null) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AtlasSectionTitle(title = "Historial")

        if (totalCount == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AtlasSurface, RoundedCornerShape(16.dp))
                    .border(1.dp, AtlasOutline, RoundedCornerShape(16.dp))
                    .padding(16.dp),
            ) {
                Text(
                    "Encara no hi ha registres ni viatges.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AtlasOnSurfaceMuted,
                )
            }
        } else {
            Column {
                tripSummaries.forEachIndexed { index, trip ->
                    TimelineItem(
                        isLast = index == tripSummaries.lastIndex && logs.isEmpty(),
                        color = AtlasTrip,
                        container = AtlasTripContainer,
                        icon = Icons.Filled.Route,
                        title = trip.title,
                        label = "Viatge",
                        dateText = trip.dateRangeText?.toHistoryDateText() ?: "Sense data",
                        meta = trip.routeText ?: "Sense ruta",
                        statusPill = {
                            AtlasPill(
                                label = trip.status.tripStatusColors().label,
                                colors = trip.status.tripStatusColors(),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                fontWeight = FontWeight.ExtraBold,
                            )
                        },
                        onClick = { onTripClick(trip.tripId) },
                    )
                }
                logs.forEachIndexed { index, log ->
                    val isLived = log.type == CountryLogType.LIVED
                    TimelineItem(
                        isLast = index == logs.lastIndex,
                        color = if (isLived) AtlasLived else AtlasVisit,
                        container = if (isLived) AtlasLivedContainer else AtlasVisitContainer,
                        icon = if (isLived) Icons.Filled.Home else Icons.Filled.Place,
                        title = log.notes?.takeIf { it.isNotBlank() } ?: log.type.toCatalanLabel(),
                        label = log.type.toCatalanLabel(),
                        dateText = log.dateRange?.toHistoryDateText() ?: "Sense data",
                        meta = log.historyMeta(),
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
    container: Color,
    icon: ImageVector,
    title: String,
    label: String,
    dateText: String,
    meta: String,
    statusPill: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.width(44.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 26.dp)
                    .size(40.dp)
                    .background(container, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(21.dp),
                    tint = color,
                )
            }
            if (!isLast) {
                Box(
                modifier = Modifier
                        .width(1.dp)
                        .height(34.dp)
                        .background(AtlasOutline),
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 6.dp)
                .background(AtlasSurface, RoundedCornerShape(14.dp))
                .border(1.dp, AtlasOutline, RoundedCornerShape(14.dp))
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // RouteThumb(container = container)
                Column(modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = dateText,
                        modifier = Modifier.padding(top = 3.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasOnSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = AtlasOnSurfaceStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (meta.isNotBlank()) {
                        Text(
                            text = meta,
                            modifier = Modifier.padding(top = 2.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AtlasOnSurfaceMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (onEdit != null || onDelete != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        onEdit?.let {
                            IconActionButton(
                                icon = Icons.Filled.Edit,
                                contentDescription = "Edita",
                                onClick = it,
                            )
                        }
                        onDelete?.let {
                            IconActionButton(
                                icon = Icons.Filled.Delete,
                                contentDescription = "Elimina",
                                onClick = it,
                            )
                        }
                    }
                } else {
                    statusPill?.invoke()
                }
            }
        }
    }
}

@Composable
private fun RouteThumb(
    container: Color,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(container, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val p1 = Offset(size.width * 0.18f, size.height * 0.22f)
            val p2 = Offset(size.width * 0.54f, size.height * 0.42f)
            val p3 = Offset(size.width * 0.82f, size.height * 0.20f)
            drawLine(Color.White.copy(alpha = 0.74f), p1, p2, strokeWidth = 1.4.dp.toPx())
            drawLine(Color.White.copy(alpha = 0.74f), p2, p3, strokeWidth = 1.4.dp.toPx())
            listOf(p1, p2, p3).forEach { point ->
                drawCircle(Color.White, radius = 2.8.dp.toPx(), center = point)
            }
        }
    }
}

@Composable
private fun IconActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(30.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(16.dp),
            tint = AtlasOnSurfaceMuted,
        )
    }
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
        if (startText == endText) startText else "$startText - $endText"
    }
    start != null -> start.toHistoryDateText()
    end != null -> "Fins ${end.toHistoryDateText()}"
    else -> "Sense data"
}

private fun FlexibleDateRange.yearDurationText(): String? {
    val startYear = start?.year ?: return null
    val endYear = end?.year ?: return null
    val years = (endYear - startYear).coerceAtLeast(1)
    return "${years.toCatalanCount()} ${if (years == 1) "any" else "anys"}"
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
        return if (start == end) start else "$start - $end"
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
