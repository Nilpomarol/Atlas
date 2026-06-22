package com.atlas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.atlas.domain.model.StopPhoto
import com.atlas.ui.theme.AtlasAccentContainer
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurfaceSubtle
import java.io.File

@Composable
fun AtlasMemoryPhotoTile(
    photo: StopPhoto,
    locationName: String,
    isCover: Boolean,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .width(136.dp)
            .height(96.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AtlasSurfaceSubtle)
            .clickable(onClick = onClick),
    ) {
        SubcomposeAsyncImage(
            model = File(context.filesDir, "photos/${photo.filename}"),
            contentDescription = "Foto de $locationName",
            modifier = Modifier.fillMaxSize(),
        ) {
            when (painter.state) {
                is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent(
                    contentScale = ContentScale.Crop,
                )
                else -> MissingPhotoPlaceholder()
            }
        }

        // Printed-photo hairline edge, drawn over the image.
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(1.dp, AtlasOutline, RoundedCornerShape(12.dp)),
        )

        if (isCover) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(7.dp),
                shape = RoundedCornerShape(999.dp),
                color = AtlasAccentContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, AtlasOutline),
            ) {
                Text(
                    text = "PORTADA",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AtlasPrimary,
                )
            }
        }
    }
}

@Composable
private fun MissingPhotoPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AtlasSurfaceSubtle),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "FOTO",
            style = MaterialTheme.typography.labelSmall,
            color = AtlasOnSurfaceMuted,
        )
    }
}
