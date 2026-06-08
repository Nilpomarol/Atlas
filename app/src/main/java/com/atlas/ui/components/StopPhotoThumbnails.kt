package com.atlas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.atlas.domain.model.StopPhoto
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasSurfaceSubtle
import java.io.File

@Composable
fun StopPhotoThumbnails(
    photos: List<StopPhoto>,
    photoCount: Int,
    size: Dp = 46.dp,
    modifier: Modifier = Modifier,
) {
    val preview = photos.take(4)
    val overflow = (photoCount - 4).coerceAtLeast(0)

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(AtlasSurfaceSubtle),
    ) {
        if (preview.size == 1) {
            PhotoCell(
                photo = preview[0],
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            val half = size / 2
            Column {
                Row {
                    PhotoCell(photo = preview[0], modifier = Modifier.size(half))
                    PhotoCell(photo = preview.getOrNull(1), modifier = Modifier.size(half))
                }
                Row {
                    PhotoCell(photo = preview.getOrNull(2), modifier = Modifier.size(half))
                    Box(modifier = Modifier.size(half)) {
                        PhotoCell(photo = preview.getOrNull(3), modifier = Modifier.fillMaxSize())
                        if (overflow > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.52f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "+$overflow",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoCell(
    photo: StopPhoto?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Box(
        modifier = modifier.background(AtlasSurfaceSubtle),
        contentAlignment = Alignment.Center,
    ) {
        if (photo != null) {
            AsyncImage(
                model = File(context.filesDir, "photos/${photo.filename}"),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
