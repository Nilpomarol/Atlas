package com.atlas.ui.screens.country

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.domain.model.Country
import com.atlas.domain.model.CountryTrackingState
import com.atlas.presentation.country.CountryDetailPillUiState
import com.atlas.ui.components.AtlasPill
import com.atlas.ui.components.AtlasSemanticColors
import com.atlas.ui.components.primaryStateColors
import com.atlas.ui.theme.AtlasLived
import com.atlas.ui.theme.AtlasLivedContainer
import com.atlas.ui.theme.AtlasLiving
import com.atlas.ui.theme.AtlasLivingContainer
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPlanned
import com.atlas.ui.theme.AtlasPlannedContainer
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasVisited
import com.atlas.ui.theme.AtlasVisitedContainer
import com.atlas.ui.theme.AtlasWished
import com.atlas.ui.theme.AtlasWishedContainer

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun CountryIdentityHeader(
    country: Country,
    style: CountryDetailStyle,
    trackingState: CountryTrackingState,
    detailPills: CountryDetailPillUiState,
) {
    val context = LocalContext.current
    val flagBitmap = remember(country.flagAsset) {
        country.flagAsset?.let { path ->
            runCatching {
                context.assets.open(path).use { BitmapFactory.decodeStream(it) }
            }.getOrNull()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AtlasSurface, RoundedCornerShape(18.dp))
            .border(1.dp, AtlasOutline, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 62.dp, height = 46.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (flagBitmap != null) {
                Image(
                    bitmap = flagBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit,
                )
            } else if (!country.flagEmoji.isNullOrBlank()) {
                Text(country.flagEmoji, fontSize = 34.sp)
            } else {
                Icon(
                    imageVector = Icons.Filled.Flag,
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                    tint = style.primary,
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f),
        ) {
            Text(
                text = listOfNotNull(country.iso3, country.continent.toCatalanContinent()).joinToString(" · "),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = style.primary,
                letterSpacing = 0.16.sp,
            )
            Text(
                text = country.nameCa,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            FlowRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                detailPills.detailStatePills().ifEmpty { listOf(trackingState.primaryStateColors()) }.forEach { colors ->
                    AtlasPill(label = colors.label, colors = colors)
                }
            }
        }
    }
}

private fun CountryDetailPillUiState.detailStatePills(): List<AtlasSemanticColors> = buildList {
    if (currentlyLiving) add(AtlasSemanticColors(AtlasLiving, AtlasLivingContainer, "Vivint-hi"))
    if (lived && !currentlyLiving) add(AtlasSemanticColors(AtlasLived, AtlasLivedContainer, "Viscut"))
    if (visited) add(AtlasSemanticColors(AtlasVisited, AtlasVisitedContainer, "Visitat"))
    if (planned) add(AtlasSemanticColors(AtlasPlanned, AtlasPlannedContainer, "Planificat"))
    if (wished) add(AtlasSemanticColors(AtlasWished, AtlasWishedContainer, "Desitjat"))
}
