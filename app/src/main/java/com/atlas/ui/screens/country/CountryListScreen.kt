package com.atlas.ui.screens.country

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.CountryTrackingState
import com.atlas.domain.model.CountryType
import com.atlas.presentation.country.CountryListFilter
import com.atlas.presentation.country.CountryListItemUiState
import com.atlas.presentation.country.CountryListUiState

@Composable
fun CountryListScreen(
    uiState: CountryListUiState,
    onCountryClick: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onFilterSelected: (CountryListFilter) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = "Països i territoris",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "${uiState.countries.size} de ${uiState.totalCountryCount} llocs",
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = {
                Text(text = "Cerca")
            },
        )

        LazyRow(
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(CountryListFilter.entries) { filter ->
                FilterChip(
                    selected = uiState.selectedFilter == filter,
                    onClick = {
                        onFilterSelected(filter)
                    },
                    label = {
                        Text(text = filter.label)
                    },
                )
            }
        }

        if (uiState.countries.isEmpty()) {
            EmptyCountryList(
                hasActiveSearchOrFilter = uiState.searchQuery.isNotBlank() ||
                    uiState.selectedFilter != CountryListFilter.All,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                items(
                    items = uiState.countries,
                    key = { item -> item.country.iso2 },
                ) { item ->
                    CountryRow(
                        item = item,
                        onClick = {
                            onCountryClick(item.country.iso2)
                        },
                    )
                    HorizontalDivider()
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
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (hasActiveSearchOrFilter) {
                "No hi ha cap resultat."
            } else {
                "Carregant països..."
            },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
        if (hasActiveSearchOrFilter) {
            Text(
                text = "Prova una altra cerca o treu el filtre actual.",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                text = "El dataset inicial es carregarà automàticament.",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
@Composable
private fun CountryRow(
    item: CountryListItemUiState,
    onClick: () -> Unit,
) {
    val country = item.country

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = country.flagEmoji.orEmpty(),
            style = MaterialTheme.typography.titleLarge,
        )
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = country.nameCa,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = country.type.toCatalanLabel(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = country.iso2,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        item.trackingState.primaryLabel()?.let { label ->
            AssistChip(
                onClick = onClick,
                label = {
                    Text(text = label)
                },
            )
        }
    }
}

private fun CountryType.toCatalanLabel(): String = when (this) {
    CountryType.SOVEREIGN_STATE -> "Estat sobirà"
    CountryType.DEPENDENT_TERRITORY -> "Territori dependent"
    CountryType.SPECIAL_REGION -> "Regió especial"
    CountryType.DISPUTED_OR_OTHER -> "Disputat o altre"
}

private fun CountryTrackingState.primaryLabel(): String? = when {
    currentlyLiving -> "Residència actual"
    lived -> "Viscut"
    visited -> "Visitat"
    planned -> "Planificat"
    wished -> "Desitjat"
    else -> null
}
