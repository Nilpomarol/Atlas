package com.atlas.ui.rework.capture

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material.icons.rounded.Luggage
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.atlas.ui.rework.components.ReworkFloatingCard
import com.atlas.ui.rework.foundation.AtlasReworkTheme

data class CaptureContext(val countryIso2: String? = null)

@Composable
fun ReworkCaptureButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AtlasReworkTheme.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = colors.accent,
        contentColor = Color.White,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
        shadowElevation = 10.dp,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (expanded) Icons.Rounded.Close else Icons.Rounded.Add,
                contentDescription = null,
            )
            Text(
                text = if (expanded) "Tanca" else "Registra",
                style = AtlasReworkTheme.typography.label,
                color = Color.White,
            )
        }
    }
}

@Composable
fun ReworkCaptureOverlay(
    context: CaptureContext,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AtlasReworkTheme.colors
    Box(
        modifier = modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.24f)).clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter,
    ) {
        ReworkFloatingCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 98.dp)
                .clickable(onClick = {}),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Què vols registrar?", style = AtlasReworkTheme.typography.title, modifier = Modifier.weight(1f))
                    Surface(
                        shape = CircleShape,
                        color = colors.ink.copy(alpha = 0.08f),
                        contentColor = colors.ink,
                        onClick = onDismiss,
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Tanca",
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                }
                context.countryIso2?.let {
                    Text("Context del mapa · $it", style = AtlasReworkTheme.typography.data, color = colors.accent)
                }
                CaptureChoice(Icons.Rounded.Place, "Registra una visita")
                CaptureChoice(Icons.Rounded.Luggage, "Crea un viatge")
                CaptureChoice(Icons.Rounded.FlightTakeoff, "Afegeix un vol")
            }
        }
    }
}

@Composable
private fun CaptureChoice(icon: ImageVector, label: String) {
    val colors = AtlasReworkTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(shape = CircleShape, color = colors.ink.copy(alpha = 0.08f)) {
            Icon(icon, contentDescription = null, modifier = Modifier.padding(9.dp), tint = colors.ink)
        }
        Text(label, style = AtlasReworkTheme.typography.body, color = colors.ink)
    }
}
