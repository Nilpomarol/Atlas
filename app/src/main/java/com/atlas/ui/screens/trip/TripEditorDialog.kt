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
        shape = RoundedCornerShape(22.dp),
        title = {
            Text(
                text = if (draft.tripId == null) {
                    "Nou viatge"
                } else {
                    "Edita viatge"
                },
                fontWeight = FontWeight.ExtraBold,
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
                        FilterChip(
                            selected = draft.status == status,
                            onClick = { onStatusChanged(status) },
                            label = {
                                Text(text = status.toCatalanLabel())
                            },
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
                Text(text = "Desa", fontWeight = FontWeight.ExtraBold)
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
