package com.atlas.ui.screens.countryinfo

import androidx.compose.foundation.layout.Arrangement
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
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasOnSurfaceFaint
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasSerif

@Composable
internal fun InfrastructureSection(items: List<SectionItem>) {
    val byKey = items.filterIsInstance<SectionItem.FactItem>().associate { it.fact.key to it.fact }
    val internet = byKey["internet_users"]
    val mobile = byKey["mobile_subs"]
    val broadband = byKey["broadband"]
    val air = byKey["air_passengers"]

    Column(
        Modifier.padding(top = 6.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Internet penetration is the connectivity headline.
        internet?.let {
            val v = it.value.toCaDouble()
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(it.value, fontFamily = AtlasSerif, fontSize = 32.sp, fontWeight = FontWeight.Medium, color = AtlasOnSurfaceStrong)
                        it.unit?.let { u -> Text(u, style = MaterialTheme.typography.bodyMedium, color = AtlasOnSurfaceMuted, modifier = Modifier.padding(bottom = 4.dp)) }
                    }
                    it.tier?.let { t -> RatingChip(tierColor(t), t) }
                }
                Text(it.label, style = MaterialTheme.typography.labelMedium, color = AtlasOnSurfaceMuted)
                TrackBar((v / 100.0).toFloat().coerceIn(0f, 1f), tierColor(it.tier))
                rankCaption(it)?.let { c -> Text(c, style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceFaint) }
            }
        }
        // Mobile and fixed broadband as meters (per 100 inhabitants).
        mobile?.let { MeterRow(it, (it.value.toCaDouble() / 100.0).toFloat().coerceIn(0f, 1f), AtlasNavy) }
        broadband?.let { MeterRow(it, (it.value.toCaDouble() / 60.0).toFloat().coerceIn(0f, 1f), AtlasNavy, rankCaption(it)) }
        // Air travel.
        air?.let {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Transport aeri", style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceFaint)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(formatCompactValue(it.value), fontFamily = AtlasSerif, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = AtlasOnSurfaceStrong)
                        it.unit?.let { u -> Text(u, style = MaterialTheme.typography.bodyMedium, color = AtlasOnSurfaceMuted, modifier = Modifier.padding(bottom = 2.dp)) }
                    }
                    rankCaption(it)?.let { c -> Text(c, style = MaterialTheme.typography.labelMedium, color = AtlasOnSurfaceFaint, modifier = Modifier.padding(bottom = 2.dp)) }
                }
            }
        }
    }
}

@Composable
private fun MeterRow(fact: CountryFactView, fraction: Float, color: Color, caption: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(fact.label, color = AtlasOnSurfaceMuted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text("${fact.value} ${fact.unit ?: ""}".trim(), color = AtlasOnSurfaceStrong, fontWeight = FontWeight.Medium)
        }
        TrackBar(fraction, color)
        caption?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceFaint) }
    }
}
