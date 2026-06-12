package com.atlas.ui.screens.countryinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.presentation.country.CountryBordersUi
import com.atlas.presentation.country.CountryFactView
import com.atlas.presentation.country.CountryInfoSection
import com.atlas.presentation.country.FactRenderType
import com.atlas.presentation.country.SectionItem
import com.atlas.ui.components.AtlasSectionLabel
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasSurfaceRaised

@Composable
internal fun SectionCard(
    section: CountryInfoSection,
    defaultExpanded: Boolean,
    bordersMap: CountryBordersUi? = null,
) {
    var expanded by rememberSaveable(section.key) { mutableStateOf(defaultExpanded) }
    val accent = sectionAccent(section.key)
    val chevronRot by animateFloatAsState(if (expanded) 180f else 0f, label = "chevron")

    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(AtlasSurfaceRaised)
            .border(1.dp, AtlasOutline, RoundedCornerShape(18.dp)),
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(sectionIcon(section.key), contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(section.title, style = MaterialTheme.typography.titleLarge, fontSize = 19.sp, color = AtlasOnSurfaceStrong, modifier = Modifier.weight(1f))
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Replega" else "Desplega",
                    tint = AtlasOnSurfaceMuted,
                    modifier = Modifier.rotate(chevronRot),
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(AtlasOutline))
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        if (section.key == "drets") {
                            DretsGrid(section.items)
                        } else if (section.key == "geo_medi") {
                            GeographySection(section.items, bordersMap)
                        } else {
                            var previousCategory: String? = null
                            section.items.forEachIndexed { index, item ->
                                val category = (item as? SectionItem.FactItem)?.fact?.category
                                val subsection = category?.let { subsectionLabel(section.key, it) }
                                if (subsection != null && category != previousCategory) {
                                    AtlasSectionLabel(
                                        title = subsection,
                                        modifier = Modifier.padding(top = if (index == 0) 4.dp else 12.dp),
                                    )
                                }
                                when (item) {
                                    is SectionItem.FactItem -> FactItemRow(item.fact, accent)
                                    is SectionItem.CompositionItem -> CompositionRow(item.title, item.segments)
                                    is SectionItem.MembershipItem -> MembershipRow(item.memberships, accent)
                                }
                                if (index != section.items.lastIndex) {
                                    HorizontalDivider(color = AtlasOutline.copy(alpha = 0.7f))
                                }
                                if (category != null) previousCategory = category
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FactItemRow(fact: CountryFactView, accent: Color) {
    // Some facts render by what they are, regardless of which section they live in.
    when (fact.key) {
        "coat_of_arms" -> CoatOfArmsRow(fact)
        "government_type" -> GovernmentRow(fact)
        "olympic_code", "fifa_code" -> MonoValueRow(fact)
        else -> when (fact.renderType) {
            FactRenderType.RANKED_NUMERIC -> RankedRow(fact)
            FactRenderType.PERCENT -> PercentRow(fact, accent)
            FactRenderType.BREAKDOWN -> BreakdownRow(fact)
            FactRenderType.STATUS -> StatusRow(fact)
            FactRenderType.MEMBERSHIP -> SimpleRow(fact)
            FactRenderType.TEXT -> if (fact.key == "borders" || fact.key == "languages") ChipsRow(fact) else SimpleRow(fact)
        }
    }
}

@Composable
private fun DretsGrid(items: List<SectionItem>) {
    val facts = items.filterIsInstance<SectionItem.FactItem>().map { it.fact }
    BoxWithConstraints {
        val singleColumn = maxWidth < 360.dp
        Column(Modifier.padding(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (singleColumn) {
                facts.forEach { fact -> RightsTile(fact, Modifier.fillMaxWidth()) }
            } else {
                facts.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { fact -> RightsTile(fact, Modifier.weight(1f)) }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RightsTile(fact: CountryFactView, modifier: Modifier = Modifier) {
    val color = statusColor(fact.value)
    Box(modifier.clip(RoundedCornerShape(13.dp)).background(color.copy(alpha = 0.12f)).padding(13.dp)) {
        Column {
            Text(
                text = fact.label,
                style = MaterialTheme.typography.labelSmall,
                color = AtlasOnSurfaceMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = fact.value,
                color = color,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            FactYear(fact.year)
        }
    }
}
