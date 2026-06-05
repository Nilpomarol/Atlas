package com.atlas.ui.screens.flight

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.atlas.ui.theme.AtlasSurfaceRaised
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 18.dp)
                .fillMaxWidth()
                .widthIn(max = 560.dp),
            shape = RoundedCornerShape(26.dp),
            color = AtlasSurface,
            border = BorderStroke(1.dp, AtlasOutline),
        ) {
            Column {
                FlightEditorHeader(
                    title = when {
                        showSearchStep -> "Cerca un vol"
                        draft.flightId == null -> "Nou vol"
                        else -> "Edita vol"
                    },
                    subtitle = if (showSearchStep) null else "Revisa ruta, horaris i dades de la companyia.",
                    showBack = draft.flightId == null && !showSearchStep,
                    onBack = onBackToSearch,
                )

                if (draft.flightId == null) {
                    FlightEditorStepIndicator(
                        isSearchStep = showSearchStep,
                        modifier = Modifier.padding(horizontal = 18.dp),
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                        .heightIn(max = 580.dp),
                ) {
                    if (showSearchStep) {
                        SearchStepContent(
                            flightNumber = draft.apiFlightNumber,
                            searchDate = draft.apiSearchDate,
                            searchState = draft.apiSearchState,
                            onApiFlightNumberChanged = onApiFlightNumberChanged,
                            onApiSearchDateChanged = onApiSearchDateChanged,
                            onSearchByFlightNumber = onSearchByFlightNumber,
                            onApplyApiResult = onApplyApiResult,
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
                }

                FlightEditorActions(
                    showSave = !showSearchStep,
                    showManualEntry = showSearchStep,
                    onDismiss = onDismiss,
                    onManualEntryClick = onManualEntryClick,
                    onSave = onSave,
                )
            }
        }
    }
}

@Composable
private fun FlightEditorHeader(
    title: String,
    subtitle: String?,
    showBack: Boolean,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AtlasSurfaceRaised)
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (showBack) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(34.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Enrere",
                    tint = AtlasOnSurfaceStrong,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(AtlasPrimary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.FlightTakeoff,
                contentDescription = null,
                tint = AtlasSurface,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceStrong,
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = AtlasOnSurfaceMuted,
                )
            }
        }
    }
}

@Composable
private fun FlightEditorStepIndicator(
    isSearchStep: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StepPill(
            label = "1 Cerca",
            selected = isSearchStep,
            modifier = Modifier.weight(1f),
        )
        StepPill(
            label = "2 Detalls",
            selected = !isSearchStep,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StepPill(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) AtlasAccentContainer else AtlasSurfaceRaised)
            .border(
                width = 1.dp,
                color = if (selected) AtlasAccent.copy(alpha = 0.32f) else AtlasOutline,
                shape = RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (selected) AtlasPrimary else AtlasOnSurfaceMuted,
        )
    }
}

@Composable
private fun FlightEditorActions(
    showSave: Boolean,
    showManualEntry: Boolean,
    onDismiss: () -> Unit,
    onManualEntryClick: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AtlasOutline.copy(alpha = 0.55f))
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = onDismiss,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Text("Cancel·la", fontWeight = FontWeight.Bold)
        }
        if (showManualEntry) {
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onManualEntryClick,
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AtlasPrimary,
                    contentColor = AtlasSurface,
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
            ) {
                Text("Introdueix manualment", fontWeight = FontWeight.ExtraBold)
            }
        }
        if (showSave) {
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onSave,
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AtlasPrimary,
                    contentColor = AtlasSurface,
                ),
            ) {
                Text("Desa", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

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
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = AtlasSurfaceRaised,
            border = BorderStroke(1.dp, AtlasOutline),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = flightNumber,
                        onValueChange = onApiFlightNumberChanged,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Núm. vol") },
                        placeholder = { Text("LH401") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Flight,
                                contentDescription = null,
                                tint = AtlasOnSurfaceMuted,
                            )
                        },
                        shape = RoundedCornerShape(13.dp),
                    )

                    TextButton(
                        onClick = { showDatePicker = true },
                        shape = RoundedCornerShape(13.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (searchDate.isBlank()) AtlasOnSurfaceMuted else AtlasPrimary,
                        ),
                        modifier = Modifier.height(56.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp),
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (searchDate.isBlank()) "Data" else formatSearchDate(searchDate),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                        )
                    }
                }

                Button(
                    onClick = onSearchByFlightNumber,
                    enabled = flightNumber.isNotBlank() &&
                        searchDate.isNotBlank() &&
                        searchState !is FlightApiSearchState.Searching,
                    shape = RoundedCornerShape(13.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AtlasPrimary,
                        contentColor = AtlasSurface,
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Cerca vol", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
            }
        }

        SearchStatePanel(
            searchState = searchState,
            onApplyApiResult = onApplyApiResult,
        )
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
private fun SearchStatePanel(
    searchState: FlightApiSearchState,
    onApplyApiResult: () -> Unit,
) {
    when (searchState) {
        is FlightApiSearchState.Searching -> {
            SearchMessageCard {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = AtlasPrimary,
                )
                Text("Cercant vol...", style = MaterialTheme.typography.bodySmall, color = AtlasOnSurfaceMuted)
            }
        }
        is FlightApiSearchState.Found -> {
            ApiResultCard(prefillSummary = searchState.prefill.toDisplayString(), onApply = onApplyApiResult)
        }
        is FlightApiSearchState.NotFound -> {
            SearchMessageCard {
                SearchMessageText("No s'ha trobat el vol.", isError = false)
            }
        }
        is FlightApiSearchState.NoApiKey -> {
            SearchMessageCard {
                SearchMessageText("Configura una clau API a Configuració per cercar vols.", isError = false)
            }
        }
        is FlightApiSearchState.RateLimited -> {
            SearchMessageCard {
                SearchMessageText("Has assolit el límit de l'API. Prova demà o introdueix manualment.", isError = false)
            }
        }
        is FlightApiSearchState.Error -> {
            SearchMessageCard {
                SearchMessageText(searchState.message, isError = true)
            }
        }
        FlightApiSearchState.Idle -> Unit
    }
}

