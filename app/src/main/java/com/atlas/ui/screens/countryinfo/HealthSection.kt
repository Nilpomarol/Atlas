package com.atlas.ui.screens.countryinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.presentation.country.CountryFactView
import com.atlas.presentation.country.SectionItem
import com.atlas.ui.components.AtlasSectionLabel
import com.atlas.ui.theme.AtlasDelay
import com.atlas.ui.theme.AtlasError
import com.atlas.ui.theme.AtlasGold
import com.atlas.ui.theme.AtlasOnSurfaceFaint
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOlive
import com.atlas.ui.theme.AtlasSerif
import com.atlas.ui.theme.AtlasVisited

@Composable
internal fun HealthSection(items: List<SectionItem>) {
    val byKey = items.filterIsInstance<SectionItem.FactItem>().associate { it.fact.key to it.fact }

    Column(
        Modifier.padding(top = 6.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        LifeExpectancyHeadline(byKey)
        MeterGroup("Mortalitat", listOfNotNull(byKey["infant_mortality"], byKey["under5_mortality"], byKey["maternal_mortality"]))
        MeterGroup("Accés bàsic", listOfNotNull(byKey["water_access"], byKey["sanitation"]))
        HealthSystem(byKey)
        MeterGroup("Factors de risc", listOfNotNull(byKey["obesity"], byKey["smoking"], byKey["suicide_rate"]))
    }
}

@Composable
private fun LifeExpectancyHeadline(byKey: Map<String, CountryFactView>) {
    val le = byKey["life_expectancy"] ?: return
    val male = byKey["life_exp_male"]
    val female = byKey["life_exp_female"]
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(le.value, fontFamily = AtlasSerif, fontSize = 32.sp, fontWeight = FontWeight.Medium, color = AtlasOnSurfaceStrong)
            le.unit?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = AtlasOnSurfaceMuted, modifier = Modifier.padding(bottom = 4.dp))
            }
            le.tier?.let { Box(Modifier.padding(bottom = 3.dp)) { TierChip(it) } }
        }
        if (le.rank != null && le.rankTotal != null && le.rankTotal > 1) {
            PositionBar(le.rank, le.rankTotal, tierColor(le.tier))
            Text(
                "${formatOrdinal(le.rank)} de ${le.rankTotal}",
                style = MaterialTheme.typography.labelMedium,
                color = AtlasOnSurfaceFaint,
            )
        }
        if (male != null && female != null) {
            val gap = female.value.toCaDouble() - male.value.toCaDouble()
            Text(
                "♂ ${male.value} · ♀ ${female.value} ${le.unit ?: ""}".trim() +
                    if (gap > 0) "  (+${formatPct(gap)} elles)" else "",
                style = MaterialTheme.typography.labelMedium,
                color = AtlasOnSurfaceMuted,
            )
        }
    }
}

@Composable
private fun MeterGroup(title: String, facts: List<CountryFactView>) {
    if (facts.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AtlasSectionLabel(title)
        facts.forEach { HealthMeter(it) }
    }
}

@Composable
private fun HealthMeter(fact: CountryFactView) {
    val quality = healthQuality(fact.key, fact.value.toCaDouble())
    val color = quality?.color ?: AtlasOnSurfaceMuted
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(fact.label, color = AtlasOnSurfaceMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                Text("${fact.value} ${fact.unit ?: ""}".trim(), color = AtlasOnSurfaceStrong, fontWeight = FontWeight.Medium)
                quality?.let { Text(it.label, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Medium) }
            }
        }
        TrackBar(healthFraction(fact.key, fact.value.toCaDouble()), color)
        if (fact.rank != null && fact.rankTotal != null && fact.rankTotal > 1) {
            Text("${formatOrdinal(fact.rank)} de ${fact.rankTotal}", style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceFaint)
        }
    }
}

