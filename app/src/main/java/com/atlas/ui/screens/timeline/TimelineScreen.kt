package com.atlas.ui.screens.timeline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.atlas.domain.model.CountryLogType
import com.atlas.domain.model.TravelStatus
import com.atlas.presentation.timeline.TimelineItem
import com.atlas.presentation.timeline.TimelineUiState
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasCompleted
import com.atlas.ui.theme.AtlasGold
import com.atlas.ui.theme.AtlasInProgress
import com.atlas.ui.theme.AtlasLived
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasOutlineStrong
import com.atlas.ui.theme.AtlasPlanned
import com.atlas.ui.theme.AtlasSurface
import java.io.File

// ── Rail geometry ─────────────────────────────────────────────────────────────

private val RailWidth = 56.dp
private val SpineColor = AtlasOutlineStrong
private val NodeTopOffset = 20.dp

// Flattened render rows so the spine can be a single continuous line.
private sealed interface TimelineRow {
    data class YearMarker(val label: String) : TimelineRow
    data class Entry(val item: TimelineItem) : TimelineRow
}

@Composable
fun TimelineScreen(
    uiState: TimelineUiState,
    onBackClick: () -> Unit,
    onTripClick: (String) -> Unit,
    onItineraryClick: (String) -> Unit,
) {
    val rows = remember(uiState.years) {
        buildList {
            uiState.years.forEach { yearGroup ->
                add(TimelineRow.YearMarker(yearGroup.year))
                yearGroup.items.forEach { add(TimelineRow.Entry(it)) }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AtlasBackground),
        contentPadding = PaddingValues(bottom = 40.dp),
    ) {
        item(key = "header") { TimelineHeader(onBackClick = onBackClick) }

        if (!uiState.isLoading && rows.isEmpty()) {
            item(key = "empty") {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Encara no hi ha activitat registrada.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AtlasOnSurfaceMuted,
                    )
                }
            }
        }

        itemsIndexed(
            items = rows,
            key = { _, row ->
                when (row) {
                    is TimelineRow.YearMarker -> "year-${row.label}"
                    is TimelineRow.Entry -> when (val item = row.item) {
                        is TimelineItem.TripItem -> "trip-${item.id}"
                        is TimelineItem.ItineraryItem -> "itinerary-${item.id}"
                        is TimelineItem.LogItem -> "log-${item.logId}"
                    }
                }
            },
        ) { index, row ->
            val drawTopLine = index != 0
            val drawBottomLine = index != rows.lastIndex
            when (row) {
                is TimelineRow.YearMarker -> YearMarkerRow(
                    label = row.label,
                    drawTopLine = drawTopLine,
                    drawBottomLine = drawBottomLine,
                )
                is TimelineRow.Entry -> when (val item = row.item) {
                    is TimelineItem.TripItem -> TripTimelineRow(
                        item = item,
                        drawTopLine = drawTopLine,
                        drawBottomLine = drawBottomLine,
                        onClick = { onTripClick(item.id) },
                    )
                    is TimelineItem.ItineraryItem -> ItineraryTimelineRow(
                        item = item,
                        drawTopLine = drawTopLine,
                        drawBottomLine = drawBottomLine,
                        onClick = { onItineraryClick(item.id) },
                    )
                    is TimelineItem.LogItem -> LogTimelineRow(
                        item = item,
                        drawTopLine = drawTopLine,
                        drawBottomLine = drawBottomLine,
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineHeader(onBackClick: () -> Unit) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 20.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Enrere",
                    tint = AtlasOnSurfaceStrong,
                )
            }
            Text(
                text = "Cronologia",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AtlasOnSurfaceStrong,
            )
        }
        Text(
            text = "EL TEU RECORREGUT",
            modifier = Modifier.padding(start = 20.dp),
            style = MaterialTheme.typography.labelSmall,
            color = AtlasOnSurfaceMuted,
        )
    }
}

// ── Rail primitive ────────────────────────────────────────────────────────────

/**
 * A single timeline row: a continuous spine with a node on the left, content on the
 * right. The spine is drawn full-height per row so consecutive rows form one line.
 */
@Composable
private fun TimelineRailRow(
    nodeColor: Color,
    drawTopLine: Boolean,
    drawBottomLine: Boolean,
    nodeRadius: Dp = 6.dp,
    nodeHollow: Boolean = false,
    nodeRing: Boolean = false,
    content: @Composable () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Canvas(modifier = Modifier.width(RailWidth).fillMaxHeight()) {
            val cx = size.width / 2f
            val nodeCy = NodeTopOffset.toPx() + nodeRadius.toPx()
            val lineW = 2.dp.toPx()
            if (drawTopLine) {
                drawLine(SpineColor, Offset(cx, 0f), Offset(cx, nodeCy), lineW, StrokeCap.Round)
            }
            if (drawBottomLine) {
                drawLine(SpineColor, Offset(cx, nodeCy), Offset(cx, size.height), lineW, StrokeCap.Round)
            }
            val r = nodeRadius.toPx()
            if (nodeRing) {
                drawCircle(nodeColor.copy(alpha = 0.18f), r + 4.dp.toPx(), Offset(cx, nodeCy))
            }
            if (nodeHollow) {
                drawCircle(AtlasBackground, r, Offset(cx, nodeCy))
                drawCircle(nodeColor, r, Offset(cx, nodeCy), style = Stroke(width = 2.dp.toPx()))
            } else {
                drawCircle(nodeColor, r, Offset(cx, nodeCy))
            }
        }
        Box(modifier = Modifier.weight(1f).padding(end = 20.dp, top = 6.dp, bottom = 6.dp)) {
            content()
        }
    }
}