@Composable
private fun SearchMessageCard(content: @Composable RowScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

@Composable
private fun ApiResultCard(prefillSummary: String, onApply: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AtlasAccentContainer)
            .border(1.dp, AtlasAccent.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AtlasPrimary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.FlightTakeoff, null, tint = AtlasSurface, modifier = Modifier.size(18.dp))
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
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AtlasPrimary, contentColor = AtlasSurface),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
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
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        FormSection(title = "Ruta") {
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
        }

        FormSection(title = "Estat") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(TravelStatus.PLANNED, TravelStatus.IN_PROGRESS, TravelStatus.COMPLETED).forEach { status ->
                    val colors = status.tripStatusColors()
                    FilterChip(
                        selected = draft.status == status,
                        onClick = { onStatusChanged(status) },
                        modifier = Modifier.weight(1f),
                        label = {
                            Text(
                                text = status.toCatalanLabel(),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.container,
                            selectedLabelColor = colors.foreground,
                        ),
                    )
                }
            }
        }

        FormSection(title = "Horaris (opcional)") {
            TimePairCard(title = "Programat") {
                DateTimePickerField(
                    label = "Sortida programada",
                    value = draft.scheduledDepartureAt,
                    onValueChanged = onScheduledDepartureAtChanged,
                    modifier = Modifier.fillMaxWidth(),
                )
                DateTimePickerField(
                    label = "Arribada programada",
                    value = draft.scheduledArrivalAt,
                    onValueChanged = onScheduledArrivalAtChanged,
                    prefillValue = draft.scheduledDepartureAt.takeIf { it.length >= 10 }?.take(10),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            TimePairCard(title = "Real") {
                DateTimePickerField(
                    label = "Sortida real",
                    value = draft.actualDepartureAt,
                    onValueChanged = onActualDepartureAtChanged,
                    prefillValue = draft.scheduledDepartureAt.takeIf { it.isNotBlank() },
                    modifier = Modifier.fillMaxWidth(),
                )
                DateTimePickerField(
                    label = "Arribada real",
                    value = draft.actualArrivalAt,
                    onValueChanged = onActualArrivalAtChanged,
                    prefillValue = draft.scheduledArrivalAt.takeIf { it.isNotBlank() },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        FormSection(title = "Vol (opcional)") {
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
                label = { Text("Núm. vol") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(13.dp),
            )

            OutlinedTextField(
                value = draft.aircraft,
                onValueChange = onAircraftChanged,
                label = { Text("Aeronau") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(13.dp),
            )

            OutlinedTextField(
                value = draft.aircraftRegistration,
                onValueChange = onAircraftRegistrationChanged,
                label = { Text("Matrícula") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(13.dp),
            )

            OutlinedTextField(
                value = draft.notes,
                onValueChange = onNotesChanged,
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(13.dp),
            )
        }

        if (draft.validationError != null) {
            Text(
                text = draft.validationError,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = AtlasError,
            )
        }
    }
}

@Composable
private fun TimePairCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AtlasSurface.copy(alpha = 0.55f))
            .border(1.dp, AtlasOutline.copy(alpha = 0.38f), RoundedCornerShape(14.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = AtlasOnSurfaceStrong,
        )
        content()
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        color = AtlasSurfaceRaised,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceMuted,
            )
            content()
        }
    }
}

private fun formatSearchDate(date: String): String {
    return try {
        val d = java.time.LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
        val month = when (d.monthValue) {
            1 -> "Gen"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Abr"
            5 -> "Mai"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Ag"
            9 -> "Set"
            10 -> "Oct"
            11 -> "Nov"
            else -> "Des"
        }
        "${d.dayOfMonth} $month ${d.year}"
    } catch (_: Exception) {
        date
    }
}

private fun com.atlas.domain.model.FlightApiPrefill.toDisplayString(): String = buildString {
    val num = flightNumber ?: "-"
    val airline = airlineName ?: airlineIata ?: "-"
    append("$num · $airline\n")
    val origin = originIata ?: "-"
    val dest = destinationIata ?: "-"
    append("$origin -> $dest")
    if (scheduledDepartureAt != null) append("  ·  ${scheduledDepartureAt.take(16)}")
    if (aircraftModel != null) append("\n$aircraftModel")
    if (aircraftRegistration != null) append("\nMatrícula: $aircraftRegistration")
}
