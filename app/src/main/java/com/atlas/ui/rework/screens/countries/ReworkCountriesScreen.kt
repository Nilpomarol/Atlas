package com.atlas.ui.rework.screens.countries

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.domain.model.CountryTrackingState
import com.atlas.presentation.country.CountryDetailUiState
import com.atlas.presentation.country.CountryListFilter
import com.atlas.presentation.country.CountryListItemUiState
import com.atlas.presentation.country.CountryListUiState
import com.atlas.presentation.country.CountrySort
import com.atlas.ui.rework.components.ReworkDropdownDivider
import com.atlas.ui.rework.components.ReworkDropdownItem
import com.atlas.ui.rework.components.ReworkDropdownMenu
import com.atlas.ui.rework.components.ReworkFloatingCard
import com.atlas.ui.rework.foundation.AtlasReworkTheme
import com.atlas.ui.rework.map.AtlasWorldLandAspectRatio
import com.atlas.ui.rework.map.AtlasWorldMap
import com.atlas.ui.rework.map.AtlasWorldMapCountries

@Composable
fun ReworkCountriesScreen(
    state: CountryListUiState,
    onSearchChanged: (String) -> Unit,
    onFilterSelected: (CountryListFilter) -> Unit,
    onSortSelected: (CountrySort) -> Unit,
    onSortDirectionToggled: () -> Unit,
    onCountryOpened: (String) -> Unit,
) {
    val colors = AtlasReworkTheme.colors
    val allCountries = state.allCountries
    val visited = allCountries.count { it.trackingState.visited || it.trackingState.lived || it.trackingState.currentlyLiving }
    // Session-remembered collapse state, keyed by continent. Absent = expanded.
    val collapsedContinents = remember { mutableStateMapOf<String, Boolean>() }
    Box(modifier = Modifier.fillMaxSize().background(colors.surface)) {
        LazyColumn(modifier = Modifier.fillMaxSize().statusBarsPadding(), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 112.dp)) {
            item {
                Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                ) {
                Column(Modifier.weight(1f)) {
                    Text("PAÏSOS", style = AtlasReworkTheme.typography.label, color = colors.accent)
                    Text("El teu arxiu", style = AtlasReworkTheme.typography.display, color = colors.ink)
                }
                Text("$visited/${state.totalCountryCount}", style = AtlasReworkTheme.typography.data, color = colors.ink)
                }
            }
            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ReworkSearchField(value = state.searchQuery, onValueChange = onSearchChanged)
                CountryListControls(
                    filter = state.selectedFilter,
                    sort = state.selectedSort,
                    ascending = state.sortAscending,
                    onFilterSelected = onFilterSelected,
                    onSortSelected = onSortSelected,
                    onSortDirectionToggled = onSortDirectionToggled,
                )
                Text("${state.countries.size} països", style = AtlasReworkTheme.typography.label, color = colors.inkMuted)
                }
            }
            val groups = state.countries.groupBy { it.country.continent }.toSortedMap(compareBy(::continentOrder))
            groups.entries.forEachIndexed { groupIndex, (continent, countries) ->
                val collapsed = collapsedContinents[continent] == true
                item(key = "continent-$continent") {
                    ContinentHeader(
                        title = continent.toCatalanContinent(),
                        count = countries.size,
                        collapsed = collapsed,
                        showTopDivider = groupIndex > 0,
                        onToggle = { collapsedContinents[continent] = !collapsed },
                    )
                }
                if (!collapsed) {
                    itemsIndexed(countries, key = { _, item -> item.country.iso2 }) { index, item ->
                        CompactCountryRow(
                            item = item,
                            onClick = { onCountryOpened(item.country.iso2) },
                            showDivider = index < countries.lastIndex,
                        )
                    }
                }
            }
            if (state.countries.isEmpty()) {
                item { ReworkFloatingCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) { Text("No hem trobat cap país amb aquests criteris.", style = AtlasReworkTheme.typography.body, color = colors.inkMuted) } }
            }
        }
    }
}

