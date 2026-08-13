package com.atlas.ui.rework.screens.trips

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.atlas.domain.model.TravelStatus
import com.atlas.presentation.trip.TripListItemUiState
import com.atlas.presentation.trip.TripListUiState
import com.atlas.ui.rework.components.ReworkDropdownItem
import com.atlas.ui.rework.components.ReworkDropdownMenu
import com.atlas.ui.rework.components.ReworkFloatingCard
import com.atlas.ui.rework.foundation.AtlasReworkTheme
import java.io.File

/** Ordering of the status groups: what is happening now comes first. */
private val StatusOrder = listOf(
    TravelStatus.IN_PROGRESS,
    TravelStatus.PLANNED,
    TravelStatus.COMPLETED,
    TravelStatus.UNKNOWN,
)

enum class TripStatusFilter(val label: String, val status: TravelStatus?) {
    All("Tots els estats", null),
    InProgress("En curs", TravelStatus.IN_PROGRESS),
    Planned("Planejats", TravelStatus.PLANNED),
    Completed("Completats", TravelStatus.COMPLETED),
}

enum class TripSort(val label: String) {
    MostRecent("Més recents"),
    Oldest("Més antics"),
    Title("Títol"),
}

@Composable
fun ReworkTripsScreen(
    state: TripListUiState,
    onTripOpened: (String) -> Unit,
    onCreateTrip: () -> Unit,
) {
    val colors = AtlasReworkTheme.colors
    var filter by remember { mutableStateOf(TripStatusFilter.All) }
    var sort by remember { mutableStateOf(TripSort.MostRecent) }

    val visible = remember(state.tripItems, filter, sort) {
        state.tripItems
            .filter { filter.status == null || it.trip.status == filter.status }
            .sortedForDisplay(sort)
    }
    val grouped = remember(visible) { visible.groupBy { it.trip.status } }

    Box(Modifier.fillMaxSize().background(colors.surface)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 196.dp),
        ) {
            item { TripsHeader(state) }
            item {
                TripListControls(
                    filter = filter,
                    sort = sort,
                    onFilterSelected = { filter = it },
                    onSortSelected = { sort = it },
                )
            }

            StatusOrder.forEach { status ->
                val trips = grouped[status].orEmpty()
                if (trips.isEmpty()) return@forEach
                item(key = "group-${status.name}") { GroupHeading(status.groupLabel()) }
                items(trips, key = { it.trip.id }) { item ->
                    if (item.isCompact) {
                        CompactTripCard(item = item, onClick = { onTripOpened(item.trip.id) })
                    } else {
                        FullTripCard(item = item, onClick = { onTripOpened(item.trip.id) })
                    }
                }
            }

            if (visible.isEmpty()) {
                item { EmptyTripsCard(hasAnyTrip = state.tripItems.isNotEmpty(), onCreateTrip = onCreateTrip) }
            }
        }
    }
}

@Composable
private fun TripsHeader(state: TripListUiState) {
    val colors = AtlasReworkTheme.colors
    Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 12.dp)) {
        Text("VIATGES", style = AtlasReworkTheme.typography.label, color = colors.accent)
        Text("El teu recorregut", style = AtlasReworkTheme.typography.display, color = colors.ink)
        Spacer(Modifier.height(6.dp))
        Text(
            buildString {
                append("${state.tripCount} ${if (state.tripCount == 1) "VIATGE" else "VIATGES"}")
                if (state.countryCount > 0) {
                    append(" · ${state.countryCount} ${if (state.countryCount == 1) "PAÍS" else "PAÏSOS"}")
                }
            },
            style = AtlasReworkTheme.typography.data,
            color = colors.inkMuted,
        )
    }
}

