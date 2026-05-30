package com.atlas.ui.screens.country

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.domain.model.Country
import com.atlas.domain.model.CountryTrackingState
import com.atlas.ui.theme.AtlasTrip

@Composable
fun CountryInfoCard(
    country: Country,
    trackingState: CountryTrackingState,
    style: CountryDetailStyle,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-36).dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(style.primary, style.secondary, AtlasTrip),
                    start = Offset.Zero,
                    end = Offset(1200f, 1200f),
                ),
            )
            .padding(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "INFORMACIÓ",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White.copy(alpha = 0.55f),
                    letterSpacing = 0.2.sp,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (country.isUnMember) InfoChip("ONU")
                    InfoChip(listOfNotNull(country.iso2, country.iso3).joinToString(" / "))
                    InfoChip(country.type.toCountryDetailCatalanLabel())
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BigMetric(Modifier.weight(1f), "POBLACIÓ", "—")
                BigMetric(Modifier.weight(1f), "PIB", "—")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.12f)),
            ) {
                CompactStat(Modifier.weight(1f), "CAPITAL", "—", true)
                CompactStat(Modifier.weight(1f), "MONEDA", "—", true)
                CompactStat(Modifier.weight(1f), "ÀREA", "—", true)
                CompactStat(Modifier.weight(1f), "ZONA HOR.", "—", false)
            }
        }
    }
}

@Composable
private fun InfoChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 0.04.sp,
        )
    }
}

@Composable
private fun BigMetric(
    modifier: Modifier,
    label: String,
    value: String,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 15.dp, vertical = 13.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White.copy(alpha = 0.55f),
                letterSpacing = 0.14.sp,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun CompactStat(
    modifier: Modifier,
    label: String,
    value: String,
    borderEnd: Boolean,
) {
    Row(modifier = modifier) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White.copy(alpha = 0.50f),
                letterSpacing = 0.12.sp,
                maxLines = 1,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (borderEnd) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .align(Alignment.CenterVertically)
                    .background(Color.White.copy(alpha = 0.12f)),
            )
        }
    }
}
