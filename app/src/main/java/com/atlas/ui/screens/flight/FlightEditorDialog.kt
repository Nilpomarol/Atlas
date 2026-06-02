package com.atlas.ui.screens.flight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.Airport
import com.atlas.domain.model.TravelStatus
import com.atlas.presentation.flight.FlightEditorDraftUiState
import com.atlas.ui.components.AirportSearchField
import com.atlas.ui.components.date.DateTimePickerField
import com.atlas.ui.components.tripStatusColors
import com.atlas.ui.screens.trip.toCatalanLabel
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasSurface

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
    onAirlineChanged: (String) -> Unit,
    onFlightNumberChanged: (String) -> Unit,
    onAircraftChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = AtlasSurface,
        title = {
            Text(
                text = if (draft.flightId == null) "Nou vol" else "Edita vol",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceStrong,
            )
        },
        text = {
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

                OutlinedTextField(
                    value = draft.airline,
                    onValueChange = onAirlineChanged,
                    label = { Text("Companyia (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
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
        },
        confirmButton = {
            TextButton(onClick = onSave) { Text("Desa") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel·la") }
        },
    )
}
