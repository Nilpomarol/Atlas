package com.atlas.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.atlas.domain.model.StopPhoto
import com.atlas.domain.model.StopType
import com.atlas.domain.usecase.photo.AddStopPhotosUseCase
import com.atlas.presentation.trip.PhotoViewerItemUiState
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasError
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasSurfaceSubtle
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopDetailModal(
    title: String,
    locationName: String,
    metaLine: String,
    notes: String?,
    photos: List<StopPhoto>,
    stopId: String,
    stopType: StopType,
    coverPhotoFilename: String?,
    onDismiss: () -> Unit,
    onEditStop: () -> Unit,
    onDeleteStop: () -> Unit,
    onAddPhotos: (stopId: String, stopType: StopType, uris: List<Uri>) -> Unit,
    onDeletePhoto: (StopPhoto) -> Unit,
    onSetCoverPhoto: (StopPhoto?) -> Unit,
    onRotatePhoto: ((StopPhoto) -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var viewerInitialIndex by remember { mutableIntStateOf(0) }
    var showViewer by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
    ) { uris ->
        if (uris.isNotEmpty()) onAddPhotos(stopId, stopType, uris)
    }

    val canAddMore = photos.size < AddStopPhotosUseCase.MAX_PHOTOS

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AtlasSurface,
        contentWindowInsets = { WindowInsets(0) },
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column {
            // ── Header ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasOnSurfaceStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (locationName != title) {
                        Text(
                            text = locationName,
                            style = MaterialTheme.typography.bodySmall,
                            color = AtlasOnSurfaceMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onEditStop, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Edit, "Edita", tint = AtlasOnSurfaceMuted, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDeleteStop, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Delete, "Elimina", tint = AtlasError, modifier = Modifier.size(18.dp))
                }
            }

            HorizontalDivider(color = AtlasOutline)

            // ── Info ──
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = metaLine,
                    style = MaterialTheme.typography.labelSmall,
                    color = AtlasOnSurfaceMuted,
                )
                if (!notes.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = AtlasOnSurfaceStrong,
                    )
                }
            }

            HorizontalDivider(color = AtlasOutline)

            // ── Photo grid ──
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                itemsIndexed(photos) { index, photo ->
                    PhotoGridCell(
                        photo = photo,
                        isCover = photo.filename == coverPhotoFilename,
                        onClick = {
                            viewerInitialIndex = index
                            showViewer = true
                        },
                    )
                }
                if (canAddMore) {
                    item(span = { GridItemSpan(1) }) {
                        AddPhotoCell(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                )
                            },
                        )
                    }
                }
                // Pad the bottom for nav bar
                item(span = { GridItemSpan(3) }) {
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }

    // ── Full-screen viewer ──
    if (showViewer && photos.isNotEmpty()) {
        val initialPhoto = photos[viewerInitialIndex.coerceAtMost(photos.lastIndex)]
        PhotoViewerDialog(
            items = photos.map { photo ->
                PhotoViewerItemUiState(
                    photo = photo,
                    stopId = stopId,
                    stopType = stopType,
                    title = title,
                    contextLabel = metaLine,
                    dateText = null,
                )
            },
            initialPhotoId = initialPhoto.id,
            coverPhotoFilename = coverPhotoFilename,
            onDismiss = { showViewer = false },
            onOpenSource = null,
            onDeletePhoto = onDeletePhoto,
            onRotatePhoto = onRotatePhoto,
            onSetCoverPhoto = onSetCoverPhoto,
        )
    }
}

@Composable
private fun PhotoGridCell(
    photo: StopPhoto,
    isCover: Boolean,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(AtlasSurfaceSubtle)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = File(context.filesDir, "photos/${photo.filename}"),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (isCover) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Portada",
                tint = AtlasPrimary,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .size(16.dp),
            )
        }
    }
}

@Composable
private fun AddPhotoCell(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(AtlasSurfaceSubtle)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Afegeix fotos",
            tint = AtlasOnSurfaceMuted,
            modifier = Modifier.size(28.dp),
        )
    }
}
