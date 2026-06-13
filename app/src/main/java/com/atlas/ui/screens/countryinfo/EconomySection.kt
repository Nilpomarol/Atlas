@file:OptIn(ExperimentalLayoutApi::class)

package com.atlas.ui.screens.countryinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.atlas.presentation.country.CompositionSegment
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
import com.atlas.ui.theme.AtlasPlanned
import com.atlas.ui.theme.AtlasSerif
import com.atlas.ui.theme.AtlasVisited
import kotlin.math.abs

@Composable
internal fun EconomySection(items: List<SectionItem>) {
    val byKey = items.filterIsInstance<SectionItem.FactItem>().associate { it.fact.key to it.fact }
    val sectors = items.filterIsInstance<SectionItem.CompositionItem>()
        .firstOrNull { it.title == "Sectors econòmics" }?.segments

    Column(
        Modifier.padding(top = 6.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        GdpStat(stat = byKey["gdp"], ppp = byKey["gdp_ppp"], label = "PIB", big = true)
        GdpStat(stat = byKey["gdp_per_capita"], ppp = byKey["gdp_pc_ppp"], label = "PIB per càpita", big = false)
        sectors?.let { SectorsBar(it) }
        IndicatorsBlock(byKey)
        TradeBlock(byKey)
        PublicFinanceBlock(byKey)
        InvestmentBlock(byKey)
    }
}

@Composable
private fun TradeBlock(byKey: Map<String, CountryFactView>) {
    val exports = byKey["exports_gdp"]
    val imports = byKey["imports_gdp"]
    val currentAccount = byKey["current_account"]
    if (listOfNotNull(exports, imports, currentAccount).isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AtlasSectionLabel("Comerç exterior")
        // Exports vs imports on a shared scale so the balance is visible.
        if (exports != null && imports != null) {
            val e = exports.value.toCaDouble()
            val i = imports.value.toCaDouble()
            val scale = maxOf(e, i, 1.0)
            MeterRow(exports.label, "${exports.value} ${exports.unit ?: ""}".trim(), (e / scale).toFloat(), AtlasVisited)
            MeterRow(imports.label, "${imports.value} ${imports.unit ?: ""}".trim(), (i / scale).toFloat(), AtlasNavy)
        } else {
            exports?.let { StatRow(it.label, "${it.value} ${it.unit ?: ""}".trim(), AtlasOnSurfaceStrong) }
            imports?.let { StatRow(it.label, "${it.value} ${it.unit ?: ""}".trim(), AtlasOnSurfaceStrong) }
        }
        // Derived figures as side-by-side callouts.
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (exports != null && imports != null) {
                val bal = exports.value.toCaDouble() - imports.value.toCaDouble()
                StatTile("Balança comercial", "${signPrefix(bal)}${formatPct(abs(bal))} % PIB", signedColor(bal))
            } else {
                Spacer(Modifier.weight(1f))
            }
            if (currentAccount != null) {
                val v = currentAccount.value.toCaDouble()
                StatTile("Compte corrent", "${signPrefix(v)}${formatPct(abs(v))} ${currentAccount.unit ?: ""}".trim(), signedColor(v))
            } else {
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PublicFinanceBlock(byKey: Map<String, CountryFactView>) {
    val debt = byKey["gov_debt"]
    val fiscal = byKey["fiscal_balance"]
    val tax = byKey["tax_revenue"]
    if (listOfNotNull(debt, fiscal, tax).isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AtlasSectionLabel("Finances públiques")
        // Government debt is the headline: big value + rating + meter.
        debt?.let {
            val v = it.value.toCaDouble()
            val rating = debtRating(v)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(it.label, style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceFaint)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(it.value, fontFamily = AtlasSerif, fontSize = 26.sp, fontWeight = FontWeight.Medium, color = AtlasOnSurfaceStrong)
                        it.unit?.let { u -> Text(u, style = MaterialTheme.typography.bodyMedium, color = AtlasOnSurfaceMuted, modifier = Modifier.padding(bottom = 2.dp)) }
                    }
                    RatingChip(rating.color, rating.label)
                }
                TrackBar((v / 150.0).toFloat().coerceIn(0f, 1f), rating.color)
            }
        }
        // Fiscal balance + tax pressure as callouts.
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (fiscal != null) {
                val v = fiscal.value.toCaDouble()
                StatTile(fiscal.label, "${signPrefix(v)}${formatPct(abs(v))} ${fiscal.unit ?: ""}".trim(), signedColor(v))
            } else {
                Spacer(Modifier.weight(1f))
            }
            if (tax != null) {
                StatTile(tax.label, "${tax.value} ${tax.unit ?: ""}".trim(), AtlasNavy)
            } else {
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InvestmentBlock(byKey: Map<String, CountryFactView>) {
    val fdi = byKey["fdi_inflows"]
    val reserves = byKey["reserves"]
    if (listOfNotNull(fdi, reserves).isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AtlasSectionLabel("Inversió exterior")
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (fdi != null) {
                StatTile("Inversió estrangera", "${formatCompactValue(fdi.value)} ${fdi.unit ?: ""}".trim(), AtlasNavy, caption = rankCaption(fdi))
            } else {
                Spacer(Modifier.weight(1f))
            }
            if (reserves != null) {
                StatTile(reserves.label, "${formatCompactValue(reserves.value)} ${reserves.unit ?: ""}".trim(), AtlasNavy, caption = rankCaption(reserves))
            } else {
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

private fun signPrefix(v: Double): String = if (v > 0) "+" else if (v < 0) "−" else ""

private fun debtRating(v: Double): Rating = when {
    v <= 40.0 -> Rating(AtlasVisited, "Baix")
    v <= 70.0 -> Rating(AtlasOlive, "Moderat")
    v <= 100.0 -> Rating(AtlasGold, "Alt")
    v <= 150.0 -> Rating(AtlasDelay, "Molt alt")
    else -> Rating(AtlasError, "Crític")
}

@Composable
private fun MeterRow(label: String, valueText: String, fraction: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(label, color = AtlasOnSurfaceMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(valueText, color = AtlasOnSurfaceStrong, fontWeight = FontWeight.Medium)
        }
        TrackBar(fraction.coerceIn(0f, 1f), color)
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

@Composable
private fun GdpStat(stat: CountryFactView?, ppp: CountryFactView?, label: String, big: Boolean) {
    if (stat == null) return
    Column(verticalArrangement = Arrangement.spacedBy(if (big) 7.dp else 5.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    formatCompactValue(stat.value),
                    fontFamily = AtlasSerif,
                    fontSize = if (big) 32.sp else 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = AtlasOnSurfaceStrong,
                )
                stat.unit?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = AtlasOnSurfaceMuted, modifier = Modifier.padding(bottom = if (big) 4.dp else 2.dp)) }
                stat.tier?.let { Box(Modifier.padding(bottom = if (big) 3.dp else 1.dp)) { TierChip(it) } }
            }
            Text(label, style = MaterialTheme.typography.labelMedium, color = AtlasOnSurfaceMuted, modifier = Modifier.padding(bottom = if (big) 5.dp else 2.dp))
        }
        if (stat.rank != null && stat.rankTotal != null && stat.rankTotal > 1) {
            PositionBar(stat.rank, stat.rankTotal, tierColor(stat.tier))
            val pppText = ppp?.let { "  ·  PPA ${formatCompactValue(it.value)} ${it.unit ?: ""}".trim() } ?: ""
            Text(
                "${formatOrdinal(stat.rank)} de ${stat.rankTotal}$pppText",
                style = if (big) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
                color = AtlasOnSurfaceFaint,
            )
        } else {
            ppp?.let { Text("PPA · ${formatCompactValue(it.value)} ${it.unit ?: ""}".trim(), style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceFaint) }
        }
    }
}

private val SECTOR_COLORS = listOf(AtlasOlive, AtlasGold, AtlasPlanned)

@Composable
private fun SectorsBar(segments: List<CompositionSegment>) {
    val present = segments.filter { it.pct > 0.0 }
    if (present.isEmpty()) return
    val total = present.sumOf { it.pct }.coerceAtLeast(0.0001)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AtlasSectionLabel("Sectors econòmics")
        Row(Modifier.fillMaxWidth().height(26.dp).clip(RoundedCornerShape(7.dp))) {
            present.forEachIndexed { i, seg ->
                Box(
                    Modifier.weight((seg.pct / total).toFloat()).fillMaxHeight().background(SECTOR_COLORS[i % SECTOR_COLORS.size]),
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
                    Dot(SECTOR_COLORS[i % SECTOR_COLORS.size])
                    Spacer(Modifier.width(6.dp))
                    Text(seg.label, style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceMuted)
                }
            }
        }
    }
}

@Composable
private fun IndicatorsBlock(byKey: Map<String, CountryFactView>) {
    val growth = byKey["gdp_growth"]
    val inflation = byKey["inflation"]
    val unemployment = byKey["unemployment"]
    val rd = byKey["rd_expenditure"]
    val labor = byKey["labor_force"]
    if (listOfNotNull(growth, inflation, unemployment, rd, labor).isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
        AtlasSectionLabel("Indicadors")
        // The two signed indicators sit side by side as callout tiles, not a list.
        if (inflation != null || growth != null) {
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (inflation != null) {
                    IndicatorCell(inflation.label, "${inflation.value} ${inflation.unit ?: ""}".trim(), inflationRating(inflation.value.toCaDouble()))
                } else {
                    Spacer(Modifier.weight(1f))
                }
                if (growth != null) {
                    val v = growth.value.toCaDouble()
                    val arrow = if (v > 0) "▲ " else if (v < 0) "▼ " else ""
                    IndicatorCell("Creixement PIB", "$arrow${growth.value} ${growth.unit ?: ""}".trim(), growthRating(v))
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
        // Bounded indicators get a meter on a sensible 0..max scale.
        unemployment?.let {
            val v = it.value.toCaDouble()
            val r = unemploymentRating(v)
            IndicatorRow(it, "${it.value} ${it.unit ?: ""}".trim(), r) { TrackBar((v / 25.0).toFloat().coerceIn(0f, 1f), r.color) }
        }
        rd?.let {
            val v = it.value.toCaDouble()
            val r = rdRating(v)
            val rankText = if (it.rank != null && it.rankTotal != null) "${formatOrdinal(it.rank)} de ${it.rankTotal}" else null
            IndicatorRow(it, "${it.value} ${it.unit ?: ""}".trim(), r, caption = rankText) { TrackBar((v / 5.0).toFloat().coerceIn(0f, 1f), r.color) }
        }
        labor?.let {
            val v = it.value.toCaDouble()
            val r = laborRating(v)
            IndicatorRow(it, "${it.value} ${it.unit ?: ""}".trim(), r) { TrackBar((v / 100.0).toFloat().coerceIn(0f, 1f), r.color) }
        }
    }
}

@Composable
private fun IndicatorRow(
    fact: CountryFactView,
    valueText: String,
    rating: Rating,
    caption: String? = null,
    valueColor: Color = AtlasOnSurfaceStrong,
    bar: (@Composable () -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(fact.label, color = AtlasOnSurfaceMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(valueText, color = valueColor, fontWeight = FontWeight.Medium)
                RatingChip(rating.color, rating.label)
            }
        }
        bar?.invoke()
        caption?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceFaint) }
    }
}

@Composable
private fun RowScope.IndicatorCell(label: String, valueText: String, rating: Rating) {
    Box(
        Modifier.weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(14.dp))
            .background(rating.color.copy(alpha = 0.10f))
            .border(1.dp, rating.color.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
            .padding(13.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false),
                )
                RatingChip(rating.color, rating.label)
            }
            Text(valueText, fontFamily = AtlasSerif, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = AtlasOnSurfaceStrong)
        }
    }
}

