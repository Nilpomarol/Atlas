package com.atlas.ui.screens.country

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.StopPhoto
import com.atlas.presentation.country.CountryMemoriesUiState
import com.atlas.presentation.country.CountryMemoryGroupUiState
import com.atlas.ui.components.AtlasCard
import com.atlas.ui.components.AtlasMemoryPhotoTile
import com.atlas.ui.components.AtlasSectionTitle
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasPrimary

@Composable
internal fun CountryMemoriesSection(
    memories: CountryMemoriesUiState,
    onTripClick: (String) -> Unit,
    onPhotoClick: (StopPhoto) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AtlasSectionTitle(
            title = "Els teus records",
            action = {
                Text(
                    text = "${memories.photoCount} ${if (memories.photoCount == 1) "FOTO" else "FOTOS"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AtlasOnSurfaceMuted,
                )
            },
        )

        memories.groups.forEach { group ->
            CountryMemoryCard(
                group = group,
                onTripClick = { onTripClick(group.tripId) },
                onPhotoClick = onPhotoClick,
            )
        }
    }
}

@Composable
private fun CountryMemoryCard(
    group: CountryMemoryGroupUiState,
    onTripClick: () -> Unit,
    onPhotoClick: (StopPhoto) -> Unit,
) {
    AtlasCard(
        contentPadding = PaddingValues(0.dp),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onTripClick)
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
                            text = "VIATGE · ${group.tripTitle}",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                            color = AtlasPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        group.tripDateText?.let { tripDate ->
                            Text(
                                text = tripDate,
                                style = MaterialTheme.typography.labelSmall,
                                color = AtlasOnSurfaceMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Text(
                        text = group.locationName,
                        style = MaterialTheme.typography.titleMedium,
                        color = AtlasOnSurfaceStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = listOfNotNull(group.sourceLabel, group.stopDateText)
                            .joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = AtlasOnSurfaceMuted,
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
                    AtlasMemoryPhotoTile(
                        photo = photo,
                        locationName = group.locationName,
                        isCover = false,
                        onClick = { onPhotoClick(photo) },
                    )
                }
            }
        }
    }
}
