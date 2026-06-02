package com.atlas.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.atlas.domain.repository.BackupImportPreview
import com.atlas.presentation.settings.SettingsUiState
import com.atlas.ui.components.AtlasCard
import com.atlas.ui.components.AtlasPage
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onConfirmImport: () -> Unit,
    onDismissImport: () -> Unit,
    onDismissMessage: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    uiState.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            onDismissMessage()
        }
    }

    AtlasPage {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = "Configuració",
                style = MaterialTheme.typography.headlineMedium,
                color = AtlasOnSurfaceStrong,
            )

            BackupCard(
                isBusy = uiState.isBusy,
                onExportClick = onExportClick,
                onImportClick = onImportClick,
            )

            Spacer(modifier = Modifier.weight(1f))
            SnackbarHost(hostState = snackbarHostState)
        }
    }

    uiState.pendingImportPreview?.let { preview ->
        ImportConfirmationDialog(
            preview = preview,
            onConfirm = onConfirmImport,
            onDismiss = onDismissImport,
        )
    }
}

@Composable
private fun BackupCard(
    isBusy: Boolean,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
) {
    AtlasCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Còpia de seguretat",
                style = MaterialTheme.typography.titleLarge,
                color = AtlasOnSurfaceStrong,
            )
            Text(
                text = "Exporta o restaura les dades personals d'Atlas en format JSON.",
                style = MaterialTheme.typography.bodyMedium,
                color = AtlasOnSurfaceMuted,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onExportClick,
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AtlasPrimary,
                        contentColor = AtlasSurface,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = null,
                    )
                    Text(
                        text = "Exporta",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                OutlinedButton(
                    onClick = onImportClick,
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AtlasPrimary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Upload,
                        contentDescription = null,
                    )
                    Text(
                        text = "Importa",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
            if (isBusy) {
                Text(
                    text = "Treballant...",
                    style = MaterialTheme.typography.bodySmall,
                    color = AtlasOnSurfaceMuted,
                )
            }
        }
    }
}

@Composable
private fun ImportConfirmationDialog(
    preview: BackupImportPreview,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AtlasSurface,
        title = {
            Text(
                text = "Substituir dades?",
                fontWeight = FontWeight.ExtraBold,
                color = AtlasOnSurfaceStrong,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Aquesta importació substituirà les dades personals actuals.")
                Spacer(modifier = Modifier.height(4.dp))
                Text("Estats de país: ${preview.countryUserStateCount}")
                Text("Registres: ${preview.countryLogCount}")
                Text("Viatges: ${preview.tripCount}")
                Text("Parades: ${preview.tripStopCount}")
                if (preview.flightCount > 0) Text("Vols: ${preview.flightCount}")
                if (preview.itineraryCount > 0) Text("Itineraris: ${preview.itineraryCount}")
                if (preview.excursionCount > 0) Text("Excursions: ${preview.excursionCount}")
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Importa", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel·la")
            }
        },
    )
}
