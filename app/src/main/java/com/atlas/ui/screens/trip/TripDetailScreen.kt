package com.atlas.ui.screens.trip

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.atlas.domain.model.Country
import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.Excursion
import com.atlas.domain.model.ExcursionStop
import com.atlas.domain.model.Itinerary
import com.atlas.domain.model.LocationSearchResult
import com.atlas.domain.model.StopPhoto
import com.atlas.domain.model.StopType
import com.atlas.domain.model.TravelStatus
import com.atlas.domain.model.Trip
import com.atlas.domain.model.TripStop
import com.atlas.domain.model.TripStopSource
import com.atlas.ui.components.StopDetailModal
import com.atlas.ui.components.StopPhotoThumbnails
import com.atlas.ui.components.PhotoViewerDialog
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.presentation.date.FlexibleDateRangeDraftField
import com.atlas.presentation.trip.TripDetailUiState
import com.atlas.presentation.trip.ExcursionStopDraftUiState
import com.atlas.presentation.trip.TripStopDraftUiState
import com.atlas.ui.components.date.FlexibleDateRangeField
import com.atlas.ui.components.tripStatusColors
import com.atlas.ui.theme.AtlasAccentContainer
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasSurfaceSubtle
import com.atlas.ui.theme.AtlasError
import com.atlas.ui.theme.AtlasMono
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPlanned
import com.atlas.ui.theme.AtlasPlannedContainer
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasVisited
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val dateRangeFormatter = FlexibleDateFormatter()

private data class TripCountryPillUiState(
    val iso2: String,
    val name: String,
    val flagEmoji: String?,
)

