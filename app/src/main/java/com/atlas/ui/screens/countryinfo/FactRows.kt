@file:OptIn(ExperimentalLayoutApi::class)

package com.atlas.ui.screens.countryinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.presentation.country.BreakdownSlice
import com.atlas.presentation.country.CompositionSegment
import com.atlas.presentation.country.CountryFactView
import com.atlas.presentation.country.Membership
import com.atlas.ui.theme.AtlasOnSurfaceFaint
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasSerif

@Composable
internal fun RankedRow(fact: CountryFactView) {
    Column(Modifier.padding(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(fact.label, color = AtlasOnSurfaceMuted, style = MaterialTheme.typography.bodyMedium)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.End),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                fact.value + (fact.unit?.let { " $it" } ?: ""),
                fontFamily = AtlasSerif,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = AtlasOnSurfaceStrong,
            )
            fact.tier?.let { TierChip(it) }
        }
        if (fact.rank != null && fact.rankTotal != null && fact.rankTotal > 1) {
            PositionBar(fact.rank, fact.rankTotal, tierColor(fact.tier))
            Text(
                "${formatOrdinal(fact.rank)} de ${fact.rankTotal}" + (fact.year?.let { " · $it" } ?: ""),
                style = MaterialTheme.typography.labelSmall,
                color = AtlasOnSurfaceFaint,
            )
        } else {
            FactYear(fact.year)
        }
    }
}

@Composable
internal fun PercentRow(fact: CountryFactView, accent: Color) {
    val fraction = (fact.value.toCaDouble() / 100.0).toFloat().coerceIn(0f, 1f)
    Column(Modifier.padding(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(fact.label, color = AtlasOnSurfaceMuted, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text("${fact.value} %", fontFamily = AtlasSerif, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = AtlasOnSurfaceStrong)
        }
        TrackBar(fraction, accent)
        FactYear(fact.year)
    }
}

@Composable
internal fun SimpleRow(fact: CountryFactView) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(fact.label, color = AtlasOnSurfaceMuted, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Column(
            modifier = Modifier.weight(1.5f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                fact.value + (fact.unit?.let { " $it" } ?: ""),
                color = AtlasOnSurfaceStrong,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
            )
            FactYear(fact.year)
        }
    }
}

@Composable
internal fun ChipsRow(fact: CountryFactView) {
    Column(Modifier.padding(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(fact.label, color = AtlasOnSurfaceMuted, style = MaterialTheme.typography.bodyMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            fact.value.split(", ").forEach { Chip(it) }
        }
        FactYear(fact.year)
    }
}

@Composable
internal fun StatusRow(fact: CountryFactView) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(fact.label, color = AtlasOnSurfaceMuted, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            StatusBadge(fact.value)
            FactYear(fact.year)
        }
    }
}

@Composable
internal fun BreakdownRow(fact: CountryFactView) {
    val slices = compactBreakdown(fact.breakdown.orEmpty())
    if (slices.isEmpty()) {
        Column(Modifier.padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(fact.label, color = AtlasOnSurfaceStrong, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
            Text("Dades no disponibles", color = AtlasOnSurfaceFaint, style = MaterialTheme.typography.labelMedium)
            FactYear(fact.year)
        }
        return
    }

    Column(Modifier.padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(fact.label, color = AtlasOnSurfaceStrong, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
        BoxWithConstraints {
            if (maxWidth < 340.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Donut(slices, Modifier.size(104.dp))
                    BreakdownLegend(slices, Modifier.fillMaxWidth())
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Donut(slices, Modifier.size(104.dp))
                    BreakdownLegend(slices, Modifier.weight(1f))
                }
            }
        }
        FactYear(fact.year)
    }
}

@Composable
private fun BreakdownLegend(slices: List<BreakdownSlice>, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        slices.forEachIndexed { index, slice ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Dot(BREAKDOWN_COLORS[index % BREAKDOWN_COLORS.size])
                Spacer(Modifier.width(7.dp))
                Text(
                    text = "${slice.name} ${formatPct(slice.pct)}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = AtlasOnSurfaceStrong,
                )
            }
        }
    }
}

@Composable
internal fun CompositionRow(title: String, segments: List<CompositionSegment>) {
    val total = segments.sumOf { it.pct }.coerceAtLeast(0.0001)
    Column(Modifier.padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(title, color = AtlasOnSurfaceStrong, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
        Row(Modifier.fillMaxWidth().height(26.dp).clip(RoundedCornerShape(7.dp))) {
            segments.forEachIndexed { i, s ->
                Box(
                    Modifier.weight((s.pct / total).toFloat().coerceAtLeast(0.0001f)).fillMaxHeight()
                        .background(COMPOSITION_COLORS[i % COMPOSITION_COLORS.size]),
                    contentAlignment = Alignment.Center,
                ) {
                    if (s.pct / total > 0.1) {
                        Text(formatPct(s.pct) + "%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            segments.forEachIndexed { i, s ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Dot(COMPOSITION_COLORS[i % COMPOSITION_COLORS.size])
                    Spacer(Modifier.width(6.dp))
                    Text(s.label, style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceMuted)
                }
            }
        }
    }
}

@Composable
internal fun MembershipRow(memberships: List<Membership>, accent: Color) {
    Column(Modifier.padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text("Pertinença", color = AtlasOnSurfaceStrong, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            memberships.forEach { MembershipPill(it, accent) }
        }
    }
}
