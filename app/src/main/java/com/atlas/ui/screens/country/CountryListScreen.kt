package com.atlas.ui.screens.country

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.CountryType
import com.atlas.presentation.country.CountryListFilter
import com.atlas.presentation.country.CountryListItemUiState
import com.atlas.presentation.country.CountryListUiState
import com.atlas.presentation.country.CountrySort
import com.atlas.ui.components.AtlasFilterPill
import com.atlas.ui.components.AtlasPage
import com.atlas.ui.components.AtlasPill
import com.atlas.ui.components.CountryFlag
import com.atlas.ui.components.primaryStateColors
import com.atlas.ui.theme.AtlasLived
import com.atlas.ui.theme.AtlasMono
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasOnSurfaceFaint
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPlanned
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasSurfaceRaised
import com.atlas.ui.theme.AtlasVisited
import com.atlas.ui.theme.AtlasWished

@Composable
fun CountryListScreen(
    uiState: CountryListUiState,
    onCountryClick: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onFilterSelected: (CountryListFilter) -> Unit,
    onSortSelected: (CountrySort) -> Unit,
    onSortDirectionToggled: () -> Unit,
) {
    AtlasPage(contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
        var collapsedContinents by rememberSaveable { mutableStateOf(emptyList<String>()) }
        var grouped by rememberSaveable { mutableStateOf(true) }
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            CountryListHeader(
                uiState = uiState,
                grouped = grouped,
                onSearchQueryChanged = onSearchQueryChanged,
                onFilterSelected = onFilterSelected,
                onSortSelected = onSortSelected,
                onSortDirectionToggled = onSortDirectionToggled,
                onToggleGrouped = { grouped = !grouped },
            )

            if (uiState.countries.isEmpty()) {
                EmptyCountryList(
                    hasActiveSearchOrFilter = uiState.searchQuery.isNotBlank() ||
                        uiState.selectedFilter != CountryListFilter.All,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 2.dp,
                        bottom = 20.dp,
                    ),
                ) {
                    if (!grouped) {
                        items(
                            items = uiState.countries,
                            key = { item -> item.country.iso2 },
                        ) { item ->
                            CountryRow(
                                item = item,
                                onClick = { onCountryClick(item.country.iso2) },
                            )
                        }
                    } else {
                    uiState.countries
                        .groupBy { it.country.continent }
                        .entries
                        .sortedByDescending { (_, countries) ->
                            countries.count { it.trackingState.visited }
                        }
                        .forEach { (continent, countries) ->
                            val isExpanded = continent !in collapsedContinents
                            val visitedCount = countries.count { it.trackingState.visited }
                            item(key = "section-$continent") {
                                ContinentHeader(
                                    continent = continent.toCatalanContinent(),
                                    visitedCount = visitedCount,
                                    totalCount = countries.size,
                                    expanded = isExpanded,
                                    onClick = {
                                        collapsedContinents = if (isExpanded) {
                                            collapsedContinents + continent
                                        } else {
                                            collapsedContinents - continent
                                        }
                                    },
                                )
                            }
                            if (isExpanded) {
                                items(
                                    items = countries,
                                    key = { item -> item.country.iso2 },
                                ) { item ->
                                    CountryRow(
                                        item = item,
                                        onClick = { onCountryClick(item.country.iso2) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CountryListHeader(
    uiState: CountryListUiState,
    grouped: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onFilterSelected: (CountryListFilter) -> Unit,
    onSortSelected: (CountrySort) -> Unit,
    onSortDirectionToggled: () -> Unit,
    onToggleGrouped: () -> Unit,
) {
        Column(
        modifier = Modifier.padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Atlas",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AtlasOnSurfaceStrong,
                )
                Text(
                    text = "${uiState.countries.size} de ${uiState.totalCountryCount} llocs",
                    style = MaterialTheme.typography.labelMedium,
                    color = AtlasOnSurfaceMuted,
                )
            }
            SortControl(
                selected = uiState.selectedSort,
                ascending = uiState.sortAscending,
                grouped = grouped,
                onSortSelected = onSortSelected,
                onDirectionToggled = onSortDirectionToggled,
                onToggleGrouped = onToggleGrouped,
            )
        }

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = AtlasOnSurfaceMuted,
                )
            },
            placeholder = {
                Text(text = "Cerca països i territoris")
            },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AtlasSurfaceRaised,
                unfocusedContainerColor = AtlasSurfaceRaised,
                focusedBorderColor = AtlasOutline,
                unfocusedBorderColor = AtlasSurfaceRaised,
                focusedTextColor = AtlasOnSurfaceStrong,
                unfocusedTextColor = AtlasOnSurfaceStrong,
            ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CountryListFilter.entries.forEach { filter ->
                AtlasFilterPill(
                    label = filter.label,
                    selected = uiState.selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    selectedContainerColor = filter.pillColor(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SortControl(
    selected: CountrySort,
    ascending: Boolean,
    grouped: Boolean,
    onSortSelected: (CountrySort) -> Unit,
    onDirectionToggled: () -> Unit,
    onToggleGrouped: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Direction toggle (ascending / descending).
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .border(1.dp, AtlasOutline, RoundedCornerShape(100.dp))
                .clickable(onClickLabel = if (ascending) "Ordena descendent" else "Ordena ascendent") {
                    onDirectionToggled()
                }
                .padding(7.dp),
        ) {
            Icon(
                imageVector = if (ascending) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                contentDescription = if (ascending) "Ascendent" else "Descendent",
                tint = AtlasOnSurfaceStrong,
                modifier = Modifier.size(16.dp),
            )
        }
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .border(1.dp, AtlasOutline, RoundedCornerShape(100.dp))
                    .clickable { expanded = true }
                    .padding(start = 12.dp, end = 10.dp, top = 7.dp, bottom = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Ordena",
                    tint = AtlasOnSurfaceMuted,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = selected.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceStrong,
                )
            }
            MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(surfaceContainer = AtlasSurface),
            shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(14.dp)),
        ) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.width(180.dp),
            ) {
                CountrySort.entries.forEach { sort ->
                    val isSelected = sort == selected
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = sort.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AtlasNavy else AtlasOnSurfaceStrong,
                            )
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = AtlasNavy,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        } else {
                            null
                        },
                        onClick = {
                            expanded = false
                            onSortSelected(sort)
                        },
                    )
                }
                HorizontalDivider(color = AtlasOutline)
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Agrupa per continent",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (grouped) FontWeight.Bold else FontWeight.Medium,
                            color = if (grouped) AtlasNavy else AtlasOnSurfaceStrong,
                        )
                    },
                    trailingIcon = if (grouped) {
                        {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = AtlasNavy,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    } else {
                        null
                    },
                    onClick = {
                        expanded = false
                        onToggleGrouped()
                    },
                )
            }
            }
        }
    }
}