// ─────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────
@Composable
fun TripDetailScreen(
    uiState: TripDetailUiState,
    onBackClick: () -> Unit,
    onDeleteTrip: () -> Unit,
    onEditTripClick: () -> Unit,
    onDismissTripDraft: () -> Unit,
    onTripTitleChanged: (String) -> Unit,
    onTripStatusChanged: (TravelStatus) -> Unit,
    onTripDatePrecisionChanged: (DatePrecision) -> Unit,
    onTripDateFieldChanged: (FlexibleDateRangeDraftField, String) -> Unit,
    onTripNotesChanged: (String) -> Unit,
    onSaveTripDraft: () -> Unit,
    onItineraryClick: (String) -> Unit,
    onStoryClick: () -> Unit,
    onOpenItineraryPicker: () -> Unit,
    onDismissItineraryPicker: () -> Unit,
    onLinkItinerary: (Itinerary) -> Unit,
    onUnlinkItinerary: () -> Unit,
    onAddStopClick: () -> Unit,
    onDismissStopDraft: () -> Unit,
    onStopLocationNameChanged: (String) -> Unit,
    onLocationSearchQueryChanged: (String) -> Unit,
    onLocationSearchResultSelected: (LocationSearchResult) -> Unit,
    onUseManualStopEntryClick: () -> Unit,
    onStopCountryChanged: (String) -> Unit,
    onStopLatitudeChanged: (String) -> Unit,
    onStopLongitudeChanged: (String) -> Unit,
    onStopDatePrecisionChanged: (DatePrecision) -> Unit,
    onStopDateFieldChanged: (FlexibleDateRangeDraftField, String) -> Unit,
    onStopNotesChanged: (String) -> Unit,
    onSaveStopDraft: () -> Unit,
    onShowGeneratedStopsOnMapChanged: (Boolean) -> Unit,
    onEditStop: (TripStop) -> Unit,
    onMoveStopUp: (TripStop) -> Unit,
    onMoveStopDown: (TripStop) -> Unit,
    onDeleteStop: (TripStop) -> Unit,
    onAddExcursionClick: (String?) -> Unit,
    onDeleteExcursion: (Excursion) -> Unit,
    onMoveExcursionUp: (Excursion) -> Unit,
    onMoveExcursionDown: (Excursion) -> Unit,
    onAddExcursionStopClick: (String) -> Unit,
    onEditExcursionStop: (ExcursionStop) -> Unit,
    onDeleteExcursionStop: (ExcursionStop) -> Unit,
    onMoveExcursionStopUp: (String, ExcursionStop) -> Unit,
    onMoveExcursionStopDown: (String, ExcursionStop) -> Unit,
    onDismissExcursionStopDraft: () -> Unit,
    onExcursionStopLocationSearchQueryChanged: (String) -> Unit,
    onExcursionStopLocationSearchResultSelected: (LocationSearchResult) -> Unit,
    onUseManualExcursionStopEntryClick: () -> Unit,
    onExcursionStopLocationNameChanged: (String) -> Unit,
    onExcursionStopCountryChanged: (String) -> Unit,
    onExcursionStopLatitudeChanged: (String) -> Unit,
    onExcursionStopLongitudeChanged: (String) -> Unit,
    onExcursionStopDatePrecisionChanged: (DatePrecision) -> Unit,
    onExcursionStopDateFieldChanged: (FlexibleDateRangeDraftField, String) -> Unit,
    onExcursionStopNotesChanged: (String) -> Unit,
    onSaveExcursionStopDraft: () -> Unit,
    onAddPhotos: (String, StopType, List<Uri>) -> Unit,
    onDeletePhoto: (StopPhoto) -> Unit,
    onSetCoverPhoto: (StopPhoto?) -> Unit,
) {
    var isDeleteTripDialogOpen by remember { mutableStateOf(false) }
    var pendingDeleteStop by remember { mutableStateOf<TripStop?>(null) }
    var pendingDeleteExcursion by remember { mutableStateOf<Excursion?>(null) }
    var pendingDeleteExcursionStop by remember { mutableStateOf<ExcursionStop?>(null) }
    var isReorderMode by remember { mutableStateOf(false) }
    var showMapModal by remember { mutableStateOf(false) }
    var selectedStop by remember { mutableStateOf<TripStop?>(null) }
    var selectedExcursionStop by remember { mutableStateOf<ExcursionStop?>(null) }
    var selectedTripPhotoId by rememberSaveable { mutableStateOf<String?>(null) }

    val trip = uiState.trip

    Box(modifier = Modifier.fillMaxSize().background(AtlasBackground)) {
        if (trip == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Carregant el viatge...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AtlasOnSurfaceMuted,
                )
            }
        } else {
            TripDetailContent(
                uiState = uiState,
                trip = trip,
                isReorderMode = isReorderMode,
                onReorderModeChanged = { isReorderMode = it },
                showGeneratedStopsOnMap = uiState.generatedStopsVisibleOnMap,
                onShowGeneratedStopsOnMapChanged = onShowGeneratedStopsOnMapChanged,
                onExpandMap = { showMapModal = true },
                onItineraryClick = onItineraryClick,
                onStoryClick = onStoryClick,
                onOpenItineraryPicker = onOpenItineraryPicker,
                onUnlinkItinerary = onUnlinkItinerary,
                onAddStopClick = onAddStopClick,
                onEditStop = onEditStop,
                onMoveStopUp = onMoveStopUp,
                onMoveStopDown = onMoveStopDown,
                onDeleteStop = { pendingDeleteStop = it },
                onAddExcursionClick = onAddExcursionClick,
                onDeleteExcursion = { pendingDeleteExcursion = it },
                onMoveExcursionUp = onMoveExcursionUp,
                onMoveExcursionDown = onMoveExcursionDown,
                onAddExcursionStopClick = onAddExcursionStopClick,
                onEditExcursionStop = onEditExcursionStop,
                onDeleteExcursionStop = { pendingDeleteExcursionStop = it },
                onMoveExcursionStopUp = onMoveExcursionStopUp,
                onMoveExcursionStopDown = onMoveExcursionStopDown,
                tripStopPhotoMap = uiState.tripStopPhotoMap,
                excursionStopPhotoMap = uiState.excursionStopPhotoMap,
                onStopClick = { selectedStop = it },
                onExcursionStopClick = { selectedExcursionStop = it },
                onPhotoClick = { selectedTripPhotoId = it.id },
            )
        }

        // Fixed overlay: back + overflow, always on top regardless of scroll
        TripDetailTopBar(
            onBackClick = onBackClick,
            onEditTripClick = onEditTripClick,
            onDeleteTripClick = { isDeleteTripDialogOpen = true },
            actionsEnabled = trip != null,
        )
    }

    // ── Interactive map modal ──
    if (showMapModal && trip != null) {
        val screenHeightDp = LocalConfiguration.current.screenHeightDp
        Dialog(
            onDismissRequest = { showMapModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 28.dp),
            ) {
                TripMapPreview(
                    stops = uiState.stops,
                    excursions = uiState.excursions,
                    mapHeight = (screenHeightDp * 0.72f).dp,
                    generatedStopsVisible = uiState.generatedStopsVisibleOnMap,
                    onGeneratedStopsVisibilityChanged = onShowGeneratedStopsOnMapChanged,
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 6.dp, end = 6.dp),
                    onClick = { showMapModal = false },
                    shape = CircleShape,
                    color = AtlasSurface.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, AtlasOutline),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Tanca",
                        tint = AtlasOnSurfaceStrong,
                        modifier = Modifier.padding(8.dp).size(16.dp),
                    )
                }
            }
        }
    }

    // ── Dialogs ──
    if (uiState.tripDraft.isOpen) {
        TripEditorDialog(
            draft = uiState.tripDraft,
            onDismiss = onDismissTripDraft,
            onTitleChanged = onTripTitleChanged,
            onStatusChanged = onTripStatusChanged,
            onDatePrecisionChanged = onTripDatePrecisionChanged,
            onDateFieldChanged = onTripDateFieldChanged,
            onNotesChanged = onTripNotesChanged,
            onSave = onSaveTripDraft,
        )
    }

    if (uiState.stopDraft.isOpen) {
        TripStopDialog(
            draft = uiState.stopDraft,
            countries = uiState.countries,
            onDismiss = onDismissStopDraft,
            onLocationNameChanged = onStopLocationNameChanged,
            onLocationSearchQueryChanged = onLocationSearchQueryChanged,
            onLocationSearchResultSelected = onLocationSearchResultSelected,
            onUseManualEntryClick = onUseManualStopEntryClick,
            onCountryChanged = onStopCountryChanged,
            onLatitudeChanged = onStopLatitudeChanged,
            onLongitudeChanged = onStopLongitudeChanged,
            onDatePrecisionChanged = onStopDatePrecisionChanged,
            onDateFieldChanged = onStopDateFieldChanged,
            onNotesChanged = onStopNotesChanged,
            onSave = onSaveStopDraft,
        )
    }

    if (uiState.isItineraryPickerOpen) {
        ItineraryPickerDialog(
            itineraries = uiState.availableItineraries,
            onDismiss = onDismissItineraryPicker,
            onSelect = onLinkItinerary,
        )
    }

    if (uiState.excursionStopDraft.isOpen) {
        ExcursionStopDialog(
            draft = uiState.excursionStopDraft,
            countries = uiState.countries,
            onDismiss = onDismissExcursionStopDraft,
            onLocationSearchQueryChanged = onExcursionStopLocationSearchQueryChanged,
            onLocationSearchResultSelected = onExcursionStopLocationSearchResultSelected,
            onUseManualEntryClick = onUseManualExcursionStopEntryClick,
            onLocationNameChanged = onExcursionStopLocationNameChanged,
            onCountryChanged = onExcursionStopCountryChanged,
            onLatitudeChanged = onExcursionStopLatitudeChanged,
            onLongitudeChanged = onExcursionStopLongitudeChanged,
            onDatePrecisionChanged = onExcursionStopDatePrecisionChanged,
            onDateFieldChanged = onExcursionStopDateFieldChanged,
            onNotesChanged = onExcursionStopNotesChanged,
            onSave = onSaveExcursionStopDraft,
        )
    }

    if (isDeleteTripDialogOpen && uiState.trip != null) {
        ConfirmDeleteDialog(
            title = "Eliminar viatge?",
            body = "S'eliminarà \"${uiState.trip.title}\" i totes les seves parades. Aquesta acció no es pot desfer.",
            onDismiss = { isDeleteTripDialogOpen = false },
            onConfirm = { isDeleteTripDialogOpen = false; onDeleteTrip() },
        )
    }

    pendingDeleteStop?.let { stop ->
        ConfirmDeleteDialog(
            title = "Eliminar parada?",
            body = "S'eliminarà \"${stop.locationName}\" del viatge. Aquesta acció no es pot desfer.",
            onDismiss = { pendingDeleteStop = null },
            onConfirm = { pendingDeleteStop = null; onDeleteStop(stop) },
        )
    }

    pendingDeleteExcursion?.let { excursion ->
        ConfirmDeleteDialog(
            title = "Eliminar excursió?",
            body = "S'eliminarà \"${excursion.title}\" i totes les seves parades. Aquesta acció no es pot desfer.",
            onDismiss = { pendingDeleteExcursion = null },
            onConfirm = { pendingDeleteExcursion = null; onDeleteExcursion(excursion) },
        )
    }

    pendingDeleteExcursionStop?.let { stop ->
        ConfirmDeleteDialog(
            title = "Eliminar parada d'excursió?",
            body = "S'eliminarà \"${stop.locationName}\" de l'excursio. Aquesta acció no es pot desfer.",
            onDismiss = { pendingDeleteExcursionStop = null },
            onConfirm = { pendingDeleteExcursionStop = null; onDeleteExcursionStop(stop) },
        )
    }

    selectedTripPhotoId?.let { photoId ->
        PhotoViewerDialog(
            items = uiState.photoGallery.viewerItems,
            initialPhotoId = photoId,
            coverPhotoFilename = uiState.trip?.coverPhotoFilename,
            onDismiss = { selectedTripPhotoId = null },
            onOpenSource = { item ->
                selectedTripPhotoId = null
                when (item.stopType) {
                    StopType.TRIP_STOP -> {
                        selectedStop = uiState.stops.firstOrNull { it.id == item.stopId }
                    }
                    StopType.EXCURSION_STOP -> {
                        selectedExcursionStop = uiState.excursions
                            .asSequence()
                            .flatMap { it.stops.asSequence() }
                            .firstOrNull { it.id == item.stopId }
                    }
                }
            },
            onDeletePhoto = onDeletePhoto,
            onSetCoverPhoto = onSetCoverPhoto,
        )
    }

    selectedStop?.let { stop ->
        val countryName = uiState.countries.firstOrNull { it.iso2 == stop.countryIso2 }?.nameCa ?: stop.countryIso2.orEmpty()
        val photos = uiState.tripStopPhotoMap[stop.id] ?: emptyList()
        val isManual = stop.source == TripStopSource.MANUAL
        StopDetailModal(
            title = stop.displayTitle ?: stop.locationName,
            locationName = stop.locationName,
            metaLine = buildStopMetaLine(stop, countryName),
            notes = stop.notes,
            photos = photos,
            stopId = stop.id,
            stopType = StopType.TRIP_STOP,
            coverPhotoFilename = uiState.trip?.coverPhotoFilename,
            onDismiss = { selectedStop = null },
            onEditStop = {
                if (isManual) {
                    selectedStop = null
                    onEditStop(stop)
                }
            },
            onDeleteStop = { selectedStop = null; pendingDeleteStop = stop },
            onAddPhotos = onAddPhotos,
            onDeletePhoto = onDeletePhoto,
            onSetCoverPhoto = onSetCoverPhoto,
        )
    }

    selectedExcursionStop?.let { stop ->
        val countryName = uiState.countries.firstOrNull { it.iso2 == stop.countryIso2 }?.nameCa ?: stop.countryIso2.orEmpty()
        val photos = uiState.excursionStopPhotoMap[stop.id] ?: emptyList()
        StopDetailModal(
            title = stop.locationName,
            locationName = stop.locationName,
            metaLine = buildExcursionStopMetaLine(stop, countryName),
            notes = stop.notes,
            photos = photos,
            stopId = stop.id,
            stopType = StopType.EXCURSION_STOP,
            coverPhotoFilename = uiState.trip?.coverPhotoFilename,
            onDismiss = { selectedExcursionStop = null },
            onEditStop = { selectedExcursionStop = null; onEditExcursionStop(stop) },
            onDeleteStop = { selectedExcursionStop = null; pendingDeleteExcursionStop = stop },
            onAddPhotos = onAddPhotos,
            onDeletePhoto = onDeletePhoto,
            onSetCoverPhoto = onSetCoverPhoto,
        )
    }
}