@Composable
fun ReworkCountryDetailScreen(state: CountryDetailUiState, onBack: () -> Unit, onCaptureRequested: () -> Unit) {
    val country = state.country
    val colors = AtlasReworkTheme.colors
    if (country == null) {
        Box(Modifier.fillMaxSize().background(colors.mapWater), contentAlignment = Alignment.Center) {
            ReworkFloatingCard(Modifier.padding(24.dp)) { Text("Aquest país ja no és disponible.", style = AtlasReworkTheme.typography.title) }
        }
        return
    }
    Box(Modifier.fillMaxSize().background(colors.mapWater)) {
        AtlasWorldMap(
            countries = AtlasWorldMapCountries(
                living = if (state.trackingState.currentlyLiving) setOf(country.iso2) else emptySet(),
                lived = if (state.trackingState.lived) setOf(country.iso2) else emptySet(),
                visited = if (state.trackingState.visited) setOf(country.iso2) else emptySet(),
                planned = if (state.trackingState.planned) setOf(country.iso2) else emptySet(),
                wished = if (state.trackingState.wished) setOf(country.iso2) else emptySet(),
            ),
            selectedCountryIso2 = country.iso2,
            initialLandTopPx = with(LocalDensity.current) { 56.dp.toPx() },
            modifier = Modifier.fillMaxSize(),
        )
        Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(bottom = 112.dp)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = colors.surfaceStrong, onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Torna", modifier = Modifier.padding(10.dp), tint = colors.ink)
                }
                Spacer(Modifier.width(12.dp))
                Text("PAÍS", style = AtlasReworkTheme.typography.label, color = colors.accent)
            }
            Spacer(Modifier.height(176.dp * AtlasWorldLandAspectRatio))
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ReworkFloatingCard(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(country.flagEmoji.orEmpty(), style = AtlasReworkTheme.typography.title)
                        Text(country.nameCa, style = AtlasReworkTheme.typography.display)
                        Text(state.trackingState.relationshipSummary(), style = AtlasReworkTheme.typography.body, color = colors.inkMuted)
                        StateLedger(state.trackingState)
                        Surface(shape = RoundedCornerShape(12.dp), color = colors.accent, onClick = onCaptureRequested) {
                            Text("Registra en aquest país", modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp), style = AtlasReworkTheme.typography.label, color = Color.White)
                        }
                    }
                }
                EvidenceCard(state)
                if (state.kpiStats.isNotEmpty()) ReferenceCard(state)
            }
        }
    }
}

@Composable private fun ReworkSearchField(value: String, onValueChange: (String) -> Unit) {
    val colors = AtlasReworkTheme.colors
    Surface(shape = RoundedCornerShape(14.dp), color = colors.surfaceStrong, border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Search, null, tint = colors.inkMuted)
            Spacer(Modifier.width(10.dp))
            BasicTextField(value = value, onValueChange = onValueChange, textStyle = AtlasReworkTheme.typography.body.copy(color = colors.ink), modifier = Modifier.weight(1f), decorationBox = { inner -> if (value.isEmpty()) Text("Cerca un país", style = AtlasReworkTheme.typography.body, color = colors.inkMuted); inner() })
        }
    }
}

@Composable
private fun CountryListControls(
    filter: CountryListFilter,
    sort: CountrySort,
    ascending: Boolean,
    onFilterSelected: (CountryListFilter) -> Unit,
    onSortSelected: (CountrySort) -> Unit,
    onSortDirectionToggled: () -> Unit,
) {
    var filterExpanded by remember { mutableStateOf(false) }
    var orderExpanded by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f)) {
            CompactMenuButton("Filtra", filter.label) { filterExpanded = true }
            ReworkDropdownMenu(expanded = filterExpanded, onDismissRequest = { filterExpanded = false }) {
                CountryListFilter.entries.forEach { option ->
                    ReworkDropdownItem(
                        label = option.label,
                        selected = option == filter,
                        onClick = { onFilterSelected(option); filterExpanded = false },
                    )
                }
            }
        }
        Box(Modifier.weight(1f)) {
            CompactMenuButton("Ordena", "${sort.label} ${if (ascending) "↑" else "↓"}") { orderExpanded = true }
            ReworkDropdownMenu(expanded = orderExpanded, onDismissRequest = { orderExpanded = false }) {
                CountrySort.entries.forEach { option ->
                    ReworkDropdownItem(
                        label = option.label,
                        selected = option == sort,
                        trailing = if (option == sort) (if (ascending) "↑" else "↓") else null,
                        onClick = { onSortSelected(option); orderExpanded = false },
                    )
                }
                ReworkDropdownDivider()
                ReworkDropdownItem(
                    label = if (ascending) "Canvia a descendent" else "Canvia a ascendent",
                    onClick = { onSortDirectionToggled(); orderExpanded = false },
                )
            }
        }
    }
}

