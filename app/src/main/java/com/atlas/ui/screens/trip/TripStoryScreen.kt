package com.atlas.ui.screens.trip

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.presentation.trip.PhotoViewerItemUiState
import com.atlas.presentation.trip.TripStoryExcursionStopUiState
import com.atlas.presentation.trip.TripStoryExcursionUiState
import com.atlas.presentation.trip.TripStoryStopUiState
import com.atlas.presentation.trip.TripStoryUiState
import com.atlas.ui.components.AtlasCard
import com.atlas.ui.components.AtlasMemoryPhotoTile
import com.atlas.ui.components.AtlasSectionTitle
import com.atlas.ui.components.PhotoViewerDialog
import com.atlas.ui.theme.AtlasAccentContainer
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasSurfaceSubtle

@Composable
fun TripStoryScreen(
    uiState: TripStoryUiState,
    onBackClick: () -> Unit,
) {
    var selectedPhotoId by rememberSaveable { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AtlasBackground),
    ) {
        if (uiState.trip == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Carregant el relat...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AtlasOnSurfaceMuted,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(72.dp))
                StoryHero(uiState = uiState)
                StoryRouteCard(uiState = uiState)
                uiState.itinerary?.let { itinerary ->
                    StoryInfoCard(
                        eyebrow = "ITINERARI",
                        title = itinerary.title,
                        body = itinerary.groupSummary ?: itinerary.notes,
                    )
                }
                if (uiState.stops.isNotEmpty()) {
                    AtlasSectionTitle(title = "Parades")
                    uiState.stops.forEach { stop ->
                        StoryStopCard(
                            stop = stop,
                            onPhotoClick = { selectedPhotoId = it.photo.id },
                        )
                    }
                }
                if (uiState.unanchoredExcursions.isNotEmpty()) {
                    AtlasSectionTitle(title = "Excursions")
                    uiState.unanchoredExcursions.forEach { excursion ->
                        StoryExcursionCard(
                            excursion = excursion,
                            onPhotoClick = { selectedPhotoId = it.photo.id },
                        )
                    }
                }
                StorySummaryCard(uiState = uiState)
                Spacer(Modifier.height(20.dp))
            }
        }

        StoryBackButton(onBackClick = onBackClick)
    }

    selectedPhotoId?.let { photoId ->
        PhotoViewerDialog(
            items = uiState.viewerItems,
            initialPhotoId = photoId,
            coverPhotoFilename = uiState.trip?.coverPhotoFilename,
            onDismiss = { selectedPhotoId = null },
        )
    }
}

@Composable
private fun StoryBackButton(onBackClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .statusBarsPadding()
            .padding(start = 16.dp, top = 12.dp)
            .size(42.dp),
        shape = CircleShape,
        color = AtlasSurface.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Torna",
                tint = AtlasOnSurfaceStrong,
            )
        }
    }
}

@Composable
private fun StoryHero(uiState: TripStoryUiState) {
    val trip = uiState.trip ?: return
    AtlasCard(
        color = AtlasSurface,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "RELAT DEL VIATGE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AtlasPrimary,
                )
                Text(
                    text = trip.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp, lineHeight = 31.sp),
                    fontWeight = FontWeight.Medium,
                    color = AtlasOnSurfaceStrong,
                )
                uiState.dateText?.let { date ->
                    Text(
                        text = date.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasOnSurfaceMuted,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                StoryStat(value = uiState.dayCountText, label = "DIES")
                StoryStat(value = uiState.stopCount.toString(), label = "PARADES")
                StoryStat(value = uiState.countryNames.size.toString(), label = "PAÏSOS")
                StoryStat(value = uiState.photoCount.toString(), label = "FOTOS")
            }
        }
    }
}

@Composable
private fun StoryRouteCard(uiState: TripStoryUiState) {
    AtlasCard(
        color = AtlasSurfaceSubtle,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "RUTA",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AtlasPrimary,
            )
            Text(
                text = uiState.routeText ?: "Encara no hi ha parades en aquest viatge.",
                style = MaterialTheme.typography.titleMedium,
                color = AtlasOnSurfaceStrong,
            )
            if (uiState.countryNames.isNotEmpty()) {
                Text(
                    text = uiState.countryNames.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = AtlasOnSurfaceMuted,
                )
            }
        }
    }
}

