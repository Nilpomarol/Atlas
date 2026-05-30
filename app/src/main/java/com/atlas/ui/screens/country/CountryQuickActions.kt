package com.atlas.ui.screens.country

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.domain.model.CountryTrackingState
import com.atlas.ui.theme.AtlasLiving
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasWished

@Composable
fun CountryQuickActions(
    trackingState: CountryTrackingState,
    onWishedChanged: (Boolean) -> Unit,
    onSetCurrentlyLiving: () -> Unit,
    onAddVisitLog: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-40).dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AddLogButton(
            modifier = Modifier.weight(2f),
            onClick = onAddVisitLog,
        )
        TintedActionButton(
            modifier = Modifier.weight(1f),
            label = if (trackingState.wished) "Desitjat" else "Desitjar",
            icon = { Icon(Icons.Filled.Star, null, Modifier.size(14.dp)) },
            color = AtlasWished,
            selected = trackingState.wished,
            onClick = { onWishedChanged(!trackingState.wished) },
        )
        TintedActionButton(
            modifier = Modifier.weight(1f),
            label = "Vivint",
            icon = { Icon(Icons.Filled.Home, null, Modifier.size(14.dp)) },
            color = AtlasLiving,
            selected = trackingState.currentlyLiving,
            onClick = {
                if (!trackingState.currentlyLiving) {
                    onSetCurrentlyLiving()
                }
            },
        )
    }
}

@Composable
private fun AddLogButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(modifier = modifier) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 44.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AtlasOnSurfaceStrong),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
        ) {
            Icon(Icons.Filled.Add, null, Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("Afegeix", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun TintedActionButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: @Composable () -> Unit,
    color: Color,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val containerColor = if (selected) color else AtlasSurface
    val textColor = if (selected) Color.White else color

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minHeight = 44.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = textColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = color.copy(alpha = 0.4f),
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
        border = BorderStroke(1.dp, color),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 11.dp),
    ) {
        icon()
        Spacer(Modifier.width(5.dp))
        Text(label, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
    }
}
