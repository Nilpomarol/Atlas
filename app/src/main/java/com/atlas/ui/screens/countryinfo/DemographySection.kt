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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.presentation.country.BreakdownSlice
import com.atlas.presentation.country.CompositionSegment
import com.atlas.presentation.country.CountryFactView
import com.atlas.presentation.country.SectionItem
import com.atlas.ui.components.AtlasSectionLabel
import com.atlas.ui.theme.AtlasDelay
import com.atlas.ui.theme.AtlasError
import com.atlas.ui.theme.AtlasGold
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasNavySoft
import com.atlas.ui.theme.AtlasOnSurfaceFaint
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasPlanned
import com.atlas.ui.theme.AtlasSerif
import com.atlas.ui.theme.AtlasVisited
import kotlin.math.abs

@Composable
internal fun DemographySection(items: List<SectionItem>) {
    val byKey = items.filterIsInstance<SectionItem.FactItem>().associate { it.fact.key to it.fact }

    // The age facts are collapsed into a composition item by the ViewModel.
    val ageSegments = items.filterIsInstance<SectionItem.CompositionItem>()
        .firstOrNull { it.title == "Estructura d'edat" }?.segments

    Column(
        Modifier.padding(top = 6.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        PopulationHeadline(pop = byKey["population"], density = byKey["density"])
        ageSegments?.let { AgeStructure(it) }
        byKey["ethnic_groups"]?.breakdown?.let { EthnicGroups(it) }
        DynamicsBlock(byKey)
    }
}

@Composable
private fun PopulationHeadline(pop: CountryFactView?, density: CountryFactView?) {
    if (pop == null) return
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                formatCompactValue(pop.value),
                fontFamily = AtlasSerif,
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = AtlasOnSurfaceStrong,
            )
            pop.unit?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AtlasOnSurfaceMuted,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            pop.tier?.let { Box(Modifier.padding(bottom = 3.dp)) { TierChip(it) } }
        }
        if (pop.rank != null && pop.rankTotal != null && pop.rankTotal > 1) {
            PositionBar(pop.rank, pop.rankTotal, tierColor(pop.tier))
            Text(
                "${formatOrdinal(pop.rank)} més poblat de ${pop.rankTotal}",
                style = MaterialTheme.typography.labelMedium,
                color = AtlasOnSurfaceFaint,
            )
        }
        density?.let {
            val rankText = if (it.rank != null && it.rankTotal != null) " · ${formatOrdinal(it.rank)} de ${it.rankTotal}" else ""
            Text(
                "Densitat · ${it.value} ${it.unit ?: ""}$rankText".trim(),
                style = MaterialTheme.typography.labelMedium,
                color = AtlasOnSurfaceMuted,
            )
        }
    }
}

private val AGE_COLORS = listOf(AtlasPlanned, AtlasVisited, AtlasGold)

@Composable
private fun AgeStructure(segments: List<CompositionSegment>) {
    val present = segments.filter { it.pct > 0.0 }
    if (present.isEmpty()) return
    val total = present.sumOf { it.pct }.coerceAtLeast(0.0001)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AtlasSectionLabel("Estructura d'edat")
        Row(Modifier.fillMaxWidth().height(26.dp).clip(RoundedCornerShape(7.dp))) {
            present.forEachIndexed { i, seg ->
                Box(
                    Modifier.weight((seg.pct / total).toFloat()).fillMaxHeight().background(AGE_COLORS[i % AGE_COLORS.size]),
                    contentAlignment = Alignment.Center,
                ) {
                    if (seg.pct / total > 0.1) {
                        Text("${formatPct(seg.pct)}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            present.forEachIndexed { i, seg ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Dot(AGE_COLORS[i % AGE_COLORS.size])
                    Spacer(Modifier.width(6.dp))
                    Text(seg.label, style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceMuted)
                }
            }
        }
    }
}

@Composable
private fun EthnicGroups(slices: List<BreakdownSlice>) {
    val compact = compactBreakdown(slices)
    if (compact.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AtlasSectionLabel("Grups ètnics")
        BoxWithConstraints {
            if (maxWidth < 340.dp) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Donut(compact, Modifier.size(104.dp))
                    BreakdownLegend(compact, Modifier.fillMaxWidth())
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Donut(compact, Modifier.size(104.dp))
                    BreakdownLegend(compact, Modifier.weight(1f))
                }
            }
        }
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
                    "${slice.name} ${formatPct(slice.pct)}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = AtlasOnSurfaceStrong,
                )
            }
        }
    }
}