private fun growthRating(v: Double): Rating = when {
    v >= 4.0 -> Rating(AtlasVisited, "Fort")
    v >= 1.5 -> Rating(AtlasOlive, "Moderat")
    v >= 0.0 -> Rating(AtlasGold, "Feble")
    else -> Rating(AtlasError, "Recessió")
}

private fun inflationRating(v: Double): Rating = when {
    v < 0.0 -> Rating(AtlasDelay, "Deflació")
    v < 1.0 -> Rating(AtlasGold, "Molt baixa")
    v <= 4.0 -> Rating(AtlasVisited, "Controlada")
    v <= 8.0 -> Rating(AtlasGold, "Elevada")
    v <= 25.0 -> Rating(AtlasDelay, "Alta")
    else -> Rating(AtlasError, "Molt alta")
}

private fun unemploymentRating(v: Double): Rating = when {
    v <= 4.0 -> Rating(AtlasVisited, "Baix")
    v <= 8.0 -> Rating(AtlasOlive, "Moderat")
    v <= 12.0 -> Rating(AtlasGold, "Alt")
    v <= 20.0 -> Rating(AtlasDelay, "Molt alt")
    else -> Rating(AtlasError, "Crític")
}

private fun rdRating(v: Double): Rating = when {
    v >= 2.5 -> Rating(AtlasVisited, "Alta")
    v >= 1.0 -> Rating(AtlasOlive, "Mitjana")
    v >= 0.5 -> Rating(AtlasGold, "Baixa")
    else -> Rating(AtlasDelay, "Molt baixa")
}

private fun laborRating(v: Double): Rating = when {
    v >= 65.0 -> Rating(AtlasVisited, "Alta")
    v >= 55.0 -> Rating(AtlasOlive, "Mitjana")
    v >= 45.0 -> Rating(AtlasGold, "Baixa")
    else -> Rating(AtlasDelay, "Molt baixa")
}
