package com.atlas.ui.rework.screens.trips

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atlas.domain.model.Country
import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.LocationSearchResult
import com.atlas.presentation.date.FlexibleDateRangeDraftField
import com.atlas.presentation.trip.TripStopDraftUiState
import com.atlas.ui.rework.components.ReworkDropdownItem
import com.atlas.ui.rework.components.ReworkDropdownMenu
import com.atlas.ui.rework.components.ReworkField
import com.atlas.ui.rework.components.ReworkFlexibleDateRange
import com.atlas.ui.rework.components.ReworkSheetActions
import com.atlas.ui.rework.components.ReworkTextInput
import com.atlas.ui.rework.foundation.AtlasReworkTheme

/**
 * Editor for a place on the trip. The same sheet serves the main route and a place
 * visited from another stop — the nesting comes from where the action was taken, so the
 * user is never asked to pick a record type.
 *
 * The place is chosen by searching a gazetteer: a picked result fills name, country and
 * coordinates at once (the coordinates are what the trip map later draws). Manual entry
 * stays available for places the search cannot find.
 */
@Composable
fun ReworkStopEditorSheet(
    draft: TripStopDraftUiState,
    countries: List<Country>,
    onDismiss: () -> Unit,
    onLocationSearchQueryChanged: (String) -> Unit,
    onLocationSearchResultSelected: (LocationSearchResult) -> Unit,
    onUseManualEntry: () -> Unit,
    onLocationNameChanged: (String) -> Unit,
    onCountryChanged: (String) -> Unit,
    onLatitudeChanged: (String) -> Unit,
    onLongitudeChanged: (String) -> Unit,
    onSideTripLabelChanged: (String) -> Unit,
    onDatePrecisionChanged: (DatePrecision) -> Unit,
    onDateFieldChanged: (FlexibleDateRangeDraftField, String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    if (!draft.isOpen) return
    val colors = AtlasReworkTheme.colors

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            shape = RoundedCornerShape(18.dp),
            color = colors.surfaceStrong,
        ) {
            Column(
                Modifier
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    draft.sheetTitle(),
                    style = AtlasReworkTheme.typography.title,
                    color = colors.ink,
                )
                draft.parentStopName?.let {
                    Text(
                        "Un lloc que vas visitar ${it.catalanFromPhrase()}",
                        style = AtlasReworkTheme.typography.body,
                        color = colors.inkMuted,
                    )
                }

                ReworkField("LLOC") {
                    LocationSearchField(
                        query = draft.locationSearchQuery,
                        results = draft.locationSearchResults,
                        isSearching = draft.isSearchingLocation,
                        onQueryChange = onLocationSearchQueryChanged,
                        onResultSelected = onLocationSearchResultSelected,
                    )
                }

                draft.locationSearchError?.let {
                    Text(it, style = AtlasReworkTheme.typography.body, color = colors.inkMuted)
                }

                // The picked place fills these; manual entry reveals them for a place the
                // gazetteer could not find. Country stays visible because it is required.
                ManualLocationDisclosure(
                    draft = draft,
                    countries = countries,
                    onUseManualEntry = onUseManualEntry,
                    onLocationNameChanged = onLocationNameChanged,
                    onCountryChanged = onCountryChanged,
                    onLatitudeChanged = onLatitudeChanged,
                    onLongitudeChanged = onLongitudeChanged,
                )

                if (draft.parentStopId != null) {
                    ReworkField("AGRUPACIÓ", optional = true) {
                        ReworkTextInput(draft.sideTripLabel, "Per exemple: Dia de temples", onSideTripLabelChanged)
                    }
                }

                ReworkField("DATES", optional = true) {
                    ReworkFlexibleDateRange(
                        dateRange = draft.dateRange,
                        onPrecisionChange = onDatePrecisionChanged,
                        onFieldChange = onDateFieldChanged,
                    )
                }

                ReworkField("NOTES", optional = true) {
                    ReworkTextInput(draft.notes, "Què hi vas fer", onNotesChanged, minHeight = 66.dp, singleLine = false)
                }

                draft.validationError?.let {
                    Text(it, style = AtlasReworkTheme.typography.body, color = colors.living)
                }

                ReworkSheetActions(
                    onCancel = onDismiss,
                    onConfirm = onSave,
                    confirmLabel = "Desa",
                )
            }
        }
    }
}

