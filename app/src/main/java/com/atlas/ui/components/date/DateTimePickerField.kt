package com.atlas.ui.components.date

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.window.Dialog
import com.atlas.ui.theme.AtlasAccent
import com.atlas.ui.theme.AtlasAccentContainer
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasSurface
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Tappable field that opens a DatePicker then a TimePicker in sequence.
 * Value is stored and returned as "YYYY-MM-DDTHH:mm" (ISO local datetime).
 */
@Composable
fun DateTimePickerField(
    label: String,
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    // When value is blank, seeds the date/time pickers from this string instead of today/00:00.
    // Pass a full "YYYY-MM-DDTHH:mm" to prefill both, or just "YYYY-MM-DD" to prefill date only.
    prefillValue: String? = null,
) {
    var step by remember { mutableStateOf<DateTimePickStep?>(null) }
    var pendingDate by remember { mutableStateOf<LocalDate?>(null) }

    val effectiveInitial = if (value.isBlank() && !prefillValue.isNullOrBlank()) prefillValue else value

    DateTimeDisplayCard(
        label = label,
        value = value,
        onClick = { step = DateTimePickStep.Date },
        onClear = { onValueChanged("") },
        modifier = modifier,
    )

    when (step) {
        DateTimePickStep.Date -> DateStepDialog(
            initialDate = parseIsoDate(effectiveInitial),
            onDismiss = { step = null },
            onConfirm = { date ->
                pendingDate = date
                step = DateTimePickStep.Time
            },
        )
        DateTimePickStep.Time -> TimeStepDialog(
            initialTime = parseIsoTime(effectiveInitial),
            onDismiss = { step = null },
            onConfirm = { hour, minute ->
                val date = pendingDate ?: LocalDate.now()
                onValueChanged("%04d-%02d-%02dT%02d:%02d".format(
                    date.year, date.monthValue, date.dayOfMonth, hour, minute,
                ))
                step = null
            },
        )
        null -> Unit
    }
}

// ─── Display card ────────────────────────────────────────────────────────────

@Composable
private fun DateTimeDisplayCard(
    label: String,
    value: String,
    onClick: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasValue = value.isNotBlank()

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = AtlasSurface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (hasValue) AtlasAccent.copy(alpha = 0.35f) else AtlasOutline,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.size(14.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (hasValue) AtlasAccentContainer else AtlasBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (hasValue) AtlasAccent else AtlasOnSurfaceMuted,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = AtlasOnSurfaceMuted,
                    letterSpacing = 0.10.sp,
                )
                Text(
                    text = if (hasValue) formatIsoDisplay(value) else "Sense data",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (hasValue) AtlasOnSurfaceStrong else AtlasOnSurfaceMuted,
                )
            }
            if (hasValue) {
                IconButton(onClick = onClear, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Neteja",
                        modifier = Modifier.size(16.dp),
                        tint = AtlasOnSurfaceMuted,
                    )
                }
            } else {
                Spacer(Modifier.size(40.dp))
            }
        }
    }
}

// ─── Step 1: date picker ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateStepDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        colors = DatePickerDefaults.colors(
            containerColor = AtlasSurface,
            titleContentColor = AtlasOnSurfaceStrong,
            headlineContentColor = AtlasOnSurfaceStrong,
            weekdayContentColor = AtlasOnSurfaceMuted,
            subheadContentColor = AtlasOnSurfaceMuted,
            yearContentColor = AtlasOnSurfaceStrong,
            currentYearContentColor = AtlasAccent,
            selectedYearContentColor = Color.White,
            selectedYearContainerColor = AtlasAccent,
            dayContentColor = AtlasOnSurfaceStrong,
            selectedDayContentColor = Color.White,
            selectedDayContainerColor = AtlasAccent,
            todayContentColor = AtlasAccent,
            todayDateBorderColor = AtlasAccent,
        ),
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = state.selectedDateMillis
                    val date = if (millis != null) {
                        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    } else initialDate
                    onConfirm(date)
                },
                modifier = Modifier.height(34.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
            ) { Text("Següent", fontWeight = FontWeight.ExtraBold) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(34.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
            ) { Text("Cancel·la", fontWeight = FontWeight.Bold) }
        },
    ) { DatePicker(state = state) }
}

// ─── Step 2: time picker ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeStepDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true,
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AtlasSurface,
        ) {
            Column(
                modifier = Modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                TimePicker(
                    state = state,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = AtlasBackground,
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = AtlasOnSurfaceStrong,
                        selectorColor = AtlasAccent,
                        containerColor = AtlasSurface,
                        periodSelectorBorderColor = AtlasOutline,
                        timeSelectorSelectedContainerColor = AtlasAccentContainer,
                        timeSelectorUnselectedContainerColor = AtlasBackground,
                        timeSelectorSelectedContentColor = AtlasAccent,
                        timeSelectorUnselectedContentColor = AtlasOnSurfaceMuted,
                    ),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    ) { Text("Cancel·la", fontWeight = FontWeight.Bold, color = AtlasOnSurfaceMuted) }
                    TextButton(
                        onClick = { onConfirm(state.hour, state.minute) },
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    ) { Text("Fet", fontWeight = FontWeight.ExtraBold, color = AtlasAccent) }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private enum class DateTimePickStep { Date, Time }

private fun parseIsoDate(value: String): LocalDate {
    return try {
        LocalDate.parse(value.take(10), DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (_: Exception) {
        LocalDate.now()
    }
}

private fun parseIsoTime(value: String): LocalTime {
    return try {
        LocalTime.parse(value.drop(11).take(5), DateTimeFormatter.ofPattern("HH:mm"))
    } catch (_: Exception) {
        LocalTime.of(0, 0)
    }
}

private fun formatIsoDisplay(value: String): String {
    return try {
        val date = LocalDate.parse(value.take(10), DateTimeFormatter.ISO_LOCAL_DATE)
        val time = value.drop(11).take(5)
        val monthLabel = date.monthValue.toDisplayMonthLabel()
        "${date.dayOfMonth} $monthLabel ${date.year}  ·  $time"
    } catch (_: Exception) {
        value
    }
}

private fun Int.toDisplayMonthLabel(): String = when (this) {
    1  -> "Gen."  2  -> "Febr." 3  -> "Març"
    4  -> "Abr."  5  -> "Maig"  6  -> "Juny"
    7  -> "Jul."  8  -> "Ag."   9  -> "Set."
    10 -> "Oct."  11 -> "Nov."  12 -> "Des."
    else -> "$this"
}