@Composable
private fun DynamicsBlock(byKey: Map<String, CountryFactView>) {
    val urban = byKey["urban_pct"]
    val growth = byKey["pop_growth"]
    val fertility = byKey["fertility"]
    val birth = byKey["birth_rate"]
    val death = byKey["death_rate"]
    val migration = byKey["net_migration"]
    if (listOfNotNull(urban, growth, fertility, birth, death, migration).isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AtlasSectionLabel("Dinàmica")

        urban?.let {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text(it.label, color = AtlasOnSurfaceMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text("${it.value} ${it.unit ?: ""}".trim(), color = AtlasOnSurfaceStrong, fontWeight = FontWeight.Medium)
                }
                TrackBar((it.value.toCaDouble() / 100.0).toFloat().coerceIn(0f, 1f), AtlasNavy)
            }
        }

        growth?.let {
            val v = it.value.toCaDouble()
            StatRow(it.label, "${if (v > 0) "+" else ""}${it.value} ${it.unit ?: ""}".trim(), signedColor(v))
        }
        fertility?.let {
            val v = it.value.toCaDouble()
            StatRow(it.label, "${it.value} ${it.unit ?: ""}".trim(), fertilityColor(v), caption = "reemplaçament 2,1")
        }
        if (birth != null || death != null) NaturalBalance(birth, death)
        migration?.let {
            val v = it.value.toCaDouble()
            StatRow(it.label, "${if (v > 0) "+" else ""}${it.value} ${it.unit ?: ""}".trim(), signedColor(v))
        }
    }
}

/** Birth vs death rates as two bars on a shared scale, plus the natural change. */
@Composable
private fun NaturalBalance(birth: CountryFactView?, death: CountryFactView?) {
    val scale = 50.0 // ‰ — comfortably above the highest birth rates
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        birth?.let { RateBar(it, it.value.toCaDouble() / scale, AtlasVisited) }
        death?.let { RateBar(it, it.value.toCaDouble() / scale, AtlasNavySoft) }
        if (birth != null && death != null) {
            val net = birth.value.toCaDouble() - death.value.toCaDouble()
            val sign = if (net > 0) "+" else if (net < 0) "−" else ""
            Text(
                "$sign${formatPct(abs(net))} ‰ · creixement natural",
                style = MaterialTheme.typography.labelMedium,
                color = signedColor(net),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun RateBar(fact: CountryFactView, fraction: Double, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(fact.label, color = AtlasOnSurfaceMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text("${fact.value} ${fact.unit ?: ""}".trim(), color = AtlasOnSurfaceStrong, fontWeight = FontWeight.Medium)
        }
        TrackBar(fraction.toFloat().coerceIn(0f, 1f), color)
    }
}

@Composable
private fun StatRow(label: String, valueText: String, valueColor: Color, caption: String? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Text(label, color = AtlasOnSurfaceMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(valueText, color = valueColor, fontWeight = FontWeight.Medium)
            caption?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceFaint) }
        }
    }
}

private fun signedColor(v: Double): Color = when {
    v > 0.05 -> AtlasVisited
    v < -0.05 -> AtlasError
    else -> AtlasOnSurfaceMuted
}

private fun fertilityColor(v: Double): Color = when {
    v >= 2.1 -> AtlasVisited
    v >= 1.5 -> AtlasGold
    else -> AtlasDelay
}
