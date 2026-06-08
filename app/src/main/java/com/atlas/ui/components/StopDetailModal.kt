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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.atlas.domain.model.StopPhoto
import com.atlas.domain.model.StopType
import com.atlas.domain.usecase.photo.AddStopPhotosUseCase
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasError
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasSurfaceSubtle
import kotlinx.coroutines.launch
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
    onDismiss: () -> Unit,
    onEditStop: () -> Unit,
    onDeleteStop: () -> Unit,
    onAddPhotos: (stopId: String, stopType: StopType, uris: List<Uri>) -> Unit,
    onDeletePhoto: (StopPhoto) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var viewerInitialIndex by remember { mutableIntStateOf(0) }
    var showViewer by remember { mutableStateOf(false) }
    var pendingDeletePhoto by remember { mutableStateOf<StopPhoto?>(null) }

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

    // ── Delete photo confirmation ──
    if (pendingDeletePhoto != null) {
        AlertDialog(
            onDismissRequest = { pendingDeletePhoto = null },
            title = { Text("Elimina la foto") },
            text = { Text("Segur que vols eliminar aquesta foto?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeletePhoto(pendingDeletePhoto!!)
                    pendingDeletePhoto = null
                }) { Text("Elimina", color = AtlasError) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletePhoto = null }) { Text("Cancel·la") }
            },
        )
    }

    // ── Full-screen viewer ──
    if (showViewer && photos.isNotEmpty()) {
        StopPhotoViewer(
            photos = photos,
            initialIndex = viewerInitialIndex.coerceAtMost(photos.lastIndex),
            onDismiss = { showViewer = false },
            onDeletePhoto = { photo ->
                pendingDeletePhoto = photo
                showViewer = false
            },
        )
    }
}

@Composable
private fun PhotoGridCell(
    photo: StopPhoto,
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

@Composable
fun StopPhotoViewer(
    photos: List<StopPhoto>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    onDeletePhoto: (StopPhoto) -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { photos.size }
    val context = LocalContext.current

    // Auto-dismiss if all photos deleted
    LaunchedEffect(photos.size) {
        if (photos.isEmpty()) onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val photo = photos.getOrNull(page) ?: return@HorizontalPager
                AsyncImage(
                    model = File(context.filesDir, "photos/${photo.filename}"),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 40.dp)
                    .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.5f),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Tanca",
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp).size(20.dp),
                    )
                }

                val currentPhoto = photos.getOrNull(pagerState.currentPage)
                if (currentPhoto != null) {
                    Surface(
                        onClick = { onDeletePhoto(currentPhoto) },
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.5f),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Elimina foto",
                            tint = Color.White,
                            modifier = Modifier.padding(10.dp).size(20.dp),
                        )
                    }
                }
            }

            // Page indicator
            if (photos.size > 1) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${photos.size}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}
