package com.atlas.ui.screens.timeline

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.atlas.domain.model.CountryLogType
import com.atlas.domain.model.TravelStatus
import com.atlas.presentation.timeline.TimelineItem
import com.atlas.presentation.timeline.TimelineUiState
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasCompleted
import com.atlas.ui.theme.AtlasInProgress
import com.atlas.ui.theme.AtlasLived
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPlanned
import com.atlas.ui.theme.AtlasSurface
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineScreen(
    uiState: TimelineUiState,
    onBackClick: () -> Unit,
    onTripClick: (String) -> Unit,
    onItineraryClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AtlasBackground),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 20.dp, top = 8.dp, bottom = 4.dp),
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
        }

        if (!uiState.isLoading && uiState.years.isEmpty()) {
            item {
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

        uiState.years.forEach { yearGroup ->
            stickyHeader(key = "year-${yearGroup.year}") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AtlasBackground)
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = yearGroup.year,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AtlasOnSurfaceStrong,
                    )
                }
            }

            items(
                items = yearGroup.items,
                key = { item ->
                    when (item) {
                        is TimelineItem.TripItem -> "trip-${item.id}"
                        is TimelineItem.ItineraryItem -> "itinerary-${item.id}"
                        is TimelineItem.LogItem -> "log-${item.logId}"
                    }
                },
            ) { item ->
                when (item) {
                    is TimelineItem.TripItem -> TripTimelineCard(
                        item = item,
                        onClick = { onTripClick(item.id) },
                    )
                    is TimelineItem.ItineraryItem -> ItineraryTimelineCard(
                        item = item,
                        onClick = { onItineraryClick(item.id) },
                    )
                    is TimelineItem.LogItem -> LogTimelineRow(item = item)
                }
            }

            item(key = "spacer-${yearGroup.year}") {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun TripTimelineCard(item: TimelineItem.TripItem, onClick: () -> Unit) {
    val statusColor = item.status.timelineColor()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AtlasSurface)
            .clickable(onClick = onClick),
    ) {
        Row {
            // Left accent strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(statusColor),
            )
            Column(modifier = Modifier.weight(1f)) {
                // Cover photo if available
                if (item.coverPhotoFilename != null) {
                    AsyncImage(
                        model = File(context.filesDir, "photos/${item.coverPhotoFilename}"),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                    )
                }
                // Content
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AtlasOnSurfaceStrong,
                        )
                        if (item.dateLabel.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.dateLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = AtlasOnSurfaceMuted,
                            )
                        }
                    }
                    StatusPill(status = item.status, color = statusColor)
                }
            }
        }
    }
}

@Composable
private fun ItineraryTimelineCard(item: TimelineItem.ItineraryItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AtlasSurface)
            .clickable(onClick = onClick),
    ) {
        Row {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(AtlasPlanned),
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.FlightTakeoff,
                    contentDescription = null,
                    tint = AtlasPlanned,
                    modifier = Modifier.size(16.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
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
                    if (item.dateLabel.isNotEmpty()) {
                        Text(
                            text = item.dateLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = AtlasOnSurfaceMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogTimelineRow(item: TimelineItem.LogItem) {
    val accentColor = if (item.logType == CountryLogType.LIVED) AtlasLived else AtlasCompleted
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 3.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(accentColor.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (item.flagEmoji != null) {
            Text(
                text = item.flagEmoji,
                style = MaterialTheme.typography.bodyLarge,
            )
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
