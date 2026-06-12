@file:OptIn(ExperimentalLayoutApi::class)

package com.atlas.ui.screens.countryinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.presentation.country.CountryBordersUi
import com.atlas.presentation.country.CountryFactView
import com.atlas.presentation.country.SectionItem
import com.atlas.ui.components.AtlasSectionLabel
import com.atlas.ui.components.geo.AtlasGeoCanvas
import com.atlas.ui.components.geo.GeoCoordinate
import com.atlas.ui.components.geo.GeoMarker
import com.atlas.ui.components.geo.GeoViewport
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasDelay
import com.atlas.ui.theme.AtlasError
import com.atlas.ui.theme.AtlasGold
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasOlive
import com.atlas.ui.theme.AtlasOnSurfaceFaint
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasSerif
import com.atlas.ui.theme.AtlasSurfaceSubtle
import com.atlas.ui.theme.AtlasVisited
import kotlin.math.roundToInt

private const val EVEREST_M = 8848.0

@Composable
internal fun GeographySection(items: List<SectionItem>, bordersMap: CountryBordersUi? = null) {
    val byKey = items.filterIsInstance<SectionItem.FactItem>().associate { it.fact.key to it.fact }
    fun v(key: String) = byKey[key]?.value

    Column(
        Modifier.padding(top = 6.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Headline(
            location = listOfNotNull(v("continent"), v("subregion")).distinct().joinToString(" · "),
            area = byKey["area"],
        )

        v("highest_point")?.let { HighestPointFeature(it) }

        BadgeRow(
            landlocked = v("landlocked"),
            borderCount = v("border_count"),
            capital = v("capital"),
        )

        when {
            bordersMap != null -> BordersMap(bordersMap)
            v("borders") != null -> BordersBlock(v("borders")!!)
        }

        LandUseBlock(
            forest = v("forest_land")?.toCaDouble(),
            agricultural = v("agri_land")?.toCaDouble(),
            arable = v("arable_land"),
        )

        EnvironmentBlock(byKey)
    }
}

@Composable
private fun Headline(location: String, area: CountryFactView?) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        if (location.isNotEmpty()) {
            Text(location, style = MaterialTheme.typography.labelMedium, color = AtlasOnSurfaceMuted)
        }
        if (area != null) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    formatCompactValue(area.value),
                    fontFamily = AtlasSerif,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium,
                    color = AtlasOnSurfaceStrong,
                )
                area.unit?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AtlasOnSurfaceMuted,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }
            if (area.rank != null && area.rankTotal != null && area.rankTotal > 1) {
                PositionBar(area.rank, area.rankTotal, tierColor(area.tier))
                Text(
                    "${formatOrdinal(area.rank)} més extens de ${area.rankTotal}" +
                        (area.tier?.let { " · $it" } ?: ""),
                    style = MaterialTheme.typography.labelMedium,
                    color = AtlasOnSurfaceFaint,
                )
            }
        }
    }
}

@Composable
private fun HighestPointFeature(raw: String) {
    val name = raw.substringBefore('·').trim()
    val elevText = Regex("([\\d.]+)\\s*m").find(raw)?.groupValues?.get(1)
    val meters = elevText?.replace(".", "")?.toDoubleOrNull()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Punt més alt", style = MaterialTheme.typography.labelMedium, color = AtlasOnSurfaceMuted)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                elevText ?: raw,
                fontFamily = AtlasSerif,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                color = AtlasOnSurfaceStrong,
            )
            if (elevText != null) {
                Text(
                    "m",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AtlasOnSurfaceMuted,
                    modifier = Modifier.padding(bottom = 3.dp),
                )
            }
            Text(
                "· $name",
                style = MaterialTheme.typography.bodyMedium,
                color = AtlasOnSurfaceMuted,
                modifier = Modifier.padding(bottom = 3.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (meters != null) {
            val fraction = (meters / EVEREST_M).toFloat().coerceIn(0f, 1f)
            TrackBar(fraction, AtlasOlive)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${(fraction * 100).roundToInt()}% de l'Everest",
                    style = MaterialTheme.typography.labelSmall,
                    color = AtlasOlive,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    "Everest · 8.848 m",
                    style = MaterialTheme.typography.labelSmall,
                    color = AtlasOnSurfaceFaint,
                )
            }
        }
    }
}

