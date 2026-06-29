package com.atlas.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Public
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import com.atlas.domain.model.CountryStatsScope
import com.atlas.domain.repository.BackupImportPreview
import com.atlas.presentation.settings.DatasetVersionInfo
import com.atlas.presentation.settings.SettingsUiState
import com.atlas.domain.repository.CloudBackupWorkStatus
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
    onChooseCloudBackupFolder: () -> Unit,
    onCloudBackupEnabledChange: (Boolean) -> Unit,
    onRunCloudBackupNow: () -> Unit,
    onDisconnectCloudBackup: () -> Unit,
    onConfirmImport: () -> Unit,
    onDismissImport: () -> Unit,
    onDismissMessage: () -> Unit,
    onSaveRapidApiKey: (String) -> Unit,
    onSaveUnsplashKey: (String) -> Unit,
    onCountryStatsScopeChange: (CountryStatsScope) -> Unit,
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
                CloudBackupCard(
                    state = uiState.cloudBackup,
                    onChooseFolder = onChooseCloudBackupFolder,
                    onEnabledChange = onCloudBackupEnabledChange,
                    onRunNow = onRunCloudBackupNow,
                    onDisconnect = onDisconnectCloudBackup,
                )
            }

            // ── API key ──────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AtlasSectionLabel("Estadístiques")
                CountryStatsScopeCard(
                    selectedScope = uiState.countryStatsScope,
                    onScopeChange = onCountryStatsScopeChange,
                )
            }

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
private fun CountryStatsScopeCard(
    selectedScope: CountryStatsScope,
    onScopeChange: (CountryStatsScope) -> Unit,
) {
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
                    Icon(
                        Icons.Filled.Public,
                        contentDescription = null,
                        tint = AtlasSurface,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Abast del recompte",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AtlasOnSurfaceStrong,
                    )
                    Text(
                        text = "NOMÉS AFECTA LES ESTADÍSTIQUES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AtlasOnSurfaceMuted,
                    )
                }
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AtlasOutline))
            CountryStatsScope.values().forEachIndexed { index, scope ->
                CountryStatsScopeRow(
                    scope = scope,
                    selected = scope == selectedScope,
                    onClick = { onScopeChange(scope) },
                )
                if (index < CountryStatsScope.values().lastIndex) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(AtlasOutline))
                }
            }
        }
    }
}

@Composable
private fun CountryStatsScopeRow(
    scope: CountryStatsScope,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = scope.settingsTitle(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = AtlasOnSurfaceStrong,
            )
            Text(
                text = scope.settingsDescription(),
                style = MaterialTheme.typography.bodySmall,
                color = AtlasOnSurfaceMuted,
            )
        }
    }
}

private fun CountryStatsScope.settingsTitle(): String = when (this) {
    CountryStatsScope.UN_195 -> "ONU 195"
    CountryStatsScope.UN_PLUS_KOSOVO_TAIWAN_197 -> "ONU + Kosovo + Taiwan 197"
    CountryStatsScope.ALL_ATLAS -> "ONU + territoris"
}

private fun CountryStatsScope.settingsDescription(): String = when (this) {
    CountryStatsScope.UN_195 -> "Membres de l'ONU, Vaticà i Palestina."
    CountryStatsScope.UN_PLUS_KOSOVO_TAIWAN_197 -> "Afegeix Kosovo i Taiwan al recompte."
    CountryStatsScope.ALL_ATLAS -> "Tots els països i territoris disponibles a Atlas."
}

@Composable
private fun BackupCard(isBusy: Boolean, onExportClick: () -> Unit, onImportClick: () -> Unit) {
    AtlasCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            CardHeader(
                icon = Icons.Filled.Backup,
                title = "Còpia de seguretat",
                subtitle = "Arxiu Atlas · v3",
            )
            Text(
                text = "Exporta o restaura les dades personals d'Atlas, incloses les fotos.",
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

@Composable
private fun CloudBackupCard(
    state: com.atlas.presentation.settings.CloudBackupUiState,
    onChooseFolder: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onRunNow: () -> Unit,
    onDisconnect: () -> Unit,
) {
    val isActive = state.workStatus != CloudBackupWorkStatus.IDLE
    val isRunning = state.workStatus == CloudBackupWorkStatus.RUNNING
    AtlasCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            CardHeader(
                icon = Icons.Filled.CloudUpload,
                title = "Còpia al núvol",
                subtitle = "Drive o fitxers · mensual",
            )
            Text(
                text = "Selecciona una carpeta de Google Drive o d'un altre proveïdor compatible. Atlas conserva les tres còpies automàtiques més recents.",
                style = MaterialTheme.typography.bodyMedium,
                color = AtlasOnSurfaceMuted,
            )

            if (!state.isConfigured) {
                Button(
                    onClick = onChooseFolder,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AtlasPrimary,
                        contentColor = AtlasSurface,
                    ),
                ) {
                    Icon(Icons.Filled.FolderOpen, contentDescription = null)
                    Text(text = "Tria una carpeta", modifier = Modifier.padding(start = 8.dp))
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = state.folderName.ifBlank { "Carpeta al núvol" },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AtlasOnSurfaceStrong,
                        )
                        Text(
                            text = if (state.isEnabled) "Còpia mensual activa" else "Còpia automàtica pausada",
                            style = MaterialTheme.typography.bodySmall,
                            color = AtlasOnSurfaceMuted,
                        )
                    }
                    Switch(
                        checked = state.isEnabled,
                        onCheckedChange = onEnabledChange,
                        enabled = !isRunning,
                    )
                }

                if (isRunning) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = AtlasPrimary,
                    )
                }
                Text(
                    text = state.statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = AtlasOnSurfaceMuted,
                )
                state.errorText?.takeIf { !isActive }?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = AtlasPrimary,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = onRunNow,
                        enabled = state.isEnabled && !isActive,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AtlasPrimary,
                            contentColor = AtlasSurface,
                        ),
                    ) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null)
                        Text(
                            text = when (state.workStatus) {
                                CloudBackupWorkStatus.QUEUED -> "En espera"
                                CloudBackupWorkStatus.RUNNING -> "Pujant…"
                                CloudBackupWorkStatus.IDLE -> "Còpia ara"
                            },
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    OutlinedButton(
                        onClick = onChooseFolder,
                        enabled = !isRunning,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AtlasPrimary),
                    ) {
                        Icon(Icons.Filled.FolderOpen, contentDescription = null)
                        Text(text = "Canvia", modifier = Modifier.padding(start = 8.dp))
                    }
                }
                TextButton(
                    onClick = onDisconnect,
                    enabled = !isRunning,
                ) {
                    Text("Desconnecta la carpeta")
                }
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
                Text("Fotos: ${preview.photoCount}")
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
