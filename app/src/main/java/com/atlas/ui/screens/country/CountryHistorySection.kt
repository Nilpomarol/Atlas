package com.atlas.ui.screens.country

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.domain.model.CountryLog
import com.atlas.domain.model.CountryLogType
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.presentation.country.CountryTripSummaryUiState
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasLived
import com.atlas.ui.theme.AtlasLivedContainer
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasTrip
import com.atlas.ui.theme.AtlasTripContainer
import com.atlas.ui.theme.AtlasVisit
import com.atlas.ui.theme.AtlasVisitContainer

private val dateRangeFormatter = FlexibleDateFormatter()

@Composable
fun CountryHistorySection(
    logs: List<CountryLog>,
    tripSummaries: List<CountryTripSummaryUiState>,
    style: CountryDetailStyle,
    onEditLog: (CountryLog) -> Unit,
    onDeleteLog: (CountryLog) -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalCount = logs.size + tripSummaries.size
    var pendingDeleteLog by remember { mutableStateOf<CountryLog?>(null) }

    Column(
        modifier = modifier.offset(y = (-20).dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Historial",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceStrong,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(AtlasOnSurfaceStrong)
                    .padding(horizontal = 11.dp, vertical = 3.dp),
            ) {
                Text(
                    text = totalCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                )
            }
        }

        if (totalCount == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(AtlasSurface)
                    .border(1.dp, AtlasOutline, RoundedCornerShape(18.dp))
                    .padding(16.dp),
            ) {
                Text(
                    "Encara no hi ha registres ni viatges.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AtlasOnSurfaceMuted,
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tripSummaries.forEach { TripHistoryRow(it) }
                logs.forEach { log ->
                    LogHistoryRow(
                        log = log,
                        onEditLog = onEditLog,
                        onDeleteLog = { pendingDeleteLog = it },
                    )
                }
            }
        }
    }

    pendingDeleteLog?.let { log ->
        ConfirmDeleteLogDialog(
            log = log,
            onDismiss = { pendingDeleteLog = null },
            onConfirm = {
                pendingDeleteLog = null
                onDeleteLog(log)
            },
        )
    }
}

@Composable
private fun TripHistoryRow(trip: CountryTripSummaryUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, AtlasOutline),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AtlasTripContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Language,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = AtlasTrip,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Text(
                        text = "VIATGE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = AtlasTrip,
                        letterSpacing = 0.14.sp,
                    )
                    TypeBadge(
                        label = "${trip.stopCount} ${if (trip.stopCount == 1) "parada" else "parades"}",
                        color = AtlasTrip,
                        background = AtlasTripContainer,
                    )
                }
                Text(
                    text = trip.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = buildString {
                        trip.dateRangeText?.let {
                            append(it)
                            append(" · ")
                        }
                        append(trip.status.toCatalanLabel())
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AtlasOnSurfaceMuted,
                )
            }
        }
    }
}

@Composable
private fun LogHistoryRow(
    log: CountryLog,
    onEditLog: (CountryLog) -> Unit,
    onDeleteLog: (CountryLog) -> Unit,
) {
    val (accentColor, accentLight, icon) = when (log.type) {
        CountryLogType.LIVED -> Triple(AtlasLived, AtlasLivedContainer, Icons.Filled.Home)
        CountryLogType.VISIT -> Triple(AtlasVisit, AtlasVisitContainer, Icons.Filled.Place)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, AtlasOutline),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(25.dp),
                    tint = accentColor,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Text(
                        text = log.type.toCatalanLabel().uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor,
                        letterSpacing = 0.14.sp,
                    )
                    TypeBadge("MANUAL", accentColor, accentLight)
                }
                Text(
                    text = log.notes?.takeIf { it.isNotBlank() } ?: log.type.toCatalanLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = log.dateRange?.let { dateRangeFormatter.format(it) } ?: "Sense data",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AtlasOnSurfaceMuted,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SmallActionButton("Edita") { onEditLog(log) }
                SmallActionButton("Elimina") { onDeleteLog(log) }
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
            .padding(horizontal = 9.dp, vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = color,
        )
    }
}

@Composable
private fun SmallActionButton(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = AtlasOnSurfaceMuted,
            containerColor = AtlasBackground,
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ConfirmDeleteLogDialog(
    log: CountryLog,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Eliminar registre?",
                fontWeight = FontWeight.ExtraBold,
            )
        },
        text = {
            Text(
                text = "S'eliminarà ${log.notes?.takeIf { it.isNotBlank() } ?: log.type.toCatalanLabel()}. Aquesta acció no es pot desfer.",
                color = AtlasOnSurfaceMuted,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Elimina",
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel·la")
            }
        },
    )
}
