package com.atlas.ui.screens.country

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlas.domain.model.CountryLogType
import com.atlas.domain.model.DatePrecision
import com.atlas.presentation.country.CountryLogDraftUiState
import com.atlas.presentation.date.FlexibleDateRangeDraftField
import com.atlas.ui.components.date.FlexibleDateRangeField

// Mirrors the palette from CountryDetailScreen / FlexibleDateRangeField
private val DialogInk      = Color(0xFF111827)
private val DialogMuted    = Color(0xFF6B7280)
private val DialogBg       = Color(0xFFF1F3F7)
private val DialogCard     = Color(0xFFFFFFFF)
private val DialogBorder   = Color(0xFFE4E8EF)
private val DialogAccent   = Color(0xFF005C38)
private val DialogVisitLt  = Color(0xFFC6EAD8)  // visit  — green tint
private val DialogLivedLt  = Color(0xFFDDD1F7)  // lived  — violet tint
private val DialogLivedClr = Color(0xFF4C1D95)

@Composable
fun CountryLogDialog(
    draft: CountryLogDraftUiState,
    onDismiss: () -> Unit,
    onTypeChanged: (CountryLogType) -> Unit,
    onPrecisionChanged: (DatePrecision) -> Unit,
    onFieldChanged: (FlexibleDateRangeDraftField, String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = DialogCard,
        title = {
            Text(
                text = if (draft.logId == null) "Afegeix registre" else "Edita registre",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = DialogInk,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                // Type selector — uses per-type accent colours
                LogTypeSelector(
                    selectedType = draft.type,
                    onTypeChanged = onTypeChanged,
                )

                // Title field
                OutlinedTextField(
                    value = draft.notes,
                    onValueChange = onNotesChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            "Títol",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                )

                // Date range — hint hidden since it's inside a dialog
                FlexibleDateRangeField(
                    draft = draft.dateRange,
                    onPrecisionChanged = onPrecisionChanged,
                    onFieldChanged = onFieldChanged,
                    showHint = false,
                )

                // Validation error
                draft.validationError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            CompactDialogActionButton(onClick = onSave) {
                Text(
                    text = "Desa",
                    fontWeight = FontWeight.ExtraBold,
                    color = DialogAccent,
                )
            }
        },
        dismissButton = {
            CompactDialogActionButton(onClick = onDismiss) {
                Text(
                    text = "Cancel·la",
                    fontWeight = FontWeight.Bold,
                    color = DialogMuted,
                )
            }
        },
    )
}

@Composable
private fun CompactDialogActionButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.height(34.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
    ) {
        content()
    }
}

// ─────────────────────────────────────────────
// Type selector
// Each segment uses its own type-specific accent
// so Visita = green, Viscut = violet — matching
// the history row colours in CountryDetailScreen.
// ─────────────────────────────────────────────
@Composable
private fun LogTypeSelector(
    selectedType: CountryLogType,
    onTypeChanged: (CountryLogType) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DialogBg)
            .border(1.dp, DialogBorder, RoundedCornerShape(10.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        TypeSegment(
            modifier = Modifier.weight(1f),
            label = "Visita",
            selected = selectedType == CountryLogType.VISIT,
            activeColor = DialogAccent,
            activeLightColor = DialogVisitLt,
            onClick = { onTypeChanged(CountryLogType.VISIT) },
        )
        TypeSegment(
            modifier = Modifier.weight(1f),
            label = "Viscut",
            selected = selectedType == CountryLogType.LIVED,
            activeColor = DialogLivedClr,
            activeLightColor = DialogLivedLt,
            onClick = { onTypeChanged(CountryLogType.LIVED) },
        )
    }
}

@Composable
private fun TypeSegment(
    modifier: Modifier,
    label: String,
    selected: Boolean,
    activeColor: Color,
    activeLightColor: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    selected -> activeColor
                    else     -> Color.Transparent
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = when {
                    selected -> Color.White
                    else     -> DialogMuted
                },
                containerColor = Color.Transparent,
            ),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        ) {
            Text(
                text = label,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                fontSize = 13.sp,
            )
        }
    }
}
