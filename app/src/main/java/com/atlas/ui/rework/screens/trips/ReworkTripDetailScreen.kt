package com.atlas.ui.rework.screens.trips

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.atlas.domain.model.StopPhoto
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.TripStop
import com.atlas.presentation.trip.TripDetailUiState
import com.atlas.presentation.trip.TripTimelineEntry
import com.atlas.ui.rework.components.ReworkDropdownDivider
import com.atlas.ui.rework.components.ReworkDropdownItem
import com.atlas.ui.rework.components.ReworkDropdownMenu
import com.atlas.ui.rework.components.ReworkFloatingCard
import com.atlas.ui.rework.foundation.AtlasReworkTheme
import java.io.File

@Composable
fun ReworkTripDetailScreen(
    state: TripDetailUiState,
    onBack: () -> Unit,
    onStopOpened: (String) -> Unit,
    onAddStop: () -> Unit,
    onAddSideTrip: (String) -> Unit,
    onDeleteStop: (TripStop) -> Unit,
    onStoryOpened: () -> Unit,
) {
    val colors = AtlasReworkTheme.colors
    val trip = state.trip

    if (trip == null) {
        Box(Modifier.fillMaxSize().background(colors.surface), contentAlignment = Alignment.Center) {
            ReworkFloatingCard(Modifier.padding(28.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Aquest viatge ja no hi és", style = AtlasReworkTheme.typography.title, color = colors.ink)
                    Text(
                        "Potser s'ha esborrat des d'un altre lloc.",
                        style = AtlasReworkTheme.typography.body,
                        color = colors.inkMuted,
                    )
                    BackChip(onBack)
                }
            }
        }
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.surface)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 148.dp),
    ) {
        TripHero(state = state, onBack = onBack)

        SectionLabel("LA RUTA")
        if (state.timeline.isNotEmpty()) {
            RouteCard(
                timeline = state.timeline,
                onStopOpened = onStopOpened,
                onAddSideTrip = onAddSideTrip,
                onDeleteStop = onDeleteStop,
                onAddStop = onAddStop,
            )
        } else {
            ReworkFloatingCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    Text(
                        "Encara no hi ha cap parada. Afegeix el primer lloc del viatge.",
                        style = AtlasReworkTheme.typography.body,
                        color = colors.inkMuted,
                    )
                    AddAction("Afegeix una parada", onAddStop)
                }
            }
        }

        if (state.photoGallery.photoCount > 0) {
            SectionLabel("ELS TEUS RECORDS")
            PhotoStrip(state)
        }

        ContextBand(state)

        trip.notes?.takeIf { it.isNotBlank() }?.let { notes ->
            SectionLabel("NOTES")
            Text(
                notes,
                modifier = Modifier.padding(horizontal = 18.dp),
                style = AtlasReworkTheme.typography.body,
                color = colors.ink,
            )
        }

        if (state.photoGallery.photoCount > 0 || state.timeline.size > 1) {
            Spacer(Modifier.height(18.dp))
            StoryButton(onStoryOpened)
        }
    }
}

@Composable
private fun TripHero(state: TripDetailUiState, onBack: () -> Unit) {
    val colors = AtlasReworkTheme.colors
    val trip = state.trip ?: return
    val context = LocalContext.current
    val cover = remember(trip.coverPhotoFilename) {
        trip.coverPhotoFilename
            ?.let { File(context.filesDir, "photos/$it") }
            ?.takeIf { it.exists() }
    }
    // A cover earns height; without one the hero collapses onto paper rather than
    // stretching an empty gradient, matching the Trips list rule.
    val heroHeight = if (cover != null) (LocalConfiguration.current.screenHeightDp * 0.30f).dp else 0.dp

    if (cover == null) {
        Column(Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BackChip(onBack)
                Spacer(Modifier.weight(1f))
                StatusPill(trip.status)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                trip.title,
                style = AtlasReworkTheme.typography.display,
                color = colors.ink,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            state.datePillText()?.let {
                Text(it.uppercase(), style = AtlasReworkTheme.typography.data, color = colors.inkMuted)
            }
        }
        return
    }

    Box(Modifier.fillMaxWidth().height(heroHeight)) {
        AsyncImage(
            model = cover,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 0.34f),
                    0.36f to Color.Transparent,
                    0.62f to Color.Black.copy(alpha = 0.26f),
                    1f to Color.Black.copy(alpha = 0.78f),
                ),
            ),
        )
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackChip(onBack)
            Spacer(Modifier.weight(1f))
            StatusPill(trip.status)
        }
        Column(Modifier.align(Alignment.BottomStart).padding(start = 16.dp, end = 16.dp, bottom = 14.dp)) {
            Text(
                trip.title,
                style = AtlasReworkTheme.typography.display,
                color = Color.White,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            state.datePillText()?.let {
                Text(
                    it.uppercase(),
                    style = AtlasReworkTheme.typography.data,
                    color = Color.White.copy(alpha = 0.88f),
                )
            }
        }
    }
}