@Composable
private fun BadgeRow(landlocked: String?, borderCount: String?, capital: String?) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        capital?.let { GeoBadge("◎ $it", AtlasNavy) }
        landlocked?.let {
            if (it == "Sí") GeoBadge("Sense litoral", AtlasGold)
            else GeoBadge("Costaner", AtlasVisited)
        }
        borderCount?.let {
            val n = it.toIntOrNull()
            GeoBadge(if (n == 0) "Sense fronteres" else "$it fronteres", AtlasNavy)
        }
    }
}

@Composable
private fun BordersMap(b: CountryBordersUi) {
    val highlights = buildMap {
        put(b.selfIso2, AtlasPrimary)
        b.borders.forEach { put(it.iso2, AtlasNavy) }
    }
    val markers = buildList {
        if (b.selfLat != null && b.selfLng != null) {
            add(GeoMarker(GeoCoordinate(b.selfLat, b.selfLng), AtlasPrimary, label = b.selfIso3 ?: b.selfIso2, labelOnly = true))
        }
        b.borders.forEach {
            if (it.lat != null && it.lng != null) {
                add(GeoMarker(GeoCoordinate(it.lat, it.lng), AtlasNavy, label = it.iso3 ?: it.iso2, labelOnly = true))
            }
        }
    }
    val points = markers.map { it.coordinate }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Fronteres", style = MaterialTheme.typography.labelMedium, color = AtlasOnSurfaceFaint)
        AtlasGeoCanvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(14.dp)),
            viewport = if (points.isNotEmpty()) {
                GeoViewport.FitPoints(points = points, minLongitudeSpanDegrees = 4.0, minLatitudeSpanDegrees = 3.0)
            } else {
                GeoViewport.World()
            },
            markers = markers,
            highlightColorByIso2 = highlights,
            mapPaddingDp = 14f,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            b.borders.forEach { BorderPill(it.nameCa) }
        }
    }
}

@Composable
private fun BorderPill(name: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AtlasNavy.copy(alpha = 0.10f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Dot(AtlasNavy)
        Text(name, style = MaterialTheme.typography.labelMedium, color = AtlasOnSurfaceStrong)
    }
}

@Composable
private fun BordersBlock(borders: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Fronteres", style = MaterialTheme.typography.labelMedium, color = AtlasOnSurfaceFaint)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            borders.split(", ").forEach { Chip(it) }
        }
    }
}

@Composable
private fun LandUseBlock(forest: Double?, agricultural: Double?, arable: String?) {
    if (forest == null && agricultural == null) return
    val f = (forest ?: 0.0).coerceIn(0.0, 100.0)
    val a = (agricultural ?: 0.0).coerceIn(0.0, 100.0 - f)
    val other = (100.0 - f - a).coerceAtLeast(0.0)
    val segments = listOf(
        Triple("Bosc", f, AtlasVisited),
        Triple("Agrícola", a, AtlasOlive),
        Triple("Altres", other, AtlasSurfaceSubtle),
    ).filter { it.second > 0.0 }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AtlasSectionLabel("Ús del sòl")
        Row(Modifier.fillMaxWidth().height(26.dp).clip(RoundedCornerShape(7.dp))) {
            segments.forEach { (_, pct, color) ->
                Box(
                    Modifier.weight(pct.toFloat()).fillMaxHeight().background(color),
                    contentAlignment = Alignment.Center,
                ) {
                    if (pct >= 12.0) {
                        Text(
                            "${formatPct(pct)}%",
                            color = if (color == AtlasSurfaceSubtle) AtlasOnSurfaceMuted else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            segments.forEach { (label, _, color) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Dot(color)
                    Spacer(Modifier.width(6.dp))
                    Text(label, style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceMuted)
                }
            }
        }
        arable?.let {
            Text(
                "Dels quals, cultivable: $it%",
                style = MaterialTheme.typography.labelSmall,
                color = AtlasOnSurfaceFaint,
            )
        }
    }
}

@Composable
private fun EnvironmentBlock(byKey: Map<String, CountryFactView>) {
    val groups = listOf(
        "Energia" to listOf("renewable_energy", "electricity_access", "energy_per_capita"),
        "Emissions" to listOf("co2_per_capita", "co2_total"),
        "Entorn natural" to listOf("pm25", "protected_areas"),
    )
    val present = groups.mapNotNull { (label, keys) ->
        keys.mapNotNull { byKey[it] }.takeIf { it.isNotEmpty() }?.let { label to it }
    }
    if (present.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        AtlasSectionLabel("Medi ambient")
        present.forEach { (label, facts) ->
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = AtlasOnSurfaceFaint,
                )
                facts.forEach { fact ->
                    if (envQuality(fact.key, fact.value.toCaDouble()) != null) EnvMeter(fact)
                    else EnvNeutralRow(fact)
                }
            }
        }
    }
}