// ── Year milestone ────────────────────────────────────────────────────────────

@Composable
private fun YearMarkerRow(label: String, drawTopLine: Boolean, drawBottomLine: Boolean) {
    TimelineRailRow(
        nodeColor = AtlasGold,
        drawTopLine = drawTopLine,
        drawBottomLine = drawBottomLine,
        nodeRadius = 7.dp,
        nodeRing = true,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AtlasOnSurfaceStrong,
        )
    }
}

// ── Trip entry ────────────────────────────────────────────────────────────────

@Composable
private fun TripTimelineRow(
    item: TimelineItem.TripItem,
    drawTopLine: Boolean,
    drawBottomLine: Boolean,
    onClick: () -> Unit,
) {
    val statusColor = item.status.timelineColor()
    val context = LocalContext.current
    TimelineRailRow(
        nodeColor = statusColor,
        drawTopLine = drawTopLine,
        drawBottomLine = drawBottomLine,
        nodeRadius = 6.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AtlasSurface)
                .border(1.dp, AtlasOutline, RoundedCornerShape(16.dp))
                .clickable(onClick = onClick),
        ) {
            if (item.coverPhotoFilename != null) {
                AsyncImage(
                    model = File(context.filesDir, "photos/${item.coverPhotoFilename}"),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(104.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (item.dateLabel.isNotEmpty()) {
                        Text(
                            text = item.dateLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = AtlasOnSurfaceMuted,
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasOnSurfaceStrong,
                    )
                }
                StatusPill(status = item.status, color = statusColor)
            }
        }
    }
}

// ── Itinerary (standalone flight) entry ──────────────────────────────────────

@Composable
private fun ItineraryTimelineRow(
    item: TimelineItem.ItineraryItem,
    drawTopLine: Boolean,
    drawBottomLine: Boolean,
    onClick: () -> Unit,
) {
    TimelineRailRow(
        nodeColor = AtlasPlanned,
        drawTopLine = drawTopLine,
        drawBottomLine = drawBottomLine,
        nodeRadius = 6.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AtlasSurface)
                .border(1.dp, AtlasOutline, RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.FlightTakeoff,
                contentDescription = null,
                tint = AtlasPlanned,
                modifier = Modifier.size(18.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                if (item.dateLabel.isNotEmpty()) {
                    Text(
                        text = item.dateLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = AtlasOnSurfaceMuted,
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                }
                Text(
                    text = item.codeLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceStrong,
                )
                if (item.cityLabel.isNotEmpty()) {
                    Text(
                        text = item.cityLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = AtlasOnSurfaceMuted,
                    )
                }
            }
        }
    }
}

// ── Country log entry ─────────────────────────────────────────────────────────

@Composable
private fun LogTimelineRow(
    item: TimelineItem.LogItem,
    drawTopLine: Boolean,
    drawBottomLine: Boolean,
) {
    val accentColor = if (item.logType == CountryLogType.LIVED) AtlasLived else AtlasCompleted
    TimelineRailRow(
        nodeColor = accentColor,
        drawTopLine = drawTopLine,
        drawBottomLine = drawBottomLine,
        nodeRadius = 5.dp,
        nodeHollow = true,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(accentColor.copy(alpha = 0.08f))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (item.flagEmoji != null) {
                Text(text = item.flagEmoji, style = MaterialTheme.typography.bodyLarge)
            } else {
                Icon(
                    imageVector = Icons.Filled.Luggage,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = item.countryName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AtlasOnSurfaceStrong,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = when (item.logType) {
                    CountryLogType.VISIT -> "Visita"
                    CountryLogType.LIVED -> "Viscut"
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor,
            )
            if (item.dateLabel.isNotEmpty()) {
                Text(
                    text = "· ${item.dateLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AtlasOnSurfaceMuted,
                )
            }
        }
    }
}

// ── Shared bits ───────────────────────────────────────────────────────────────

@Composable
private fun StatusPill(status: TravelStatus, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = status.timelineLabel(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

private fun TravelStatus.timelineColor(): Color = when (this) {
    TravelStatus.COMPLETED -> AtlasCompleted
    TravelStatus.IN_PROGRESS -> AtlasInProgress
    TravelStatus.PLANNED -> AtlasPlanned
    TravelStatus.UNKNOWN -> AtlasOnSurfaceMuted
}

private fun TravelStatus.timelineLabel(): String = when (this) {
    TravelStatus.COMPLETED -> "Completat"
    TravelStatus.IN_PROGRESS -> "En curs"
    TravelStatus.PLANNED -> "Planejat"
    TravelStatus.UNKNOWN -> "Desconegut"
}
