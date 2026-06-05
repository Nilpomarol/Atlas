package com.atlas.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.atlas.ui.theme.AtlasOnSurfaceMuted

@Composable
fun CountryFlag(
    iso2: String,
    modifier: Modifier = Modifier,
) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data("https://flagcdn.com/w40/${iso2.lowercase()}.png")
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = modifier,
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent(
                contentScale = ContentScale.Crop,
            )
            is AsyncImagePainter.State.Error,
            is AsyncImagePainter.State.Empty -> FlagIso2Fallback(iso2)
            else -> Unit
        }
    }
}

@Composable
private fun FlagIso2Fallback(iso2: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = iso2.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AtlasOnSurfaceMuted,
        )
    }
}