// ─────────────────────────────────────────────
// Fixed overlay top bar (floats above scroll)
// ─────────────────────────────────────────────
@Composable
private fun TripDetailTopBar(
    onBackClick: () -> Unit,
    onEditTripClick: () -> Unit,
    onDeleteTripClick: () -> Unit,
    actionsEnabled: Boolean,
) {
    var actionsExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.size(42.dp),
            shape = CircleShape,
            color = AtlasSurface.copy(alpha = 0.92f),
            border = BorderStroke(1.dp, AtlasOutline),
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Enrere",
                    tint = AtlasOnSurfaceStrong,
                    modifier = Modifier.size(19.dp),
                )
            }
        }

        Box(modifier = Modifier.weight(1f))

        if (actionsEnabled) {
            Box {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = AtlasSurface.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, AtlasOutline),
                ) {
                    IconButton(onClick = { actionsExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Accions",
                            tint = AtlasOnSurfaceStrong,
                        )
                    }
                }
                MaterialTheme(
                    colorScheme = MaterialTheme.colorScheme.copy(surfaceContainer = AtlasSurface),
                    shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(14.dp)),
                ) {
                    DropdownMenu(
                        expanded = actionsExpanded,
                        onDismissRequest = { actionsExpanded = false },
                        modifier = Modifier.width(190.dp),
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Edita viatge",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = AtlasOnSurfaceStrong,
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.Edit, null, tint = AtlasOnSurfaceStrong, modifier = Modifier.size(16.dp))
                            },
                            onClick = { actionsExpanded = false; onEditTripClick() },
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AtlasOutline))
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Elimina viatge",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = AtlasError,
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.Delete, null, tint = AtlasError, modifier = Modifier.size(16.dp))
                            },
                            onClick = { actionsExpanded = false; onDeleteTripClick() },
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Main scrollable content
// ─────────────────────────────────────────────
@Composable
private fun TripDetailContent(
    uiState: TripDetailUiState,
    trip: Trip,
    isReorderMode: Boolean,
    onReorderModeChanged: (Boolean) -> Unit,
    showGeneratedStopsOnMap: Boolean,
    onShowGeneratedStopsOnMapChanged: (Boolean) -> Unit,
    onExpandMap: () -> Unit,
    onItineraryClick: (String) -> Unit,
    onStoryClick: () -> Unit,
    onOpenItineraryPicker: () -> Unit,
    onUnlinkItinerary: () -> Unit,
    onAddStopClick: () -> Unit,
    onEditStop: (TripStop) -> Unit,
    onMoveStopUp: (TripStop) -> Unit,
    onMoveStopDown: (TripStop) -> Unit,
    onDeleteStop: (TripStop) -> Unit,
    onAddExcursionClick: (String?) -> Unit,
    onDeleteExcursion: (Excursion) -> Unit,
    onMoveExcursionUp: (Excursion) -> Unit,
    onMoveExcursionDown: (Excursion) -> Unit,
    onAddExcursionStopClick: (String) -> Unit,
    onEditExcursionStop: (ExcursionStop) -> Unit,
    onDeleteExcursionStop: (ExcursionStop) -> Unit,
    onMoveExcursionStopUp: (String, ExcursionStop) -> Unit,
    onMoveExcursionStopDown: (String, ExcursionStop) -> Unit,
    tripStopPhotoMap: Map<String, List<StopPhoto>>,
    excursionStopPhotoMap: Map<String, List<StopPhoto>>,
    onStopClick: (TripStop) -> Unit,
    onExcursionStopClick: (ExcursionStop) -> Unit,
    onPhotoClick: (StopPhoto) -> Unit,
) {
    val tripCountries = uiState.stops
        .mapNotNull { it.countryIso2?.takeIf { iso -> iso.isNotBlank() } }
        .distinct()
        .mapNotNull { iso2 ->
            val country = uiState.countries.firstOrNull { it.iso2 == iso2 } ?: return@mapNotNull null
            TripCountryPillUiState(
                iso2 = iso2,
                name = country.nameCa,
                flagEmoji = country.flagEmoji,
            )
        }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Spacer to clear the fixed overlay top bar
        Spacer(Modifier.height(54.dp))

        TripInfoCard(
            trip = trip,
            stopCount = uiState.stops.size,
            tripCountries = tripCountries,
            onStoryClick = onStoryClick,
        )

        LinkedItineraryPanel(
            linkedItinerary = uiState.linkedItinerary,
            availableCount = uiState.availableItineraries.size,
            onItineraryClick = onItineraryClick,
            onOpenItineraryPicker = onOpenItineraryPicker,
            onUnlinkItinerary = onUnlinkItinerary,
        )

        // Static MapLibre map card (gestures disabled; expand icon in footer)
        TripMapPreview(
            stops = uiState.stops,
            excursions = uiState.excursions,
            mapHeight = 220.dp,
            gesturesEnabled = false,
            showFooter = true,
            generatedStopsVisible = showGeneratedStopsOnMap,
            onGeneratedStopsVisibilityChanged = onShowGeneratedStopsOnMapChanged,
            onExpandClick = onExpandMap,
        )

        TripStopsSection(
            stops = uiState.stops,
            excursions = uiState.excursions,
            countries = uiState.countries,
            isReorderMode = isReorderMode,
            onReorderModeChanged = onReorderModeChanged,
            onAddStopClick = onAddStopClick,
            onEditStop = onEditStop,
            onMoveStopUp = onMoveStopUp,
            onMoveStopDown = onMoveStopDown,
            onDeleteStop = onDeleteStop,
            onAddExcursionClick = onAddExcursionClick,
            onDeleteExcursion = onDeleteExcursion,
            onMoveExcursionUp = onMoveExcursionUp,
            onMoveExcursionDown = onMoveExcursionDown,
            onAddExcursionStopClick = onAddExcursionStopClick,
            onEditExcursionStop = onEditExcursionStop,
            onDeleteExcursionStop = onDeleteExcursionStop,
            onMoveExcursionStopUp = onMoveExcursionStopUp,
            onMoveExcursionStopDown = onMoveExcursionStopDown,
            tripStopPhotoMap = tripStopPhotoMap,
            excursionStopPhotoMap = excursionStopPhotoMap,
            onStopClick = onStopClick,
            onExcursionStopClick = onExcursionStopClick,
        )

        TripPhotoGallerySection(
            gallery = uiState.photoGallery,
            coverPhotoFilename = trip.coverPhotoFilename,
            onGroupClick = { group ->
                when (group.stopType) {
                    StopType.TRIP_STOP -> uiState.stops
                        .firstOrNull { it.id == group.stopId }
                        ?.let(onStopClick)
                    StopType.EXCURSION_STOP -> uiState.excursions
                        .asSequence()
                        .flatMap { it.stops.asSequence() }
                        .firstOrNull { it.id == group.stopId }
                        ?.let(onExcursionStopClick)
                }
            },
            onPhotoClick = onPhotoClick,
        )

        Spacer(Modifier.height(24.dp))
    }
}