@Composable
private fun StoryInfoCard(
    eyebrow: String,
    title: String,
    body: String?,
) {
    AtlasCard(
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = eyebrow,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AtlasPrimary,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = AtlasOnSurfaceStrong,
            )
            body?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = AtlasOnSurfaceMuted,
                )
            }
        }
    }
}

@Composable
private fun StoryStopCard(
    stop: TripStoryStopUiState,
    onPhotoClick: (PhotoViewerItemUiState) -> Unit,
) {
    AtlasCard(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            StoryPlaceHeader(
                eyebrow = "PARADA",
                title = stop.title,
                contextText = stop.contextText,
                notes = stop.notes,
            )
            StoryPhotoRow(
                photos = stop.photos,
                onPhotoClick = onPhotoClick,
            )
            stop.excursions.forEach { excursion ->
                StoryExcursionCard(
                    excursion = excursion,
                    onPhotoClick = onPhotoClick,
                    nested = true,
                )
            }
        }
    }
}

@Composable
private fun StoryExcursionCard(
    excursion: TripStoryExcursionUiState,
    onPhotoClick: (PhotoViewerItemUiState) -> Unit,
    nested: Boolean = false,
) {
    AtlasCard(
        color = if (nested) AtlasSurfaceSubtle else AtlasSurface,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            StoryPlaceHeader(
                eyebrow = "EXCURSIÓ",
                title = excursion.title,
                contextText = null,
                notes = excursion.notes,
            )
            excursion.stops.forEach { stop ->
                StoryExcursionStopBlock(
                    stop = stop,
                    onPhotoClick = onPhotoClick,
                )
            }
        }
    }
}

@Composable
private fun StoryExcursionStopBlock(
    stop: TripStoryExcursionStopUiState,
    onPhotoClick: (PhotoViewerItemUiState) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        StoryPlaceHeader(
            eyebrow = null,
            title = stop.title,
            contextText = stop.contextText,
            notes = stop.notes,
        )
        StoryPhotoRow(
            photos = stop.photos,
            onPhotoClick = onPhotoClick,
        )
    }
}

@Composable
private fun StoryPlaceHeader(
    eyebrow: String?,
    title: String,
    contextText: String?,
    notes: String?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        eyebrow?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AtlasPrimary,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = AtlasOnSurfaceStrong,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        contextText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = AtlasOnSurfaceMuted,
            )
        }
        notes?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = AtlasOnSurfaceMuted,
            )
        }
    }
}

@Composable
private fun StoryPhotoRow(
    photos: List<PhotoViewerItemUiState>,
    onPhotoClick: (PhotoViewerItemUiState) -> Unit,
) {
    if (photos.isEmpty()) return

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(
            items = photos,
            key = { it.photo.id },
        ) { item ->
            Box(modifier = Modifier.width(136.dp)) {
                AtlasMemoryPhotoTile(
                    photo = item.photo,
                    locationName = item.title,
                    isCover = false,
                    onClick = { onPhotoClick(item) },
                )
            }
        }
    }
}

@Composable
private fun StorySummaryCard(uiState: TripStoryUiState) {
    AtlasCard(
        color = AtlasAccentContainer,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "RESUM",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AtlasPrimary,
            )
            Text(
                text = "${uiState.stopCount} parades · ${uiState.countryNames.size} països · ${uiState.photoCount} fotos",
                style = MaterialTheme.typography.titleMedium,
                color = AtlasOnSurfaceStrong,
            )
            Text(
                text = "Relat generat automàticament amb les dades actuals del viatge.",
                style = MaterialTheme.typography.bodySmall,
                color = AtlasOnSurfaceMuted,
            )
        }
    }
}

@Composable
private fun StoryStat(value: String, label: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            color = AtlasOnSurfaceStrong,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AtlasOnSurfaceMuted,
        )
    }
}
