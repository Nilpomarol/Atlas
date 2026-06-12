package com.atlas.ui.screens.countryinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.presentation.country.CountryHighlight
import com.atlas.presentation.country.CountryKpi
import com.atlas.ui.components.AtlasSectionTitle
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasSerif
import com.atlas.ui.theme.AtlasSurfaceRaised

@Composable
internal fun HighlightsShelf(highlights: List<CountryHighlight>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AtlasSectionTitle("Destacats mundials")
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            highlights.forEach { h ->
                val c = tierColor(h.tier)
                Box(
                    Modifier.width(158.dp).clip(RoundedCornerShape(16.dp)).background(AtlasSurfaceRaised)
                        .border(1.dp, AtlasOutline, RoundedCornerShape(16.dp)),
                ) {
                    Box(Modifier.fillMaxHeight().width(5.dp).background(c).align(Alignment.CenterStart))
                    Column(Modifier.padding(start = 17.dp, top = 14.dp, end = 13.dp, bottom = 14.dp)) {
                        Text(formatOrdinal(h.rank), fontFamily = AtlasSerif, fontSize = 34.sp, fontWeight = FontWeight.Medium, color = c)
                        Text(
                            text = h.label,
                            color = AtlasOnSurfaceStrong,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${h.valueText} · de ${h.rankTotal}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AtlasOnSurfaceMuted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun KpiGrid(kpis: List<CountryKpi>) {
    BoxWithConstraints {
        val singleColumn = maxWidth < 360.dp
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (singleColumn) {
                kpis.forEach { kpi -> KpiTile(kpi, Modifier.fillMaxWidth()) }
            } else {
                kpis.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { kpi -> KpiTile(kpi, Modifier.weight(1f)) }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiTile(kpi: CountryKpi, modifier: Modifier = Modifier) {
    val color = tierColor(kpi.tier)
    Box(
        modifier.clip(RoundedCornerShape(16.dp)).background(color.copy(alpha = 0.10f)).padding(15.dp),
    ) {
        Column {
            Text(
                text = kpi.label,
                style = MaterialTheme.typography.labelMedium,
                color = AtlasOnSurfaceMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = kpi.value + (kpi.unit?.let { " $it" } ?: ""),
                fontFamily = AtlasSerif,
                fontSize = 25.sp,
                fontWeight = FontWeight.Medium,
                color = AtlasOnSurfaceStrong,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (kpi.rank != null) {
                Text(
                    text = "${formatOrdinal(kpi.rank)} del món" + (kpi.tier?.let { " · $it" } ?: ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                )
            }
        }
    }
}