// ─────────────────────────────────────────────
// Combined info card: title + stats + countries
// ─────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TripInfoCard(
    trip: Trip,
    stopCount: Int,
    tripCountries: List<TripCountryPillUiState>,
    onStoryClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = trip.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp, lineHeight = 27.sp),
                        fontWeight = FontWeight.Medium,
                        color = AtlasOnSurfaceStrong,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    TripStatusPill(status = trip.status)
                }

                trip.dateRange?.let { range ->
                    Text(
                        text = dateRangeFormatter.format(range).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasOnSurfaceMuted,
                    )
                }

                trip.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AtlasOnSurfaceMuted,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                TripStatItem(value = trip.dayCountText(), label = "DIES")
                TripStatItem(value = stopCount.toString(), label = "PARADES")
                TripStatItem(value = tripCountries.size.toString(), label = "PAÏSOS")
            }

            if (tripCountries.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    tripCountries.forEach { country ->
                        TripCountryPill(country = country)
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onStoryClick),
                shape = RoundedCornerShape(999.dp),
                color = AtlasAccentContainer,
                border = BorderStroke(1.dp, AtlasOutline),
            ) {
                Text(
                    text = "Veure relat",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AtlasPrimary,
                )
            }
        }
    }
}

@Composable
private fun TripStatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp, lineHeight = 25.sp),
            fontWeight = FontWeight.Medium,
            color = AtlasOnSurfaceStrong,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AtlasOnSurfaceMuted,
        )
    }
}

@Composable
private fun TripCountryPill(country: TripCountryPillUiState) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(AtlasBackground)
            .border(1.dp, AtlasOutline, RoundedCornerShape(999.dp))
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        country.flagEmoji?.takeIf { it.isNotBlank() }?.let { flag ->
            Text(
                text = flag,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Text(
            text = country.iso2.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = AtlasNavy,
        )
        Text(
            text = country.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AtlasOnSurfaceStrong,
        )
    }
}

@Composable
private fun TripStatusPill(status: TravelStatus) {
    val colors = status.tripStatusColors()
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(colors.container)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(colors.foreground),
        )
        Text(
            text = status.toCatalanLabel().uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = colors.foreground,
            letterSpacing = 0.sp,
        )
    }
}

