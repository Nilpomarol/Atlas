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
import com.atlas.ui.theme.AtlasPlanned
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasVisited
import com.atlas.ui.theme.AtlasWished

// --------------------------------------------------------------------------- //
// Formatting + numeric helpers
// --------------------------------------------------------------------------- //
internal fun String.toCaDouble(): Double = replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0

internal fun formatPct(v: Double): String {
    val r = (v * 10).toLong() / 10.0
    return (if (r % 1.0 == 0.0) r.toLong().toString() else r.toString()).replace(".", ",")
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
