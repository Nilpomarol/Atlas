package com.atlas.ui.screens.itinerary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.atlas.domain.model.Itinerary
import com.atlas.presentation.itinerary.ItineraryEditorDraft
import com.atlas.presentation.itinerary.ItineraryListUiState
import com.atlas.ui.components.AtlasPage
import com.atlas.ui.theme.AtlasAccentContainer
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface

@Composable
fun ItineraryListScreen(
    uiState: ItineraryListUiState,
    onItineraryClick: (String) -> Unit,
    onCreateItineraryClick: () -> Unit,
    onEditItineraryClick: (Itinerary) -> Unit,
    onDeleteItineraryClick: (Itinerary) -> Unit,
    onDismissDraft: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSaveDraft: () -> Unit,
) {
    var itineraryToDelete by remember { mutableStateOf<Itinerary?>(null) }

    AtlasPage(contentPadding = PaddingValues(0.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            ItineraryListHeader(
                count = uiState.itineraries.size,
                onCreateClick = onCreateItineraryClick,
            )

            if (uiState.itineraries.isEmpty()) {
                EmptyItineraryList(onCreateClick = onCreateItineraryClick)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.itineraries, key = { it.id }) { itinerary ->
                        ItineraryCard(
                            itinerary = itinerary,
                            onClick = { onItineraryClick(itinerary.id) },
                            onEditClick = { onEditItineraryClick(itinerary) },
                            onDeleteClick = { itineraryToDelete = itinerary },
                        )
                    }
                }
            }
        }
    }

    if (uiState.draft.isOpen) {
        ItineraryEditorDialog(
            draft = uiState.draft,
            onDismiss = onDismissDraft,
            onTitleChanged = onTitleChanged,
            onNotesChanged = onNotesChanged,
            onSave = onSaveDraft,
        )
    }

    itineraryToDelete?.let { itinerary ->
        AlertDialog(
            onDismissRequest = { itineraryToDelete = null },
            title = { Text("Elimina itinerari") },
            text = { Text("Vols eliminar «${itinerary.title}»? Els grups i els vols associats quedaran sense assignar. Aquesta acció no es pot desfer.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteItineraryClick(itinerary)
                    itineraryToDelete = null
                }) { Text("Elimina") }
            },
            dismissButton = {
                TextButton(onClick = { itineraryToDelete = null }) { Text("Cancel·la") }
            },
        )
    }
}

@Composable
private fun ItineraryListHeader(count: Int, onCreateClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Itineraris",
                style = MaterialTheme.typography.headlineSmall,
                color = AtlasOnSurfaceStrong,
            )
            Text(
                text = "$count ${if (count == 1) "itinerari" else "itineraris"}",
                style = MaterialTheme.typography.labelMedium,
                color = AtlasOnSurfaceMuted,
            )
        }
        Button(
            onClick = onCreateClick,
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AtlasAccentContainer,
                contentColor = AtlasPrimary,
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(text = "Nou", modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Composable
private fun ItineraryCard(
    itinerary: Itinerary,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = AtlasSurface,
        border = BorderStroke(1.dp, AtlasOutline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = itinerary.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AtlasOnSurfaceStrong,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!itinerary.notes.isNullOrBlank()) {
                    Text(
                        text = itinerary.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = AtlasOnSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Edita",
                    tint = AtlasOnSurfaceMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
            IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Elimina",
                    tint = AtlasOnSurfaceMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun EmptyItineraryList(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Encara no hi ha cap itinerari.",
            style = MaterialTheme.typography.titleMedium,
            color = AtlasOnSurfaceStrong,
        )
        Text(
            text = "Crea un itinerari per agrupar vols en trajectes i evitar comptar les escales com a destinacions.",
            style = MaterialTheme.typography.bodyMedium,
            color = AtlasOnSurfaceMuted,
        )
        Button(onClick = onCreateClick, shape = RoundedCornerShape(999.dp)) {
            Text(text = "Nou itinerari")
        }
    }
}

@Composable
fun ItineraryEditorDialog(
    draft: ItineraryEditorDraft,
    onDismiss: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (draft.itineraryId == null) "Nou itinerari" else "Edita itinerari")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = draft.title,
                    onValueChange = onTitleChanged,
                    label = { Text("Títol") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = draft.validationError != null,
                )
                OutlinedTextField(
                    value = draft.notes,
                    onValueChange = onNotesChanged,
                    label = { Text("Notes (opcional)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (draft.validationError != null) {
                    Text(
                        text = draft.validationError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onSave) { Text("Desa") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel·la") } },
    )
}
