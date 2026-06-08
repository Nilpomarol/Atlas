package com.atlas.ui.screens.trip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Map
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.presentation.date.FlexibleDateRangeDraftField
import com.atlas.presentation.trip.TripListItemUiState
import com.atlas.presentation.trip.TripListUiState
import com.atlas.presentation.trip.TripStopMapPoint
import com.atlas.ui.components.AtlasPage
import com.atlas.ui.components.AtlasFilterPill
import com.atlas.ui.components.tripStatusColors
import com.atlas.ui.components.geo.AtlasGeoCanvas
import com.atlas.ui.components.geo.GeoCoordinate
import com.atlas.ui.components.geo.GeoMarker
import com.atlas.ui.components.geo.GeoRouteSegment
import com.atlas.ui.components.geo.GeoViewport
import com.atlas.ui.theme.AtlasNavy
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
                    verticalArrangement = Arrangement.spacedBy(8.dp),
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
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
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
                    text = "$tripCount ${if (tripCount == 1) "viatge" else "viatges"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = AtlasOnSurfaceMuted,
                )
            }
            Button(
                onClick = onCreateTripClick,
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AtlasNavy,
                    contentColor = AtlasSurface,
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TripStatusFilterChip(
                label = "Tots",
                selected = selectedStatus == null,
                onClick = { onSelectedStatusChanged(null) },
                modifier = Modifier.weight(1f),
            )
            listOf(TravelStatus.PLANNED, TravelStatus.IN_PROGRESS, TravelStatus.COMPLETED).forEach { status ->
                val colors = status.tripStatusColors()
                TripStatusFilterChip(
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
private fun TripStatusFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedContainerColor: Color = AtlasNavy,
    selectedContentColor: Color = AtlasSurface,
) {
    AtlasFilterPill(
        label = label,
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        selectedContainerColor = selectedContainerColor,
        selectedContentColor = selectedContentColor,
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
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(AtlasSurfaceRaised, RoundedCornerShape(16.dp))
                .border(1.dp, AtlasOutline, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Map,
                contentDescription = null,
                tint = AtlasPrimary,
                modifier = Modifier.size(25.dp),
            )
        }
        Text(
            text = "Encara no hi ha cap viatge.",
            style = MaterialTheme.typography.titleMedium,
            color = AtlasOnSurfaceStrong,
        )
        Text(
            text = "Crea el primer viatge i afegeix-hi parades quan vulguis.",
            style = MaterialTheme.typography.bodyMedium,
            color = AtlasOnSurfaceMuted,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onCreateTripClick,
            shape = RoundedCornerShape(13.dp),
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
    val routeText = item.routeText() ?: "Sense parades"
    val dateText = trip.dateRange?.let { dateRangeFormatter.format(it) } ?: "Sense data"

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
                    .height(124.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(AtlasSurfaceSubtle),
            ) {
                val context = LocalContext.current
                if (item.coverPhotoFilename != null) {
                    AsyncImage(
                        model = File(context.filesDir, "photos/${item.coverPhotoFilename}"),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    TripCardMap(
                        mapPoints = item.mapPoints,
                        stopCount = item.stopCount,
                        routeColor = colors.foreground,
                    )
                }
                TripStatePill(
                    label = colors.label,
                    color = colors.foreground,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(15.dp),
                )
                Text(
                    text = dateText.uppercase(),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 15.dp, top = 16.dp, end = 120.dp)
                        .background(AtlasSurface.copy(alpha = 0.88f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 18.dp, end = 18.dp, bottom = 3.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    Text(
                        text = trip.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 27.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasOnSurfaceStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AtlasSurface,
                shadowElevation = 0.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AtlasSurface)
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = null,
                        tint = AtlasPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = routeText,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasOnSurfaceStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(
                        modifier = Modifier
                            .height(18.dp)
                            .width(1.dp)
                            .background(AtlasOutline),
                    )
                    Text(
                        text = "${item.stopCount} ${if (item.stopCount == 1) "parada" else "parades"}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AtlasOnSurfaceMuted,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun TripCardMap(
    mapPoints: List<TripStopMapPoint>,
    stopCount: Int,
    routeColor: Color,
) {
    val coordinates = mapPoints.map { GeoCoordinate(latitude = it.latitude, longitude = it.longitude) }
    if (coordinates.isEmpty()) {
        TripMapTexture()
        RouteLineCanvas(stopCount = stopCount)
        return
    }

    AtlasGeoCanvas(
        modifier = Modifier.fillMaxSize(),
        viewport = GeoViewport.FitPoints(
            points = coordinates,
            minLongitudeSpanDegrees = 4.8,
            minLatitudeSpanDegrees = 3.2,
        ),
        routeSegments = coordinates.zipWithNext { from, to ->
            GeoRouteSegment(from = from, to = to, color = routeColor, alpha = 0.9f)
        },
        markers = coordinates.mapIndexed { index, coordinate ->
            GeoMarker(
                coordinate = coordinate,
                color = routeColor,
                radiusMultiplier = if (index == 0 || index == coordinates.lastIndex) 0.58f else 0.46f,
                isHollow = index == coordinates.lastIndex && coordinates.size > 1,
            )
        },
    )
}

@Composable
private fun TripMapTexture() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val spacing = 24.dp.toPx()
        var x = 0f
        while (x <= size.width) {
            drawLine(
                color = AtlasNavy.copy(alpha = 0.1f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1.dp.toPx(),
            )
            x += spacing
        }
        var y = 0f
        while (y <= size.height) {
            drawLine(
                color = AtlasNavy.copy(alpha = 0.1f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx(),
            )
            y += spacing
        }
    }
}

@Composable
private fun RouteLineCanvas(stopCount: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val visibleStops = stopCount.coerceIn(2, 5)
        val basePoints = listOf(
            Offset(size.width * 0.10f, size.height * 0.62f),
            Offset(size.width * 0.32f, size.height * 0.36f),
            Offset(size.width * 0.55f, size.height * 0.48f),
            Offset(size.width * 0.74f, size.height * 0.30f),
            Offset(size.width * 0.92f, size.height * 0.52f),
        )
        val points = basePoints.take(visibleStops)
        val path = Path().apply {
            points.firstOrNull()?.let { moveTo(it.x, it.y) }
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.82f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )
        points.forEachIndexed { index, point ->
            val isLast = index == points.lastIndex
            if (isLast) {
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = point,
                    style = Stroke(width = 2.dp.toPx()),
                )
            } else {
                drawCircle(Color.White, radius = 5.dp.toPx(), center = point)
            }
        }
    }
}

@Composable
private fun TripStatePill(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(color, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(Color.White, RoundedCornerShape(999.dp)),
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
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
