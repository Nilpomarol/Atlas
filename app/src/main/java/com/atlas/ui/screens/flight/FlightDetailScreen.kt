package com.atlas.ui.screens.flight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.Airport
import com.atlas.domain.model.Flight
import com.atlas.domain.model.TravelStatus
import com.atlas.presentation.flight.FlightDetailUiState
import com.atlas.presentation.flight.FlightEditorDraftUiState
import com.atlas.ui.components.AirlineLogo
import com.atlas.ui.components.AtlasCard
import com.atlas.ui.components.AtlasPill
import com.atlas.ui.components.AtlasSemanticColors
import com.atlas.ui.components.tripStatusColors
import com.atlas.ui.components.map.AtlasMapView
import com.atlas.ui.components.map.toHexColor
import com.atlas.ui.screens.country.BackPill
import com.atlas.ui.screens.trip.toCatalanLabel
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasOnSurfaceFaint
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineCap
import org.maplibre.android.style.layers.PropertyFactory.lineDasharray
import org.maplibre.android.style.layers.PropertyFactory.lineJoin
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun FlightDetailScreen(
    uiState: FlightDetailUiState,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onDismissDraft: () -> Unit,
    onOriginQueryChanged: (String) -> Unit,
    onOriginSelected: (com.atlas.domain.model.Airport) -> Unit,
    onDestinationQueryChanged: (String) -> Unit,
    onDestinationSelected: (com.atlas.domain.model.Airport) -> Unit,
    onStatusChanged: (TravelStatus) -> Unit,
    onScheduledDepartureAtChanged: (String) -> Unit,
    onScheduledArrivalAtChanged: (String) -> Unit,
    onActualDepartureAtChanged: (String) -> Unit,
    onActualArrivalAtChanged: (String) -> Unit,
    onAirlineChanged: (String) -> Unit,
    onFlightNumberChanged: (String) -> Unit,
    onAircraftChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSaveDraft: () -> Unit,
) {
    val flight = uiState.flight
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (flight == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AtlasBackground)
                .padding(20.dp),
        ) {
            BackPill(onBackClick = onBackClick, modifier = Modifier.padding(top = 16.dp))
        }
        return
    }

    val statusColors = flight.status.tripStatusColors()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AtlasBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        FlightHero(
            flight = flight,
            originAirport = uiState.originAirport,
            destinationAirport = uiState.destinationAirport,
            prevContextAirport = uiState.prevContextAirport,
            nextContextAirport = uiState.nextContextAirport,
            statusColors = statusColors,
            onBackClick = onBackClick,
        )

        Column(
            modifier = Modifier
                .offset(y = (-42).dp)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            FlightIdentityCard(
                flight = flight,
                originAirport = uiState.originAirport,
                destinationAirport = uiState.destinationAirport,
                statusColors = statusColors,
                groupPositionLabel = uiState.groupPositionLabel,
            )

            FlightDatesCard(flight)

            val hasAnyMeta = flight.airline != null || flight.flightNumber != null || flight.aircraft != null
            if (hasAnyMeta) {
                FlightMetaCard(flight, uiState.resolvedAirlineName)
            }

            if (!flight.notes.isNullOrBlank()) {
                FlightNotesCard(flight.notes)
            }

            FlightActions(
                onEditClick = onEditClick,
                onDeleteClick = { showDeleteDialog = true },
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Elimina vol") },
            text = { Text("Vols eliminar aquest vol? Aquesta acció no es pot desfer.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteConfirmed()
                }) { Text("Elimina") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel·la") }
            },
        )
    }

    if (uiState.draft.isOpen) {
        FlightEditorDialog(
            draft = uiState.draft,
            originResults = uiState.originSearchResults,
            destinationResults = uiState.destinationSearchResults,
            onDismiss = onDismissDraft,
            onOriginQueryChanged = onOriginQueryChanged,
            onOriginSelected = onOriginSelected,
            onDestinationQueryChanged = onDestinationQueryChanged,
            onDestinationSelected = onDestinationSelected,
            onStatusChanged = onStatusChanged,
            onScheduledDepartureAtChanged = onScheduledDepartureAtChanged,
            onScheduledArrivalAtChanged = onScheduledArrivalAtChanged,
            onActualDepartureAtChanged = onActualDepartureAtChanged,
            onActualArrivalAtChanged = onActualArrivalAtChanged,
            onAirlineChanged = onAirlineChanged,
            onFlightNumberChanged = onFlightNumberChanged,
            onAircraftChanged = onAircraftChanged,
            onNotesChanged = onNotesChanged,
            onSave = onSaveDraft,
        )
    }
}

