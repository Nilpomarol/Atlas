package com.atlas.ui.screens.flight

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.domain.model.Airline
import com.atlas.domain.model.Airport
import com.atlas.domain.model.TravelStatus
import com.atlas.presentation.flight.FlightApiSearchState
import com.atlas.presentation.flight.FlightEditorDraftUiState
import com.atlas.ui.components.AirlineSearchField
import com.atlas.ui.components.AirportSearchField
import com.atlas.ui.components.date.DateTimePickerField
import com.atlas.ui.components.tripStatusColors
import com.atlas.ui.screens.trip.toCatalanLabel
import com.atlas.ui.theme.AtlasAccent
import com.atlas.ui.theme.AtlasAccentContainer
import com.atlas.ui.theme.AtlasError
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightEditorDialog(
    draft: FlightEditorDraftUiState,
    originResults: List<Airport>,
    destinationResults: List<Airport>,
    onDismiss: () -> Unit,
    onOriginQueryChanged: (String) -> Unit,
    onOriginSelected: (Airport) -> Unit,
    onDestinationQueryChanged: (String) -> Unit,
    onDestinationSelected: (Airport) -> Unit,
    onStatusChanged: (TravelStatus) -> Unit,
    onScheduledDepartureAtChanged: (String) -> Unit,
    onScheduledArrivalAtChanged: (String) -> Unit,
    onActualDepartureAtChanged: (String) -> Unit,
    onActualArrivalAtChanged: (String) -> Unit,
    airlineResults: List<Airline>,
    onAirlineQueryChanged: (String) -> Unit,
    onAirlineSelected: (Airline) -> Unit,
    onFlightNumberChanged: (String) -> Unit,
    onAircraftChanged: (String) -> Unit,
    onAircraftRegistrationChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onApiFlightNumberChanged: (String) -> Unit,
    onApiSearchDateChanged: (String) -> Unit,
    onSearchByFlightNumber: () -> Unit,
    onApplyApiResult: () -> Unit,
    onManualEntryClick: () -> Unit,
    onBackToSearch: () -> Unit,
    onSave: () -> Unit,
) {
    val showSearchStep = draft.flightId == null && !draft.showForm

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = AtlasSurface,
        title = {
            if (showSearchStep) {
                Text(
                    text = "Cerca un vol",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = AtlasOnSurfaceStrong,
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (draft.flightId == null) {
                        IconButton(
                            onClick = onBackToSearch,
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Enrere",
                                tint = AtlasOnSurfaceMuted,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = if (draft.flightId == null) "Nou vol" else "Edita vol",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = AtlasOnSurfaceStrong,
                    )
                }
            }
        },
        text = {
            if (showSearchStep) {
                SearchStepContent(
                    flightNumber = draft.apiFlightNumber,
                    searchDate = draft.apiSearchDate,
                    searchState = draft.apiSearchState,
                    onApiFlightNumberChanged = onApiFlightNumberChanged,
                    onApiSearchDateChanged = onApiSearchDateChanged,
                    onSearchByFlightNumber = onSearchByFlightNumber,
                    onApplyApiResult = onApplyApiResult,
                    onManualEntryClick = onManualEntryClick,
                )
            } else {
                FormStepContent(
                    draft = draft,
                    originResults = originResults,
                    destinationResults = destinationResults,
                    airlineResults = airlineResults,
                    onOriginQueryChanged = onOriginQueryChanged,
                    onOriginSelected = onOriginSelected,
                    onDestinationQueryChanged = onDestinationQueryChanged,
                    onDestinationSelected = onDestinationSelected,
                    onStatusChanged = onStatusChanged,
                    onScheduledDepartureAtChanged = onScheduledDepartureAtChanged,
                    onScheduledArrivalAtChanged = onScheduledArrivalAtChanged,
                    onActualDepartureAtChanged = onActualDepartureAtChanged,
                    onActualArrivalAtChanged = onActualArrivalAtChanged,
                    onAirlineQueryChanged = onAirlineQueryChanged,
                    onAirlineSelected = onAirlineSelected,
                    onFlightNumberChanged = onFlightNumberChanged,
                    onAircraftChanged = onAircraftChanged,
                    onAircraftRegistrationChanged = onAircraftRegistrationChanged,
                    onNotesChanged = onNotesChanged,
                )
            }
        },
        confirmButton = {
            if (!showSearchStep) {
                TextButton(onClick = onSave) { Text("Desa") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel·la") }
        },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 1 — Search
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchStepContent(
    flightNumber: String,
    searchDate: String,
    searchState: FlightApiSearchState,
    onApiFlightNumberChanged: (String) -> Unit,
    onApiSearchDateChanged: (String) -> Unit,
    onSearchByFlightNumber: () -> Unit,
    onApplyApiResult: () -> Unit,
    onManualEntryClick: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = flightNumber,
                onValueChange = onApiFlightNumberChanged,
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Vol (p.ex. LH401)", fontSize = 11.sp) },
                shape = RoundedCornerShape(12.dp),
            )

            TextButton(
                onClick = { showDatePicker = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (searchDate.isBlank()) AtlasOnSurfaceMuted else AtlasPrimary,
                ),
                modifier = Modifier.height(56.dp),
            ) {
                Text(
                    text = if (searchDate.isBlank()) "Data" else formatSearchDate(searchDate),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
            }
        }

        Button(
            onClick = onSearchByFlightNumber,
            enabled = flightNumber.isNotBlank() && searchDate.isNotBlank() &&
                searchState !is FlightApiSearchState.Searching,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AtlasPrimary,
                contentColor = AtlasSurface,
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text("Cerca vol", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
        }

        when (searchState) {
            is FlightApiSearchState.Searching -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = AtlasPrimary)
                    Text("Cercant vol...", style = MaterialTheme.typography.bodySmall, color = AtlasOnSurfaceMuted)
                }
            }
            is FlightApiSearchState.Found -> {
                ApiResultCard(prefillSummary = searchState.prefill.toDisplayString(), onApply = onApplyApiResult)
            }
            is FlightApiSearchState.NotFound -> {
                SearchMessageText("No s'ha trobat el vol.", isError = false)
            }
            is FlightApiSearchState.NoApiKey -> {
                SearchMessageText("Configura una clau API a Configuració per cercar vols.", isError = false)
            }
            is FlightApiSearchState.RateLimited -> {
                SearchMessageText("Has assolit el límit de l'API. Prova demà o introdueix manualment.", isError = false)
            }
            is FlightApiSearchState.Error -> {
                SearchMessageText(searchState.message, isError = true)
            }
            FlightApiSearchState.Idle -> Unit
        }

        TextButton(
            onClick = onManualEntryClick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            Text(
                text = "Entrada manual",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = AtlasOnSurfaceMuted,
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            shape = RoundedCornerShape(20.dp),
            colors = DatePickerDefaults.colors(containerColor = AtlasSurface),
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onApiSearchDateChanged(
                            "%04d-%02d-%02d".format(date.year, date.monthValue, date.dayOfMonth),
                        )
                    }
                    showDatePicker = false
                }) { Text("Fet", fontWeight = FontWeight.ExtraBold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel·la") }
            },
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
private fun ApiResultCard(prefillSummary: String, onApply: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AtlasAccentContainer)
            .border(1.dp, AtlasAccent.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AtlasPrimary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.FlightTakeoff, null, tint = AtlasSurface, modifier = Modifier.size(16.dp))
            }
            Text(
                text = prefillSummary,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = AtlasOnSurfaceStrong,
                modifier = Modifier.weight(1f),
            )
        }
        Button(
            onClick = onApply,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AtlasPrimary, contentColor = AtlasSurface),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text("Utilitza aquests resultats", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun SearchMessageText(message: String, isError: Boolean) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold,
        color = if (isError) AtlasError else AtlasOnSurfaceMuted,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 2 — Form
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FormStepContent(
    draft: FlightEditorDraftUiState,
    originResults: List<Airport>,
    destinationResults: List<Airport>,
    airlineResults: List<Airline>,
    onOriginQueryChanged: (String) -> Unit,
    onOriginSelected: (Airport) -> Unit,
    onDestinationQueryChanged: (String) -> Unit,
    onDestinationSelected: (Airport) -> Unit,
    onStatusChanged: (TravelStatus) -> Unit,
    onScheduledDepartureAtChanged: (String) -> Unit,
    onScheduledArrivalAtChanged: (String) -> Unit,
    onActualDepartureAtChanged: (String) -> Unit,
    onActualArrivalAtChanged: (String) -> Unit,
    onAirlineQueryChanged: (String) -> Unit,
    onAirlineSelected: (Airline) -> Unit,
    onFlightNumberChanged: (String) -> Unit,
    onAircraftChanged: (String) -> Unit,
    onAircraftRegistrationChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AirportSearchField(
            label = "Origen",
            query = draft.originQuery,
            results = originResults,
            onQueryChanged = onOriginQueryChanged,
            onAirportSelected = onOriginSelected,
            modifier = Modifier.fillMaxWidth(),
        )

        AirportSearchField(
            label = "Destí",
            query = draft.destinationQuery,
            results = destinationResults,
            onQueryChanged = onDestinationQueryChanged,
            onAirportSelected = onDestinationSelected,
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = "Estat",
            style = MaterialTheme.typography.labelMedium,
            color = AtlasOnSurfaceMuted,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(TravelStatus.entries) { status ->
                val colors = status.tripStatusColors()
                FilterChip(
                    selected = draft.status == status,
                    onClick = { onStatusChanged(status) },
                    label = { Text(status.toCatalanLabel()) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.container,
                        selectedLabelColor = colors.foreground,
                    ),
                )
            }
        }

        DateTimePickerField(
            label = "Sortida programada",
            value = draft.scheduledDepartureAt,
            onValueChanged = onScheduledDepartureAtChanged,
        )

        DateTimePickerField(
            label = "Arribada programada",
            value = draft.scheduledArrivalAt,
            onValueChanged = onScheduledArrivalAtChanged,
            prefillValue = draft.scheduledDepartureAt.takeIf { it.length >= 10 }?.take(10),
        )

        DateTimePickerField(
            label = "Sortida real",
            value = draft.actualDepartureAt,
            onValueChanged = onActualDepartureAtChanged,
            prefillValue = draft.scheduledDepartureAt.takeIf { it.isNotBlank() },
        )

        DateTimePickerField(
            label = "Arribada real",
            value = draft.actualArrivalAt,
            onValueChanged = onActualArrivalAtChanged,
            prefillValue = draft.scheduledArrivalAt.takeIf { it.isNotBlank() },
        )

        AirlineSearchField(
            query = draft.airlineQuery,
            results = airlineResults,
            onQueryChanged = onAirlineQueryChanged,
            onAirlineSelected = onAirlineSelected,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = draft.flightNumber,
            onValueChange = onFlightNumberChanged,
            label = { Text("Número de vol (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        OutlinedTextField(
            value = draft.aircraft,
            onValueChange = onAircraftChanged,
            label = { Text("Aeronau (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        OutlinedTextField(
            value = draft.aircraftRegistration,
            onValueChange = onAircraftRegistrationChanged,
            label = { Text("Matrícula (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        OutlinedTextField(
            value = draft.notes,
            onValueChange = onNotesChanged,
            label = { Text("Notes (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )

        if (draft.validationError != null) {
            Text(
                text = draft.validationError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun formatSearchDate(date: String): String {
    return try {
        val d = java.time.LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
        val month = when (d.monthValue) {
            1 -> "Gen"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Abr"; 5 -> "Mai"; 6 -> "Jun"
            7 -> "Jul"; 8 -> "Ag"; 9 -> "Set"; 10 -> "Oct"; 11 -> "Nov"; else -> "Des"
        }
        "${d.dayOfMonth} $month ${d.year}"
    } catch (_: Exception) { date }
}

private fun com.atlas.domain.model.FlightApiPrefill.toDisplayString(): String = buildString {
    val num = flightNumber ?: "—"
    val airline = airlineName ?: airlineIata ?: "—"
    append("$num · $airline\n")
    val origin = originIata ?: "—"
    val dest = destinationIata ?: "—"
    append("$origin → $dest")
    if (scheduledDepartureAt != null) append("  ·  ${scheduledDepartureAt.take(16)}")
    if (aircraftModel != null) append("\n$aircraftModel")
    if (aircraftRegistration != null) append("\nMatrícula: $aircraftRegistration")
}
