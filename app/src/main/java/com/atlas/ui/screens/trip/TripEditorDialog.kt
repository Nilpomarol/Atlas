package com.atlas.ui.screens.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.domain.model.Country
import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.LocationSearchResult
import com.atlas.domain.model.TravelStatus
import com.atlas.presentation.date.FlexibleDateRangeDraftField
import com.atlas.presentation.trip.QuickTripDraftUiState
import com.atlas.presentation.trip.TripEditorDraftUiState
import com.atlas.ui.components.date.FlexibleDateRangeField
import com.atlas.ui.components.tripStatusColors
import com.atlas.ui.theme.AtlasAccent
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasError
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface

@Composable
fun TripEditorDialog(
    draft: TripEditorDraftUiState,
    onDismiss: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onStatusChanged: (TravelStatus) -> Unit,
    onDatePrecisionChanged: (DatePrecision) -> Unit,
    onDateFieldChanged: (FlexibleDateRangeDraftField, String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = AtlasSurface,
        title = {
            Text(
                text = if (draft.tripId == null) {
                    "Nou viatge"
                } else {
                    "Edita viatge"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceStrong,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = draft.title,
                    onValueChange = onTitleChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = {
                        Text(text = "Títol")
                    },
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(TravelStatus.entries) { status ->
                        val statusColors = status.tripStatusColors()
                        val selected = draft.status == status
                        FilterChip(
                            selected = selected,
                            onClick = { onStatusChanged(status) },
                            label = {
                                Text(
                                    text = status.toCatalanLabel(),
                                    fontWeight = FontWeight.Bold,
                                )
                            },
                            shape = RoundedCornerShape(999.dp),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = statusColors.foreground.copy(alpha = 0.22f),
                                selectedBorderColor = statusColors.foreground.copy(alpha = 0.22f),
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = AtlasSurface,
                                labelColor = AtlasOnSurfaceMuted,
                                selectedContainerColor = statusColors.container,
                                selectedLabelColor = statusColors.foreground,
                            ),
                        )
                    }
                }

                FlexibleDateRangeField(
                    draft = draft.dateRange,
                    onPrecisionChanged = onDatePrecisionChanged,
                    onFieldChanged = onDateFieldChanged,
                    showHint = false,
                )

                OutlinedTextField(
                    value = draft.notes,
                    onValueChange = onNotesChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "Notes")
                    },
                    minLines = 1,
                    maxLines = 3,
                )

                draft.validationError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            CompactTripDialogActionButton(onClick = onSave) {
                Text(text = "Desa", fontWeight = FontWeight.ExtraBold, color = AtlasAccent)
            }
        },
        dismissButton = {
            CompactTripDialogActionButton(onClick = onDismiss) {
                Text(text = "Cancel·la")
            }
        },
    )
}

