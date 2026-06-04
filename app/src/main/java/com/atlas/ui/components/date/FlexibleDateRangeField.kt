package com.atlas.ui.components.date

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.FlexibleDate
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.presentation.date.FlexibleDateRangeDraftField
import com.atlas.presentation.date.FlexibleDateRangeDraftUiState
import com.atlas.ui.theme.AtlasAccent
import com.atlas.ui.theme.AtlasAccentContainer
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasSurface
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

// ─────────────────────────────────────────────
// Palette (mirrors CountryDetailScreen accents)
// ─────────────────────────────────────────────
private val FdrInk      = AtlasOnSurfaceStrong
private val FdrMuted    = AtlasOnSurfaceMuted
private val FdrBg       = AtlasBackground
private val FdrCard     = AtlasSurface
private val FdrBorder   = AtlasOutline
private val FdrAccent   = AtlasAccent
private val FdrAccentLt = AtlasAccentContainer

// ─────────────────────────────────────────────
// Public entry point
// ─────────────────────────────────────────────
@Composable
fun FlexibleDateRangeField(
    draft: FlexibleDateRangeDraftUiState,
    onPrecisionChanged: (DatePrecision) -> Unit,
    onFieldChanged: (FlexibleDateRangeDraftField, String) -> Unit,
    modifier: Modifier = Modifier,
    showHint: Boolean = true,
) {
    var activeSide by remember { mutableStateOf<FlexibleDateSide?>(null) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {

        // ── Precision segmented control ──
        PrecisionSegmentedControl(
            selected = draft.precision,
            onSelected = onPrecisionChanged,
        )

        // ── Date input fields ──
        DateInputField(
            label = "Inici",
            precision = draft.precision,
            year = draft.startYear,
            month = draft.startMonth,
            day = draft.startDay,
            onClick = { activeSide = FlexibleDateSide.Start },
            onClear = {
                onFieldChanged(FlexibleDateRangeDraftField.StartYear, "")
                onFieldChanged(FlexibleDateRangeDraftField.StartMonth, "")
                onFieldChanged(FlexibleDateRangeDraftField.StartDay, "")
            },
        )

        DateInputField(
            label = "Final",
            precision = draft.precision,
            year = draft.endYear,
            month = draft.endMonth,
            day = draft.endDay,
            onClick = { activeSide = FlexibleDateSide.End },
            onClear = {
                onFieldChanged(FlexibleDateRangeDraftField.EndYear, "")
                onFieldChanged(FlexibleDateRangeDraftField.EndMonth, "")
                onFieldChanged(FlexibleDateRangeDraftField.EndDay, "")
            },
        )

        if (showHint) {
            // ── Hint ──
            Text(
                text = "La data és opcional.",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = FdrMuted,
            )
        }
    }

    // ── Date picker dialogs ──
    activeSide?.let { side ->
        FlexibleDatePickerDialog(
            side = side,
            precision = draft.precision,
            year = draft.yearFor(side),
            month = draft.monthFor(side),
            day = draft.dayFor(side),
            onDismiss = { activeSide = null },
            onConfirm = { selection ->
                writeSelection(
                    side = side,
                    precision = draft.precision,
                    selection = selection,
                    onFieldChanged = onFieldChanged,
                )
                activeSide = null
            },
        )
    }
}

// ─────────────────────────────────────────────
// Precision segmented control
// Replaces FilterChip row — one unified pill with
// three segments, active segment gets solid fill.
// ─────────────────────────────────────────────
@Composable
private fun PrecisionSegmentedControl(
    selected: DatePrecision,
    onSelected: (DatePrecision) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FdrBg)
            .border(1.dp, FdrBorder, RoundedCornerShape(10.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        DatePrecision.entries.forEach { precision ->
            val isSelected = precision == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) FdrAccent else Color.Transparent)
                    .then(
                        if (!isSelected) Modifier else Modifier,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                TextButton(
                    onClick = { onSelected(precision) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isSelected) Color.White else FdrMuted,
                        containerColor = Color.Transparent,
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                ) {
                    Text(
                        text = precision.toCatalanLabel(),
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Date input field
// ─────────────────────────────────────────────
@Composable
private fun DateInputField(
    label: String,
    precision: DatePrecision,
    year: String,
    month: String,
    day: String,
    onClick: () -> Unit,
    onClear: () -> Unit,
) {
    val hasValue = year.isNotBlank()
    val displayText = formatDraftDate(precision, year, month, day)

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = FdrCard,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (hasValue) FdrAccent.copy(alpha = 0.35f) else FdrBorder,
        ),
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Icon tile
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (hasValue) FdrAccentLt else FdrBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (hasValue) FdrAccent else FdrMuted,
                )
            }

            // Label + value
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = FdrMuted,
                    letterSpacing = 0.10.sp,
                )
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (hasValue) FdrInk else FdrMuted,
                )
            }

            // Clear button
            if (hasValue) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Neteja",
                        modifier = Modifier.size(16.dp),
                        tint = FdrMuted,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Dialog router