// ─────────────────────────────────────────────
// Linked itinerary panel
// ─────────────────────────────────────────────
@Composable
private fun LinkedItineraryPanel(
    linkedItinerary: Itinerary?,
    availableCount: Int,
    onItineraryClick: (String) -> Unit,
    onOpenItineraryPicker: () -> Unit,
    onUnlinkItinerary: () -> Unit,
) {
    if (linkedItinerary == null) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = availableCount > 0, onClick = onOpenItineraryPicker),
            shape = RoundedCornerShape(18.dp),
            color = AtlasSurface,
            border = BorderStroke(1.dp, AtlasOutline),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LinkedCardIcon()
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Itinerari vinculat".uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AtlasOnSurfaceMuted,
                    )
                    Text(
                        text = "Sense itinerari assignat",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasOnSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (availableCount > 0) {
                    LinkedCardActionChip(text = "Assigna")
                }
            }
        }
        return
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItineraryClick(linkedItinerary.id) },
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LinkedCardIcon()
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Itinerari vinculat".uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceMuted,
                )
                Text(
                    text = linkedItinerary.title.ifBlank { "Itinerari" },
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 23.sp, lineHeight = 25.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            TextButton(
                onClick = onUnlinkItinerary,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = AtlasPrimary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text("Desvincula", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

// ─────────────────────────────────────────────
// Itinerary picker dialog
// ─────────────────────────────────────────────
@Composable
private fun LinkedCardIcon() {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(AtlasBackground)
            .border(1.dp, AtlasOutline, RoundedCornerShape(11.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Link, null, tint = AtlasOnSurfaceMuted, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun LinkedCardActionChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(AtlasAccentContainer)
            .padding(horizontal = 11.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = AtlasPrimary,
        )
    }
}

@Composable
private fun ItineraryPickerDialog(
    itineraries: List<Itinerary>,
    onDismiss: () -> Unit,
    onSelect: (Itinerary) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = AtlasSurface,
        title = {
            Text(
                text = "Vincula itinerari",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceStrong,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (itineraries.isEmpty()) {
                    Text(
                        text = "No hi ha itineraris sense viatge.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AtlasOnSurfaceMuted,
                    )
                } else {
                    itineraries.forEach { itinerary ->
                        Surface(
                            onClick = { onSelect(itinerary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = AtlasBackground,
                            border = BorderStroke(1.dp, AtlasOutline),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = itinerary.title.ifBlank { "Itinerari" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AtlasOnSurfaceStrong,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel·la", fontWeight = FontWeight.Bold, color = AtlasOnSurfaceMuted)
            }
        },
    )
}

// ─────────────────────────────────────────────
// Stops section
// ─────────────────────────────────────────────

private val ExcursionColor = Color(0xFF7C3AED)
private val ExcursionContainerColor = Color(0xFFF5F3FF)

@Composable
private fun TripStopsSection(
    stops: List<TripStop>,
    excursions: List<Excursion>,
    countries: List<Country>,
    isReorderMode: Boolean,
    onReorderModeChanged: (Boolean) -> Unit,
    onAddStopClick: () -> Unit,
    onEditStop: (TripStop) -> Unit,
    onMoveStopUp: (TripStop) -> Unit,
    onMoveStopDown: (TripStop) -> Unit,
    onDeleteStop: (TripStop) -> Unit,
    onAddExcursionClick: (String?) -> Unit,
    onDeleteExcursion: (Excursion) -> Unit,
    onMoveExcursionUp: (Excursion) -> Unit,
    onMoveExcursionDown: (Excursion) -> Unit,
    onAddExcursionStopClick: (String) -> Unit,
    onEditExcursionStop: (ExcursionStop) -> Unit,
    onDeleteExcursionStop: (ExcursionStop) -> Unit,
    onMoveExcursionStopUp: (String, ExcursionStop) -> Unit,
    onMoveExcursionStopDown: (String, ExcursionStop) -> Unit,
    tripStopPhotoMap: Map<String, List<StopPhoto>>,
    excursionStopPhotoMap: Map<String, List<StopPhoto>>,
    onStopClick: (TripStop) -> Unit,
    onExcursionStopClick: (ExcursionStop) -> Unit,
) {
    val totalTimelineItems = stops.size + excursions.size

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Parades", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = AtlasOnSurfaceStrong)
                if (totalTimelineItems > 0) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(AtlasOnSurfaceStrong).padding(horizontal = 9.dp, vertical = 2.dp)) {
                        Text(totalTimelineItems.toString(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onAddStopClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AtlasNavy, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
                ) {
                    Icon(Icons.Filled.Add, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Afegeix", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }
                if (totalTimelineItems > 0) {
                    IconButton(
                        onClick = { onReorderModeChanged(!isReorderMode) },
                        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                            .background(if (isReorderMode) AtlasPrimary else AtlasSurface)
                            .border(1.dp, if (isReorderMode) AtlasPrimary else AtlasOutline, RoundedCornerShape(12.dp)),
                    ) {
                        Icon(if (isReorderMode) Icons.Filled.Check else Icons.Filled.Menu, null, tint = if (isReorderMode) Color.White else AtlasOnSurfaceMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        if (stops.isEmpty() && excursions.isEmpty()) {
            EmptyStopsState(onAddStopClick = onAddStopClick)
        } else {
            TripStopsTimeline(
                stops = stops,
                excursions = excursions,
                countries = countries,
                isReorderMode = isReorderMode,
                onEditStop = onEditStop,
                onMoveStopUp = onMoveStopUp,
                onMoveStopDown = onMoveStopDown,
                onDeleteStop = onDeleteStop,
                onDeleteExcursion = onDeleteExcursion,
                onMoveExcursionUp = onMoveExcursionUp,
                onMoveExcursionDown = onMoveExcursionDown,
                onAddExcursionClick = onAddExcursionClick,
                onAddExcursionStopClick = onAddExcursionStopClick,
                onEditExcursionStop = onEditExcursionStop,
                onDeleteExcursionStop = onDeleteExcursionStop,
                onMoveExcursionStopUp = onMoveExcursionStopUp,
                onMoveExcursionStopDown = onMoveExcursionStopDown,
                tripStopPhotoMap = tripStopPhotoMap,
                excursionStopPhotoMap = excursionStopPhotoMap,
                onStopClick = onStopClick,
                onExcursionStopClick = onExcursionStopClick,
            )
        }
    }
}

@Composable
private fun TripStopsTimeline(
    stops: List<TripStop>,
    excursions: List<Excursion>,
    countries: List<Country>,
    isReorderMode: Boolean,
    onEditStop: (TripStop) -> Unit,
    onMoveStopUp: (TripStop) -> Unit,
    onMoveStopDown: (TripStop) -> Unit,
    onDeleteStop: (TripStop) -> Unit,
    onDeleteExcursion: (Excursion) -> Unit,
    onMoveExcursionUp: (Excursion) -> Unit,
    onMoveExcursionDown: (Excursion) -> Unit,
    onAddExcursionClick: (String?) -> Unit,
    onAddExcursionStopClick: (String) -> Unit,
    onEditExcursionStop: (ExcursionStop) -> Unit,
    onDeleteExcursionStop: (ExcursionStop) -> Unit,
    onMoveExcursionStopUp: (String, ExcursionStop) -> Unit,
    onMoveExcursionStopDown: (String, ExcursionStop) -> Unit,
    tripStopPhotoMap: Map<String, List<StopPhoto>>,
    excursionStopPhotoMap: Map<String, List<StopPhoto>>,
    onStopClick: (TripStop) -> Unit,
    onExcursionStopClick: (ExcursionStop) -> Unit,
) {
    val excursionsByAnchor = excursions.groupBy { it.anchorTripStopId }

    Column {
        stops.forEachIndexed { index, stop ->
            val anchored = excursionsByAnchor[stop.id].orEmpty()
            val hasLineBelow = index < stops.lastIndex || anchored.isNotEmpty() || !excursionsByAnchor[null].isNullOrEmpty()
            val countryName = countries.firstOrNull { it.iso2 == stop.countryIso2 }?.nameCa ?: stop.countryIso2.orEmpty()

            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                // Timeline left column: circle + connecting line
                Column(modifier = Modifier.fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                    StopNumberCircle(number = index + 1, modifier = Modifier.padding(top = 10.dp))
                    if (hasLineBelow) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .weight(1f)
                                .padding(vertical = 1.dp)
                                .background(AtlasVisited.copy(alpha = 0.35f)),
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                // Right side: stop card + anchored excursions
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Spacer(Modifier.height(3.dp))
                    val stopPhotos = tripStopPhotoMap[stop.id] ?: emptyList()
                    TripStopCard(
                        stop = stop,
                        countryName = countryName,
                        isReorderMode = isReorderMode,
                        canMoveUp = index > 0,
                        canMoveDown = index < stops.lastIndex,
                        onEditStop = onEditStop,
                        onMoveStopUp = onMoveStopUp,
                        onMoveStopDown = onMoveStopDown,
                        onDeleteStop = onDeleteStop,
                        onAddExcursionClick = { onAddExcursionClick(stop.id) },
                        previewPhotos = stopPhotos.take(4),
                        photoCount = stopPhotos.size,
                        onStopClick = { onStopClick(stop) },
                    )
                    anchored.forEach { excursion ->
                        val excursionIndex = excursions.indexOf(excursion)
                        ExcursionTimelineCard(
                            excursion = excursion,
                            countries = countries,
                            isReorderMode = isReorderMode,
                            canMoveUp = excursionIndex > 0,
                            canMoveDown = excursionIndex < excursions.lastIndex,
                            onDeleteExcursion = onDeleteExcursion,
                            onMoveExcursionUp = onMoveExcursionUp,
                            onMoveExcursionDown = onMoveExcursionDown,
                            onAddExcursionStopClick = onAddExcursionStopClick,
                            onEditExcursionStop = onEditExcursionStop,
                            onDeleteExcursionStop = onDeleteExcursionStop,
                            onMoveExcursionStopUp = onMoveExcursionStopUp,
                            onMoveExcursionStopDown = onMoveExcursionStopDown,
                            excursionStopPhotoMap = excursionStopPhotoMap,
                            onExcursionStopClick = onExcursionStopClick,
                        )
                    }
                    Spacer(Modifier.height(1.dp))
                }
            }
        }

        // Unanchored excursions (no circle, just aligned card)
        excursionsByAnchor[null].orEmpty().forEach { excursion ->
            val excursionIndex = excursions.indexOf(excursion)
            Row {
                Spacer(Modifier.width(30.dp + 12.dp)) // align with card column
                ExcursionTimelineCard(
                    modifier = Modifier.weight(1f),
                    excursion = excursion,
                    countries = countries,
                    isReorderMode = isReorderMode,
                    canMoveUp = excursionIndex > 0,
                    canMoveDown = excursionIndex < excursions.lastIndex,
                    onDeleteExcursion = onDeleteExcursion,
                    onMoveExcursionUp = onMoveExcursionUp,
                    onMoveExcursionDown = onMoveExcursionDown,
                    onAddExcursionStopClick = onAddExcursionStopClick,
                    onEditExcursionStop = onEditExcursionStop,
                    onDeleteExcursionStop = onDeleteExcursionStop,
                    onMoveExcursionStopUp = onMoveExcursionStopUp,
                    onMoveExcursionStopDown = onMoveExcursionStopDown,
                    excursionStopPhotoMap = excursionStopPhotoMap,
                    onExcursionStopClick = onExcursionStopClick,
                )
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun StopNumberCircle(number: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(AtlasVisited),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun TripStopCard(
    stop: TripStop,
    countryName: String,
    isReorderMode: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onEditStop: (TripStop) -> Unit,
    onMoveStopUp: (TripStop) -> Unit,
    onMoveStopDown: (TripStop) -> Unit,
    onDeleteStop: (TripStop) -> Unit,
    onAddExcursionClick: () -> Unit,
    previewPhotos: List<StopPhoto> = emptyList(),
    photoCount: Int = 0,
    onStopClick: () -> Unit = {},
) {
    val isManual = stop.source == TripStopSource.MANUAL
    val isItinerary = stop.source == TripStopSource.ITINERARY_GROUP
    val hasPhotos = previewPhotos.isNotEmpty()

    Surface(
        onClick = onStopClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            if (hasPhotos) {
                StopPhotoThumbnails(
                    photos = previewPhotos,
                    photoCount = photoCount,
                    size = 46.dp,
                )
            } else {
                StopThumbnail(
                    modifier = Modifier.size(46.dp),
                    accent = if (isItinerary) AtlasPrimary else AtlasOnSurfaceMuted,
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stop.displayTitle ?: stop.locationName,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, lineHeight = 18.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasOnSurfaceStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (isItinerary) {
                        TypeBadge(label = "VOL", color = AtlasPrimary, background = AtlasAccentContainer)
                    }
                }
                Text(
                    text = buildStopMetaLine(stop, countryName),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = AtlasMono,
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Actions
            if (isReorderMode && isManual) {
                Column {
                    IconButton(onClick = { onMoveStopUp(stop) }, enabled = canMoveUp, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.KeyboardArrowUp, "Mou amunt", tint = if (canMoveUp) AtlasOnSurfaceStrong else AtlasOutline, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { onMoveStopDown(stop) }, enabled = canMoveDown, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.KeyboardArrowDown, "Mou avall", tint = if (canMoveDown) AtlasOnSurfaceStrong else AtlasOutline, modifier = Modifier.size(18.dp))
                    }
                }
            } else if (!isReorderMode && isManual) {
                StopOverflowMenu(
                    onEdit = { onEditStop(stop) },
                    onAddExcursion = onAddExcursionClick,
                    onDelete = { onDeleteStop(stop) },
                )
            }
        }
    }
}

@Composable
private fun StopIcon(
    modifier: Modifier = Modifier,
    tint: Color = AtlasPrimary,
    background: Color = AtlasAccentContainer,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Place,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(26.dp),
        )
    }
}

@Composable
private fun StopThumbnail(
    modifier: Modifier = Modifier,
    accent: Color = AtlasOnSurfaceMuted,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(AtlasSurfaceSubtle)
            .border(1.dp, AtlasOutline, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val stripe = accent.copy(alpha = 0.10f)
            val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            var x = -size.height
            while (x < size.width) {
                drawLine(
                    color = stripe,
                    start = androidx.compose.ui.geometry.Offset(x, size.height),
                    end = androidx.compose.ui.geometry.Offset(x + size.height, 0f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round,
                )
                x += 12.dp.toPx()
            }
        }
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(AtlasSurface.copy(alpha = 0.72f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Place,
                contentDescription = null,
                tint = accent.copy(alpha = 0.72f),
                modifier = Modifier.size(13.dp),
            )
        }
    }
}

@Composable
private fun StopOverflowMenu(
    onEdit: () -> Unit,
    onAddExcursion: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.MoreVert, "Accions", tint = AtlasOnSurfaceMuted, modifier = Modifier.size(18.dp))
        }
        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(surfaceContainer = AtlasSurface),
            shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(14.dp)),
        ) {
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.width(160.dp)) {
                DropdownMenuItem(
                    text = { Text("Edita", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = AtlasOnSurfaceStrong) },
                    leadingIcon = { Icon(Icons.Filled.Edit, null, tint = AtlasOnSurfaceStrong, modifier = Modifier.size(15.dp)) },
                    onClick = { expanded = false; onEdit() },
                )
                DropdownMenuItem(
                    text = { Text("Afegeix excursió", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = AtlasOnSurfaceStrong) },
                    leadingIcon = { Icon(Icons.Filled.Add, null, tint = AtlasOnSurfaceStrong, modifier = Modifier.size(15.dp)) },
                    onClick = { expanded = false; onAddExcursion() },
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AtlasOutline))
                DropdownMenuItem(
                    text = { Text("Elimina", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = AtlasError) },
                    leadingIcon = { Icon(Icons.Filled.Delete, null, tint = AtlasError, modifier = Modifier.size(15.dp)) },
                    onClick = { expanded = false; onDelete() },
                )
            }
        }
    }
}

@Composable
private fun TypeBadge(label: String, color: Color, background: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = color)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExcursionTimelineCard(
    excursion: Excursion,
    countries: List<Country>,
    isReorderMode: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onDeleteExcursion: (Excursion) -> Unit,
    onMoveExcursionUp: (Excursion) -> Unit,
    onMoveExcursionDown: (Excursion) -> Unit,
    onAddExcursionStopClick: (String) -> Unit,
    onEditExcursionStop: (ExcursionStop) -> Unit,
    onDeleteExcursionStop: (ExcursionStop) -> Unit,
    onMoveExcursionStopUp: (String, ExcursionStop) -> Unit,
    onMoveExcursionStopDown: (String, ExcursionStop) -> Unit,
    excursionStopPhotoMap: Map<String, List<StopPhoto>> = emptyMap(),
    onExcursionStopClick: (ExcursionStop) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val sortedStops = excursion.stops.sortedBy { it.sortOrder }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AtlasSurface)
            .drawBehind {
                drawRoundRect(
                    color = ExcursionColor.copy(alpha = 0.34f),
                    cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(7.dp.toPx(), 5.dp.toPx())),
                    ),
                )
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        // Header row: EXCURSIÓ badge + overflow/reorder
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(ExcursionColor.copy(alpha = 0.12f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text("EXCURSIÓ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = ExcursionColor)
            }
            Spacer(modifier = Modifier.weight(1f))
            if (isReorderMode) {
                Row {
                    IconButton(onClick = { onMoveExcursionUp(excursion) }, enabled = canMoveUp, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.KeyboardArrowUp, "Mou amunt", tint = if (canMoveUp) AtlasOnSurfaceStrong else AtlasOutline)
                    }
                    IconButton(onClick = { onMoveExcursionDown(excursion) }, enabled = canMoveDown, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.KeyboardArrowDown, "Mou avall", tint = if (canMoveDown) AtlasOnSurfaceStrong else AtlasOutline)
                    }
                }
            } else {
                ExcursionOverflowMenu(onDelete = { onDeleteExcursion(excursion) })
            }
        }

        if (sortedStops.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                sortedStops.forEachIndexed { index, stop ->
                    val countryName = countries.firstOrNull { it.iso2 == stop.countryIso2 }?.nameCa ?: stop.countryIso2.orEmpty()
                    val stopPhotos = excursionStopPhotoMap[stop.id] ?: emptyList()
                    ExcursionStopCard(
                        stop = stop,
                        countryName = countryName,
                        isReorderMode = isReorderMode,
                        canMoveUp = index > 0,
                        canMoveDown = index < sortedStops.lastIndex,
                        onEdit = { onEditExcursionStop(stop) },
                        onDelete = { onDeleteExcursionStop(stop) },
                        onMoveUp = { onMoveExcursionStopUp(excursion.id, stop) },
                        onMoveDown = { onMoveExcursionStopDown(excursion.id, stop) },
                        previewPhotos = stopPhotos.take(4),
                        photoCount = stopPhotos.size,
                        onStopClick = { onExcursionStopClick(stop) },
                    )
                }
            }
        }

        if (!isReorderMode) {
            TextButton(onClick = { onAddExcursionStopClick(excursion.id) }, modifier = Modifier.height(28.dp), contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)) {
                Text("+ Afegeix parada", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = ExcursionColor)
            }
        }
    }
}

@Composable
private fun ExcursionStopChip(
    index: Int,
    stop: ExcursionStop,
    isReorderMode: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(AtlasSurface)
            .border(1.dp, AtlasOutline, RoundedCornerShape(999.dp))
            .clickable(onClick = onEdit)
            .padding(start = 9.dp, top = 5.dp, end = if (isReorderMode) 3.dp else 10.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            text = index.toString(),
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = AtlasMono),
            fontWeight = FontWeight.ExtraBold,
            color = AtlasOnSurfaceMuted,
        )
        Text(
            text = stop.locationName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = AtlasOnSurfaceStrong,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (isReorderMode) {
            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Filled.KeyboardArrowUp, "Mou amunt", tint = if (canMoveUp) AtlasOnSurfaceStrong else AtlasOutline, modifier = Modifier.size(15.dp))
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Filled.KeyboardArrowDown, "Mou avall", tint = if (canMoveDown) AtlasOnSurfaceStrong else AtlasOutline, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}

@Composable
private fun ExcursionStopCard(
    stop: ExcursionStop,
    countryName: String,
    isReorderMode: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    previewPhotos: List<StopPhoto> = emptyList(),
    photoCount: Int = 0,
    onStopClick: () -> Unit = {},
) {
    val hasPhotos = previewPhotos.isNotEmpty()

    Surface(
        onClick = onStopClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            if (hasPhotos) {
                StopPhotoThumbnails(
                    photos = previewPhotos,
                    photoCount = photoCount,
                    size = 46.dp,
                )
            } else {
                StopThumbnail(
                    modifier = Modifier.size(46.dp),
                    accent = ExcursionColor,
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stop.locationName,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, lineHeight = 18.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = AtlasOnSurfaceStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
                Text(
                    text = buildExcursionStopMetaLine(stop, countryName),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = AtlasMono,
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = AtlasOnSurfaceMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Actions
            if (isReorderMode) {
                Column {
                    IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.KeyboardArrowUp, "Mou amunt", tint = if (canMoveUp) AtlasOnSurfaceStrong else AtlasOutline, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.KeyboardArrowDown, "Mou avall", tint = if (canMoveDown) AtlasOnSurfaceStrong else AtlasOutline, modifier = Modifier.size(18.dp))
                    }
                }
            } else {
                ExcursionStopOverflowMenu(onEdit = onEdit, onDelete = onDelete)
            }
        }
    }
}

@Composable
private fun ExcursionOverflowMenu(onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Filled.MoreVert, "Accions", tint = AtlasOnSurfaceMuted, modifier = Modifier.size(16.dp))
        }
        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(surfaceContainer = AtlasSurface),
            shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(14.dp)),
        ) {
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.width(160.dp)) {
                DropdownMenuItem(
                    text = { Text("Elimina", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = AtlasError) },
                    leadingIcon = { Icon(Icons.Filled.Delete, null, tint = AtlasError, modifier = Modifier.size(15.dp)) },
                    onClick = { expanded = false; onDelete() },
                )
            }
        }
    }
}

