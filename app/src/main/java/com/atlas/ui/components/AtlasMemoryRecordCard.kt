package com.atlas.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.domain.model.StopPhoto
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasPrimary

/** One photo within a memory record card. [caption] is shown under the tile when non-null. */
data class AtlasMemoryTile(
    val photo: StopPhoto,
    val caption: String?,
    val isCover: Boolean,
    val onClick: () -> Unit,
)

/**
 * Shared "records" card used by both the trip gallery and country memories so the two read
 * identically. An editorial header (mono eyebrow, serif title, mono date + count) sits over a
 * filmstrip of [AtlasMemoryPhotoTile]s. Per-photo captions appear only where they add context
 * (e.g. country memories spanning several places).
 */
@Composable
fun AtlasMemoryRecordCard(
    eyebrow: String,
    title: String,
    dateText: String?,
    countLabel: String,
    tiles: List<AtlasMemoryTile>,
    onHeaderClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AtlasCard(modifier = modifier, contentPadding = PaddingValues(0.dp)) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onHeaderClick)
                    .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = eyebrow.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AtlasPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 17.sp, lineHeight = 21.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasOnSurfaceStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    dateText?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = AtlasOnSurfaceMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Text(
                    text = countLabel,
                    modifier = Modifier.padding(top = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 1,
                )
            }

            LazyRow(
                // Horizontal inset lives on the modifier (not contentPadding) so the scroll
                // viewport clips with a margin — a partially-overflowing tile keeps a gap from
                // the card edge instead of being cut off flush against it.
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                contentPadding = PaddingValues(top = 2.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(items = tiles, key = { it.photo.id }) { tile ->
                    if (tile.caption != null) {
                        Column(
                            modifier = Modifier.width(136.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            AtlasMemoryPhotoTile(
                                photo = tile.photo,
                                locationName = tile.caption,
                                isCover = tile.isCover,
                                onClick = tile.onClick,
                            )
                            Text(
                                text = tile.caption,
                                style = MaterialTheme.typography.labelSmall,
                                color = AtlasOnSurfaceMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    } else {
                        AtlasMemoryPhotoTile(
                            photo = tile.photo,
                            locationName = title,
                            isCover = tile.isCover,
                            onClick = tile.onClick,
                        )
                    }
                }
            }
        }
    }
}