// ─── Hero ─────────────────────────────────────────────────────────────────────

@Composable
private fun FlightHero(
    flight: Flight,
    originAirport: Airport?,
    destinationAirport: Airport?,
    prevContextAirport: Airport?,
    nextContextAirport: Airport?,
    statusColors: AtlasSemanticColors,
    onBackClick: () -> Unit,
) {
    val originCode = originAirport?.iata ?: originAirport?.icao ?: flight.originAirportId.uppercase()
    val destinationCode = destinationAirport?.iata ?: destinationAirport?.icao ?: flight.destinationAirportId.uppercase()
    val mapRef = remember { mutableStateOf<Pair<MapLibreMap, Style>?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp),
    ) {
        AtlasMapView(
            modifier = Modifier.fillMaxSize(),
            onMapReady = { map, style -> mapRef.value = Pair(map, style) },
        )

        BackPill(
            onBackClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 16.dp),
        )

        // Route code label — frosted pill for readability over map tiles
        Text(
            text = "$originCode → $destinationCode",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.82f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = AtlasOnSurfaceMuted,
        )
    }

    LaunchedEffect(mapRef.value, originAirport, destinationAirport, prevContextAirport, nextContextAirport) {
        val (map, style) = mapRef.value ?: return@LaunchedEffect
        setupFlightMap(map, style, originAirport, destinationAirport, prevContextAirport, nextContextAirport, statusColors)
    }
}

