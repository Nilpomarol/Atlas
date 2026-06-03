package com.atlas.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.Airline
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasSurfaceRaised

@Composable
fun AirlineSearchField(
    query: String,
    results: List<Airline>,
    onQueryChanged: (String) -> Unit,
    onAirlineSelected: (Airline) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            label = { Text("Companyia (opcional)") },
            placeholder = { Text("Nom o codi IATA", color = AtlasOnSurfaceMuted) },
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
                results.forEach { airline ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAirlineSelected(airline) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AirlineLogo(
                            iata = airline.iata,
                            modifier = Modifier
                                .height(20.dp)
                                .widthIn(max = 60.dp),
                        )
                        Text(
                            text = "${airline.name} (${airline.iata})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AtlasOnSurfaceStrong,
                        )
                    }
                }
            }
        }
    }
}
