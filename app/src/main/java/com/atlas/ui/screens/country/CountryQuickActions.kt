package com.atlas.ui.screens.country

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.CountryTrackingState
import com.atlas.ui.components.AtlasSectionTitle
import com.atlas.ui.theme.AtlasLiving
import com.atlas.ui.theme.AtlasLivingContainer
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasWished
import com.atlas.ui.theme.AtlasWishedContainer

@Composable
fun CountryQuickActions(
    trackingState: CountryTrackingState,
    onWishedChanged: (Boolean) -> Unit,
    onSetCurrentlyLiving: () -> Unit,
    onAddVisitLog: () -> Unit,
) {
    Column {
        AtlasSectionTitle(title = "El teu seguiment")
        QuickActionsCard(
            trackingState = trackingState,
            onWishedChanged = onWishedChanged,
            onSetCurrentlyLiving = onSetCurrentlyLiving,
            onAddVisitLog = onAddVisitLog,
        )
    }
}

@Composable
private fun QuickActionsCard(
    trackingState: CountryTrackingState,
    onWishedChanged: (Boolean) -> Unit,
    onSetCurrentlyLiving: () -> Unit,
    onAddVisitLog: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(top = 10.dp)
            .fillMaxWidth()
            .background(AtlasSurface, RoundedCornerShape(16.dp))
            .border(1.dp, AtlasOutline, RoundedCornerShape(16.dp)),
    ) {
        ActionToggleRow(
            title = "A la llista de desitjos",
            subtitle = "Un lloc on vull anar",
            icon = Icons.Filled.FavoriteBorder,
            color = AtlasWished,
            container = AtlasWishedContainer,
            checked = trackingState.wished,
            onCheckedChange = onWishedChanged,
        )
        HorizontalDivider(color = AtlasOutline, modifier = Modifier.padding(horizontal = 14.dp))
        ActionToggleRow(
            title = "Visc aquí",
            subtitle = "Defineix-lo com a base",
            icon = Icons.Filled.Home,
            color = AtlasLiving,
            container = AtlasLivingContainer,
            checked = trackingState.currentlyLiving,
            onCheckedChange = { checked ->
                if (checked && !trackingState.currentlyLiving) {
                    onSetCurrentlyLiving()
                }
            },
        )
        Button(
            onClick = onAddVisitLog,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AtlasNavy),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
            contentPadding = PaddingValues(vertical = 10.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(
                text = "Afegeix registre",
                modifier = Modifier.padding(start = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@Composable
private fun ActionToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    container: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(container, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = color,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = AtlasOnSurfaceStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = AtlasOnSurfaceMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = color,
                uncheckedThumbColor = AtlasOnSurfaceMuted.copy(alpha = 0.38f),
                uncheckedTrackColor = container,
                uncheckedBorderColor = AtlasOutline,
            ),
        )
    }
}
