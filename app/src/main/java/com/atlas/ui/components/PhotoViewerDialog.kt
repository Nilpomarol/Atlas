package com.atlas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atlas.domain.model.StopPhoto
import com.atlas.presentation.trip.PhotoViewerItemUiState
import com.atlas.ui.theme.AtlasError
import com.atlas.ui.theme.AtlasPrimary
import java.io.File
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage

@Composable
fun PhotoViewerDialog(
    items: List<PhotoViewerItemUiState>,
    initialPhotoId: String,
    coverPhotoFilename: String?,
    onDismiss: () -> Unit,
    onOpenSource: ((PhotoViewerItemUiState) -> Unit)?,
    onDeletePhoto: (StopPhoto) -> Unit,
    onSetCoverPhoto: (StopPhoto?) -> Unit,
) {
    if (items.isEmpty()) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }

    val initialPage = remember(initialPhotoId, items) {
        items.indexOfFirst { it.photo.id == initialPhotoId }.coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(initialPage = initialPage) { items.size }
    var pendingDelete by remember { mutableStateOf<PhotoViewerItemUiState?>(null) }

    LaunchedEffect(items.size) {
        if (pagerState.currentPage > items.lastIndex) {
            pagerState.scrollToPage(items.lastIndex)
        }
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
                key = { page -> items[page].photo.id },
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val item = items.getOrNull(page) ?: return@HorizontalPager
                ViewerPhoto(item)
            }

            val currentIndex = pagerState.currentPage.coerceAtMost(items.lastIndex)
            val currentItem = items.getOrNull(currentIndex)

            ViewerTopBar(
                item = currentItem,
                coverPhotoFilename = coverPhotoFilename,
                onDismiss = onDismiss,
                onDelete = { currentItem?.let { pendingDelete = it } },
                onSetCoverPhoto = onSetCoverPhoto,
            )

            currentItem?.let { item ->
                ViewerContextBar(
                    item = item,
                    position = currentIndex + 1,
                    total = items.size,
                    onOpenSource = onOpenSource?.let { callback -> { callback(item) } },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Elimina la foto") },
            text = { Text("Segur que vols eliminar aquesta foto?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDelete = null
                        onDeletePhoto(item.photo)
                    },
                ) {
                    Text("Elimina", color = AtlasError)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Cancel·la")
                }
            },
        )
    }
}

@Composable
private fun ViewerPhoto(item: PhotoViewerItemUiState) {
    val context = LocalContext.current
    val file = remember(item.photo.filename) {
        File(context.filesDir, "photos/${item.photo.filename}")
    }

    if (file.exists()) {
        ZoomableAsyncImage(
            model = file,
            contentDescription = "Foto de ${item.title}",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No s'ha pogut carregar la foto",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.72f),
            )
        }
    }
}

@Composable
private fun ViewerTopBar(
    item: PhotoViewerItemUiState?,
    coverPhotoFilename: String?,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onSetCoverPhoto: (StopPhoto?) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        ViewerIconButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Tanca",
                tint = Color.White,
            )
        }

        item?.let { current ->
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 108.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = current.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = current.contextLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.68f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            val isCover = current.photo.filename == coverPhotoFilename
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ViewerIconButton(
                    onClick = {
                        onSetCoverPhoto(if (isCover) null else current.photo)
                    },
                ) {
                    Icon(
                        imageVector = if (isCover) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = if (isCover) "Treu portada" else "Fes portada",
                        tint = if (isCover) AtlasPrimary else Color.White,
                    )
                }
                ViewerIconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Elimina foto",
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewerContextBar(
    item: PhotoViewerItemUiState,
    position: Int,
    total: Int,
    onOpenSource: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.64f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "$position / $total",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.68f),
                )
                item.dateText?.let { date ->
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            onOpenSource?.let { openSource ->
                ViewerIconButton(onClick = openSource) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = "Obre la parada",
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewerIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.52f),
    ) {
        Box(
            modifier = Modifier.size(42.dp),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}