// ─────────────────────────────────────────────
@Composable
private fun FlexibleDatePickerDialog(
    side: FlexibleDateSide,
    precision: DatePrecision,
    year: String,
    month: String,
    day: String,
    onDismiss: () -> Unit,
    onConfirm: (DateSelection) -> Unit,
) {
    when (precision) {
        DatePrecision.YEAR -> YearPickerDialog(
            title = "${side.toCatalanLabel()} — Any",
            initialYear = year.toIntOrNull() ?: currentYear(),
            onDismiss = onDismiss,
            onConfirm = { onConfirm(DateSelection(year = it)) },
        )
        DatePrecision.MONTH -> MonthPickerDialog(
            title = "${side.toCatalanLabel()} — Mes",
            initialYear = year.toIntOrNull() ?: currentYear(),
            initialMonth = month.toIntOrNull()?.coerceIn(1, 12) ?: currentMonth(),
            onDismiss = onDismiss,
            onConfirm = { y, m -> onConfirm(DateSelection(year = y, month = m)) },
        )
        DatePrecision.DAY -> DayPickerDialog(
            initialDate = draftLocalDate(year, month, day),
            onDismiss = onDismiss,
            onConfirm = { date ->
                onConfirm(DateSelection(date.year, date.monthValue, date.dayOfMonth))
            },
        )
    }
}

// ─────────────────────────────────────────────
// Year picker dialog
// ─────────────────────────────────────────────
@Composable
private fun YearPickerDialog(
    title: String,
    initialYear: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var selectedYear by remember(initialYear) { mutableStateOf(initialYear) }

    AtlasDialog(
        title = title,
        onDismiss = onDismiss,
        onConfirm = { onConfirm(selectedYear) },
    ) {
        YearStepper(
            year = selectedYear,
            onPrevious = { selectedYear -= 1 },
            onNext = { selectedYear += 1 },
        )
    }
}

// ─────────────────────────────────────────────
// Month picker dialog
// ─────────────────────────────────────────────
@Composable
private fun MonthPickerDialog(
    title: String,
    initialYear: Int,
    initialMonth: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    var selectedYear  by remember(initialYear)  { mutableStateOf(initialYear) }
    var selectedMonth by remember(initialMonth) { mutableStateOf(initialMonth) }

    AtlasDialog(
        title = title,
        onDismiss = onDismiss,
        onConfirm = { onConfirm(selectedYear, selectedMonth) },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            YearStepper(
                year = selectedYear,
                onPrevious = { selectedYear -= 1 },
                onNext = { selectedYear += 1 },
            )
            MonthGrid(
                selectedMonth = selectedMonth,
                onMonthSelected = { selectedMonth = it },
            )
        }
    }
}