/**
 * The spine of the page: places and flight legs in one sequence. Legs are drawn as a
 * different kind of row — no number, no edit affordance — because they are derived from
 * flights rather than edited here.
 */
@Composable
private fun RouteCard(
    timeline: List<TripTimelineEntry>,
    onStopOpened: (String) -> Unit,
    onAddSideTrip: (String) -> Unit,
    onDeleteStop: (TripStop) -> Unit,
    onAddStop: () -> Unit,
) {
    ReworkFloatingCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(start = 14.dp, end = 13.dp, top = 14.dp, bottom = 13.dp),
    ) {
        Column {
            timeline.forEachIndexed { index, entry ->
                val isLast = index == timeline.lastIndex
                when (entry) {
                    is TripTimelineEntry.Place -> PlaceRow(
                        entry = entry,
                        isLast = isLast,
                        onStopOpened = onStopOpened,
                        onAddSideTrip = onAddSideTrip,
                        onDeleteStop = onDeleteStop,
                    )
                    is TripTimelineEntry.Leg -> LegRow(entry, isLast)
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(AtlasReworkTheme.colors.border))
            Spacer(Modifier.height(11.dp))
            AddAction("Afegeix una parada", onAddStop)
        }
    }
}

@Composable
private fun AddAction(label: String, onClick: () -> Unit) {
    val colors = AtlasReworkTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border),
        onClick = onClick,
    ) {
        Row(
            Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, tint = colors.accent, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(7.dp))
            Text(label, style = AtlasReworkTheme.typography.label, color = colors.ink)
        }
    }
}