@Composable
private fun TripListControls(
    filter: TripStatusFilter,
    sort: TripSort,
    onFilterSelected: (TripStatusFilter) -> Unit,
    onSortSelected: (TripSort) -> Unit,
) {
    var filterExpanded by remember { mutableStateOf(false) }
    var sortExpanded by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.weight(1f)) {
            ControlButton(filter.label) { filterExpanded = true }
            ReworkDropdownMenu(expanded = filterExpanded, onDismissRequest = { filterExpanded = false }) {
                TripStatusFilter.entries.forEach { option ->
                    ReworkDropdownItem(
                        label = option.label,
                        selected = option == filter,
                        onClick = { onFilterSelected(option); filterExpanded = false },
                    )
                }
            }
        }
        Box(Modifier.weight(1f)) {
            ControlButton(sort.label) { sortExpanded = true }
            ReworkDropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                TripSort.entries.forEach { option ->
                    ReworkDropdownItem(
                        label = option.label,
                        selected = option == sort,
                        onClick = { onSortSelected(option); sortExpanded = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlButton(value: String, onClick: () -> Unit) {
    val colors = AtlasReworkTheme.colors
    Surface(
        shape = RoundedCornerShape(AtlasReworkTheme.dimensions.controlRadius),
        color = colors.surfaceStrong,
        border = BorderStroke(1.dp, colors.border),
        onClick = onClick,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 13.dp, end = 9.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                value.uppercase(),
                style = AtlasReworkTheme.typography.label,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(17.dp),
            )
        }
    }
}

@Composable
private fun GroupHeading(label: String) {
    Text(
        label,
        modifier = Modifier.padding(start = 18.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
        style = AtlasReworkTheme.typography.label,
        color = AtlasReworkTheme.colors.inkMuted,
    )
}

/** The tall card: a cover photo earns the trip a memory-page presentation. */
@Composable
private fun FullTripCard(item: TripListItemUiState, onClick: () -> Unit) {
    val colors = AtlasReworkTheme.colors
    val context = LocalContext.current
    val coverFile = remember(item.coverPhotoFilename) {
        item.coverPhotoFilename?.let { File(context.filesDir, "photos/$it") }?.takeIf { it.exists() }
    }

    ReworkFloatingCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable(onClick = onClick),
        contentPadding = PaddingValues(0.dp),
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(150.dp)) {
                if (coverFile != null) {
                    AsyncImage(
                        model = coverFile,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(Modifier.fillMaxSize().background(item.trip.id.coverGradient()))
                }
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.24f),
                            0.34f to Color.Transparent,
                            0.52f to Color.Black.copy(alpha = 0.10f),
                            0.72f to Color.Black.copy(alpha = 0.38f),
                            1f to Color.Black.copy(alpha = 0.72f),
                        ),
                    ),
                )
                StatusPill(item.trip.status, Modifier.align(Alignment.TopStart).padding(11.dp))
                Column(Modifier.align(Alignment.BottomStart).padding(start = 14.dp, end = 14.dp, bottom = 11.dp)) {
                    Text(
                        item.trip.title,
                        style = AtlasReworkTheme.typography.title,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    item.datePillText?.let {
                        Text(
                            it.uppercase(),
                            style = AtlasReworkTheme.typography.data,
                            color = Color.White.copy(alpha = 0.86f),
                        )
                    }
                }
            }

            Column(Modifier.padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 11.dp)) {
                if (item.routeNames.isNotEmpty()) {
                    RouteLine(item.routeNames)
                }
                if (item.sideTripNames.isNotEmpty()) {
                    Spacer(Modifier.height(5.dp))
                    SideTripLine(item.sideTripNames)
                }
                Spacer(Modifier.height(9.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    item.countryFlags.take(MAX_FLAGS).forEach { flag ->
                        Text(flag, style = AtlasReworkTheme.typography.title.copy(fontSize = 16.sp))
                        Spacer(Modifier.width(4.dp))
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        item.countriesLabel(),
                        style = AtlasReworkTheme.typography.label,
                        color = colors.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        item.metricsText(),
                        style = AtlasReworkTheme.typography.data,
                        color = colors.inkMuted,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/** The compact row: no cover photo, so the tall layout would be mostly empty. */
@Composable
private fun CompactTripCard(item: TripListItemUiState, onClick: () -> Unit) {
    val colors = AtlasReworkTheme.colors
    ReworkFloatingCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable(onClick = onClick),
        contentPadding = PaddingValues(start = 13.dp, end = 11.dp, top = 11.dp, bottom = 11.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                item.primaryFlag ?: "🌍",
                style = AtlasReworkTheme.typography.title.copy(fontSize = 19.sp),
                color = colors.inkMuted,
            )
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.trip.title,
                    style = AtlasReworkTheme.typography.title.copy(fontSize = 16.sp, lineHeight = 19.sp),
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    item.compactSubtitle(),
                    style = AtlasReworkTheme.typography.data,
                    color = colors.inkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(item.trip.status.statusColor()),
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.inkMuted,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun RouteLine(names: List<String>) {
    val colors = AtlasReworkTheme.colors
    val text = buildAnnotatedRoute(names)
    Text(
        text,
        style = AtlasReworkTheme.typography.data.copy(fontSize = 12.5.sp, lineHeight = 18.sp),
        color = colors.ink,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * Places visited from a main-route stop. The corner glyph carries the nesting, so no
 * grouping noun is needed — the user never meets the word "excursió".
 */
@Composable
private fun SideTripLine(names: List<String>) {
    val colors = AtlasReworkTheme.colors
    Row(Modifier.fillMaxWidth()) {
        Text(
            "└",
            style = AtlasReworkTheme.typography.data.copy(fontSize = 12.sp),
            color = colors.accent.copy(alpha = 0.55f),
        )
        Spacer(Modifier.width(7.dp))
        Text(
            names.joinToString(" · "),
            style = AtlasReworkTheme.typography.data.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
            color = colors.inkMuted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StatusPill(status: TravelStatus, modifier: Modifier = Modifier) {
    val label = status.pillLabel() ?: return
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = status.statusColor(),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = AtlasReworkTheme.typography.label,
            color = Color.White,
        )
    }
}

@Composable
private fun EmptyTripsCard(hasAnyTrip: Boolean, onCreateTrip: () -> Unit) {
    val colors = AtlasReworkTheme.colors
    ReworkFloatingCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (hasAnyTrip) {
                Text("Cap viatge amb aquest filtre", style = AtlasReworkTheme.typography.title, color = colors.ink)
                Text(
                    "Prova un altre estat per veure la resta del teu recorregut.",
                    style = AtlasReworkTheme.typography.body,
                    color = colors.inkMuted,
                )
            } else {
                Text("Comença el teu recorregut", style = AtlasReworkTheme.typography.title, color = colors.ink)
                Text(
                    "Registra el primer viatge i el teu atles començarà a créixer.",
                    style = AtlasReworkTheme.typography.body,
                    color = colors.inkMuted,
                )
                Spacer(Modifier.height(2.dp))
                Surface(
                    shape = RoundedCornerShape(11.dp),
                    color = colors.accent,
                    onClick = onCreateTrip,
                ) {
                    Text(
                        "NOU VIATGE",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = AtlasReworkTheme.typography.label,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun buildAnnotatedRoute(names: List<String>) =
    androidx.compose.ui.text.buildAnnotatedString {
        val arrowColor = AtlasReworkTheme.colors.accent
        names.forEachIndexed { index, name ->
            append(name)
            if (index != names.lastIndex) {
                pushStyle(androidx.compose.ui.text.SpanStyle(color = arrowColor))
                append("  →  ")
                pop()
            }
        }
    }

private fun List<TripListItemUiState>.sortedForDisplay(sort: TripSort): List<TripListItemUiState> =
    when (sort) {
        // The upstream list already arrives newest-first with undated trips last.
        TripSort.MostRecent -> this
        TripSort.Oldest -> asReversed()
        TripSort.Title -> sortedBy { it.trip.title.lowercase() }
    }

private const val MAX_FLAGS = 3

/** One country reads by name; several read as a count, so nothing is silently hidden. */
private fun TripListItemUiState.countriesLabel(): String = when (countryIso2s.size) {
    0 -> ""
    1 -> primaryCountryName?.uppercase() ?: countryIso2s.first()
    else -> "${countryIso2s.size} PAÏSOS"
}

private fun TripListItemUiState.metricsText(): String = buildList {
    if (mainStopCount > 0) add("$mainStopCount ${if (mainStopCount == 1) "PARADA" else "PARADES"}")
    if (sideTripCount > 0) add("$sideTripCount ${if (sideTripCount == 1) "SORTIDA" else "SORTIDES"}")
    dayCount?.let { add("$it ${if (it == 1) "DIA" else "DIES"}") }
}.joinToString(" · ")

private fun TripListItemUiState.compactSubtitle(): String = listOfNotNull(
    primaryCountryName?.uppercase() ?: routeNames.firstOrNull()?.uppercase(),
    datePillText?.uppercase() ?: "SENSE DATA",
).joinToString(" · ")

private fun TravelStatus.groupLabel(): String = when (this) {
    TravelStatus.IN_PROGRESS -> "EN CURS"
    TravelStatus.PLANNED -> "PLANEJATS"
    TravelStatus.COMPLETED -> "COMPLETATS"
    TravelStatus.UNKNOWN -> "SENSE ESTAT"
}

private fun TravelStatus.pillLabel(): String? = when (this) {
    TravelStatus.IN_PROGRESS -> "EN CURS"
    TravelStatus.PLANNED -> "PLANEJAT"
    TravelStatus.COMPLETED -> "COMPLETAT"
    TravelStatus.UNKNOWN -> null
}

@Composable
private fun TravelStatus.statusColor(): Color {
    val colors = AtlasReworkTheme.colors
    return when (this) {
        TravelStatus.IN_PROGRESS -> colors.living
        TravelStatus.PLANNED -> colors.planned
        TravelStatus.COMPLETED -> colors.visited
        TravelStatus.UNKNOWN -> colors.inkMuted
    }
}

/**
 * Deterministic cover for a trip with no photo, so a card never shows a grey hole and
 * the same trip always looks the same. Only reached when a trip has a photo that has
 * since gone missing from disk.
 */
private fun String.coverGradient(): Brush {
    val palettes = listOf(
        listOf(Color(0xFF2F6F62), Color(0xFF123F3A)),
        listOf(Color(0xFF8A5A2B), Color(0xFF482E12)),
        listOf(Color(0xFF33587E), Color(0xFF14283F)),
        listOf(Color(0xFF6B4A72), Color(0xFF2E1E33)),
    )
    val palette = palettes[(hashCode().mod(palettes.size))]
    return Brush.linearGradient(palette)
}