@Composable
private fun ExcursionStopOverflowMenu(onEdit: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.MoreVert, "Accions", tint = AtlasOnSurfaceMuted, modifier = Modifier.size(18.dp))
        }
        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(surfaceContainer = AtlasSurface),
            shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(14.dp)),
        ) {
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.width(160.dp)) {
                DropdownMenuItem(
                    text = { Text("Edita", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = AtlasOnSurfaceStrong) },
                    leadingIcon = { Icon(Icons.Filled.Edit, null, tint = AtlasOnSurfaceStrong, modifier = Modifier.size(15.dp)) },
                    onClick = { expanded = false; onEdit() },
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AtlasOutline))
                DropdownMenuItem(
                    text = { Text("Elimina", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = AtlasError) },
                    leadingIcon = { Icon(Icons.Filled.Delete, null, tint = AtlasError, modifier = Modifier.size(15.dp)) },
                    onClick = { expanded = false; onDelete() },
                )
            }
        }
    }
}

@Composable
private fun EmptyStopsState(onAddStopClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(AtlasSurface).border(1.dp, AtlasOutline, RoundedCornerShape(18.dp)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Sense parades encara.", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold, color = AtlasOnSurfaceStrong)
        Text("Afegeix els llocs del viatge en ordre. Després els podràs reordenar.", style = MaterialTheme.typography.bodyMedium, color = AtlasOnSurfaceMuted)
        Spacer(Modifier.height(2.dp))
        Button(onClick = onAddStopClick, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = AtlasPrimary, contentColor = Color.White), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)) {
            Text("Afegeix la primera parada", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
        }
    }
}

