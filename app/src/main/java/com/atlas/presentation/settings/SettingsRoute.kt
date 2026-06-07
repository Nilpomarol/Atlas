package com.atlas.presentation.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atlas.app.AtlasApplication
import com.atlas.ui.screens.settings.SettingsScreen
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsRoute(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val app = context.applicationContext as AtlasApplication
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(
            backupRepository = app.container.backupRepository,
            apiKeyRepository = app.container.apiKeyRepository,
            datasetMetadataDao = app.container.database.datasetMetadataDao(),
        ),
    )
    val uiState by viewModel.uiState.collectAsState()
    val rapidApiKey by viewModel.rapidApiKey.collectAsState()
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val json = viewModel.buildBackupJson()
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { output ->
                            output.writer().use { writer -> writer.write(json) }
                        } ?: error("No s'ha pogut obrir el fitxer de sortida.")
                    }
                }.onSuccess {
                    viewModel.onExportFinished()
                }.onFailure { error ->
                    viewModel.onOperationFailed(error.message ?: "No s'ha pogut exportar la còpia.")
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    readTextFromUri(context.contentResolver, uri)
                }.onSuccess { json ->
                    viewModel.previewImport(json)
                }.onFailure { error ->
                    viewModel.onOperationFailed(error.message ?: "No s'ha pogut llegir el fitxer.")
                }
            }
        }
    }

    SettingsScreen(
        uiState = uiState,
        rapidApiKey = rapidApiKey,
        onBackClick = onBackClick,
        onExportClick = {
            exportLauncher.launch("atlas-backup-${LocalDate.now()}.json")
        },
        onImportClick = {
            importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
        },
        onConfirmImport = viewModel::confirmImport,
        onDismissImport = viewModel::dismissImportPreview,
        onDismissMessage = viewModel::clearMessage,
        onSaveRapidApiKey = viewModel::saveRapidApiKey,
    )
}

private suspend fun readTextFromUri(
    contentResolver: android.content.ContentResolver,
    uri: Uri,
): String = withContext(Dispatchers.IO) {
    contentResolver.openInputStream(uri)?.use { input ->
        input.reader().use { reader -> reader.readText() }
    } ?: error("No s'ha pogut obrir el fitxer.")
}