@Composable
private fun HealthSystem(byKey: Map<String, CountryFactView>) {
    val physicians = byKey["physicians"]
    val beds = byKey["hospital_beds"]
    val expPc = byKey["health_exp_pc"]
    val expGdp = byKey["health_exp_gdp"]
    if (listOfNotNull(physicians, beds, expPc, expGdp).isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AtlasSectionLabel("Sistema sanitari")
        physicians?.let { ResourceRow(it) }
        beds?.let { ResourceRow(it) }
        if (expPc != null || expGdp != null) {
            val parts = listOfNotNull(
                expGdp?.let { "${it.value} ${it.unit ?: ""}".trim() },
                expPc?.let { "${formatCompactValue(it.value)} ${it.unit ?: ""} per càpita".trim() },
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text("Despesa en salut", color = AtlasOnSurfaceMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text(parts.joinToString(" · "), color = AtlasOnSurfaceStrong, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ResourceRow(fact: CountryFactView) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Text(fact.label, color = AtlasOnSurfaceMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text("${fact.value} ${fact.unit ?: ""}".trim(), color = AtlasOnSurfaceStrong, fontWeight = FontWeight.Medium)
            if (fact.rank != null && fact.rankTotal != null && fact.rankTotal > 1) {
                Text("${formatOrdinal(fact.rank)} de ${fact.rankTotal}", style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceFaint)
            }
        }
    }
}

private data class HealthQuality(val color: Color, val label: String)

/** Color + label for a health metric (green good → red poor). */
private fun healthQuality(key: String, v: Double): HealthQuality? = when (key) {
    "infant_mortality" -> low(v, 5.0, 15.0, 30.0, 50.0)
    "under5_mortality" -> low(v, 6.0, 18.0, 35.0, 60.0)
    "maternal_mortality" -> low(v, 10.0, 50.0, 150.0, 500.0)
    "water_access", "sanitation" -> high(v, 99.0, 90.0, 75.0)
    "obesity" -> low(v, 25.0, 40.0, 55.0, 65.0, riskLabels = true)
    "smoking" -> low(v, 10.0, 18.0, 26.0, 35.0, riskLabels = true)
    else -> null
}

private fun healthFraction(key: String, v: Double): Float = when (key) {
    "maternal_mortality" -> v / 800.0
    "under5_mortality" -> v / 120.0
    "infant_mortality" -> v / 100.0
    "smoking" -> v / 60.0
    "suicide_rate" -> v / 40.0
    else -> v / 100.0 // percentages
}.toFloat().coerceIn(0f, 1f)

private val GOOD_LABELS = listOf("Molt baixa", "Baixa", "Moderada", "Alta", "Molt alta")
private val RISK_LABELS = listOf("Baix", "Moderat", "Alt", "Molt alt", "Extrem")
private val HIGH_LABELS = listOf("Total", "Alta", "Parcial", "Baixa")

/** Lower value is better: four thresholds, five bands (green → red). */
private fun low(v: Double, a: Double, b: Double, c: Double, e: Double, riskLabels: Boolean = false): HealthQuality {
    val labels = if (riskLabels) RISK_LABELS else GOOD_LABELS
    return when {
        v <= a -> HealthQuality(AtlasVisited, labels[0])
        v <= b -> HealthQuality(AtlasOlive, labels[1])
        v <= c -> HealthQuality(AtlasGold, labels[2])
        v <= e -> HealthQuality(AtlasDelay, labels[3])
        else -> HealthQuality(AtlasError, labels[4])
    }
}

/** Higher value is better: three thresholds, four bands (green → orange). */
private fun high(v: Double, a: Double, b: Double, c: Double): HealthQuality = when {
    v >= a -> HealthQuality(AtlasVisited, HIGH_LABELS[0])
    v >= b -> HealthQuality(AtlasOlive, HIGH_LABELS[1])
    v >= c -> HealthQuality(AtlasGold, HIGH_LABELS[2])
    else -> HealthQuality(AtlasDelay, HIGH_LABELS[3])
}
