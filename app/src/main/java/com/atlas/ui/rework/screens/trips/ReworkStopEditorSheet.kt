package com.atlas.ui.rework.screens.trips

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.KeyboardOptions
import com.atlas.domain.model.Country
import com.atlas.domain.model.DatePrecision
import com.atlas.presentation.date.FlexibleDateRangeDraftField
import com.atlas.presentation.trip.TripStopDraftUiState
import com.atlas.ui.rework.components.ReworkDropdownItem
import com.atlas.ui.rework.components.ReworkDropdownMenu
import com.atlas.ui.rework.foundation.AtlasReworkTheme

/**
 * Editor for a place on the trip. The same sheet serves the main route and a place
 * visited from another stop — the nesting comes from where the action was taken, so the
 * user is never asked to pick a record type.
 */
@Composable
fun ReworkStopEditorSheet(
    draft: TripStopDraftUiState,
    countries: List<Country>,
    onDismiss: () -> Unit,
    onLocationNameChanged: (String) -> Unit,
    onCountryChanged: (String) -> Unit,
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
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp),
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

                Field("LLOC") {
                    EditorTextField(draft.locationName, "Nom del lloc", onLocationNameChanged)
                }

                Field("PAÍS O TERRITORI") {
                    CountryPicker(
                        countries = countries,
                        selectedIso2 = draft.countryIso2,
                        onSelected = onCountryChanged,
                    )
                }

                if (draft.parentStopId != null) {
                    Field("AGRUPACIÓ (OPCIONAL)") {
                        EditorTextField(draft.sideTripLabel, "Per exemple: Dia de temples", onSideTripLabelChanged)
                    }
                }

                Field("PRECISIÓ DE LA DATA") {
                    PrecisionPicker(draft.dateRange.precision, onDatePrecisionChanged)
                }

                Field("DATES (OPCIONAL)") {
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        DateRow(
                            label = "Inici",
                            precision = draft.dateRange.precision,
                            year = draft.dateRange.startYear,
                            month = draft.dateRange.startMonth,
                            day = draft.dateRange.startDay,
                            onYear = { onDateFieldChanged(FlexibleDateRangeDraftField.StartYear, it) },
                            onMonth = { onDateFieldChanged(FlexibleDateRangeDraftField.StartMonth, it) },
                            onDay = { onDateFieldChanged(FlexibleDateRangeDraftField.StartDay, it) },
                        )
                        DateRow(
                            label = "Final",
                            precision = draft.dateRange.precision,
                            year = draft.dateRange.endYear,
                            month = draft.dateRange.endMonth,
                            day = draft.dateRange.endDay,
                            onYear = { onDateFieldChanged(FlexibleDateRangeDraftField.EndYear, it) },
                            onMonth = { onDateFieldChanged(FlexibleDateRangeDraftField.EndMonth, it) },
                            onDay = { onDateFieldChanged(FlexibleDateRangeDraftField.EndDay, it) },
                        )
                    }
                }

                Field("NOTES (OPCIONAL)") {
                    EditorTextField(draft.notes, "Què hi vas fer", onNotesChanged, minHeight = 66.dp)
                }

                draft.validationError?.let {
                    Text(it, style = AtlasReworkTheme.typography.body, color = colors.living)
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(11.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border),
                        onClick = onDismiss,
                    ) {
                        Text(
                            "Cancel·la",
                            Modifier.padding(vertical = 11.dp),
                            style = AtlasReworkTheme.typography.label,
                            color = colors.ink,
                        )
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(11.dp),
                        color = colors.accent,
                        onClick = onSave,
                    ) {
                        Text(
                            "Desa",
                            Modifier.padding(vertical = 11.dp),
                            style = AtlasReworkTheme.typography.label,
                            color = androidx.compose.ui.graphics.Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Field(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, style = AtlasReworkTheme.typography.label, color = AtlasReworkTheme.colors.accent)
        content()
    }
}

@Composable
private fun EditorTextField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    minHeight: androidx.compose.ui.unit.Dp = 42.dp,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val colors = AtlasReworkTheme.colors
    Surface(
        shape = RoundedCornerShape(11.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border),
    ) {
        Box(Modifier.fillMaxWidth().heightIn(min = minHeight).padding(horizontal = 12.dp, vertical = 11.dp)) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = AtlasReworkTheme.typography.body.copy(color = colors.ink),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(placeholder, style = AtlasReworkTheme.typography.body, color = colors.inkMuted)
                    }
                    inner()
                },
            )
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
    val colors = AtlasReworkTheme.colors
    val selected = countries.firstOrNull { it.iso2 == selectedIso2 }
    Box {
        Surface(
            shape = RoundedCornerShape(11.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border),
            onClick = { expanded = true },
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 11.dp),
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
        ReworkDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            countries.take(MAX_COUNTRY_OPTIONS).forEach { country ->
                ReworkDropdownItem(
                    label = "${country.flagEmoji.orEmpty()} ${country.nameCa}".trim(),
                    selected = country.iso2 == selectedIso2,
                    onClick = { onSelected(country.iso2); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun PrecisionPicker(precision: DatePrecision, onSelected: (DatePrecision) -> Unit) {
    val colors = AtlasReworkTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        DatePrecision.entries.forEach { option ->
            val isSelected = option == precision
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(9.dp),
                color = if (isSelected) colors.ink else colors.surface,
                border = BorderStroke(1.dp, colors.border),
                onClick = { onSelected(option) },
            ) {
                Text(
                    option.label(),
                    Modifier.padding(vertical = 9.dp),
                    style = AtlasReworkTheme.typography.label,
                    color = if (isSelected) colors.surfaceStrong else colors.ink,
                )
            }
        }
    }
}

@Composable
private fun DateRow(
    label: String,
    precision: DatePrecision,
    year: String,
    month: String,
    day: String,
    onYear: (String) -> Unit,
    onMonth: (String) -> Unit,
    onDay: (String) -> Unit,
) {
    val colors = AtlasReworkTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = AtlasReworkTheme.typography.body,
            color = colors.inkMuted,
            modifier = Modifier.width(52.dp),
        )
        if (precision == DatePrecision.DAY) {
            Box(Modifier.weight(1f)) { EditorTextField(day, "DD", onDay, keyboardType = KeyboardType.Number) }
            Spacer(Modifier.width(6.dp))
        }
        if (precision != DatePrecision.YEAR) {
            Box(Modifier.weight(1f)) { EditorTextField(month, "MM", onMonth, keyboardType = KeyboardType.Number) }
            Spacer(Modifier.width(6.dp))
        }
        Box(Modifier.weight(1.4f)) { EditorTextField(year, "AAAA", onYear, keyboardType = KeyboardType.Number) }
    }
}

private const val MAX_COUNTRY_OPTIONS = 60

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

private fun DatePrecision.label(): String = when (this) {
    DatePrecision.YEAR -> "ANY"
    DatePrecision.MONTH -> "MES"
    DatePrecision.DAY -> "DIA"
}
