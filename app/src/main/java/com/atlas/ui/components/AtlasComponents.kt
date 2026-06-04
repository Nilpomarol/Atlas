package com.atlas.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.domain.model.CountryTrackingState
import com.atlas.domain.model.TravelStatus
import com.atlas.ui.theme.AtlasBackground
import com.atlas.ui.theme.AtlasCompleted
import com.atlas.ui.theme.AtlasCompletedContainer
import com.atlas.ui.theme.AtlasDelay
import com.atlas.ui.theme.AtlasDelayContainer
import com.atlas.ui.theme.AtlasInProgress
import com.atlas.ui.theme.AtlasInProgressContainer
import com.atlas.ui.theme.AtlasLived
import com.atlas.ui.theme.AtlasLivedContainer
import com.atlas.ui.theme.AtlasLiving
import com.atlas.ui.theme.AtlasLivingContainer
import com.atlas.ui.theme.AtlasOnSurfaceFaint
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceSoft
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPending
import com.atlas.ui.theme.AtlasPendingContainer
import com.atlas.ui.theme.AtlasPlanned
import com.atlas.ui.theme.AtlasPlannedContainer
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasSurfaceSubtle
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasVisited
import com.atlas.ui.theme.AtlasVisitedContainer
import com.atlas.ui.theme.AtlasWished
import com.atlas.ui.theme.AtlasWishedContainer

data class AtlasSemanticColors(
    val foreground: Color,
    val container: Color,
    val label: String,
)

object AtlasDimens {
    val PagePadding = 20.dp
    val CardRadius = 18.dp
    val SmallRadius = 10.dp
    val PillRadius = 999.dp
    val CardBorder = 1.dp
}

@Composable
fun AtlasPage(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(AtlasDimens.PagePadding),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AtlasBackground)
            .padding(contentPadding),
    ) {
        content()
    }
}

@Composable
fun AtlasCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    color: Color = AtlasSurface,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AtlasDimens.CardRadius),
        color = color,
        border = BorderStroke(AtlasDimens.CardBorder, AtlasOutline),
        shadowElevation = 0.dp,
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

@Composable
fun AtlasPill(
    label: String,
    colors: AtlasSemanticColors,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 9.dp, vertical = 4.dp),
    fontWeight: FontWeight = FontWeight.Bold,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AtlasDimens.PillRadius))
            .background(colors.container)
            .border(1.dp, colors.foreground.copy(alpha = 0.16f), RoundedCornerShape(AtlasDimens.PillRadius))
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = fontWeight,
            color = colors.foreground,
            letterSpacing = 0.sp,
        )
    }
}

@Composable
fun AtlasSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            color = AtlasOnSurfaceStrong,
        )
        action?.invoke()
    }
}

@Composable
fun AtlasSectionLabel(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AtlasOnSurfaceMuted,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(AtlasOutline),
        )
        if (actionLabel != null) {
            Text(
                text = actionLabel,
                modifier = if (onActionClick != null) Modifier.clickable(onClick = onActionClick) else Modifier,
                style = MaterialTheme.typography.labelSmall,
                color = AtlasPrimary,
            )
        }
    }
}

@Composable
fun AtlasFilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedContainerColor: Color = AtlasNavy,
    selectedContentColor: Color = AtlasSurface,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AtlasDimens.PillRadius))
            .background(if (selected) selectedContainerColor else AtlasSurface)
            .border(1.dp, if (selected) selectedContainerColor else AtlasOutline, RoundedCornerShape(AtlasDimens.PillRadius))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (selected) selectedContentColor else AtlasOnSurfaceSoft,
            maxLines = 1,
        )
    }
}

@Composable
fun AtlasDot(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(7.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(color),
    )
}

