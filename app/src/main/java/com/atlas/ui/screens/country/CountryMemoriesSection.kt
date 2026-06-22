package com.atlas.ui.screens.country

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.atlas.presentation.country.CountryMemoriesUiState
import com.atlas.presentation.country.CountryMemoryTripUiState
import com.atlas.presentation.trip.PhotoViewerItemUiState
import com.atlas.ui.components.AtlasMemoryRecordCard
import com.atlas.ui.components.AtlasMemoryTile
import com.atlas.ui.components.AtlasSectionTitle
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasPrimary

@Composable
internal fun CountryMemoriesSection(
    memories: CountryMemoriesUiState,
    isVisible: Boolean,
    onTripClick: (String) -> Unit,
    onPhotoClick: (PhotoViewerItemUiState) -> Unit,
    onVisibleChanged: (Boolean) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AtlasSectionTitle(
            title = "Els teus records",
            action = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${memories.photoCount} ${if (memories.photoCount == 1) "FOTO" else "FOTOS"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AtlasOnSurfaceMuted,
                    )
                    Text(
                        text = if (isVisible) "AMAGA" else "MOSTRA",
                        modifier = Modifier
                            .clickable(
                                role = Role.Button,
                                onClick = { onVisibleChanged(!isVisible) },
                            )
                            .padding(vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AtlasPrimary,
                    )
                }
            },
        )

        if (isVisible) {
            memories.trips.forEach { trip ->
                CountryMemoryTripCard(
                    trip = trip,
                    onTripClick = { onTripClick(trip.tripId) },
                    onPhotoClick = onPhotoClick,
                )
            }
        }
    }
}

@Composable
private fun CountryMemoryTripCard(
    trip: CountryMemoryTripUiState,
    onTripClick: () -> Unit,
    onPhotoClick: (PhotoViewerItemUiState) -> Unit,
) {
    // A country card spans several places, so each photo keeps its own location caption.
    AtlasMemoryRecordCard(
        eyebrow = "Viatge",
        title = trip.tripTitle,
        dateText = trip.tripDateText,
        countLabel = "${trip.items.size} ${if (trip.items.size == 1) "FOTO" else "FOTOS"}",
        tiles = trip.items.map { item ->
            AtlasMemoryTile(
                photo = item.photo,
                caption = item.title,
                isCover = false,
                onClick = { onPhotoClick(item) },
            )
        },
        onHeaderClick = onTripClick,
    )
}
