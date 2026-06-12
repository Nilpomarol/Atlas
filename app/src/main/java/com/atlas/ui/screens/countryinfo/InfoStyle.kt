package com.atlas.ui.screens.countryinfo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.atlas.presentation.country.BreakdownSlice
import com.atlas.ui.theme.AtlasDelay
import com.atlas.ui.theme.AtlasError
import com.atlas.ui.theme.AtlasGold
import com.atlas.ui.theme.AtlasLived
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasNavySoft
import com.atlas.ui.theme.AtlasOlive
import com.atlas.ui.theme.AtlasOnSurfaceFaint
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasBronze
import com.atlas.ui.theme.AtlasMedalGold
import com.atlas.ui.theme.AtlasPlanned
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSilver
import com.atlas.ui.theme.AtlasVisited
import com.atlas.ui.theme.AtlasWished
import kotlin.math.abs
import kotlin.math.roundToLong

// --------------------------------------------------------------------------- //
// Formatting + numeric helpers
// --------------------------------------------------------------------------- //
internal fun String.toCaDouble(): Double = replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0

internal fun formatPct(v: Double): String {
    val r = (v * 10).toLong() / 10.0
    return (if (r % 1.0 == 0.0) r.toLong().toString() else r.toString()).replace(".", ",")
}

/**
 * Compact, Catalan-formatted magnitude for large numbers so tiles never overflow:
 * k (milers), M (milions), B (mil milions), T (bilions). Values below 1000 or that
 * are not parseable numbers are returned untouched, so decimals, percentages, codes
 * and short values keep their original formatting.
 */
internal fun formatCompactValue(raw: String): String {
    val parsed = raw.replace(".", "").replace(",", ".").toDoubleOrNull() ?: return raw
    val magnitude = abs(parsed)
    if (magnitude < 1000) return raw
    val (scaled, suffix) = when {
        magnitude >= 1e12 -> parsed / 1e12 to "T"
        magnitude >= 1e9 -> parsed / 1e9 to "B"
        magnitude >= 1e6 -> parsed / 1e6 to "M"
        else -> parsed / 1e3 to "k"
    }
    val rounded = (scaled * 10).roundToLong() / 10.0
    val text = if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString()
    return text.replace(".", ",") + suffix
}

/**
 * Medal color for a highlight by the country's actual world rank: the literal
 * podium (1/2/3) is gold/silver/bronze, and everything below it is green, deeper
 * for the stronger ranks. (Highlights are already filtered to roughly the top 12%.)
 */
internal fun highlightStandingColor(rank: Int, rankTotal: Int): Color = when {
    rank == 1 -> AtlasMedalGold
    rank == 2 -> AtlasSilver
    rank == 3 -> AtlasBronze
    rank.toDouble() / rankTotal <= 0.05 -> AtlasVisited
    else -> AtlasOlive
}

/** A simplified, clean government descriptor derived from the raw dataset value. */
internal data class GovernmentSummary(val form: String, val structure: String?)

/**
 * The raw `government_type` values are long and inconsistently translated (69 distinct
 * variants). Collapse them into a small set of clean Catalan forms plus an optional
 * structure tag (Unitari / Federal).
 */
internal fun simplifyGovernment(raw: String): GovernmentSummary {
    val s = raw.lowercase()
    val structure = when {
        "federal" in s -> "Federal"
        "unitàri" in s || "unitari" in s -> "Unitari"
        else -> null
    }
    val form = when {
        "teocr" in s || "theocr" in s || "ecclesiastical" in s -> "Teocràcia"
        "monarquia" in s && ("absoluta" in s || "islàmica" in s || "islamica" in s) -> "Monarquia absoluta"
        "monarquia" in s -> "Monarquia constitucional"
        "comunista" in s -> "Estat comunista"
        "one party" in s || "dominant party" in s || "totalitari" in s || "non partisan" in s ->
            "Estat unipartidista"
        "semi presidencialista" in s -> "República semipresidencialista"
        "presidencialista" in s -> "República presidencialista"
        "parlament" in s -> "República parlamentària"
        "dependent" in s || "direct rule" in s || "devolution" in s || "self-governance" in s ->
            "Territori dependent"
        "rep" in s -> "República"
        structure == "Federal" -> "Estat federal"
        structure == "Unitari" -> "Estat unitari"
        else -> raw.replaceFirstChar { it.uppercase() }
    }
    // Keep the structure (Unitari / Federal) as a plain qualifier, unless the form
    // already names it ("Estat unitari" / "Estat federal").
    val tag = structure?.takeUnless { form == "Estat unitari" || form == "Estat federal" }
    return GovernmentSummary(form, tag)
}