private fun setupFlightMap(
    map: MapLibreMap,
    style: Style,
    originAirport: Airport?,
    destinationAirport: Airport?,
    prevContextAirport: Airport?,
    nextContextAirport: Airport?,
    statusColors: AtlasSemanticColors,
) {
    val origin = originAirport ?: return
    val destination = destinationAirport ?: return
    val accentHex = statusColors.foreground.toHexColor()

    // Clear any previous content
    listOf("ctx-prev-line", "ctx-next-line", "main-route-line", "ctx-prev-marker", "ctx-next-marker", "origin-marker", "dest-marker")
        .forEach { if (style.getLayer(it) != null) style.removeLayer(it) }
    listOf("ctx-prev-line-src", "ctx-next-line-src", "main-route-src", "ctx-prev-src", "ctx-next-src", "origin-src", "dest-src")
        .forEach { if (style.getSource(it) != null) style.removeSource(it) }

    // Context line: prev airport → origin (dashed)
    if (prevContextAirport != null) {
        val coords = listOf(
            Point.fromLngLat(prevContextAirport.longitude, prevContextAirport.latitude),
            Point.fromLngLat(origin.longitude, origin.latitude),
        )
        style.addSource(GeoJsonSource("ctx-prev-line-src", Feature.fromGeometry(LineString.fromLngLats(coords))))
        style.addLayer(LineLayer("ctx-prev-line", "ctx-prev-line-src").apply {
            setProperties(lineWidth(2f), lineColor(accentHex), lineCap("round"), lineJoin("round"),
                lineDasharray(arrayOf(2f, 2f)))
        })
    }

    // Context line: destination → next airport (dashed)
    if (nextContextAirport != null) {
        val coords = listOf(
            Point.fromLngLat(destination.longitude, destination.latitude),
            Point.fromLngLat(nextContextAirport.longitude, nextContextAirport.latitude),
        )
        style.addSource(GeoJsonSource("ctx-next-line-src", Feature.fromGeometry(LineString.fromLngLats(coords))))
        style.addLayer(LineLayer("ctx-next-line", "ctx-next-line-src").apply {
            setProperties(lineWidth(2f), lineColor(accentHex), lineCap("round"), lineJoin("round"),
                lineDasharray(arrayOf(2f, 2f)))
        })
    }

    // Main route line
    val mainCoords = listOf(
        Point.fromLngLat(origin.longitude, origin.latitude),
        Point.fromLngLat(destination.longitude, destination.latitude),
    )
    style.addSource(GeoJsonSource("main-route-src", Feature.fromGeometry(LineString.fromLngLats(mainCoords))))
    style.addLayer(LineLayer("main-route-line", "main-route-src").apply {
        setProperties(lineWidth(3f), lineColor(accentHex), lineCap("round"), lineJoin("round"))
    })

    // Context airport markers (smaller, semi-transparent)
    prevContextAirport?.let { apt ->
        style.addSource(GeoJsonSource("ctx-prev-src",
            Feature.fromGeometry(Point.fromLngLat(apt.longitude, apt.latitude))))
        style.addLayer(CircleLayer("ctx-prev-marker", "ctx-prev-src").apply {
            setProperties(circleRadius(6f), circleColor(accentHex),
                circleStrokeWidth(1.5f), circleStrokeColor("#FFFFFF"))
        })
    }
    nextContextAirport?.let { apt ->
        style.addSource(GeoJsonSource("ctx-next-src",
            Feature.fromGeometry(Point.fromLngLat(apt.longitude, apt.latitude))))
        style.addLayer(CircleLayer("ctx-next-marker", "ctx-next-src").apply {
            setProperties(circleRadius(6f), circleColor(accentHex),
                circleStrokeWidth(1.5f), circleStrokeColor("#FFFFFF"))
        })
    }

    // Main airport markers
    style.addSource(GeoJsonSource("origin-src",
        Feature.fromGeometry(Point.fromLngLat(origin.longitude, origin.latitude))))
    style.addLayer(CircleLayer("origin-marker", "origin-src").apply {
        setProperties(circleRadius(10f), circleColor(accentHex),
            circleStrokeWidth(2.5f), circleStrokeColor("#FFFFFF"))
    })
    style.addSource(GeoJsonSource("dest-src",
        Feature.fromGeometry(Point.fromLngLat(destination.longitude, destination.latitude))))
    style.addLayer(CircleLayer("dest-marker", "dest-src").apply {
        setProperties(circleRadius(10f), circleColor(accentHex),
            circleStrokeWidth(2.5f), circleStrokeColor("#FFFFFF"))
    })

    // Camera fits only the current flight — context airports may be partially off-screen
    val bounds = LatLngBounds.Builder()
        .include(LatLng(origin.latitude, origin.longitude))
        .include(LatLng(destination.latitude, destination.longitude))
        .build()
    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80))
}

// ─── Cards ────────────────────────────────────────────────────────────────────