@Composable
private fun CompactMenuButton(label: String, value: String, onClick: () -> Unit) {
    val colors = AtlasReworkTheme.colors
    val shape = RoundedCornerShape(12.dp)
    Surface(
        modifier = Modifier.shadow(6.dp, shape, ambientColor = colors.shadow, spotColor = colors.shadow),
        shape = shape,
        color = colors.surfaceStrong,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
        onClick = onClick,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(colors.surfaceStrong, colors.surface)))
                .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(label.uppercase(), style = AtlasReworkTheme.typography.label, color = colors.accent)
                Text(value, style = AtlasReworkTheme.typography.body.copy(fontWeight = FontWeight.SemiBold), color = colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(28.dp)
                    .background(colors.accent.copy(alpha = 0.10f), RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
            }
        }
    }
}
@Composable private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = AtlasReworkTheme.colors
    Surface(shape = RoundedCornerShape(999.dp), color = if (selected) colors.ink else colors.surfaceStrong, onClick = onClick) {
        Text(label, Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = AtlasReworkTheme.typography.label, color = if (selected) colors.surfaceStrong else colors.ink)
    }
}
@Composable private fun ContinentHeader(
    title: String,
    count: Int,
    collapsed: Boolean,
    showTopDivider: Boolean,
    onToggle: () -> Unit,
) {
    val colors = AtlasReworkTheme.colors
    val chevronRotation by animateFloatAsState(targetValue = if (collapsed) 0f else 180f, label = "continentChevron")
    Column {
        if (showTopDivider) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(colors.border))
        }
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title.uppercase(), style = AtlasReworkTheme.typography.label, color = colors.accent, modifier = Modifier.weight(1f))
            Text(count.toString(), style = AtlasReworkTheme.typography.data, color = colors.inkMuted)
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Rounded.KeyboardArrowDown,
                contentDescription = if (collapsed) "Desplega $title" else "Replega $title",
                tint = colors.inkMuted,
                modifier = Modifier.size(20.dp).rotate(chevronRotation),
            )
        }
    }
}

@Composable private fun CompactCountryRow(item: CountryListItemUiState, onClick: () -> Unit, showDivider: Boolean) {
    val colors = AtlasReworkTheme.colors
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(item.country.flagEmoji.orEmpty(), style = AtlasReworkTheme.typography.title.copy(fontSize = 20.sp, lineHeight = 22.sp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.country.nameCa,
                    style = AtlasReworkTheme.typography.title.copy(fontSize = 17.sp, lineHeight = 20.sp),
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(item.trackingState.relationshipSummary(), style = AtlasReworkTheme.typography.label, color = colors.inkMuted)
            }
            Spacer(Modifier.width(10.dp))
            Text(item.sortValueLabel ?: item.country.iso2, style = AtlasReworkTheme.typography.data, color = colors.accent)
        }
        if (showDivider) {
            Box(Modifier.fillMaxWidth().padding(start = 48.dp, end = 16.dp).height(1.dp).background(colors.border))
        }
    }
}
@Composable private fun StateLedger(state: CountryTrackingState) {
    val labels = listOfNotNull(if (state.currentlyLiving) "HI VIUS" else null, if (state.lived) "HI HAS VISCUT" else null, if (state.visited) "VISITAT" else null, if (state.planned) "PLANEJAT" else null, if (state.wished) "DESITJAT" else null)
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { labels.forEach { FilterChip(it, true, {}) } }
}
@Composable private fun EvidenceCard(state: CountryDetailUiState) {
    val colors = AtlasReworkTheme.colors
    ReworkFloatingCard(Modifier.fillMaxWidth()) { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("LA TEVA PETJADA", style = AtlasReworkTheme.typography.label, color = colors.accent)
        val evidence = state.logs.size + state.tripSummaries.size + state.airTravelSummaries.size
        Text(if (evidence == 0) "Encara no hi ha cap registre." else "$evidence registres expliquen la teva relació.", style = AtlasReworkTheme.typography.title)
        state.tripSummaries.take(3).forEach { Text("Viatge · ${it.title}", style = AtlasReworkTheme.typography.body) }
        state.airTravelSummaries.take(3).forEach { Text("Vol · ${it.title}", style = AtlasReworkTheme.typography.body) }
        state.logs.take(3).forEach { Text("Registre · ${it.type.name.lowercase()}", style = AtlasReworkTheme.typography.body) }
    } }
}
@Composable private fun ReferenceCard(state: CountryDetailUiState) { val colors = AtlasReworkTheme.colors; ReworkFloatingCard(Modifier.fillMaxWidth()) { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("CONTEXT DEL PAÍS", style = AtlasReworkTheme.typography.label, color = colors.accent); Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { state.kpiStats.take(3).forEach { Column { Text(it.value, style = AtlasReworkTheme.typography.title); Text(it.label, style = AtlasReworkTheme.typography.label, color = colors.inkMuted) } } } } } }
private fun continentOrder(continent: String): Int = when (continent) {
    "Europe" -> 0
    "Africa" -> 1
    "Asia" -> 2
    "North America" -> 3
    "South America" -> 4
    "Oceania" -> 5
    "Antarctica" -> 6
    else -> 7
}

private fun String.toCatalanContinent(): String = when (this) {
    "Europe" -> "Europa"
    "Africa" -> "Àfrica"
    "Asia" -> "Àsia"
    "North America" -> "Amèrica del Nord"
    "South America" -> "Amèrica del Sud"
    "Antarctica" -> "Antàrtida"
    else -> this
}
private fun CountryTrackingState.relationshipSummary(): String = when { currentlyLiving -> "Hi vius ara"; lived -> "Hi has viscut"; visited -> "L'has visitat"; planned -> "El tens planejat"; wished -> "El vols visitar"; else -> "Encara sense registre" }
