package com.atlas.ui.screens.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.StopPhoto
import com.atlas.presentation.trip.TripPhotoGalleryUiState
import com.atlas.presentation.trip.TripPhotoGroupUiState
import com.atlas.ui.components.AtlasCard
import com.atlas.ui.components.AtlasMemoryRecordCard
import com.atlas.ui.components.AtlasMemoryTile
import com.atlas.ui.components.AtlasSectionTitle
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurfaceSubtle

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
    // One location per group, so the photos share the group title — no per-tile caption.
    AtlasMemoryRecordCard(
        eyebrow = group.contextLabel,
        title = group.title,
        dateText = group.dateText,
        countLabel = "${group.photos.size} ${if (group.photos.size == 1) "FOTO" else "FOTOS"}",
        tiles = group.photos.map { photo ->
            AtlasMemoryTile(
                photo = photo,
                caption = null,
                isCover = photo.filename == coverPhotoFilename,
                onClick = { onPhotoClick(photo) },
            )
        },
        onHeaderClick = onClick,
    )
}
