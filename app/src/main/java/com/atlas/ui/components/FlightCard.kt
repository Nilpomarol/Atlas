package com.atlas.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.domain.model.Flight
import com.atlas.domain.service.FlexibleDateFormatter
import com.atlas.domain.util.dayOffsetBetween
import com.atlas.domain.util.utcAwareArrivalDelayMinutes
import com.atlas.domain.util.utcAwareDepartureDelayMinutes
import com.atlas.domain.util.utcAwareDurationMinutes
import com.atlas.presentation.flight.FlightListItemUiState
import com.atlas.ui.theme.AtlasError
import com.atlas.ui.theme.AtlasDelay
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasVisited

@Composable
fun FlightCard(
    item: FlightListItemUiState,
    onClick: () -> Unit,
) {
    val flight = item.flight
    val colors = flight.status.tripStatusColors()
    val arrivalDayOffset = dayOffsetBetween(
        departureDatetime = flight.actualDepartureAt ?: flight.scheduledDepartureAt,
        arrivalDatetime = flight.actualArrivalAt ?: flight.scheduledArrivalAt,
    )
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                flight.airline?.let { iata ->
                    AirlineLogo(
                        iata = iata,
                        modifier = Modifier
                            .height(34.dp)
                            .widthIn(max = 92.dp),
                    )
                } ?: AirlineFallbackLogo(colors.foreground, colors.container)
                flight.flightNumber?.takeIf { it.isNotBlank() }?.let { flightNumber ->
                    Text(
                        text = flightNumber,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                        fontWeight = FontWeight.Bold,
                        color = AtlasOnSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                } ?: Box(modifier = Modifier.weight(1f))
                AtlasPill(
                    label = colors.label,
                    colors = colors,
                    contentPadding = PaddingValues(horizontal = 13.dp, vertical = 7.dp),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 17.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                AirportEndpoint(
                    airportCode = item.originLabel,
                    city = item.originCity,
                    scheduledTime = flight.scheduledDepartureAt?.timePart(),
                    actualTime = flight.actualDepartureAt?.timePart(),
                    delayMinutes = flight.utcAwareDepartureDelayMinutes(),
                    modifier = Modifier.weight(1f),
                )
                FlightRouteMiddle(
                    durationMinutes = flight.utcAwareDurationMinutes(),
                    color = colors.foreground,
                    modifier = Modifier.weight(1.15f),
                )
                AirportEndpoint(
                    airportCode = item.destinationLabel,
                    city = item.destinationCity,
                    scheduledTime = flight.scheduledArrivalAt?.timePart(),
                    actualTime = flight.actualArrivalAt?.timePart(),
                    delayMinutes = flight.utcAwareArrivalDelayMinutes(),
                    dayOffset = arrivalDayOffset,
                    modifier = Modifier.weight(1f),
                    alignEnd = true,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .height(1.dp)
                    .background(AtlasOutline),
            )
            Row(
                modifier = Modifier.padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FlightMetadataItem(
                    icon = Icons.Filled.DateRange,
                    text = flight.dateText(),
                    modifier = Modifier.weight(1.05f),
                )
                FlightMetadataItem(
                    icon = Icons.Filled.Flight,
                    text = flight.aircraft?.takeIf { it.isNotBlank() } ?: "Aeronau",
                    modifier = Modifier.weight(1.15f),
                )
                FlightMetadataItem(
                    icon = Icons.Filled.FlightTakeoff,
                    text = flight.aircraftRegistration?.takeIf { it.isNotBlank() } ?: "Matricula",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AirlineFallbackLogo(
    foreground: Color,
    container: Color,
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Flight,
            contentDescription = null,
            tint = foreground,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun AirportEndpoint(
    airportCode: String,
    city: String?,
    scheduledTime: String?,
    actualTime: String?,
    delayMinutes: Long?,
    dayOffset: Int? = null,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false,
) {
    val displayTime = actualTime ?: scheduledTime
    val hasActual = actualTime != null
    val actualColor = when {
        delayMinutes == null || delayMinutes <= 0 -> AtlasVisited
        delayMinutes < 45 -> AtlasDelay
        else -> AtlasError
    }
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
    ) {
        Text(
            text = airportCode,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 31.sp),
            fontWeight = FontWeight.SemiBold,
            color = AtlasOnSurfaceStrong,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
        )
        city?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = AtlasOnSurfaceStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        displayTime?.let { time ->
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = if (hasActual) actualColor else AtlasOnSurfaceStrong,
                )
                dayOffset?.let {
                    Text(
                        text = if (it > 0) "+$it" else "$it",
                        modifier = Modifier.padding(top = 1.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AtlasOnSurfaceMuted,
                    )
                }
            }
        }
        if (hasActual && scheduledTime != null) {
            Text(
                text = scheduledTime,
                style = MaterialTheme.typography.labelSmall.copy(
                    textDecoration = TextDecoration.LineThrough,
                ),
                color = AtlasOnSurfaceMuted,
            )
        }
    }
}

@Composable
private fun FlightRouteMiddle(
    durationMinutes: Long?,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(top = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = durationMinutes?.toFlightDurationLabel() ?: "",
            style = MaterialTheme.typography.labelMedium,
            color = AtlasOnSurfaceMuted,
            maxLines = 1,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 7.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(color.copy(alpha = 0.5f)),
            )
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(AtlasSurface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Flight,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun FlightMetadataItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AtlasOnSurfaceMuted,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            fontWeight = FontWeight.Bold,
            color = AtlasOnSurfaceMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun Long.toFlightDurationLabel(): String {
    val hours = this / 60
    val minutes = this % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

private fun String.timePart(): String? =
    substringAfter('T', missingDelimiterValue = "")
        .take(5)
        .takeIf { it.length == 5 }

private fun Flight.dateText(): String =
    (actualDepartureAt ?: scheduledDepartureAt ?: actualArrivalAt ?: scheduledArrivalAt)
        ?.let { flightCardDateFormatter.formatIsoDate(it) }
        ?: "Sense data"

private val flightCardDateFormatter = FlexibleDateFormatter()
