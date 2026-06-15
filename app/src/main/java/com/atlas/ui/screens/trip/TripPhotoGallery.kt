package com.atlas.ui.screens.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.atlas.domain.model.StopPhoto
import com.atlas.presentation.trip.TripPhotoGalleryUiState
import com.atlas.presentation.trip.TripPhotoGroupUiState
import com.atlas.ui.components.AtlasCard
import com.atlas.ui.components.AtlasSectionTitle
import com.atlas.ui.theme.AtlasAccentContainer
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurfaceSubtle
import java.io.File

@Composable
internal fun TripPhotoGallerySection(
    gallery: TripPhotoGalleryUiState,
    coverPhotoFilename: String?,
    onGroupClick: (TripPhotoGroupUiState) -> Unit,
    onPhotoClick: (StopPhoto) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AtlasSectionTitle(
            title = "Records",
            action = {
                if (gallery.photoCount > 0) {
                    Text(
                        text = "${gallery.photoCount} ${if (gallery.photoCount == 1) "FOTO" else "FOTOS"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AtlasOnSurfaceMuted,
                    )
                }
            },
        )

        if (gallery.groups.isEmpty()) {
            TripPhotoGalleryEmptyState()
        } else {
            gallery.groups.forEach { group ->
                TripPhotoGroupCard(
                    group = group,
                    coverPhotoFilename = coverPhotoFilename,
                    onClick = { onGroupClick(group) },
                    onPhotoClick = onPhotoClick,
                )
            }
        }
    }
}

@Composable
private fun TripPhotoGalleryEmptyState() {
    AtlasCard(
        color = AtlasSurfaceSubtle,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 20.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "EL VIATGE EN IMATGES",
                style = MaterialTheme.typography.labelSmall,
                color = AtlasPrimary,
            )
            Text(
                text = "Encara no hi ha records fotogràfics",
                style = MaterialTheme.typography.titleMedium,
                color = AtlasOnSurfaceStrong,
            )
            Text(
                text = "Afegeix fotos des de qualsevol parada i apareixeran aquí en l'ordre del viatge.",
                style = MaterialTheme.typography.bodySmall,
                color = AtlasOnSurfaceMuted,
            )
        }
    }
}

@Composable
private fun TripPhotoGroupCard(
    group: TripPhotoGroupUiState,
    coverPhotoFilename: String?,
    onClick: () -> Unit,
    onPhotoClick: (StopPhoto) -> Unit,
) {
    AtlasCard(
        contentPadding = PaddingValues(0.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = group.contextLabel,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                            color = AtlasPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val metadata = listOfNotNull(group.dateText, group.countryIso2)
                            .joinToString(" · ")
                        if (metadata.isNotEmpty()) {
                            Text(
                                text = metadata,
                                style = MaterialTheme.typography.labelSmall,
                                color = AtlasOnSurfaceMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Text(
                        text = group.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = AtlasOnSurfaceStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = group.photos.size.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.labelMedium,
                    color = AtlasOnSurfaceMuted,
                    modifier = Modifier.padding(top = 1.dp),
                )
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(end = 10.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(
                    items = group.photos,
                    key = StopPhoto::id,
                ) { photo ->
                    TripPhotoTile(
                        photo = photo,
                        locationName = group.title,
                        isCover = photo.filename == coverPhotoFilename,
                        onClick = { onPhotoClick(photo) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TripPhotoTile(
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
