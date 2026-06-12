package com.atlas.ui.screens.countryinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.atlas.presentation.country.CountryInfoUiState
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasSerif
import java.io.File

@Composable
internal fun InfoHero(uiState: CountryInfoUiState, onBackClick: () -> Unit) {
    val country = uiState.country
    val photo = uiState.photo
    val context = LocalContext.current
    Box(Modifier.fillMaxWidth().height(if (photo != null) 520.dp else 184.dp).background(AtlasNavy)) {
        if (photo != null) {
            AsyncImage(
                model = File(context.filesDir, "country_photos/${photo.filename}"),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.30f), Color.Transparent, Color.Black.copy(alpha = 0.78f))),
                ),
            )
        }
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(8.dp).clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha = 0.25f)),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Enrere", tint = Color.White)
        }
        Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text(country?.flagEmoji ?: "", fontSize = 38.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                country?.nameCa ?: "",
                fontFamily = AtlasSerif,
                fontSize = 34.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val subtitle = listOfNotNull(country?.capitalNameCa, country?.subregion).joinToString(" · ")
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (photo?.author != null) {
                Text(
                    "Foto: ${photo.author} · Unsplash",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.76f),
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