internal fun formatOrdinal(rank: Int): String = when (rank) {
    1 -> "1r"
    2 -> "2n"
    3 -> "3r"
    4 -> "4t"
    else -> "${rank}è"
}

@Composable
internal fun FactYear(year: Int?) {
    year ?: return
    Text(
        text = year.toString(),
        style = MaterialTheme.typography.labelSmall,
        color = AtlasOnSurfaceFaint,
    )
}

internal fun compactBreakdown(slices: List<BreakdownSlice>): List<BreakdownSlice> {
    if (slices.size <= 6) return slices
    return slices.take(5) + BreakdownSlice(
        name = "Altres",
        pct = slices.drop(5).sumOf { it.pct },
    )
}

internal fun subsectionLabel(sectionKey: String, category: String): String? {
    if (sectionKey !in MERGED_SECTION_KEYS) return null
    return SUBSECTION_LABELS[category]
}

// --------------------------------------------------------------------------- //
// Colors + section metadata
// --------------------------------------------------------------------------- //
internal val BREAKDOWN_COLORS = listOf(
    AtlasVisited, AtlasPlanned, AtlasLived, AtlasWished, AtlasGold, AtlasOnSurfaceMuted, AtlasOlive, AtlasDelay,
)
internal val COMPOSITION_COLORS = listOf(AtlasPlanned, AtlasVisited, AtlasGold, AtlasLived)

internal fun tierColor(tier: String?): Color = when (tier) {
    "Capdavanter", "Molt alt" -> AtlasVisited
    "Alt" -> AtlasOlive
    "Mitjà" -> AtlasGold
    "Baix" -> AtlasDelay
    "Inferior" -> AtlasError
    "Gegant", "Gran", "Petit", "Microestat" -> AtlasNavy
    else -> AtlasOnSurfaceMuted
}

internal fun statusColor(value: String): Color = when {
    value in setOf("Legal", "Lliure demanda", "Abolida") -> AtlasVisited
    value in setOf("Vigent", "Prohibit", "Il·legal", "Criminalitzat") -> AtlasError
    value.startsWith("Varia") -> AtlasPlanned
    else -> AtlasDelay
}

internal fun sectionAccent(key: String): Color = when (key) {
    "identitat" -> AtlasNavy
    "geo_medi" -> AtlasOlive
    "demografia" -> AtlasNavySoft
    "salut" -> AtlasPrimary
    "economia" -> AtlasGold
    "finances" -> AtlasNavy
    "desenvolupament" -> AtlasOlive
    "infraestructura" -> AtlasNavySoft
    "governanca" -> AtlasNavy
    "cultura" -> AtlasPrimary
    "drets" -> AtlasGold
    "practic" -> AtlasNavySoft
    else -> AtlasOnSurfaceMuted
}

private val MERGED_SECTION_KEYS = setOf(
    "geo_medi",
    "finances",
    "desenvolupament",
    "cultura",
)

private val SUBSECTION_LABELS = mapOf(
    "geografia" to "Geografia",
    "mediambient" to "Medi ambient",
    "finances" to "Finances",
    "desigualtat" to "Desigualtat",
    "desenvolupament" to "Desenvolupament humà",
    "educacio" to "Educació",
    "cultura" to "Cultura",
    "turisme" to "Turisme",
)

internal fun sectionIcon(key: String): ImageVector = when (key) {
    "identitat" -> Icons.Outlined.Flag
    "geo_medi" -> Icons.Outlined.Public
    "demografia" -> Icons.Outlined.Groups
    "salut" -> Icons.Outlined.Favorite
    "economia" -> Icons.Outlined.TrendingUp
    "finances" -> Icons.Outlined.AccountBalance
    "desenvolupament" -> Icons.Outlined.School
    "infraestructura" -> Icons.Outlined.Wifi
    "governanca" -> Icons.Outlined.Gavel
    "cultura" -> Icons.Outlined.Language
    "drets" -> Icons.Outlined.Balance
    "practic" -> Icons.Outlined.FlightTakeoff
    else -> Icons.Outlined.Public
}
