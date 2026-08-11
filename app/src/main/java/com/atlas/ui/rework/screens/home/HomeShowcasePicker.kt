package com.atlas.ui.rework.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.atlas.ui.rework.components.ReworkFloatingCard
import com.atlas.ui.rework.foundation.AtlasReworkTheme

@Composable
fun HomeShowcasePicker(
    scenarios: List<HomeShowcaseScenario>,
    selectedScenarioId: String?,
    onSelect: (HomeShowcaseScenario?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.24f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        ReworkFloatingCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .widthIn(max = 420.dp)
                .clickable(onClick = {}),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("ESCENARIS DE DESENVOLUPAMENT", style = AtlasReworkTheme.typography.label)
                Text("Prova la pàgina d’inici", style = AtlasReworkTheme.typography.title)
                ScenarioRow(
                    label = "Dades reals",
                    description = "Estat actual de l’aplicació",
                    selected = selectedScenarioId == null,
                    onClick = { onSelect(null) },
                )
                scenarios.forEach { scenario ->
                    ScenarioRow(
                        label = scenario.label,
                        description = scenario.description,
                        selected = scenario.id == selectedScenarioId,
                        onClick = { onSelect(scenario) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ScenarioRow(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = AtlasReworkTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = AtlasReworkTheme.typography.body, color = colors.ink)
            Text(description, style = AtlasReworkTheme.typography.data, color = colors.inkMuted)
        }
        if (selected) {
            Surface(shape = CircleShape, color = colors.accent, contentColor = Color.White) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = "Seleccionat",
                    modifier = Modifier.padding(5.dp),
                )
            }
        }
    }
}
