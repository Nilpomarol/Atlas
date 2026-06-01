package com.atlas.ui.screens.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.DatePrecision
import com.atlas.domain.model.TravelStatus
import com.atlas.presentation.date.FlexibleDateRangeDraftField
import com.atlas.presentation.trip.TripEditorDraftUiState
import com.atlas.ui.components.date.FlexibleDateRangeField
import com.atlas.ui.components.tripStatusColors
import com.atlas.ui.theme.AtlasAccent
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasSurface

@Composable
fun TripEditorDialog(
    draft: TripEditorDraftUiState,
    onDismiss: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onStatusChanged: (TravelStatus) -> Unit,
    onDatePrecisionChanged: (DatePrecision) -> Unit,
    onDateFieldChanged: (FlexibleDateRangeDraftField, String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = AtlasSurface,
        title = {
            Text(
                text = if (draft.tripId == null) {
                    "Nou viatge"
                } else {
                    "Edita viatge"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceStrong,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = draft.title,
                    onValueChange = onTitleChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = {
                        Text(text = "Títol")
                    },
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(TravelStatus.entries) { status ->
                        val statusColors = status.tripStatusColors()
                        val selected = draft.status == status
                        FilterChip(
                            selected = selected,
                            onClick = { onStatusChanged(status) },
                            label = {
                                Text(
                                    text = status.toCatalanLabel(),
                                    fontWeight = FontWeight.Bold,
                                )
                            },
                            shape = RoundedCornerShape(999.dp),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = statusColors.foreground.copy(alpha = 0.22f),
                                selectedBorderColor = statusColors.foreground.copy(alpha = 0.22f),
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = AtlasSurface,
                                labelColor = AtlasOnSurfaceMuted,
                                selectedContainerColor = statusColors.container,
                                selectedLabelColor = statusColors.foreground,
                            ),
                        )
                    }
                }

                FlexibleDateRangeField(
                    draft = draft.dateRange,
                    onPrecisionChanged = onDatePrecisionChanged,
                    onFieldChanged = onDateFieldChanged,
                    showHint = false,
                )

                OutlinedTextField(
                    value = draft.notes,
                    onValueChange = onNotesChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "Notes")
                    },
                    minLines = 1,
                    maxLines = 3,
                )

                draft.validationError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            CompactTripDialogActionButton(onClick = onSave) {
                Text(text = "Desa", fontWeight = FontWeight.ExtraBold, color = AtlasAccent)
            }
        },
        dismissButton = {
            CompactTripDialogActionButton(onClick = onDismiss) {
                Text(text = "Cancel·la")
            }
        },
    )
}

@Composable
fun CompactTripDialogActionButton(
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
