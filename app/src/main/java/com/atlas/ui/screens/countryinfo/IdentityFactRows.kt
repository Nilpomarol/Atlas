package com.atlas.ui.screens.countryinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.atlas.presentation.country.CountryFactView
import com.atlas.ui.theme.AtlasMono
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong

/** Coat of arms: render the SVG emblem instead of its raw URL. */
@Composable
internal fun CoatOfArmsRow(fact: CountryFactView) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            fact.label,
            color = AtlasOnSurfaceMuted,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
        )
        CoatOfArms(fact.value, Modifier.size(46.dp))
    }
}

/** Government type: render the simplified form plus its structure qualifier. */
@Composable
internal fun GovernmentRow(fact: CountryFactView) {
    val gov = simplifyGovernment(fact.value)
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            fact.label,
            color = AtlasOnSurfaceMuted,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Column(
            modifier = Modifier.weight(1.5f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                gov.form + (gov.structure?.let { " · $it" } ?: ""),
                color = AtlasOnSurfaceStrong,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
            )
            FactYear(fact.year)
        }
    }
}

/** Codes (Olympic, FIFA) shown in the monospace instrument style. */
@Composable
internal fun MonoValueRow(fact: CountryFactView) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            fact.label,
            color = AtlasOnSurfaceMuted,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            fact.value,
            fontFamily = AtlasMono,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = AtlasOnSurfaceStrong,
        )
    }
}

@Composable
internal fun CoatOfArms(url: String, modifier: Modifier = Modifier) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .decoderFactory(SvgDecoder.Factory())
            .crossfade(true)
            .build(),
        contentDescription = "Escut",
        modifier = modifier,
    ) {
        // The emblem is decorative; if it fails to load just leave its space empty.
        if (painter.state is AsyncImagePainter.State.Success) SubcomposeAsyncImageContent()
    }
}

