package com.atlas.ui.screens.countryinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import com.atlas.domain.model.CountryPhoto
import com.atlas.presentation.country.CountryInfoUiState
import com.atlas.ui.components.CountryFlag
import com.atlas.ui.screens.country.BackPill
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasSerif
import java.io.File

private val HERO_PHOTO_HEIGHT = 560.dp
private val HERO_FALLBACK_HEIGHT = 184.dp

@Composable
internal fun InfoHero(uiState: CountryInfoUiState, onBackClick: () -> Unit) {
    val country = uiState.country ?: return
    val photo = uiState.photo

    Box(
        Modifier
            .fillMaxWidth()
            .height(if (photo != null) HERO_PHOTO_HEIGHT else HERO_FALLBACK_HEIGHT)
            .background(AtlasNavy),
    ) {
        if (photo != null) {
            AsyncImage(
                model = File(LocalContext.current.filesDir, "country_photos/${photo.filename}"),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            // Single smooth dark scrim down to the bottom edge so the white caption
            // stays legible over any photo.
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0.35f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.78f),
                    ),
                ),
            )
        }

        BackPill(
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 16.dp),
        )

        HeroCaption(
            country = country,
            photo = photo,
            onPhoto = photo != null,
            modifier = Modifier.align(Alignment.BottomStart),
        )
    }
}

@Composable
private fun HeroCaption(
    country: Country,
    photo: CountryPhoto?,
    onPhoto: Boolean,
    modifier: Modifier = Modifier,
) {
    // The caption sits over the photo (or the navy fallback), so it always uses
    // light-on-dark colors.
    val titleColor = Color.White
    val subtitleColor = Color.White.copy(alpha = 0.85f)
    val captionColor = Color.White.copy(alpha = 0.76f)
    val flagBackground = if (onPhoto) AtlasBackground else AtlasNavy

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                country.nameCa,
                fontFamily = AtlasSerif,
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = titleColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val subtitle = listOfNotNull(country.capitalNameCa, country.subregion).joinToString(" · ")
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtitleColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (photo?.author != null) {
                Text(
                    "Foto: ${photo.author} · Unsplash",
                    style = MaterialTheme.typography.labelSmall,
                    color = captionColor,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        CountryFlag(
            iso2 = country.iso2,
            modifier = Modifier
                .width(62.dp)
                .height(46.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(flagBackground)
                .border(1.dp, AtlasOutline, RoundedCornerShape(4.dp)),
        )
    }
}