@Composable
private fun EmptyCountryList(
    hasActiveSearchOrFilter: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (hasActiveSearchOrFilter) {
                "No hi ha cap resultat."
            } else {
                "Carregant països..."
            },
            style = MaterialTheme.typography.titleMedium,
            color = AtlasOnSurfaceStrong,
        )
        Text(
            text = if (hasActiveSearchOrFilter) {
                "Prova una altra cerca o treu el filtre actual."
            } else {
                "El dataset inicial es carregarà automàticament."
            },
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = AtlasOnSurfaceMuted,
        )
    }
}

@Composable
private fun ContinentHeader(
    continent: String,
    visitedCount: Int,
    totalCount: Int,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(top = 22.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = continent.uppercase(),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = AtlasOnSurfaceMuted,
        )
        Text(
            text = if (visitedCount > 0) "$visitedCount / $totalCount" else "$totalCount",
            style = MaterialTheme.typography.labelSmall,
            color = AtlasOnSurfaceFaint,
        )
        Icon(
            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = null,
            tint = AtlasOnSurfaceFaint,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun CountryRow(
    item: CountryListItemUiState,
    onClick: () -> Unit,
) {
    val country = item.country
    val stateColors = item.trackingState.primaryStateColors()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(stateColors.foreground),
            )
            CountryFlag(
                iso2 = country.iso2,
                modifier = Modifier
                    .width(44.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(AtlasSurfaceRaised)
                    .border(1.dp, AtlasOutline, RoundedCornerShape(3.dp)),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = country.nameCa,
                    style = MaterialTheme.typography.titleSmall,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = null,
                        tint = AtlasOnSurfaceFaint,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = country.metaText(),
                        style = MaterialTheme.typography.bodySmall,
                        color = AtlasOnSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            item.sortValueLabel?.let { label ->
                Text(
                    text = label,
                    fontFamily = AtlasMono,
                    style = MaterialTheme.typography.labelMedium,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                )
            }
            AtlasPill(
                label = stateColors.label,
                colors = stateColors,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 11.dp, vertical = 6.dp),
                fontWeight = FontWeight.ExtraBold,
            )
        }
        HorizontalDivider(color = AtlasOutline)
    }
}

private fun CountryListFilter.pillColor(): Color = when (this) {
    CountryListFilter.All -> AtlasPrimary
    CountryListFilter.Visited -> AtlasVisited
    CountryListFilter.Wished -> AtlasWished
    CountryListFilter.Planned -> AtlasPlanned
    CountryListFilter.Lived -> AtlasLived
}

private fun com.atlas.domain.model.Country.metaText(): String =
    listOfNotNull(
        capitalNameCa,
        subregion?.toCatalanSubregion(),
        type.toCatalanLabel().takeIf { capitalNameCa == null && subregion == null },
    ).joinToString(" · ")

private fun CountryType.toCatalanLabel(): String = when (this) {
    CountryType.SOVEREIGN_STATE -> "Estat sobirà"
    CountryType.DEPENDENT_TERRITORY -> "Territori dependent"
    CountryType.SPECIAL_REGION -> "Regió especial"
    CountryType.DISPUTED_OR_OTHER -> "Disputat o altre"
}