// ─────────────────────────────────────────────
// Dialogs (unchanged from original)
// ─────────────────────────────────────────────
@Composable
private fun ExcursionStopDialog(
    draft: ExcursionStopDraftUiState,
    countries: List<Country>,
    onDismiss: () -> Unit,
    onLocationSearchQueryChanged: (String) -> Unit,
    onLocationSearchResultSelected: (LocationSearchResult) -> Unit,
    onUseManualEntryClick: () -> Unit,
    onLocationNameChanged: (String) -> Unit,
    onCountryChanged: (String) -> Unit,
    onLatitudeChanged: (String) -> Unit,
    onLongitudeChanged: (String) -> Unit,
    onDatePrecisionChanged: (DatePrecision) -> Unit,
    onDateFieldChanged: (FlexibleDateRangeDraftField, String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    val hasCoordinates = draft.latitude.isNotBlank() && draft.longitude.isNotBlank()
    val selectedCountry = countries.firstOrNull { it.iso2 == draft.countryIso2 }
    val showManualFields = draft.isManualEntryVisible

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = AtlasSurface,
        title = {
            Text(
                text = if (draft.stopId == null) "Afegeix parada d'excursio" else "Edita parada d'excursio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceStrong,
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draft.locationSearchQuery,
                    onValueChange = onLocationSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Cerca un lloc", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    placeholder = { Text("Nom o adreça...", color = AtlasOnSurfaceMuted) },
                    leadingIcon = {
                        if (draft.isSearchingLocation) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = AtlasPrimary)
                        else Icon(Icons.Filled.Search, null, tint = AtlasOnSurfaceMuted, modifier = Modifier.size(18.dp))
                    },
                    shape = RoundedCornerShape(14.dp),
                )
                draft.locationSearchError?.let { Text(it, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = AtlasError) }
                if (draft.locationSearchResults.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AtlasBackground).border(1.dp, AtlasOutline, RoundedCornerShape(12.dp))) {
                        draft.locationSearchResults.forEach { result -> LocationSearchResultRow(result = result, onClick = { onLocationSearchResultSelected(result) }) }
                    }
                }
                if (draft.locationName.isNotBlank()) {
                    SelectedLocationSummary(locationName = draft.locationName, countryName = selectedCountry?.nameCa ?: draft.countryIso2, hasCoordinates = hasCoordinates, showEditDetails = !showManualFields, onEditDetailsClick = onUseManualEntryClick)
                }
                if (!showManualFields && draft.locationName.isBlank()) {
                    TextButton(onClick = onUseManualEntryClick, modifier = Modifier.height(32.dp), colors = ButtonDefaults.textButtonColors(contentColor = AtlasPrimary), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                        Text("Entrada manual", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }
                if (showManualFields) {
                    DialogSectionLabel("Detalls manuals")
                    OutlinedTextField(value = draft.locationName, onValueChange = onLocationNameChanged, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Nom del lloc", fontWeight = FontWeight.Bold, fontSize = 12.sp) }, shape = RoundedCornerShape(14.dp))
                    CountryDropdown(countries = countries, selectedIso2 = draft.countryIso2, onCountryChanged = onCountryChanged)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = draft.latitude, onValueChange = onLatitudeChanged, modifier = Modifier.weight(1f), singleLine = true, label = { Text("Latitud", fontWeight = FontWeight.Bold, fontSize = 12.sp) }, placeholder = { Text("Opcional", color = AtlasOnSurfaceMuted) }, shape = RoundedCornerShape(14.dp))
                        OutlinedTextField(value = draft.longitude, onValueChange = onLongitudeChanged, modifier = Modifier.weight(1f), singleLine = true, label = { Text("Longitud", fontWeight = FontWeight.Bold, fontSize = 12.sp) }, placeholder = { Text("Opcional", color = AtlasOnSurfaceMuted) }, shape = RoundedCornerShape(14.dp))
                    }
                }
                Text("Dades de cerca OpenStreetMap contributors", style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceMuted)
                DialogSectionLabel("Data")
                FlexibleDateRangeField(draft = draft.dateRange, onPrecisionChanged = onDatePrecisionChanged, onFieldChanged = onDateFieldChanged, showHint = false)
                OutlinedTextField(value = draft.notes, onValueChange = onNotesChanged, modifier = Modifier.fillMaxWidth(), label = { Text("Notes", fontWeight = FontWeight.Bold) }, shape = RoundedCornerShape(14.dp), minLines = 1, maxLines = 3)
                draft.validationError?.let { Text(it, color = AtlasError, fontWeight = FontWeight.SemiBold) }
            }
        },
        confirmButton = { CompactTripDialogActionButton(onClick = onSave) { Text("Desa", fontWeight = FontWeight.ExtraBold, color = AtlasPrimary) } },
        dismissButton = { CompactTripDialogActionButton(onClick = onDismiss) { Text("Cancel.la", fontWeight = FontWeight.Bold, color = AtlasOnSurfaceMuted) } },
    )
}

