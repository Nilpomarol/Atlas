package com.atlas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.Airport
import com.atlas.presentation.flight.displayLabel
import com.atlas.ui.theme.AtlasAccentContainer
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasSurfaceRaised

@Composable
fun AirportSearchField(
    label: String,
    query: String,
    results: List<Airport>,
    onQueryChanged: (String) -> Unit,
    onAirportSelected: (Airport) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            label = { Text(label) },
            placeholder = { Text("IATA, nom o ciutat", color = AtlasOnSurfaceMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (results.isNotEmpty() && query.isNotBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AtlasSurfaceRaised),
            ) {
                results.take(6).forEach { airport ->
                    Text(
                        text = airport.displayLabel(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AtlasOnSurfaceStrong,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAirportSelected(airport) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}