@Composable
private fun EnvMeter(fact: CountryFactView) {
    val value = fact.value.toCaDouble()
    val quality = envQuality(fact.key, value)
    val color = quality?.color ?: AtlasOnSurfaceMuted
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                fact.label,
                color = AtlasOnSurfaceMuted,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                Text(
                    fact.value + (fact.unit?.let { " $it" } ?: ""),
                    color = AtlasOnSurfaceStrong,
                    fontWeight = FontWeight.Medium,
                )
                quality?.let {
                    Text(it.label, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Medium)
                }
            }
        }
        TrackBar(envFraction(fact.key, value), color)
        if (fact.rank != null && fact.rankTotal != null && fact.rankTotal > 1) {
            Text(
                "${formatOrdinal(fact.rank)} de ${fact.rankTotal}",
                style = MaterialTheme.typography.labelSmall,
                color = AtlasOnSurfaceFaint,
            )
        }
    }
}

@Composable
private fun EnvNeutralRow(fact: CountryFactView) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                fact.label,
                color = AtlasOnSurfaceMuted,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Text(
                fact.value + (fact.unit?.let { " $it" } ?: ""),
                color = AtlasOnSurfaceStrong,
                fontWeight = FontWeight.Medium,
            )
        }
        if (fact.rank != null && fact.rankTotal != null && fact.rankTotal > 1) {
            Text(
                "${formatOrdinal(fact.rank)} de ${fact.rankTotal}",
                style = MaterialTheme.typography.labelSmall,
                color = AtlasOnSurfaceFaint,
            )
        }
    }
}

@Composable
private fun GeoBadge(text: String, color: Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.13f))
            .padding(horizontal = 11.dp, vertical = 6.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Medium)
    }
}

private data class Quality(val color: Color, val label: String)

/** Color + qualitative Catalan label for an environmental metric (green good, red poor). */
private fun envQuality(key: String, v: Double): Quality? = when (key) {
    "renewable_energy" -> band(v, 50.0, 25.0, 10.0, listOf("Alta", "Mitjana", "Baixa", "Molt baixa"))
    "protected_areas" -> band(v, 30.0, 17.0, 5.0, listOf("Alta", "Mitjana", "Baixa", "Molt baixa"))
    "electricity_access" -> band(v, 99.0, 90.0, 60.0, listOf("Total", "Alta", "Parcial", "Baixa"))
    "pm25" -> bandLow(v, 5.0, 15.0, 25.0, 35.0, listOf("Excel·lent", "Bona", "Moderada", "Dolenta", "Molt dolenta"))
    "co2_per_capita" -> bandLow(v, 2.0, 5.0, 10.0, 15.0, listOf("Molt baixes", "Baixes", "Moderades", "Altes", "Molt altes"))
    else -> null
}

/** Normalized 0..1 position for an environmental meter. */
private fun envFraction(key: String, v: Double): Float = when (key) {
    "pm25" -> (v / 75.0)
    "co2_per_capita" -> (v / 30.0)
    else -> (v / 100.0) // percentage metrics
}.toFloat().coerceIn(0f, 1f)

/** Higher value is better: three thresholds, four bands (green → orange). */
private fun band(v: Double, a: Double, b: Double, c: Double, labels: List<String>): Quality = when {
    v >= a -> Quality(AtlasVisited, labels[0])
    v >= b -> Quality(AtlasOlive, labels[1])
    v >= c -> Quality(AtlasGold, labels[2])
    else -> Quality(AtlasDelay, labels[3])
}

/** Lower value is better: four thresholds, five bands (green → red). */
private fun bandLow(v: Double, a: Double, b: Double, c: Double, e: Double, labels: List<String>): Quality = when {
    v <= a -> Quality(AtlasVisited, labels[0])
    v <= b -> Quality(AtlasOlive, labels[1])
    v <= c -> Quality(AtlasGold, labels[2])
    v <= e -> Quality(AtlasDelay, labels[3])
    else -> Quality(AtlasError, labels[4])
}