@Composable
private fun LocationSearchField(
    query: String,
    results: List<LocationSearchResult>,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    onResultSelected: (LocationSearchResult) -> Unit,
) {
    val colors = AtlasReworkTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Surface(
            shape = RoundedCornerShape(11.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Search, null, tint = colors.inkMuted, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(4.dp))
                Box(Modifier.weight(1f)) {
                    // Borderless inner field: the row above already draws the surface.
                    ReworkTextInput(
                        value = query,
                        placeholder = "Cerca una ciutat o un lloc",
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        bordered = false,
                    )
                }
                if (isSearching) {
                    Spacer(Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = colors.accent,
                    )
                }
            }
        }
        if (results.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(11.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border),
            ) {
                Column {
                    val shown = results.take(MAX_SEARCH_RESULTS)
                    shown.forEachIndexed { index, result ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onResultSelected(result) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Place, null, tint = colors.accent, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(9.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    result.name,
                                    style = AtlasReworkTheme.typography.body,
                                    color = colors.ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    result.displayName,
                                    style = AtlasReworkTheme.typography.data.copy(fontSize = 11.sp),
                                    color = colors.inkMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        if (index < shown.lastIndex) {
                            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualLocationDisclosure(
    draft: TripStopDraftUiState,
    countries: List<Country>,
    onUseManualEntry: () -> Unit,
    onLocationNameChanged: (String) -> Unit,
    onCountryChanged: (String) -> Unit,
    onLatitudeChanged: (String) -> Unit,
    onLongitudeChanged: (String) -> Unit,
) {
    val colors = AtlasReworkTheme.colors
    val hasCoordinates = draft.latitude.isNotBlank() && draft.longitude.isNotBlank()

    if (!draft.isManualEntryVisible && !hasCoordinates) {
        Text(
            "No el trobes? Afegeix-lo manualment",
            style = AtlasReworkTheme.typography.label,
            color = colors.accent,
            modifier = Modifier.clickable(onClick = onUseManualEntry),
        )
        return
    }

    // Manual entry owns the name; a searched result already carries one and shows it in
    // the search field above.
    if (draft.isManualEntryVisible) {
        ReworkField("NOM DEL LLOC") {
            ReworkTextInput(draft.locationName, "Nom del lloc", onLocationNameChanged)
        }
    }

    ReworkField("PAÍS O TERRITORI") {
        CountryPicker(
            countries = countries,
            selectedIso2 = draft.countryIso2,
            onSelected = onCountryChanged,
        )
    }

    if (draft.isManualEntryVisible) {
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            ReworkField("LATITUD", Modifier.weight(1f), optional = true) {
                ReworkTextInput(draft.latitude, "0.0", onLatitudeChanged, keyboardType = KeyboardType.Number, mono = true)
            }
            ReworkField("LONGITUD", Modifier.weight(1f), optional = true) {
                ReworkTextInput(draft.longitude, "0.0", onLongitudeChanged, keyboardType = KeyboardType.Number, mono = true)
            }
        }
    }
}

@Composable
private fun CountryPicker(
    countries: List<Country>,
    selectedIso2: String,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf("") }
    val colors = AtlasReworkTheme.colors
    val selected = countries.firstOrNull { it.iso2 == selectedIso2 }
    val filtered = remember(filter, countries) {
        if (filter.isBlank()) countries
        else countries.filter { it.nameCa.contains(filter, ignoreCase = true) }
    }
    Box {
        Surface(
            shape = RoundedCornerShape(11.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border),
            onClick = { expanded = true },
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    selected?.let { "${it.flagEmoji.orEmpty()} ${it.nameCa}" }?.trim() ?: "Tria un país",
                    style = AtlasReworkTheme.typography.body,
                    color = if (selected == null) colors.inkMuted else colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.Rounded.KeyboardArrowDown, null, tint = colors.accent, modifier = Modifier.size(18.dp))
            }
        }
        ReworkDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false; filter = "" }) {
            Box(Modifier.padding(horizontal = 8.dp, vertical = 6.dp).widthIn(min = 240.dp)) {
                ReworkTextInput(filter, "Cerca un país", { filter = it }, modifier = Modifier.fillMaxWidth())
            }
            filtered.take(MAX_COUNTRY_OPTIONS).forEach { country ->
                ReworkDropdownItem(
                    label = "${country.flagEmoji.orEmpty()} ${country.nameCa}".trim(),
                    selected = country.iso2 == selectedIso2,
                    onClick = { onSelected(country.iso2); expanded = false; filter = "" },
                )
            }
        }
    }
}

private const val MAX_COUNTRY_OPTIONS = 40
private const val MAX_SEARCH_RESULTS = 6

/**
 * Catalan elides `de` to `d'` before a vowel or a silent h, so a place name decides the
 * preposition: "des d'Osaka" but "des de Tòquio". Accented vowels count.
 */
private fun String.catalanFromPhrase(): String {
    val first = trimStart().firstOrNull()?.lowercaseChar() ?: return "des de $this"
    val elides = first == 'h' || first in "aeiou" || first in "àèéíòóúï"
    return if (elides) "des d'$this" else "des de $this"
}

private fun TripStopDraftUiState.sheetTitle(): String = when {
    isEditing && parentStopId != null -> "Edita la sortida"
    isEditing -> "Edita la parada"
    parentStopId != null -> "Nova sortida"
    else -> "Nova parada"
}
