package com.atlas.ui.screens.countryinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.atlas.presentation.country.Membership
import com.atlas.presentation.country.SectionItem
import com.atlas.ui.components.AtlasSectionLabel
import com.atlas.ui.theme.AtlasMono
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasOnSurfaceFaint
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasSerif

@Composable
internal fun GovernanceSection(items: List<SectionItem>) {
    val byKey = items.filterIsInstance<SectionItem.FactItem>().associate { it.fact.key to it.fact }
    val memberships = items.filterIsInstance<SectionItem.MembershipItem>().firstOrNull()?.memberships

    Column(
        Modifier.padding(top = 6.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StateHeader(
            officialName = byKey["official_name"]?.value,
            coatUrl = byKey["coat_of_arms"]?.value,
            government = byKey["government_type"]?.value,
        )

        GovernanceIndices(byKey)

        DefenseBlock(usd = byKey["military_exp_usd"], gdp = byKey["military_exp_gdp"])

        memberships?.let { MembershipBlock(it) }
    }
}

@Composable
private fun StateHeader(officialName: String?, coatUrl: String?, government: String?) {
    if (officialName == null && coatUrl == null && government == null) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        coatUrl?.let { CoatOfArms(it, Modifier.size(72.dp)) }
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            officialName?.let {
                Text(
                    it,
                    fontFamily = AtlasSerif,
                    fontSize = 19.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = AtlasOnSurfaceStrong,
                )
            }
            government?.let {
                val gov = simplifyGovernment(it)
                Text(
                    gov.form + (gov.structure?.let { s -> " · $s" } ?: ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AtlasOnSurfaceMuted,
                )
            }
        }
    }
}

@Composable
private fun GovernanceIndices(byKey: Map<String, CountryFactView>) {
    val indices = listOfNotNull(
        byKey["democracy_index"]?.let { it to 10.0 },
        byKey["press_freedom"]?.let { it to 100.0 },
        byKey["cpi"]?.let { it to 100.0 },
    )
    if (indices.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
        AtlasSectionLabel("Qualitat de govern")
        indices.forEach { (fact, max) -> IndexMeter(fact, max) }
    }
}

@Composable
private fun IndexMeter(fact: CountryFactView, max: Double) {
    val color = tierColor(fact.tier)
    val fraction = (fact.value.toCaDouble() / max).toFloat().coerceIn(0f, 1f)
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
                fact.tier?.let { TierChip(it) }
            }
        }
        TrackBar(fraction, color)
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
private fun DefenseBlock(usd: CountryFactView?, gdp: CountryFactView?) {
    if (usd == null && gdp == null) return
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        AtlasSectionLabel("Defensa")
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            // Absolute spend as the headline.
            if (usd != null) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        formatCompactValue(usd.value),
                        fontFamily = AtlasSerif,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Medium,
                        color = AtlasOnSurfaceStrong,
                    )
                    usd.unit?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AtlasOnSurfaceMuted,
                            modifier = Modifier.padding(bottom = 3.dp),
                        )
                    }
                }
            }
            // Share of GDP to the right, above the bar.
            gdp?.let {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        it.value,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = AtlasOnSurfaceStrong,
                    )
                    Text(
                        it.unit ?: "% PIB",
                        style = MaterialTheme.typography.labelMedium,
                        color = AtlasOnSurfaceMuted,
                        modifier = Modifier.padding(bottom = 1.dp),
                    )
                }
            }
        }
        if (usd?.rank != null && usd.rankTotal != null && usd.rankTotal > 1) {
            PositionBar(usd.rank, usd.rankTotal, tierColor(usd.tier))
            Text(
                "${formatOrdinal(usd.rank)} de ${usd.rankTotal} en despesa militar",
                style = MaterialTheme.typography.labelSmall,
                color = AtlasOnSurfaceFaint,
            )
        }
    }
}

@Composable
private fun MembershipBlock(memberships: List<Membership>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AtlasSectionLabel("Pertinença internacional", modifier = Modifier.weight(1f))
            Spacer(Modifier.size(10.dp))
            Text(
                "${memberships.count { it.isMember }}/${memberships.size}",
                fontFamily = AtlasMono,
                fontSize = 13.sp,
                color = AtlasOnSurfaceMuted,
            )
        }
        // Lay out as an even grid so 5 chips read as 3 + 2, not 4 + 1.
        val columns = (memberships.size + 1) / 2
        memberships.chunked(columns).forEach { rowItems ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowItems.forEach { MembershipChip(it, Modifier.weight(1f)) }
                repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun MembershipChip(m: Membership, modifier: Modifier = Modifier) {
    val base = modifier
        .clip(RoundedCornerShape(20.dp))
        .then(
            if (m.isMember) Modifier.background(AtlasNavy)
            else Modifier.border(1.dp, AtlasOutline, RoundedCornerShape(20.dp)),
        )
        .padding(horizontal = 13.dp, vertical = 7.dp)
    Box(base, contentAlignment = Alignment.Center) {
        Text(
            if (m.isMember) "✓ ${m.label}" else m.label,
            style = MaterialTheme.typography.labelMedium,
            color = if (m.isMember) Color.White else AtlasOnSurfaceFaint,
            fontWeight = if (m.isMember) FontWeight.Medium else FontWeight.Normal,
        )
    }
}