@Composable
fun AtlasSpecSheet(
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AtlasDimens.CardRadius),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Column {
            rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (index > 0) Modifier.border(0.dp, Color.Transparent) else Modifier)
                        .padding(horizontal = 18.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = row.first.uppercase(),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = AtlasOnSurfaceMuted,
                    )
                    Text(
                        text = row.second,
                        style = MaterialTheme.typography.labelMedium,
                        color = AtlasOnSurfaceStrong,
                    )
                }
                if (index < rows.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(AtlasOutline.copy(alpha = 0.55f)),
                    )
                }
            }
        }
    }
}

@Composable
fun AtlasStatStrip(
    stats: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Row {
            stats.forEachIndexed { index, stat ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                ) {
                    Text(
                        text = stat.first,
                        style = MaterialTheme.typography.titleLarge,
                        color = AtlasOnSurfaceStrong,
                    )
                    Text(
                        text = stat.second.uppercase(),
                        modifier = Modifier.padding(top = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = AtlasOnSurfaceMuted,
                    )
                }
                if (index < stats.lastIndex) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(56.dp)
                            .padding(vertical = 8.dp)
                            .background(AtlasOutline),
                    )
                }
            }
        }
    }
}

@Composable
fun AtlasMetricCard(
    value: String,
    label: String,
    colors: AtlasSemanticColors,
    modifier: Modifier = Modifier,
) {
    AtlasCard(
        modifier = modifier,
        contentPadding = PaddingValues(12.dp),
    ) {
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = AtlasOnSurfaceStrong,
            )
            Text(
                text = label,
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = AtlasOnSurfaceMuted,
            )
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(AtlasDimens.PillRadius))
                    .background(colors.foreground)
                    .fillMaxWidth(0.42f)
                    .padding(vertical = 2.dp),
            )
        }
    }
}

@Composable
fun AtlasDottedCanvas(
    modifier: Modifier = Modifier,
    height: Dp = 132.dp,
    tint: Color = AtlasOnSurfaceFaint,
    content: @Composable () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(AtlasDimens.CardRadius))
            .background(AtlasSurfaceSubtle)
            .border(1.dp, AtlasOutline, RoundedCornerShape(AtlasDimens.CardRadius)),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
                .background(Color.Transparent)
                .then(Modifier),
        ) {
            val spacing = 18.dp.toPx()
            var y = spacing / 2
            while (y < size.height) {
                var x = spacing / 2
                while (x < size.width) {
                    drawCircle(
                        color = tint.copy(alpha = 0.22f),
                        radius = 1.4.dp.toPx(),
                        center = Offset(x, y),
                    )
                    x += spacing
                }
                y += spacing
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
        ) {
            content()
        }
    }
}

fun CountryTrackingState.primaryStateColors(): AtlasSemanticColors = when {
    currentlyLiving -> AtlasSemanticColors(AtlasLiving, AtlasLivingContainer, "Vivint-hi")
    lived -> AtlasSemanticColors(AtlasLived, AtlasLivedContainer, "Viscut")
    visited -> AtlasSemanticColors(AtlasVisited, AtlasVisitedContainer, "Visitat")
    planned -> AtlasSemanticColors(AtlasPlanned, AtlasPlannedContainer, "Planificat")
    wished -> AtlasSemanticColors(AtlasWished, AtlasWishedContainer, "Desitjat")
    else -> AtlasSemanticColors(AtlasPending, AtlasPendingContainer, "No visitat")
}

fun TravelStatus.tripStatusColors(): AtlasSemanticColors = when (this) {
    TravelStatus.PLANNED -> AtlasSemanticColors(AtlasPlanned, AtlasPlannedContainer, "Planificat")
    TravelStatus.IN_PROGRESS -> AtlasSemanticColors(AtlasInProgress, AtlasInProgressContainer, "En curs")
    TravelStatus.COMPLETED -> AtlasSemanticColors(AtlasCompleted, AtlasCompletedContainer, "Completat")
    TravelStatus.UNKNOWN -> AtlasSemanticColors(AtlasPending, AtlasPendingContainer, "Desconegut")
}

fun delayStatusColors(): AtlasSemanticColors =
    AtlasSemanticColors(AtlasDelay, AtlasDelayContainer, "Retard")