@Composable
private fun FlightIdentityCard(
    flight: Flight,
    originAirport: Airport?,
    destinationAirport: Airport?,
    statusColors: AtlasSemanticColors,
    groupPositionLabel: String?,
) {
    val originCode = originAirport?.iata ?: originAirport?.icao ?: flight.originAirportId.uppercase()
    val destCode = destinationAirport?.iata ?: destinationAirport?.icao ?: flight.destinationAirportId.uppercase()
    val originCity = originAirport?.city
    val destCity = destinationAirport?.city

    AtlasCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Route headline
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = originCode,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AtlasOnSurfaceStrong,
                )
                Icon(
                    imageVector = Icons.Filled.Flight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = AtlasOnSurfaceMuted,
                )
                Text(
                    text = destCode,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AtlasOnSurfaceStrong,
                )
            }

            // City names
            if (originCity != null || destCity != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    originCity?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = AtlasOnSurfaceMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    destCity?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = AtlasOnSurfaceMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // Pills row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AtlasPill(label = statusColors.label, colors = statusColors)
                if (groupPositionLabel != null) {
                    AtlasPill(
                        label = groupPositionLabel,
                        colors = AtlasSemanticColors(
                            foreground = AtlasOnSurfaceMuted,
                            container = AtlasSurface,
                            label = groupPositionLabel,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun FlightDatesCard(flight: Flight) {
    val hasScheduled = flight.scheduledDepartureAt != null || flight.scheduledArrivalAt != null
    val hasActual = flight.actualDepartureAt != null || flight.actualArrivalAt != null
    if (!hasScheduled && !hasActual) return

    AtlasCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (hasScheduled) {
                Text(
                    text = "Horari programat",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AtlasOnSurfaceMuted,
                )
                flight.scheduledDepartureAt?.let { DatetimeRow("Sortida", it) }
                flight.scheduledArrivalAt?.let { DatetimeRow("Arribada", it) }
            }
            if (hasScheduled && hasActual) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(AtlasOnSurfaceFaint.copy(alpha = 0.25f)),
                )
            }
            if (hasActual) {
                Text(
                    text = "Horari real",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AtlasOnSurfaceMuted,
                )
                flight.actualDepartureAt?.let { DatetimeRow("Sortida", it) }
                flight.actualArrivalAt?.let { DatetimeRow("Arribada", it) }
            }
        }
    }
}

@Composable
private fun DatetimeRow(label: String, isoValue: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AtlasOnSurfaceMuted,
        )
        Text(
            text = formatDatetime(isoValue),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = AtlasOnSurfaceStrong,
        )
    }
}

@Composable
private fun FlightMetaCard(flight: Flight, resolvedAirlineName: String?) {
    AtlasCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            flight.airline?.let { iata ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Companyia",
                        style = MaterialTheme.typography.bodySmall,
                        color = AtlasOnSurfaceMuted,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AirlineLogo(
                            iata = iata,
                            modifier = Modifier
                                .height(24.dp)
                                .widthIn(max = 80.dp),
                        )
                        Text(
                            text = resolvedAirlineName ?: iata,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AtlasOnSurfaceStrong,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            flight.flightNumber?.let { MetaRow("Número de vol", it) }
            flight.aircraft?.let { MetaRow("Aeronau", it) }
        }
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AtlasOnSurfaceMuted,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = AtlasOnSurfaceStrong,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FlightNotesCard(notes: String) {
    AtlasCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Notes",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceMuted,
            )
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
                color = AtlasOnSurfaceStrong,
            )
        }
    }
}

@Composable
private fun FlightActions(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = onEditClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AtlasOnSurfaceStrong),
        ) {
            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(text = "Edita", modifier = Modifier.padding(start = 6.dp))
        }
        OutlinedButton(
            onClick = onDeleteClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = androidx.compose.material3.MaterialTheme.colorScheme.error),
        ) {
            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(text = "Elimina", modifier = Modifier.padding(start = 6.dp))
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun formatDatetime(value: String): String {
    return try {
        val date = LocalDate.parse(value.take(10), DateTimeFormatter.ISO_LOCAL_DATE)
        val time = value.drop(11).take(5)
        val month = date.monthValue.toMonthLabel()
        "${date.dayOfMonth} $month ${date.year}  ·  $time"
    } catch (_: Exception) {
        value
    }
}

private fun Int.toMonthLabel(): String = when (this) {
    1  -> "Gen."  2  -> "Febr." 3  -> "Març"
    4  -> "Abr."  5  -> "Maig"  6  -> "Juny"
    7  -> "Jul."  8  -> "Ag."   9  -> "Set."
    10 -> "Oct."  11 -> "Nov."  12 -> "Des."
    else -> "$this"
}
