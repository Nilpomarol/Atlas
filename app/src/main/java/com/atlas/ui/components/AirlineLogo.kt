package com.atlas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasSurfaceSubtle

/**
 * Loads an airline logo from avs.io by IATA code.
 * Falls back to a styled IATA monogram when offline or the logo is not found.
 */
@Composable
fun AirlineLogo(
    iata: String,
    modifier: Modifier = Modifier,
) {
    val url = "https://pics.avs.io/200/100/${iata.uppercase()}.png"

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = iata,
        modifier = modifier,
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent(
                contentScale = ContentScale.Fit,
            )
            is AsyncImagePainter.State.Error,
            is AsyncImagePainter.State.Empty -> IataMonogram(iata)
            else -> Unit // loading — show nothing
        }
    }
}

@Composable
private fun IataMonogram(iata: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AtlasSurfaceSubtle, RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = iata.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AtlasOnSurfaceMuted,
        )
    }
}