@Composable
private fun TripStopDialog(
    draft: TripStopDraftUiState,
    countries: List<Country>,
    onDismiss: () -> Unit,
    onLocationNameChanged: (String) -> Unit,
    onLocationSearchQueryChanged: (String) -> Unit,
    onLocationSearchResultSelected: (LocationSearchResult) -> Unit,
    onUseManualEntryClick: () -> Unit,
    onCountryChanged: (String) -> Unit,
    onLatitudeChanged: (String) -> Unit,
    onLongitudeChanged: (String) -> Unit,
    onDatePrecisionChanged: (DatePrecision) -> Unit,
    onDateFieldChanged: (FlexibleDateRangeDraftField, String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    val hasCoordinates = draft.latitude.isNotBlank() && draft.longitude.isNotBlank()
    val selectedCountry = countries.firstOrNull { it.iso2 == draft.countryIso2 }
    val showManualFields = draft.isManualEntryVisible

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = AtlasSurface,
        title = {
            Text(
                text = if (draft.isEditing) "Edita parada" else "Afegeix parada",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceStrong,
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draft.locationSearchQuery,
                    onValueChange = onLocationSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Cerca un lloc", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    placeholder = { Text("Nom o adreça...", color = AtlasOnSurfaceMuted) },
                    leadingIcon = {
                        if (draft.isSearchingLocation) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = AtlasPrimary)
                        else Icon(Icons.Filled.Search, null, tint = AtlasOnSurfaceMuted, modifier = Modifier.size(18.dp))
                    },
                    shape = RoundedCornerShape(14.dp),
                )
                draft.locationSearchError?.let { Text(it, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = AtlasError) }
                if (draft.locationSearchResults.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AtlasBackground).border(1.dp, AtlasOutline, RoundedCornerShape(12.dp))) {
                        draft.locationSearchResults.forEach { result -> LocationSearchResultRow(result = result, onClick = { onLocationSearchResultSelected(result) }) }
                    }
                }
                if (draft.locationName.isNotBlank()) {
                    SelectedLocationSummary(locationName = draft.locationName, countryName = selectedCountry?.nameCa ?: draft.countryIso2, hasCoordinates = hasCoordinates, showEditDetails = !showManualFields, onEditDetailsClick = onUseManualEntryClick)
                }
                if (!showManualFields && draft.locationName.isBlank()) {
                    TextButton(onClick = onUseManualEntryClick, modifier = Modifier.height(32.dp), colors = ButtonDefaults.textButtonColors(contentColor = AtlasPrimary), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                        Text("Entrada manual", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }
                if (showManualFields) {
                    DialogSectionLabel("Detalls manuals")
                    OutlinedTextField(value = draft.locationName, onValueChange = onLocationNameChanged, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Nom del lloc", fontWeight = FontWeight.Bold, fontSize = 12.sp) }, shape = RoundedCornerShape(14.dp))
                    CountryDropdown(countries = countries, selectedIso2 = draft.countryIso2, onCountryChanged = onCountryChanged)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = draft.latitude, onValueChange = onLatitudeChanged, modifier = Modifier.weight(1f), singleLine = true, label = { Text("Latitud", fontWeight = FontWeight.Bold, fontSize = 12.sp) }, placeholder = { Text("Opcional", color = AtlasOnSurfaceMuted) }, shape = RoundedCornerShape(14.dp))
                        OutlinedTextField(value = draft.longitude, onValueChange = onLongitudeChanged, modifier = Modifier.weight(1f), singleLine = true, label = { Text("Longitud", fontWeight = FontWeight.Bold, fontSize = 12.sp) }, placeholder = { Text("Opcional", color = AtlasOnSurfaceMuted) }, shape = RoundedCornerShape(14.dp))
                    }
                }
                Text("Dades de cerca © OpenStreetMap contributors", style = MaterialTheme.typography.labelSmall, color = AtlasOnSurfaceMuted)
                DialogSectionLabel("Data")
                FlexibleDateRangeField(draft = draft.dateRange, onPrecisionChanged = onDatePrecisionChanged, onFieldChanged = onDateFieldChanged, showHint = false)
                OutlinedTextField(value = draft.notes, onValueChange = onNotesChanged, modifier = Modifier.fillMaxWidth(), label = { Text("Notes", fontWeight = FontWeight.Bold, fontSize = 12.sp) }, shape = RoundedCornerShape(14.dp), minLines = 1, maxLines = 3)
                draft.validationError?.let { Text(it, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = AtlasError) }
            }
        },
        confirmButton = { CompactTripDialogActionButton(onClick = onSave) { Text("Desa", fontWeight = FontWeight.ExtraBold, color = AtlasPrimary) } },
        dismissButton = { CompactTripDialogActionButton(onClick = onDismiss) { Text("Cancel·la", fontWeight = FontWeight.Bold, color = AtlasOnSurfaceMuted) } },
    )
}

@Composable
private fun DialogSectionLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = AtlasOnSurfaceMuted, letterSpacing = 0.12.sp)
}

@Composable
private fun SelectedLocationSummary(
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
        Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(AtlasPrimary), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Place, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(text = locationName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = AtlasOnSurfaceStrong, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
            TextButton(onClick = onEditDetailsClick, modifier = Modifier.height(30.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp), colors = ButtonDefaults.textButtonColors(contentColor = AtlasPrimary)) {
                Text("Edita", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun LocationSearchResultRow(result: LocationSearchResult, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(0.dp), colors = ButtonDefaults.textButtonColors(contentColor = AtlasOnSurfaceStrong), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = result.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = result.displayName, style = MaterialTheme.typography.bodySmall, color = AtlasOnSurfaceMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryDropdown(countries: List<Country>, selectedIso2: String, onCountryChanged: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCountry = countries.firstOrNull { it.iso2 == selectedIso2 }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(value = selectedCountry?.nameCa.orEmpty(), onValueChange = {}, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(), readOnly = true, label = { Text("País o territori", fontWeight = FontWeight.Bold) }, shape = RoundedCornerShape(14.dp), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) })
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            countries.forEach { country ->
                DropdownMenuItem(text = { Text(country.nameCa, fontWeight = FontWeight.SemiBold) }, onClick = { onCountryChanged(country.iso2); expanded = false })
            }
        }
    }
}

@Composable
private fun ConfirmDeleteDialog(title: String, body: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = AtlasSurface,
        title = { Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = AtlasOnSurfaceStrong) },
        text = { Text(text = body, style = MaterialTheme.typography.bodyMedium, color = AtlasOnSurfaceMuted) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Elimina", fontWeight = FontWeight.ExtraBold, color = AtlasError) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel·la", fontWeight = FontWeight.Bold, color = AtlasOnSurfaceMuted) } },
    )
}

private fun TripStop.hasCoordinates(): Boolean = latitude != null && longitude != null

private fun buildExcursionStopMetaLine(stop: ExcursionStop, countryName: String): String = buildString {
    if (countryName.isNotBlank()) append(countryName)
    stop.dateRange?.let { range ->
        if (isNotEmpty()) append(" · ")
        append(dateRangeFormatter.format(range))
    }
    if (stop.latitude != null && stop.longitude != null) {
        if (isNotEmpty()) append(" · ")
        append("%.2f, %.2f".format(stop.latitude, stop.longitude))
    }
}

private fun buildStopMetaLine(stop: TripStop, countryName: String): String = buildString {
    if (countryName.isNotBlank()) append(countryName)
    stop.dateRange?.let { range ->
        if (isNotEmpty()) append(" · ")
        append(dateRangeFormatter.format(range))
    }
    if (stop.hasCoordinates()) {
        if (isNotEmpty()) append(" · ")
        append("%.2f, %.2f".format(stop.latitude!!, stop.longitude!!))
    }
}

private fun Trip.dayCountText(): String {
    val range = dateRange ?: return "—"
    val start = range.start?.toLocalDateOrNull() ?: return "—"
    val end   = range.end?.toLocalDateOrNull() ?: start
    val days  = ChronoUnit.DAYS.between(start, end).coerceAtLeast(0) + 1
    return days.toString()
}

private fun com.atlas.domain.model.FlexibleDate.toLocalDateOrNull(): LocalDate? {
    val m = month ?: return null
    val d = day   ?: return null
    return runCatching { LocalDate.of(year, m, d) }.getOrNull()
}