@Composable
private fun PlaceRow(
    entry: TripTimelineEntry.Place,
    isLast: Boolean,
    onStopOpened: (String) -> Unit,
    onAddSideTrip: (String) -> Unit,
    onDeleteStop: (TripStop) -> Unit,
) {
    val colors = AtlasReworkTheme.colors
    var menuExpanded by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf(false) }

    if (pendingDelete) {
        DeleteStopDialog(
            stop = entry.stop,
            sideTripCount = entry.sideTrips.size,
            onDismiss = { pendingDelete = false },
            onConfirm = { pendingDelete = false; onDeleteStop(entry.stop) },
        )
    }

    Row(Modifier.fillMaxWidth().clickable { onStopOpened(entry.stop.id) }) {
        Rail(isLast = isLast) {
            Box(
                Modifier.size(24.dp).clip(CircleShape).background(colors.visited),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    entry.number.toString(),
                    style = AtlasReworkTheme.typography.data.copy(fontSize = 11.sp),
                    color = Color.White,
                )
            }
        }
        Column(Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else 14.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    entry.stop.displayName(),
                    style = AtlasReworkTheme.typography.title.copy(fontSize = 16.sp, lineHeight = 19.sp),
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                entry.dateText?.let {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        it.uppercase(),
                        style = AtlasReworkTheme.typography.data.copy(fontSize = 10.5.sp),
                        color = colors.inkMuted,
                        maxLines = 1,
                    )
                }
                Box {
                    Icon(
                        Icons.Rounded.MoreVert,
                        contentDescription = "Accions de la parada",
                        tint = colors.inkMuted,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(19.dp)
                            .clickable { menuExpanded = true },
                    )
                    ReworkDropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        ReworkDropdownItem(
                            label = "Edita la parada",
                            onClick = { menuExpanded = false; onStopOpened(entry.stop.id) },
                        )
                        ReworkDropdownItem(
                            label = SIDE_TRIP_ACTION,
                            onClick = { menuExpanded = false; onAddSideTrip(entry.stop.id) },
                        )
                        ReworkDropdownDivider()
                        ReworkDropdownItem(
                            label = "Elimina la parada",
                            onClick = { menuExpanded = false; pendingDelete = true },
                        )
                    }
                }
            }
            if (entry.sideTrips.isNotEmpty()) {
                Spacer(Modifier.height(5.dp))
                Row {
                    Text(
                        "└",
                        style = AtlasReworkTheme.typography.data.copy(fontSize = 12.sp),
                        color = colors.accent.copy(alpha = 0.55f),
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        entry.sideTrips.joinToString(" · ") { it.displayName() },
                        style = AtlasReworkTheme.typography.data.copy(fontSize = 11.sp, lineHeight = 16.sp),
                        color = colors.inkMuted,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** The cascade is real: deleting a stop removes its nested places and their photos. */
@Composable
private fun DeleteStopDialog(
    stop: TripStop,
    sideTripCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val colors = AtlasReworkTheme.colors
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = colors.surfaceStrong) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Vols eliminar " + stop.displayName() + "?",
                    style = AtlasReworkTheme.typography.title,
                    color = colors.ink,
                )
                Text(
                    if (sideTripCount > 0) {
                        "També s'eliminaran " + sideTripCount + " " +
                            (if (sideTripCount == 1) "sortida" else "sortides") +
                            " i les seves fotos. Aquesta acció no es pot desfer."
                    } else {
                        "També s'eliminaran les seves fotos. Aquesta acció no es pot desfer."
                    },
                    style = AtlasReworkTheme.typography.body,
                    color = colors.inkMuted,
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(11.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border),
                        onClick = onDismiss,
                    ) {
                        Text(
                            CANCEL_LABEL,
                            Modifier.padding(vertical = 11.dp),
                            style = AtlasReworkTheme.typography.label,
                            color = colors.ink,
                        )
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(11.dp),
                        color = colors.living,
                        onClick = onConfirm,
                    ) {
                        Text(
                            "Elimina",
                            Modifier.padding(vertical = 11.dp),
                            style = AtlasReworkTheme.typography.label,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegRow(entry: TripTimelineEntry.Leg, isLast: Boolean) {
    val colors = AtlasReworkTheme.colors
    Row(Modifier.fillMaxWidth()) {
        Rail(isLast = isLast) {
            Box(
                Modifier
                    .size(19.dp)
                    .clip(CircleShape)
                    .background(colors.surface)
                    .padding(1.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Flight,
                    contentDescription = null,
                    tint = colors.inkMuted,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
        Row(
            Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 14.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(colors.ink.copy(alpha = 0.045f))
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("VOL", style = AtlasReworkTheme.typography.label, color = colors.inkMuted)
                Text(
                    entry.title,
                    style = AtlasReworkTheme.typography.data.copy(fontSize = 12.sp),
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            entry.dateText?.let {
                Spacer(Modifier.width(8.dp))
                Text(
                    it.uppercase(),
                    style = AtlasReworkTheme.typography.data.copy(fontSize = 10.5.sp),
                    color = colors.inkMuted,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Left gutter holding the marker and the connecting line. */
@Composable
private fun Rail(isLast: Boolean, marker: @Composable () -> Unit) {
    val colors = AtlasReworkTheme.colors
    Column(
        modifier = Modifier.width(34.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        marker()
        if (!isLast) {
            Box(
                Modifier
                    .padding(start = 11.dp)
                    .width(1.5.dp)
                    .height(34.dp)
                    .background(colors.border),
            )
        }
    }
}

@Composable
private fun PhotoStrip(state: TripDetailUiState) {
    val colors = AtlasReworkTheme.colors
    val context = LocalContext.current
    val photos: List<StopPhoto> = remember(state.photoGallery) {
        state.photoGallery.groups.flatMap { it.photos }.take(12)
    }
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        photos.forEach { photo ->
            AsyncImage(
                model = File(context.filesDir, "photos/${photo.filename}"),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(colors.surfaceStrong),
            )
        }
    }
}

@Composable
private fun ContextBand(state: TripDetailUiState) {
    val colors = AtlasReworkTheme.colors
    val metrics = state.metricsText()
    if (metrics.isBlank()) return
    Row(
        Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        state.flags().take(3).forEach { flag ->
            Text(flag, style = AtlasReworkTheme.typography.title.copy(fontSize = 15.sp))
            Spacer(Modifier.width(4.dp))
        }
        Spacer(Modifier.weight(1f))
        Text(metrics, style = AtlasReworkTheme.typography.data, color = colors.inkMuted, maxLines = 1)
    }
}

@Composable
private fun StoryButton(onClick: () -> Unit) {
    val colors = AtlasReworkTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = colors.ink,
        onClick = onClick,
    ) {
        Row(
            Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(7.dp))
            Text(
                "Veure el relat",
                style = AtlasReworkTheme.typography.body.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        modifier = Modifier.padding(start = 18.dp, end = 16.dp, top = 20.dp, bottom = 9.dp),
        style = AtlasReworkTheme.typography.label,
        color = AtlasReworkTheme.colors.accent,
    )
}

@Composable
private fun BackChip(onBack: () -> Unit) {
    val colors = AtlasReworkTheme.colors
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = colors.surfaceStrong,
        onClick = onBack,
        shadowElevation = 5.dp,
    ) {
        Icon(
            Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Torna",
            modifier = Modifier.padding(8.dp).size(18.dp),
            tint = colors.ink,
        )
    }
}

@Composable
private fun StatusPill(status: TravelStatus) {
    val label = when (status) {
        TravelStatus.IN_PROGRESS -> "EN CURS"
        TravelStatus.PLANNED -> "PLANEJAT"
        TravelStatus.COMPLETED -> "COMPLETAT"
        TravelStatus.UNKNOWN -> return
    }
    val colors = AtlasReworkTheme.colors
    val color = when (status) {
        TravelStatus.IN_PROGRESS -> colors.living
        TravelStatus.PLANNED -> colors.planned
        else -> colors.visited
    }
    Surface(shape = RoundedCornerShape(50), color = color) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = AtlasReworkTheme.typography.label,
            color = Color.White,
        )
    }
}

private fun TripStop.displayName(): String =
    displayTitle?.takeIf { it.isNotBlank() } ?: locationName

private fun TripDetailUiState.datePillText(): String? =
    trip?.dateRange?.let { com.atlas.domain.service.FlexibleDateFormatter().formatTripPill(it) }

private fun TripDetailUiState.flags(): List<String> {
    val iso2s = stops.map { it.countryIso2 }.filter { it.isNotBlank() }.distinct()
    return iso2s.mapNotNull { iso2 ->
        countries.firstOrNull { it.iso2 == iso2 }?.flagEmoji?.takeIf(String::isNotBlank)
    }
}

private fun TripDetailUiState.metricsText(): String {
    val mainStops = stops.count { it.parentStopId == null && it.source != com.atlas.domain.model.TripStopSource.ITINERARY_GROUP }
    val sideTrips = stops.count { it.parentStopId != null }
    return buildList {
        if (mainStops > 0) add("$mainStops ${if (mainStops == 1) "PARADA" else "PARADES"}")
        if (sideTrips > 0) add("$sideTrips ${if (sideTrips == 1) "SORTIDA" else "SORTIDES"}")
    }.joinToString(" · ")
}

private const val SIDE_TRIP_ACTION = "Afegeix una sortida des d'aquí"
private const val CANCEL_LABEL = "Cancel·la"
