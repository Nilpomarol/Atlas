package com.atlas.ui.screens.country

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.domain.model.Country
import com.atlas.domain.model.CountryTrackingState
import com.atlas.ui.theme.AtlasBackground

@Composable
fun CountryMapHero(
    country: Country,
    style: CountryDetailStyle,
    trackingState: CountryTrackingState,
    onBackClick: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mapFloat")
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "floatY",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0.0f to Color(0xFF0A2218),
                        0.5f to Color(0xFF091520),
                        1.0f to Color(0xFF07101C),
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1200f, 1200f),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0x99004A2A),
                            0.45f to Color(0x40006438),
                            1.0f to Color(0x5A023C6E),
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1200f, 1200f),
                    ),
                ),
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawTopoRings()
            drawCountrySilhouette(
                latitude = country.latitude,
                longitude = country.longitude,
                primary = style.primary,
                floatOffsetY = floatY,
            )
            drawCapitalMarker(
                latitude = country.capitalLatitude,
                longitude = country.capitalLongitude,
                floatOffsetY = floatY,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, AtlasBackground),
                    ),
                ),
        )

        BackPill(
            onBackClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 18.dp, top = 18.dp),
        )

        StateBadge(
            label = trackingState.toStateLabel(),
            color = style.primary,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 18.dp, top = 18.dp),
        )
    }
}

private fun DrawScope.drawTopoRings() {
    val cx = size.width * 0.46f
    val cy = size.height * 0.52f
    for (i in 1..5) {
        val rx = size.width * 0.09f * i
        val ry = size.height * 0.12f * i
        drawOval(
            color = Color.White.copy(alpha = 0.022f),
            topLeft = Offset(cx - rx, cy - ry),
            size = androidx.compose.ui.geometry.Size(rx * 2, ry * 2),
            style = Stroke(width = 1f),
        )
    }
}

private fun DrawScope.drawCountrySilhouette(
    latitude: Double?,
    longitude: Double?,
    primary: Color,
    floatOffsetY: Float,
) {
    val nx = (((longitude ?: 10.0) + 180.0) / 360.0).coerceIn(0.15, 0.85).toFloat()
    val ny = ((90.0 - (latitude ?: 51.0)) / 180.0).coerceIn(0.18, 0.72).toFloat()
    val cx = size.width * nx
    val cy = size.height * ny + floatOffsetY
    val w = size.width * 0.14f
    val h = size.height * 0.44f

    val path = Path().apply {
        moveTo(cx - w * 0.34f, cy - h * 0.42f)
        lineTo(cx + w * 0.20f, cy - h * 0.50f)
        lineTo(cx + w * 0.48f, cy - h * 0.08f)
        lineTo(cx + w * 0.28f, cy + h * 0.45f)
        lineTo(cx - w * 0.38f, cy + h * 0.35f)
        lineTo(cx - w * 0.52f, cy - h * 0.05f)
        close()
    }

    drawPath(path, color = primary.copy(alpha = 0.22f))
    drawPath(path, color = primary.copy(alpha = 0.75f))
    drawPath(path, color = Color(0xFF6EE7B7).copy(alpha = 0.9f), style = Stroke(width = 2f))

    val dotX = cx + w * 0.10f
    val dotY = cy - h * 0.08f
    drawCircle(Color(0xFF6EE7B7).copy(alpha = 0.28f), radius = 11f, center = Offset(dotX, dotY))
    drawCircle(Color(0xFF6EE7B7), radius = 5f, center = Offset(dotX, dotY))
}

private fun DrawScope.drawCapitalMarker(
    latitude: Double?,
    longitude: Double?,
    floatOffsetY: Float,
) {
    if (latitude == null || longitude == null) return

    val nx = ((longitude + 180.0) / 360.0).coerceIn(0.12, 0.88).toFloat()
    val ny = ((90.0 - latitude) / 180.0).coerceIn(0.16, 0.76).toFloat()
    val center = Offset(size.width * nx, size.height * ny + floatOffsetY)

    drawCircle(Color.White.copy(alpha = 0.24f), radius = 14f, center = center)
    drawCircle(Color(0xFFC15B17), radius = 7f, center = center)
    drawCircle(Color.White, radius = 3f, center = center)
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
        color = Color.White.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Enrere",
                modifier = Modifier.size(13.dp),
                tint = Color.White,
            )
            Text(
                text = "Enrere",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

@Composable
fun StateBadge(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(color)
            .padding(horizontal = 15.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 0.14.sp,
        )
    }
}
