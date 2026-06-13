package com.atlas.ui.screens.countryinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasOlive
import com.atlas.ui.theme.AtlasOnSurfaceFaint
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasSerif
import com.atlas.ui.theme.AtlasVisited
import kotlin.math.abs

@Composable
internal fun DevelopmentSection(items: List<SectionItem>) {
    val byKey = items.filterIsInstance<SectionItem.FactItem>().associate { it.fact.key to it.fact }
    Column(
        Modifier.padding(top = 6.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        HumanDevelopment(byKey)
        Education(byKey)
        Inequality(byKey)
    }
}

@Composable
private fun HumanDevelopment(byKey: Map<String, CountryFactView>) {
    val hdi = byKey["hdi"]
    val ihdi = byKey["ihdi"]
    val gii = byKey["gii"]
    val gdi = byKey["gdi"]
    if (listOfNotNull(hdi, ihdi, gii, gdi).isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        hdi?.let {
            val v = it.value.toCaDouble()
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text(it.value, fontFamily = AtlasSerif, fontSize = 32.sp, fontWeight = FontWeight.Medium, color = AtlasOnSurfaceStrong)
                    it.tier?.let { t -> RatingChip(tierColor(t), t) }
                }
                Text("IDH · índex de desenvolupament humà", style = MaterialTheme.typography.labelMedium, color = AtlasOnSurfaceMuted)
                TrackBar(v.toFloat().coerceIn(0f, 1f), tierColor(it.tier))
                rankCaption(it)?.let { c -> Text(c, style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceFaint) }
            }
        }
        ihdi?.let { MetricMeter(it.label, it.value, it.value.toCaDouble(), tierColor(it.tier), it.tier, rankCaption(it)) }
        // Gender indices side by side as callout tiles.
        if (gii != null || gdi != null) {
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (gii != null) StatTile("Desigualtat de gènere", gii.value, tierColor(gii.tier), rankCaption(gii)) else Spacer(Modifier.weight(1f))
                if (gdi != null) {
                    val r = parityRating(gdi.value.toCaDouble())
                    StatTile("Desenv. de gènere", gdi.value, r.color, r.label)
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun Education(byKey: Map<String, CountryFactView>) {
    val literacy = byKey["literacy"]
    val mean = byKey["mean_schooling"]
    val expected = byKey["expected_schooling"]
    val compulsory = byKey["compulsory_years"]
    val primary = byKey["primary_enroll"]
    val secondary = byKey["secondary_enroll"]
    val tertiary = byKey["tertiary_enroll"]
    val expenditure = byKey["edu_expenditure"]
    if (listOfNotNull(literacy, mean, expected, primary, secondary, tertiary, expenditure).isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AtlasSectionLabel("Educació")
        literacy?.let {
            MetricMeter(it.label, "${it.value} ${it.unit ?: ""}".trim(), it.value.toCaDouble() / 100.0, tierColor(it.tier), it.tier, rankCaption(it))
        }
        // Years of schooling: actual vs expected on a shared 0–20 scale.
        if (mean != null || expected != null) {
            mean?.let { MetricMeter(it.label, "${it.value} ${it.unit ?: ""}".trim(), it.value.toCaDouble() / 20.0, AtlasOlive) }
            expected?.let {
                MetricMeter(it.label, "${it.value} ${it.unit ?: ""}".trim(), it.value.toCaDouble() / 20.0, AtlasVisited,
                    caption = compulsory?.let { c -> "Obligatòria: ${c.value} ${c.unit ?: ""}".trim() })
            }
        }
        // Enrollment ratios as a compact trio of tiles.
        if (primary != null || secondary != null || tertiary != null) {
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (primary != null) StatTile("Primària", "${primary.value} %", AtlasNavy) else Spacer(Modifier.weight(1f))
                if (secondary != null) StatTile("Secundària", "${secondary.value} %", AtlasNavy) else Spacer(Modifier.weight(1f))
                if (tertiary != null) StatTile("Terciària", "${tertiary.value} %", AtlasNavy, rankCaption(tertiary)) else Spacer(Modifier.weight(1f))
            }
        }
        // Education spending featured: how much of GDP a country invests in education.
        expenditure?.let {
            val v = it.value.toCaDouble()
            val r = eduSpendRating(v)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(it.label, style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceFaint)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(it.value, fontFamily = AtlasSerif, fontSize = 24.sp, fontWeight = FontWeight.Medium, color = AtlasOnSurfaceStrong)
                        it.unit?.let { u -> Text(u, style = MaterialTheme.typography.bodyMedium, color = AtlasOnSurfaceMuted, modifier = Modifier.padding(bottom = 2.dp)) }
                    }
                    RatingChip(r.color, r.label)
                }
                TrackBar((v / 12.0).toFloat().coerceIn(0f, 1f), r.color)
            }
        }
    }
}