@Composable
fun QuickTripDialog(
    draft: QuickTripDraftUiState,
    countries: List<Country>,
    onDismiss: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onStatusChanged: (TravelStatus) -> Unit,
    onDatePrecisionChanged: (DatePrecision) -> Unit,
    onDateFieldChanged: (FlexibleDateRangeDraftField, String) -> Unit,
    onLocationSearchQueryChanged: (String) -> Unit,
    onLocationSearchResultSelected: (LocationSearchResult) -> Unit,
    onUseManualLocationClick: () -> Unit,
    onLocationNameChanged: (String) -> Unit,
    onCountryChanged: (String) -> Unit,
    onLatitudeChanged: (String) -> Unit,
    onLongitudeChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    val selectedCountry = countries.firstOrNull { it.iso2 == draft.countryIso2 }
    val hasCoordinates = draft.latitude.isNotBlank() && draft.longitude.isNotBlank()
    val showManualFields = draft.isManualEntryVisible

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = AtlasSurface,
        title = {
            Text(
                text = "Viatge ràpid",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceStrong,
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = draft.title,
                    onValueChange = onTitleChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(text = "Títol") },
                    shape = RoundedCornerShape(14.dp),
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TravelStatus.entries) { status ->
                        val statusColors = status.tripStatusColors()
                        val selected = draft.status == status
                        FilterChip(
                            selected = selected,
                            onClick = { onStatusChanged(status) },
                            label = {
                                Text(
                                    text = status.toCatalanLabel(),
                                    fontWeight = FontWeight.Bold,
                                )
                            },
                            shape = RoundedCornerShape(999.dp),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = statusColors.foreground.copy(alpha = 0.22f),
                                selectedBorderColor = statusColors.foreground.copy(alpha = 0.22f),
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = AtlasSurface,
                                labelColor = AtlasOnSurfaceMuted,
                                selectedContainerColor = statusColors.container,
                                selectedLabelColor = statusColors.foreground,
                            ),
                        )
                    }
                }

                QuickDialogSectionLabel("Lloc")
                OutlinedTextField(
                    value = draft.locationSearchQuery,
                    onValueChange = onLocationSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Cerca un lloc", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    placeholder = { Text("Nom o adreça...", color = AtlasOnSurfaceMuted) },
                    leadingIcon = {
                        if (draft.isSearchingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = AtlasPrimary,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = AtlasOnSurfaceMuted,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                )

                draft.locationSearchError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasError,
                    )
                }

                if (draft.locationSearchResults.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AtlasBackground)
                            .border(1.dp, AtlasOutline, RoundedCornerShape(12.dp)),
                    ) {
                        draft.locationSearchResults.forEach { result ->
                            QuickLocationSearchResultRow(
                                result = result,
                                onClick = { onLocationSearchResultSelected(result) },
                            )
                        }
                    }
                }

                if (draft.locationName.isNotBlank()) {
                    QuickSelectedLocationSummary(
                        locationName = draft.locationName,
                        countryName = selectedCountry?.nameCa ?: draft.countryIso2,
                        hasCoordinates = hasCoordinates,
                        showEditDetails = !showManualFields,
                        onEditDetailsClick = onUseManualLocationClick,
                    )
                }

                if (!showManualFields && draft.locationName.isBlank()) {
                    TextButton(
                        onClick = onUseManualLocationClick,
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = AtlasPrimary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    ) {
                        Text("Entrada manual", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }

                if (showManualFields) {
                    OutlinedTextField(
                        value = draft.locationName,
                        onValueChange = onLocationNameChanged,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Nom del lloc", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        shape = RoundedCornerShape(14.dp),
                    )
                    QuickCountryDropdown(
                        countries = countries,
                        selectedIso2 = draft.countryIso2,
                        onCountryChanged = onCountryChanged,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = draft.latitude,
                            onValueChange = onLatitudeChanged,
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("Latitud", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            placeholder = { Text("Opcional", color = AtlasOnSurfaceMuted) },
                            shape = RoundedCornerShape(14.dp),
                        )
                        OutlinedTextField(
                            value = draft.longitude,
                            onValueChange = onLongitudeChanged,
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("Longitud", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            placeholder = { Text("Opcional", color = AtlasOnSurfaceMuted) },
                            shape = RoundedCornerShape(14.dp),
                        )
                    }
                }

                Text(
                    text = "Dades de cerca © OpenStreetMap contributors",
                    style = MaterialTheme.typography.labelSmall,
                    color = AtlasOnSurfaceMuted,
                )

                QuickDialogSectionLabel("Data")
                FlexibleDateRangeField(
                    draft = draft.dateRange,
                    onPrecisionChanged = onDatePrecisionChanged,
                    onFieldChanged = onDateFieldChanged,
                    showHint = false,
                )

                draft.validationError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasError,
                    )
                }
            }
        },
        confirmButton = {
            CompactTripDialogActionButton(onClick = onSave) {
                Text(text = "Crea", fontWeight = FontWeight.ExtraBold, color = AtlasAccent)
            }
        },
        dismissButton = {
            CompactTripDialogActionButton(onClick = onDismiss) {
                Text(text = "Cancel·la", fontWeight = FontWeight.Bold, color = AtlasOnSurfaceMuted)
            }
        },
    )
}

@Composable
private fun QuickDialogSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        color = AtlasOnSurfaceMuted,
        letterSpacing = 0.12.sp,
    )
}

@Composable
private fun QuickSelectedLocationSummary(
    locationName: String,
    countryName: String,
    hasCoordinates: Boolean,
    showEditDetails: Boolean,
    onEditDetailsClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFE6F0FA))
            .border(1.dp, Color(0xFFBFD7EE), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AtlasPrimary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Place,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = locationName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = buildString {
                    append(countryName.ifBlank { "País pendent" })
                    append(" · ")
                    append(if (hasCoordinates) "Amb mapa" else "Manual")
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = AtlasOnSurfaceMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (showEditDetails) {
            TextButton(
                onClick = onEditDetailsClick,
                modifier = Modifier.height(30.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = AtlasPrimary),
            ) {
                Text("Edita", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun QuickLocationSearchResultRow(
    result: LocationSearchResult,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = AtlasOnSurfaceStrong),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = result.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = result.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = AtlasOnSurfaceMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickCountryDropdown(
    countries: List<Country>,
    selectedIso2: String,
    onCountryChanged: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCountry = countries.firstOrNull { it.iso2 == selectedIso2 }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selectedCountry?.nameCa.orEmpty(),
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            label = { Text("País o territori", fontWeight = FontWeight.Bold) },
            shape = RoundedCornerShape(14.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            countries.forEach { country ->
                DropdownMenuItem(
                    text = { Text(country.nameCa, fontWeight = FontWeight.SemiBold) },
                    onClick = {
                        onCountryChanged(country.iso2)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun CompactTripDialogActionButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.height(34.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
    ) {
        content()
    }
}
