package com.atlas.ui.screens.country

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.Country
import com.atlas.domain.model.CountryTrackingState
import com.atlas.ui.components.AtlasSectionTitle
import com.atlas.ui.theme.AtlasOnSurfaceFaint
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasSurface

@Composable
fun CountryInfoCard(
    country: Country,
    trackingState: CountryTrackingState,
    style: CountryDetailStyle,
) {
    Column {
        AtlasSectionTitle(title = "Informació")
        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
                .background(AtlasSurface, RoundedCornerShape(16.dp))
                .border(1.dp, AtlasOutline, RoundedCornerShape(16.dp)),
        ) {
            Column {
                Row {
                    InfoCell(
                        modifier = Modifier.weight(1f),
                        label = "Capital",
                        value = country.capitalNameCa ?: "Sense dades",
                        borderEnd = true,
                    )
                    InfoCell(
                        modifier = Modifier.weight(1f),
                        label = "Regió",
                        value = country.subregion?.toCatalanSubregion() ?: country.continent.toCatalanContinent(),
                    )
                }
                RowDivider()
                Row {
                    InfoCell(
                        modifier = Modifier.weight(1f),
                        label = "Tipus",
                        value = country.type.toCountryDetailCatalanLabel(),
                        borderEnd = true,
                    )
                    InfoCell(
                        modifier = Modifier.weight(1f),
                        label = "Codi",
                        value = listOfNotNull(country.iso2, country.iso3).joinToString(" / "),
                    )
                }
                RowDivider()
                Row {
                    InfoCell(
                        modifier = Modifier.weight(1f),
                        label = "Continents",
                        value = country.continent.toCatalanContinent(),
                        borderEnd = true,
                    )
                    InfoCell(
                        modifier = Modifier.weight(1f),
                        label = "Estat",
                        value = when {
                            trackingState.currentlyLiving -> "Residència actual"
                            trackingState.lived -> "Viscut"
                            trackingState.visited -> "Visitat"
                            trackingState.planned -> "Planificat"
                            trackingState.wished -> "Desitjat"
                            else -> "No visitat"
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCell(
    modifier: Modifier,
    label: String,
    value: String,
    borderEnd: Boolean = false,
) {
    Row(modifier = modifier) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 13.dp),
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceFaint,
            )
            Text(
                text = value,
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.titleSmall,
                color = AtlasOnSurfaceStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (borderEnd) {
            Box(
                modifier = Modifier
                    .padding(vertical = 1.dp)
                    .background(AtlasOutline)
                    .width(1.dp),
            )
        }
    }
}

@Composable
private fun RowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AtlasOutline),
    )
}
