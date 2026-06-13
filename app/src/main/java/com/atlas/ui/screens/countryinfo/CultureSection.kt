@file:OptIn(ExperimentalLayoutApi::class)

package com.atlas.ui.screens.countryinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.atlas.presentation.country.SectionItem
import com.atlas.ui.components.AtlasSectionLabel
import com.atlas.ui.theme.AtlasGold
import com.atlas.ui.theme.AtlasOnSurfaceFaint
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSerif

@Composable
internal fun CultureSection(items: List<SectionItem>) {
    val byKey = items.filterIsInstance<SectionItem.FactItem>().associate { it.fact.key to it.fact }

    Column(
        Modifier.padding(top = 6.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        // Religion donut is the cultural centerpiece.
        byKey["religion"]?.let { BreakdownRow(it) }

        // Official languages as accent chips.
        byKey["languages"]?.let { langs ->
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                AtlasSectionLabel("Llengües oficials")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    langs.value.split(", ").forEach { AccentChip(it, AtlasPrimary) }
                }
            }
        }

        // Heritage & recognition as gold trophy tiles.
        val unesco = byKey["unesco_sites"]
        val nobel = byKey["nobel_laureates"]
        if (unesco != null || nobel != null) {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                AtlasSectionLabel("Patrimoni i reconeixement")
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (unesco != null) TrophyTile("Patrimoni de la Humanitat", unesco.value, rankCaption(unesco)) else Spacer(Modifier.weight(1f))
                    if (nobel != null) TrophyTile("Premis Nobel", nobel.value, rankCaption(nobel)) else Spacer(Modifier.weight(1f))
                }
            }
        }

        // Tourism.
        val arrivals = byKey["tourist_arrivals"]
        val receipts = byKey["tourism_receipts"]
        if (arrivals != null || receipts != null) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AtlasSectionLabel("Turisme")
                arrivals?.let {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(formatCompactValue(it.value), fontFamily = AtlasSerif, fontSize = 26.sp, fontWeight = FontWeight.Medium, color = AtlasOnSurfaceStrong)
                                it.unit?.let { u -> Text(u, style = MaterialTheme.typography.bodyMedium, color = AtlasOnSurfaceMuted, modifier = Modifier.padding(bottom = 2.dp)) }
                            }
                            it.tier?.let { t -> RatingChip(tierColor(t), t) }
                        }
                        Text(it.label, style = MaterialTheme.typography.labelMedium, color = AtlasOnSurfaceMuted)
                        rankCaption(it)?.let { c -> Text(c, style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceFaint) }
                    }
                }
                receipts?.let {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text(it.label, color = AtlasOnSurfaceMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${formatCompactValue(it.value)} ${it.unit ?: ""}".trim(), color = AtlasOnSurfaceStrong, fontWeight = FontWeight.Medium)
                            rankCaption(it)?.let { c -> Text(c, style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceFaint) }
                        }
                    }
                }
            }
        }

        // Reference details as a compact two-column grid.
        val details = listOfNotNull(
            byKey["currency"]?.let { KeyFact("Moneda", it.value, mono = false) },
            byKey["demonym"]?.let { KeyFact("Gentilici", it.value, mono = false) },
            byKey["national_holiday"]?.let { KeyFact("Festa nacional", formatNationalDay(it.value), mono = false) },
            byKey["anthem"]?.let { KeyFact("Himne nacional", it.value, mono = false) },
            byKey["olympic_code"]?.let { KeyFact("Codi olímpic", it.value, mono = true) },
            byKey["fifa_code"]?.let { KeyFact("Codi FIFA", it.value, mono = true) },
        )
        if (details.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AtlasSectionLabel("Detalls")
                details.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        row.forEach { KeyFactCell(it) }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.TrophyTile(label: String, value: String, caption: String?) {
    Box(
        Modifier.weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(14.dp))
            .background(AtlasGold.copy(alpha = 0.12f))
            .border(1.dp, AtlasGold.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, fontFamily = AtlasSerif, fontSize = 30.sp, fontWeight = FontWeight.Medium, color = AtlasGold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceMuted, maxLines = 2)
            caption?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceFaint) }
        }
    }
}

@Composable
private fun AccentChip(text: String, color: Color) {
    Box(Modifier.clip(RoundedCornerShape(20.dp)).background(color.copy(alpha = 0.13f)).padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Medium)
    }
}

private val CA_MONTHS = listOf(
    "gener", "febrer", "març", "abril", "maig", "juny",
    "juliol", "agost", "setembre", "octubre", "novembre", "desembre",
)

/** "1919-08-19" -> "19 d'agost". The year is dropped (often a founding year, sometimes dirty). */
private fun formatNationalDay(raw: String): String {
    val parts = raw.split("-")
    if (parts.size == 3) {
        val month = parts[1].toIntOrNull()
        val day = parts[2].toIntOrNull()
        if (month != null && month in 1..12 && day != null) {
            val name = CA_MONTHS[month - 1]
            val prep = if (name.first() in "aeiouàèéíòóú") "d'" else "de "
            return "$day $prep$name"
        }
    }
    return raw
}
