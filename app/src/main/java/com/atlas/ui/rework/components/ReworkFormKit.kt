package com.atlas.ui.rework.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.DatePrecision
import com.atlas.presentation.date.FlexibleDateRangeDraftField
import com.atlas.presentation.date.FlexibleDateRangeDraftUiState
import com.atlas.ui.rework.foundation.AtlasReworkTheme

/**
 * Shared form primitives for the rework editor sheets (stop editor today, trip editor
 * next). Extracted so the two sheets read as one family and a fix to a field lands in
 * both. Nothing here owns trip- or stop-specific meaning.
 */

/** Accent label above a field. Marks optional fields quietly rather than flagging required. */
@Composable
fun ReworkField(
    label: String,
    modifier: Modifier = Modifier,
    optional: Boolean = false,
    content: @Composable () -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(label, style = AtlasReworkTheme.typography.label, color = AtlasReworkTheme.colors.accent)
            if (optional) {
                Spacer(Modifier.width(6.dp))
                Text(
                    "opcional",
                    style = AtlasReworkTheme.typography.label,
                    color = AtlasReworkTheme.colors.inkMuted,
                )
            }
        }
        content()
    }
}

/** A paper/ink text input matching the rework surface. Body font for prose, mono for codes. */
@Composable
fun ReworkTextInput(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    minHeight: Dp = 44.dp,
    keyboardType: KeyboardType = KeyboardType.Text,
    mono: Boolean = false,
    singleLine: Boolean = true,
    bordered: Boolean = true,
) {
    val colors = AtlasReworkTheme.colors
    val textStyle = (if (mono) AtlasReworkTheme.typography.data else AtlasReworkTheme.typography.body)
        .copy(color = colors.ink)
    val field: @Composable () -> Unit = {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = textStyle,
            singleLine = singleLine,
            cursorBrush = SolidColor(colors.accent),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(placeholder, style = textStyle.copy(color = colors.inkMuted))
                }
                inner()
            },
        )
    }
    if (bordered) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(11.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border),
        ) {
            Box(Modifier.fillMaxWidth().heightIn(min = minHeight).padding(horizontal = 12.dp, vertical = 11.dp)) {
                field()
            }
        }
    } else {
        Box(modifier.fillMaxWidth().heightIn(min = minHeight).padding(vertical = 11.dp)) {
            field()
        }
    }
}

/**
 * Flexible date range input. One segmented control chooses precision; the fields below
 * show only the parts that precision carries, so a year-only trip never shows day boxes.
 * Dates are typed and monospace, matching how dates read everywhere else in the rework.
 */
@Composable
fun ReworkFlexibleDateRange(
    dateRange: FlexibleDateRangeDraftUiState,
    onPrecisionChange: (DatePrecision) -> Unit,
    onFieldChange: (FlexibleDateRangeDraftField, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(9.dp)) {
        PrecisionToggle(dateRange.precision, onPrecisionChange)
        DateLine(
            label = "Inici",
            precision = dateRange.precision,
            day = dateRange.startDay,
            month = dateRange.startMonth,
            year = dateRange.startYear,
            onDay = { onFieldChange(FlexibleDateRangeDraftField.StartDay, it) },
            onMonth = { onFieldChange(FlexibleDateRangeDraftField.StartMonth, it) },
            onYear = { onFieldChange(FlexibleDateRangeDraftField.StartYear, it) },
        )
        DateLine(
            label = "Final",
            precision = dateRange.precision,
            day = dateRange.endDay,
            month = dateRange.endMonth,
            year = dateRange.endYear,
            onDay = { onFieldChange(FlexibleDateRangeDraftField.EndDay, it) },
            onMonth = { onFieldChange(FlexibleDateRangeDraftField.EndMonth, it) },
            onYear = { onFieldChange(FlexibleDateRangeDraftField.EndYear, it) },
        )
    }
}

@Composable
private fun PrecisionToggle(precision: DatePrecision, onSelected: (DatePrecision) -> Unit) {
    val colors = AtlasReworkTheme.colors
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border),
    ) {
        Row(Modifier.padding(3.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            DatePrecision.entries.forEach { option ->
                val isSelected = option == precision
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) colors.ink else Color.Transparent,
                    onClick = { onSelected(option) },
                ) {
                    Text(
                        option.segmentLabel(),
                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        style = AtlasReworkTheme.typography.label,
                        color = if (isSelected) colors.surfaceStrong else colors.inkMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScopeSpacer() = Spacer(Modifier.width(6.dp))

@Composable
private fun DateLine(
    label: String,
    precision: DatePrecision,
    day: String,
    month: String,
    year: String,
    onDay: (String) -> Unit,
    onMonth: (String) -> Unit,
    onYear: (String) -> Unit,
) {
    val colors = AtlasReworkTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = AtlasReworkTheme.typography.body,
            color = colors.inkMuted,
            modifier = Modifier.width(48.dp),
        )
        if (precision == DatePrecision.DAY) {
            Box(Modifier.weight(1f)) {
                ReworkTextInput(day, "DD", onDay, keyboardType = KeyboardType.Number, mono = true)
            }
            RowScopeSpacer()
        }
        if (precision != DatePrecision.YEAR) {
            Box(Modifier.weight(1f)) {
                ReworkTextInput(month, "MM", onMonth, keyboardType = KeyboardType.Number, mono = true)
            }
            RowScopeSpacer()
        }
        Box(Modifier.weight(1.4f)) {
            ReworkTextInput(year, "AAAA", onYear, keyboardType = KeyboardType.Number, mono = true)
        }
    }
}

/** Cancel / confirm footer for an editor sheet. The confirm button carries the accent. */
@Composable
fun ReworkSheetActions(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    confirmLabel: String,
    modifier: Modifier = Modifier,
    cancelLabel: String = "Cancel·la",
) {
    val colors = AtlasReworkTheme.colors
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(11.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border),
            onClick = onCancel,
        ) {
            Text(
                cancelLabel,
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                style = AtlasReworkTheme.typography.label,
                color = colors.ink,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(11.dp),
            color = colors.accent,
            onClick = onConfirm,
        ) {
            Text(
                confirmLabel,
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                style = AtlasReworkTheme.typography.label,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

private fun DatePrecision.segmentLabel(): String = when (this) {
    DatePrecision.YEAR -> "ANY"
    DatePrecision.MONTH -> "MES"
    DatePrecision.DAY -> "DIA"
}
