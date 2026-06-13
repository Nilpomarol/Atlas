package com.atlas.ui.screens.countryinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import com.atlas.presentation.country.CountryFactView
import com.atlas.presentation.country.SectionItem
import com.atlas.ui.components.AtlasSectionLabel
import com.atlas.ui.theme.AtlasDelay
import com.atlas.ui.theme.AtlasError
import com.atlas.ui.theme.AtlasGold
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasOnSurfaceFaint
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOlive
import com.atlas.ui.theme.AtlasPlanned
import com.atlas.ui.theme.AtlasSerif
import com.atlas.ui.theme.AtlasSurfaceSubtle
import com.atlas.ui.theme.AtlasVisited
import com.atlas.ui.theme.AtlasWished
import kotlin.math.abs

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
            LifeExpectancyGap(male, female, le.unit ?: "anys")
        }
    }
}

/** Male vs female life expectancy as a dumbbell on a zoomed axis so the gap shows. */
@Composable
private fun LifeExpectancyGap(male: CountryFactView, female: CountryFactView, unit: String) {
    val axisMin = 50.0
    val axisMax = 90.0
    val span = axisMax - axisMin
    fun frac(v: Double) = ((v - axisMin) / span).toFloat().coerceIn(0f, 1f)
    val m = male.value.toCaDouble()
    val f = female.value.toCaDouble()
    val mF = frac(m)
    val fF = frac(f)
    val lo = minOf(mF, fF)
    val hi = maxOf(mF, fF)
    val dot = 14.dp

    Column(Modifier.padding(top = 2.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        BoxWithConstraints(Modifier.fillMaxWidth().height(dot)) {
            val w = maxWidth
            Box(
                Modifier.align(Alignment.CenterStart).fillMaxWidth().height(4.dp)
                    .clip(RoundedCornerShape(2.dp)).background(AtlasSurfaceSubtle),
            )
            Box(
                Modifier.align(Alignment.CenterStart).offset(x = w * lo).width(w * (hi - lo)).height(4.dp)
                    .background(AtlasGold),
            )
            GenderMarker(this, (w * mF - dot / 2).coerceIn(0.dp, w - dot), AtlasPlanned)
            GenderMarker(this, (w * fF - dot / 2).coerceIn(0.dp, w - dot), AtlasWished)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("♂ ${male.value}", style = MaterialTheme.typography.labelMedium, color = AtlasPlanned, fontWeight = FontWeight.Medium)
            Text("Δ ${formatPct(abs(f - m))} $unit", style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceMuted)
            Text("♀ ${female.value}", style = MaterialTheme.typography.labelMedium, color = AtlasWished, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun GenderMarker(scope: androidx.compose.foundation.layout.BoxWithConstraintsScope, offsetX: androidx.compose.ui.unit.Dp, color: Color) {
    with(scope) {
        Box(
            Modifier.align(Alignment.CenterStart).offset(x = offsetX).size(14.dp)
                .clip(CircleShape).background(color).border(2.dp, Color.White, CircleShape),
        )
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

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AtlasSectionLabel("Sistema sanitari")
        // Capacity per 1.000 inhabitants, as comparable bars.
        physicians?.let { CapacityMeter(it, scale = 7.0) }
        beds?.let { CapacityMeter(it, scale = 13.0) }
        // Spending: share of GDP as the headline, per-capita as context.
        if (expGdp != null || expPc != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text("Despesa en salut", style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceFaint)
                    expGdp?.let {
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(it.value, fontFamily = AtlasSerif, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = AtlasOnSurfaceStrong)
                            Text(it.unit ?: "% PIB", style = MaterialTheme.typography.bodyMedium, color = AtlasOnSurfaceMuted, modifier = Modifier.padding(bottom = 2.dp))
                        }
                    }
                }
                expPc?.let {
                    Text(
                        "${formatCompactValue(it.value)} ${it.unit ?: ""}/càpita".trim(),
                        style = MaterialTheme.typography.labelMedium,
                        color = AtlasOnSurfaceMuted,
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CapacityMeter(fact: CountryFactView, scale: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(fact.label, color = AtlasOnSurfaceMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                Text("${fact.value} ${fact.unit ?: ""}".trim(), color = AtlasOnSurfaceStrong, fontWeight = FontWeight.Medium)
                if (fact.rank != null && fact.rankTotal != null && fact.rankTotal > 1) {
                    Text("${formatOrdinal(fact.rank)} de ${fact.rankTotal}", style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceFaint)
                }
            }
        }
        TrackBar((fact.value.toCaDouble() / scale).toFloat().coerceIn(0f, 1f), AtlasNavy)
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