// ─────────────────────────────────────────────
// Day picker dialog — delegates to Material3
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayPickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toUtcMillis(),
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        colors = DatePickerDefaults.colors(
            containerColor = FdrCard,
            titleContentColor = FdrInk,
            headlineContentColor = FdrInk,
            weekdayContentColor = FdrMuted,
            subheadContentColor = FdrMuted,
            yearContentColor = FdrInk,
            currentYearContentColor = FdrAccent,
            selectedYearContentColor = Color.White,
            selectedYearContainerColor = FdrAccent,
            dayContentColor = FdrInk,
            selectedDayContentColor = Color.White,
            selectedDayContainerColor = FdrAccent,
            todayContentColor = FdrAccent,
            todayDateBorderColor = FdrAccent,
        ),
        confirmButton = {
            CompactDialogActionButton(
                onClick = {
                    val date = state.selectedDateMillis?.toLocalDate() ?: initialDate
                    onConfirm(date)
                },
            ) {
                Text("Fet", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            CompactDialogActionButton(onClick = onDismiss) {
                Text("Cancel·la", fontWeight = FontWeight.Bold)
            }
        },
    ) {
        DatePicker(state = state)
    }
}

// ─────────────────────────────────────────────
// Shared dialog shell — rounded, styled
// ─────────────────────────────────────────────
@Composable
private fun AtlasDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = FdrCard,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = FdrInk,
            )
        },
        text = { content() },
        confirmButton = {
            CompactDialogActionButton(onClick = onConfirm) {
                Text("Fet", fontWeight = FontWeight.ExtraBold, color = FdrAccent)
            }
        },
        dismissButton = {
            CompactDialogActionButton(onClick = onDismiss) {
                Text("Cancel·la", fontWeight = FontWeight.Bold, color = FdrMuted)
            }
        },
    )
}

@Composable
private fun CompactDialogActionButton(
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

// ─────────────────────────────────────────────
// Year stepper — prev / year / next
// ─────────────────────────────────────────────
@Composable
private fun YearStepper(
    year: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FdrBg)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = "Any anterior",
                tint = FdrInk,
            )
        }
        Text(
            text = year.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = FdrInk,
        )
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Any següent",
                tint = FdrInk,
            )
        }
    }
}

