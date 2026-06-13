package com.atlas.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.atlas.domain.repository.BackupImportPreview
import com.atlas.presentation.settings.DatasetVersionInfo
import com.atlas.presentation.settings.SettingsUiState
import com.atlas.ui.components.AtlasCard
import com.atlas.ui.components.AtlasDot
import com.atlas.ui.components.AtlasPage
import com.atlas.ui.components.AtlasSectionLabel
import com.atlas.ui.theme.AtlasNavy
import com.atlas.ui.theme.AtlasOnSurfaceMuted
import com.atlas.ui.theme.AtlasOnSurfaceStrong
import com.atlas.ui.theme.AtlasOutline
import com.atlas.ui.theme.AtlasPrimary
import com.atlas.ui.theme.AtlasSurface
import com.atlas.ui.theme.AtlasVisited

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    rapidApiKey: String,
    unsplashKey: String,
    onBackClick: () -> Unit = {},
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onConfirmImport: () -> Unit,
    onDismissImport: () -> Unit,
    onDismissMessage: () -> Unit,
    onSaveRapidApiKey: (String) -> Unit,
    onSaveUnsplashKey: (String) -> Unit,
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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // ── Page header ──────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Enrere", tint = AtlasOnSurfaceStrong)
                }
                Text(
                    text = "Configuració",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AtlasOnSurfaceStrong,
                )
            }

            // ── Backup ───────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AtlasSectionLabel("Còpia de seguretat")
                BackupCard(isBusy = uiState.isBusy, onExportClick = onExportClick, onImportClick = onImportClick)
            }

            // ── API key ──────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AtlasSectionLabel("Integracions")
                ApiKeyCard(savedKey = rapidApiKey, onSave = onSaveRapidApiKey)
                UnsplashKeyCard(savedKey = unsplashKey, onSave = onSaveUnsplashKey)
            }

            // ── Dataset health ───────────────────────────────────────────────
            if (uiState.datasetVersions.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AtlasSectionLabel("Estat de les dades")
                    DatasetVersionsCard(versions = uiState.datasetVersions)
                }
            }

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

// ── Backup card ───────────────────────────────────────────────────────────────

@Composable
private fun BackupCard(isBusy: Boolean, onExportClick: () -> Unit, onImportClick: () -> Unit) {
    AtlasCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            CardHeader(
                icon = Icons.Filled.Backup,
                title = "Còpia de seguretat",
                subtitle = "Format JSON · v2",
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
                    colors = ButtonDefaults.buttonColors(containerColor = AtlasPrimary, contentColor = AtlasSurface),
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null)
                    Text(text = "Exporta", modifier = Modifier.padding(start = 8.dp))
                }
                OutlinedButton(
                    onClick = onImportClick,
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AtlasPrimary),
                ) {
                    Icon(Icons.Filled.Upload, contentDescription = null)
                    Text(text = "Importa", modifier = Modifier.padding(start = 8.dp))
                }
            }
            if (isBusy) {
                Text("Treballant...", style = MaterialTheme.typography.bodySmall, color = AtlasOnSurfaceMuted)
            }
        }
    }
}

// ── API key card ──────────────────────────────────────────────────────────────

