package com.atlas.ui.screens.country

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.ui.components.AtlasSectionTitle
import com.atlas.ui.theme.AtlasMono
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasOnSurfaceFaint
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasSurfaceSubtle
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

/**
 * Compact currency converter: a single row of editable amount → converted value with a
 * swap toggle between them, plus the reference rate in the section header. Hidden when no
 * rate is cached (offline first run or an unsupported currency). [eurRate] is local
 * currency units per 1 EUR.
 */
@Composable
fun CountryCurrencyCard(
    currencyCode: String?,
    currencyName: String?,
    eurRate: Double?,
    rateAge: String?,
) {
    if (currencyCode == null || eurRate == null || eurRate <= 0.0) return

    // Prefill with 1 so the card shows a live conversion (and the rate in action) on load.
    var amountText by rememberSaveable(currencyCode) { mutableStateOf("1") }
    // false = local → EUR (the common case: "how much is 100 ¥ in €?").
    var eurToLocal by rememberSaveable(currencyCode) { mutableStateOf(false) }

    val fromCode = if (eurToLocal) "EUR" else currencyCode
    val toCode = if (eurToLocal) currencyCode else "EUR"

    val amount = amountText.replace(",", ".").toDoubleOrNull()
    val converted = amount?.let { if (eurToLocal) it * eurRate else it / eurRate }

    Column {
        AtlasSectionTitle(title = "Moneda")
        Column(
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
                .background(AtlasSurface, RoundedCornerShape(16.dp))
                .border(1.dp, AtlasOutline, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = currencyName ?: currencyCode,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "1 EUR = ${formatMoney(eurRate)} $currencyCode",
                    fontFamily = AtlasMono,
                    fontSize = 12.sp,
                    color = AtlasOnSurfaceFaint,
                    maxLines = 1,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Editable "from" field.
                ConversionField(code = fromCode, filled = false, modifier = Modifier.weight(1f)) {
                    Box {
                        if (amountText.isEmpty()) {
                            Text("0", fontFamily = AtlasMono, fontSize = 17.sp, color = AtlasOnSurfaceFaint)
                        }
                        BasicTextField(
                            value = amountText,
                            onValueChange = { input -> amountText = input.filter { it.isDigit() || it == ',' || it == '.' } },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontFamily = AtlasMono,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,
                                color = AtlasOnSurfaceStrong,
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            cursorBrush = SolidColor(AtlasNavy),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(AtlasNavy)
                        .clickable(onClickLabel = "Inverteix la direcció") { eurToLocal = !eurToLocal }
                        .padding(7.dp),
                ) {
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = "Inverteix la direcció",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }

                // Read-only "to" field.
                ConversionField(code = toCode, filled = true, modifier = Modifier.weight(1f)) {
                    Text(
                        text = converted?.let { formatMoney(it) } ?: "—",
                        fontFamily = AtlasMono,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = AtlasOnSurfaceStrong,
                        maxLines = 1,
                    )
                }
            }

            Text(
                text = if (rateAge != null) "Actualitzat $rateAge · BCE" else "Font: BCE",
                style = MaterialTheme.typography.labelSmall,
                color = AtlasOnSurfaceFaint,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ConversionField(
    code: String,
    filled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (filled) AtlasSurfaceSubtle else AtlasSurface)
            .border(1.dp, AtlasOutline, RoundedCornerShape(10.dp))
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(modifier = Modifier.weight(1f)) { content() }
        Text(
            code,
            fontFamily = AtlasMono,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = AtlasOnSurfaceMuted,
        )
    }
}

private fun formatMoney(value: Double): String {
    val symbols = DecimalFormatSymbols().apply {
        decimalSeparator = ','
        groupingSeparator = '.'
    }
    return DecimalFormat("#,##0.##", symbols).format(value)
}