private fun eduSpendRating(v: Double): Rating = when {
    v >= 6.0 -> Rating(AtlasVisited, "Alta")
    v >= 4.5 -> Rating(AtlasOlive, "Mitjana")
    v >= 3.0 -> Rating(AtlasGold, "Baixa")
    else -> Rating(AtlasDelay, "Molt baixa")
}

@Composable
private fun Inequality(byKey: Map<String, CountryFactView>) {
    val gini = byKey["gini"]
    val top10 = byKey["income_top10"]
    val bottom10 = byKey["income_bottom10"]
    val povertyNat = byKey["poverty_national"]
    val poverty215 = byKey["poverty_215"]
    if (listOfNotNull(gini, top10, bottom10, povertyNat, poverty215).isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
        AtlasSectionLabel("Desigualtat")
        // Gini as the featured inequality figure.
        gini?.let {
            val v = it.value.toCaDouble()
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text(it.value, fontFamily = AtlasSerif, fontSize = 26.sp, fontWeight = FontWeight.Medium, color = AtlasOnSurfaceStrong)
                    it.tier?.let { t -> RatingChip(tierColor(t), t) }
                }
                Text("Índex de Gini", style = MaterialTheme.typography.labelMedium, color = AtlasOnSurfaceMuted)
                TrackBar((v / 100.0).toFloat().coerceIn(0f, 1f), tierColor(it.tier))
                rankCaption(it)?.let { c -> Text(c, style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceFaint) }
            }
        }
        // Income concentration: top vs bottom decile on a shared scale.
        if (top10 != null && bottom10 != null) {
            MetricMeter(top10.label, "${top10.value} ${top10.unit ?: ""}".trim(), top10.value.toCaDouble() / 45.0, AtlasNavy)
            MetricMeter(bottom10.label, "${bottom10.value} ${bottom10.unit ?: ""}".trim(), bottom10.value.toCaDouble() / 45.0, AtlasOlive)
        }
        // Poverty as a callout pair.
        if (povertyNat != null || poverty215 != null) {
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (povertyNat != null) {
                    val r = povertyRating(povertyNat.value.toCaDouble())
                    StatTile("Pobresa nacional", "${povertyNat.value} %", r.color, r.label)
                } else {
                    Spacer(Modifier.weight(1f))
                }
                if (poverty215 != null) {
                    val r = povertyRating(poverty215.value.toCaDouble())
                    StatTile("Pobresa extrema", "${poverty215.value} %", r.color, r.label)
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MetricMeter(
    label: String,
    valueText: String,
    fraction: Double,
    color: Color,
    ratingLabel: String? = null,
    caption: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(label, color = AtlasOnSurfaceMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(valueText, color = AtlasOnSurfaceStrong, fontWeight = FontWeight.Medium)
                ratingLabel?.let { RatingChip(color, it) }
            }
        }
        TrackBar(fraction.toFloat().coerceIn(0f, 1f), color)
        caption?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceFaint) }
    }
}

private fun parityRating(v: Double): Rating {
    val d = abs(1.0 - v)
    return when {
        d <= 0.03 -> Rating(AtlasVisited, "Paritat")
        d <= 0.07 -> Rating(AtlasOlive, "Lleu bretxa")
        d <= 0.12 -> Rating(AtlasGold, "Bretxa")
        else -> Rating(AtlasDelay, "Gran bretxa")
    }
}

private fun povertyRating(v: Double): Rating = when {
    v <= 2.0 -> Rating(AtlasVisited, "Molt baixa")
    v <= 10.0 -> Rating(AtlasOlive, "Baixa")
    v <= 25.0 -> Rating(AtlasGold, "Moderada")
    v <= 40.0 -> Rating(AtlasDelay, "Alta")
    else -> Rating(AtlasError, "Molt alta")
}
