package com.atlas.presentation.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atlas.app.AtlasApplication
import com.atlas.ui.screens.settings.SettingsScreen
import java.io.File
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
            cloudBackupPreferencesRepository = app.container.cloudBackupPreferencesRepository,
            cloudBackupScheduler = app.container.cloudBackupScheduler,
            apiKeyRepository = app.container.apiKeyRepository,
            datasetMetadataDao = app.container.database.datasetMetadataDao(),
        ),
    )
    val uiState by viewModel.uiState.collectAsState()
    val rapidApiKey by viewModel.rapidApiKey.collectAsState()
    val unsplashKey by viewModel.unsplashKey.collectAsState()
    val scope = rememberCoroutineScope()
    var pendingCloudBackupAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        val action = pendingCloudBackupAction
        pendingCloudBackupAction = null
        action?.invoke()
    }

    fun runAfterNotificationPermission(action: () -> Unit) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingCloudBackupAction = action
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            action()
        }
    }

    val cloudFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    persistTreePermission(context.contentResolver, uri)
                    val folderName = withContext(Dispatchers.IO) {
                        resolveTreeDisplayName(context.contentResolver, uri)
                    }
                    runAfterNotificationPermission {
                        viewModel.configureCloudBackup(uri.toString(), folderName)
                    }
                }.onFailure { error ->
                    viewModel.onOperationFailed(
                        error.message ?: "No s'ha pogut accedir a la carpeta seleccionada.",
                    )
                }
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        if (uri != null) {
            viewModel.onOperationStarted()
            scope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { output ->
                            viewModel.exportBackup(output)
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
            viewModel.onOperationStarted()
            scope.launch {
                runCatching {
                    copyUriToTempFile(
                        contentResolver = context.contentResolver,
                        uri = uri,
                        cacheDirectory = context.cacheDir,
                    )
                }.onSuccess { file ->
                    viewModel.previewImport(file)
                }.onFailure { error ->
                    viewModel.onOperationFailed(error.message ?: "No s'ha pogut llegir el fitxer.")
                }
            }
        }
    }

    SettingsScreen(
        uiState = uiState,
        rapidApiKey = rapidApiKey,
        unsplashKey = unsplashKey,
        onBackClick = onBackClick,
        onExportClick = {
            exportLauncher.launch("atlas-backup-${LocalDate.now()}.atlasbackup")
        },
        onImportClick = {
            importLauncher.launch(arrayOf("application/zip", "application/json", "application/octet-stream", "*/*"))
        },
        onChooseCloudBackupFolder = {
            cloudFolderLauncher.launch(null)
        },
        onCloudBackupEnabledChange = viewModel::setCloudBackupEnabled,
        onRunCloudBackupNow = {
            runAfterNotificationPermission(viewModel::runCloudBackupNow)
        },
        onDisconnectCloudBackup = viewModel::disconnectCloudBackup,
        onConfirmImport = viewModel::confirmImport,
        onDismissImport = viewModel::dismissImportPreview,
        onDismissMessage = viewModel::clearMessage,
        onSaveRapidApiKey = viewModel::saveRapidApiKey,
        onSaveUnsplashKey = viewModel::saveUnsplashKey,
    )
}

private suspend fun copyUriToTempFile(
    contentResolver: android.content.ContentResolver,
    uri: Uri,
    cacheDirectory: File,
): File = withContext(Dispatchers.IO) {
    val target = File.createTempFile("atlas-import-", ".backup", cacheDirectory)
    try {
        contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().buffered().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var total = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    total += read
                    require(total <= MAX_IMPORT_FILE_BYTES) { "El fitxer de còpia és massa gran." }
                    output.write(buffer, 0, read)
                }
            }
        } ?: error("No s'ha pogut obrir el fitxer.")
        target
    } catch (error: Throwable) {
        target.delete()
        throw error
    }
}

private const val MAX_IMPORT_FILE_BYTES = 2L * 1024L * 1024L * 1024L

private fun persistTreePermission(
    contentResolver: android.content.ContentResolver,
    uri: Uri,
) {
    contentResolver.takePersistableUriPermission(
        uri,
        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
    )
}

private fun resolveTreeDisplayName(
    contentResolver: android.content.ContentResolver,
    treeUri: Uri,
): String {
    val documentUri = DocumentsContract.buildDocumentUriUsingTree(
        treeUri,
        DocumentsContract.getTreeDocumentId(treeUri),
    )
    return contentResolver.query(
        documentUri,
        arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
        null,
        null,
        null,
    )?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }.orEmpty().ifBlank { "Carpeta al núvol" }
}