@Composable
private fun ApiKeyCard(savedKey: String, onSave: (String) -> Unit) {
    var draft by rememberSaveable(savedKey) { mutableStateOf(savedKey) }
    var showKey by remember { mutableStateOf(false) }

    AtlasCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            CardHeader(
                icon = Icons.Filled.VpnKey,
                title = "Integracions",
                subtitle = "AeroDataBox · RapidAPI",
            )
            Text(
                text = "Clau d'API de RapidAPI per cercar vols per número. Cada persona que utilitzi l'app necessita la seva pròpia clau gratuïta.",
                style = MaterialTheme.typography.bodyMedium,
                color = AtlasOnSurfaceMuted,
            )
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Clau RapidAPI") },
                placeholder = { Text("Enganxa la clau aquí", color = AtlasOnSurfaceMuted) },
                singleLine = true,
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { showKey = !showKey }) {
                        Icon(
                            imageVector = if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showKey) "Amaga" else "Mostra",
                            tint = AtlasOnSurfaceMuted,
                        )
                    }
                },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { onSave(draft) },
                    enabled = draft != savedKey,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AtlasPrimary, contentColor = AtlasSurface),
                ) {
                    Text("Desa clau")
                }
                if (savedKey.isNotBlank()) {
                    OutlinedButton(
                        onClick = { draft = ""; onSave("") },
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AtlasPrimary),
                    ) {
                        Text("Elimina")
                    }
                }
                if (savedKey.isNotBlank()) {
                    Text(
                        text = "Clau configurada ✓",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AtlasPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun UnsplashKeyCard(savedKey: String, onSave: (String) -> Unit) {
    var draft by rememberSaveable(savedKey) { mutableStateOf(savedKey) }
    var showKey by remember { mutableStateOf(false) }

    AtlasCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            CardHeader(
                icon = Icons.Filled.Image,
                title = "Fotos dels països",
                subtitle = "Unsplash",
            )
            Text(
                text = "Clau d'accés d'Unsplash per mostrar una foto de cada país, que es renova cada dia. És gratuïta a unsplash.com/developers.",
                style = MaterialTheme.typography.bodyMedium,
                color = AtlasOnSurfaceMuted,
            )
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Clau d'accés Unsplash") },
                placeholder = { Text("Enganxa la clau aquí", color = AtlasOnSurfaceMuted) },
                singleLine = true,
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { showKey = !showKey }) {
                        Icon(
                            imageVector = if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showKey) "Amaga" else "Mostra",
                            tint = AtlasOnSurfaceMuted,
                        )
                    }
                },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { onSave(draft) },
                    enabled = draft != savedKey,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AtlasPrimary, contentColor = AtlasSurface),
                ) {
                    Text("Desa clau")
                }
                if (savedKey.isNotBlank()) {
                    OutlinedButton(
                        onClick = { draft = ""; onSave("") },
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AtlasPrimary),
                    ) {
                        Text("Elimina")
                    }
                }
                if (savedKey.isNotBlank()) {
                    Text(
                        text = "Clau configurada ✓",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AtlasPrimary,
                    )
                }
            }
        }
    }
}

// ── Dataset versions card ─────────────────────────────────────────────────────

@Composable
private fun DatasetVersionsCard(versions: List<DatasetVersionInfo>) {
    AtlasCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AtlasNavy),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Layers, contentDescription = null, tint = AtlasSurface, modifier = Modifier.size(22.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Conjunts de dades", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AtlasOnSurfaceStrong)
                    Text("VERSIONS INSTAL·LADES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AtlasOnSurfaceMuted)
                }
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AtlasOutline))
            versions.forEachIndexed { index, info ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AtlasDot(color = AtlasVisited)
                    Text(
                        text = info.name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AtlasOnSurfaceStrong,
                    )
                    Text(
                        text = info.version,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AtlasOnSurfaceMuted,
                    )
                }
                if (index < versions.lastIndex) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AtlasOutline))
                }
            }
        }
    }
}

// ── Shared card header ────────────────────────────────────────────────────────

@Composable
private fun CardHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AtlasNavy),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = AtlasSurface, modifier = Modifier.size(22.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AtlasOnSurfaceStrong)
            Text(subtitle.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AtlasOnSurfaceMuted)
        }
    }
}

// ── Import confirmation dialog ────────────────────────────────────────────────

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
            Text(text = "Substituir dades?", fontWeight = FontWeight.ExtraBold, color = AtlasOnSurfaceStrong)
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
            TextButton(onClick = onConfirm) { Text("Importa", fontWeight = FontWeight.ExtraBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel·la") }
        },
    )
}