// ─────────────────────────────────────────────
// Month grid — 4×3, single MonthCell composable
// ─────────────────────────────────────────────
@Composable
private fun MonthGrid(
    selectedMonth: Int,
    onMonthSelected: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        (1..12).chunked(4).forEach { rowMonths ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                rowMonths.forEach { month ->
                    MonthCell(
                        label = month.toMonthLabel(),
                        selected = month == selectedMonth,
                        modifier = Modifier.weight(1f),
                        onClick = { onMonthSelected(month) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthCell(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) FdrAccent else FdrBg,
            contentColor   = if (selected) Color.White else FdrInk,
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
    ) {
        Text(
            text = label,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontSize = 12.sp,
        )
    }
}

// ─────────────────────────────────────────────
// Internal models — unchanged logic
// ─────────────────────────────────────────────
private enum class FlexibleDateSide { Start, End }

private data class DateSelection(
    val year: Int,
    val month: Int? = null,
    val day: Int? = null,
)

private fun writeSelection(
    side: FlexibleDateSide,
    precision: DatePrecision,
    selection: DateSelection,
    onFieldChanged: (FlexibleDateRangeDraftField, String) -> Unit,
) {
    val fields = side.fields()
    onFieldChanged(fields.year, selection.year.toString())
    onFieldChanged(
        fields.month,
        when (precision) {
            DatePrecision.YEAR  -> ""
            DatePrecision.MONTH,
            DatePrecision.DAY   -> selection.month?.toString().orEmpty()
        },
    )
    onFieldChanged(
        fields.day,
        when (precision) {
            DatePrecision.YEAR,
            DatePrecision.MONTH -> ""
            DatePrecision.DAY   -> selection.day?.toString().orEmpty()
        },
    )
}

private data class SideFields(
    val year: FlexibleDateRangeDraftField,
    val month: FlexibleDateRangeDraftField,
    val day: FlexibleDateRangeDraftField,
)

private fun FlexibleDateSide.fields(): SideFields = when (this) {
    FlexibleDateSide.Start -> SideFields(
        year  = FlexibleDateRangeDraftField.StartYear,
        month = FlexibleDateRangeDraftField.StartMonth,
        day   = FlexibleDateRangeDraftField.StartDay,
    )
    FlexibleDateSide.End -> SideFields(
        year  = FlexibleDateRangeDraftField.EndYear,
        month = FlexibleDateRangeDraftField.EndMonth,
        day   = FlexibleDateRangeDraftField.EndDay,
    )
}

private fun FlexibleDateRangeDraftUiState.yearFor(side: FlexibleDateSide)  = when (side) { FlexibleDateSide.Start -> startYear;  FlexibleDateSide.End -> endYear }
private fun FlexibleDateRangeDraftUiState.monthFor(side: FlexibleDateSide) = when (side) { FlexibleDateSide.Start -> startMonth; FlexibleDateSide.End -> endMonth }
private fun FlexibleDateRangeDraftUiState.dayFor(side: FlexibleDateSide)   = when (side) { FlexibleDateSide.Start -> startDay;   FlexibleDateSide.End -> endDay }

private fun FlexibleDateSide.toCatalanLabel(): String = when (this) {
    FlexibleDateSide.Start -> "Inici"
    FlexibleDateSide.End   -> "Final"
}

// ─────────────────────────────────────────────
// Formatting helpers — unchanged logic
// ─────────────────────────────────────────────
private fun formatDraftDate(
    precision: DatePrecision,
    year: String,
    month: String,
    day: String,
): String {
    val y = year.toIntOrNull() ?: return "Sense data"
    return when (precision) {
        DatePrecision.YEAR  -> y.toString()
        DatePrecision.MONTH -> {
            val m = month.toIntOrNull()?.coerceIn(1, 12) ?: return "Sense data"
            flexibleDateRangeFieldFormatter.format(FlexibleDate(y, m, null, DatePrecision.MONTH))
        }
        DatePrecision.DAY   -> {
            val m = month.toIntOrNull()?.coerceIn(1, 12) ?: return "Sense data"
            val d = day.toIntOrNull() ?: return "Sense data"
            flexibleDateRangeFieldFormatter.format(FlexibleDate(y, m, d, DatePrecision.DAY))
        }
    }
}

private fun draftLocalDate(year: String, month: String, day: String): LocalDate {
    val now = LocalDate.now()
    val y   = year.toIntOrNull()  ?: now.year
    val m   = month.toIntOrNull()?.coerceIn(1, 12) ?: now.monthValue
    val max = LocalDate.of(y, m, 1).lengthOfMonth()
    val d   = day.toIntOrNull()?.coerceIn(1, max) ?: now.dayOfMonth.coerceAtMost(max)
    return LocalDate.of(y, m, d)
}

private fun LocalDate.toUtcMillis(): Long =
    atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

private fun Int.toMonthLabel(): String = when (this) {
    1  -> "Gen."  2  -> "Febr." 3  -> "Març"
    4  -> "Abr."  5  -> "Maig"  6  -> "Juny"
    7  -> "Jul."  8  -> "Ag."   9  -> "Set."
    10 -> "Oct."  11 -> "Nov."  12 -> "Des."
    else -> "Mes $this"
}

private val flexibleDateRangeFieldFormatter = FlexibleDateFormatter()

private fun currentYear()  = LocalDate.now().year
private fun currentMonth() = LocalDate.now().monthValue

private fun DatePrecision.toCatalanLabel(): String = when (this) {
    DatePrecision.YEAR  -> "Any"
    DatePrecision.MONTH -> "Mes"
    DatePrecision.DAY   -> "Dia"
}
