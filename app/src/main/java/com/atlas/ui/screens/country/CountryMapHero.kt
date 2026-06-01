package com.atlas.ui.screens.country

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.Country
import com.atlas.domain.model.CountryTrackingState
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasOnSurfaceFaint
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline

@Composable
fun CountryMapHero(
    country: Country,
    style: CountryDetailStyle,
    trackingState: CountryTrackingState,
    onBackClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AtlasBackground)
            .height(232.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(232.dp)
                .background(AtlasBackground),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawAtlasGrid()
                drawCountryPoint(
                    latitude = country.latitude,
                    longitude = country.longitude,
                    color = style.primary,
                    radius = 9f,
                )
                drawCapitalMarker(
                    latitude = country.capitalLatitude,
                    longitude = country.capitalLongitude,
                    color = style.primary,
                )
            }

            BackPill(
                onBackClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 20.dp, top = 16.dp),
            )

            country.capitalNameCa?.let { capital ->
                Text(
                    text = capital,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = AtlasOnSurfaceMuted,
                )
            }
        }
    }
}

private fun DrawScope.drawAtlasGrid() {
    val spacing = 18.dp.toPx()
    var y = spacing / 2
    while (y < size.height) {
        var x = spacing / 2
        while (x < size.width) {
            drawCircle(
                color = AtlasOnSurfaceFaint.copy(alpha = 0.22f),
                radius = 1.35.dp.toPx(),
                center = Offset(x, y),
            )
            x += spacing
        }
        y += spacing
    }
}

private fun DrawScope.drawCountryPoint(
    latitude: Double?,
    longitude: Double?,
    color: Color,
    radius: Float,
) {
    val point = project(latitude = latitude, longitude = longitude)
    drawCircle(color.copy(alpha = 0.14f), radius = radius * 2.2f, center = point)
    drawCircle(color.copy(alpha = 0.88f), radius = radius, center = point)
    drawCircle(Color.White, radius = radius * 0.45f, center = point)
}

private fun DrawScope.drawCapitalMarker(
    latitude: Double?,
    longitude: Double?,
    color: Color,
) {
    if (latitude == null || longitude == null) return
    val center = project(latitude = latitude, longitude = longitude)
    drawCircle(Color.White, radius = 13f, center = center)
    drawCircle(color, radius = 8f, center = center)
    drawCircle(Color.White, radius = 3.2f, center = center)
}

private fun DrawScope.project(latitude: Double?, longitude: Double?): Offset {
    val nx = (((longitude ?: 10.0) + 180.0) / 360.0).coerceIn(0.10, 0.90).toFloat()
    val ny = ((90.0 - (latitude ?: 42.0)) / 180.0).coerceIn(0.15, 0.78).toFloat()
    return Offset(size.width * nx, size.height * ny)
}

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
        border = androidx.compose.foundation.BorderStroke(1.dp, AtlasOutline),
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
