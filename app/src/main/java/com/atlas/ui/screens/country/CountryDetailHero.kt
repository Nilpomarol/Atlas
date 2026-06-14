package com.atlas.ui.screens.country

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.atlas.domain.model.Country
import com.atlas.domain.model.CountryTrackingState
import com.atlas.presentation.country.CountryDetailPillUiState
import com.atlas.ui.components.CountryFlag
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasLived
import com.atlas.ui.theme.AtlasLiving
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPending
import com.atlas.ui.theme.AtlasPlanned
import com.atlas.ui.theme.AtlasSerif
import com.atlas.ui.theme.AtlasVisited
import com.atlas.ui.theme.AtlasWished
import java.io.File

private val HERO_PHOTO_HEIGHT = 320.dp
private val HERO_FALLBACK_HEIGHT = 212.dp

/**
 * Country detail hero. Carries the screen's identity directly on the image (like the
 * Country Info hero): the name, location, flag, and — most prominently — the tracking
 * state. Falls back to a navy panel with the same overlay when no photo is cached.
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
fun CountryDetailHero(
    country: Country,
    trackingState: CountryTrackingState,
    detailPills: CountryDetailPillUiState,
    photoFilename: String?,
    photoAuthor: String?,
    onBackClick: () -> Unit,
) {
    val onPhoto = photoFilename != null
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (onPhoto) HERO_PHOTO_HEIGHT else HERO_FALLBACK_HEIGHT)
            .background(AtlasNavy),
    ) {
        if (onPhoto) {
            AsyncImage(
                model = File(LocalContext.current.filesDir, "country_landscape_photos/$photoFilename"),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        // Dark scrim from the bottom so the overlaid identity stays legible on any photo
        // (and gives the navy fallback some depth).
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0.30f to Color.Transparent,
                    1.0f to Color.Black.copy(alpha = if (onPhoto) 0.80f else 0.30f),
                ),
            ),
        )

        BackPill(
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopStart).padding(start = 20.dp, top = 16.dp),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                detailPills.heroStatePills().forEach { (label, color) -> HeroStatePill(label, color) }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        country.nameCa,
                        fontFamily = AtlasSerif,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val subtitle = listOfNotNull(
                        country.capitalNameCa,
                        country.subregion?.toCatalanSubregion(),
                    ).joinToString(" · ")
                    if (subtitle.isNotEmpty()) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (photoAuthor != null) {
                        Text(
                            "Foto: $photoAuthor · Unsplash",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.70f),
                            modifier = Modifier.padding(top = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                CountryFlag(
                    iso2 = country.iso2 ?: "",
                    modifier = Modifier
                        .width(62.dp)
                        .height(46.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(AtlasBackground)
                        .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                )
            }
        }
    }
}

/** A solid, white-on-color state badge that reads clearly over a photo. */
@Composable
private fun HeroStatePill(label: String, color: Color) {
    Box(
        Modifier.clip(RoundedCornerShape(100.dp)).background(color).padding(horizontal = 11.dp, vertical = 5.dp),
    ) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 0.4.sp,
        )
    }
}

/** All applicable tracking states as (label, color), or a single "No visitat" fallback. */
private fun CountryDetailPillUiState.heroStatePills(): List<Pair<String, Color>> = buildList {
    if (currentlyLiving) add("Vivint-hi" to AtlasLiving)
    if (lived && !currentlyLiving) add("Viscut" to AtlasLived)
    if (visited) add("Visitat" to AtlasVisited)
    if (planned) add("Planificat" to AtlasPlanned)
    if (wished) add("Desitjat" to AtlasWished)
}.ifEmpty { listOf("No visitat" to AtlasPending) }

@Composable
fun BackPill(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onBackClick,
        modifier = modifier,
        shape = RoundedCornerShape(100.dp),
        color = Color.White.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, AtlasOutline),
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Enrere",
                modifier = Modifier.size(14.dp),
                tint = AtlasOnSurfaceStrong,
            )
            Text(
                text = "Enrere",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = AtlasOnSurfaceStrong,
            )
        }
    }
}
