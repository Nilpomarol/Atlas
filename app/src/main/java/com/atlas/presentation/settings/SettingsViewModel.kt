package com.atlas.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atlas.core.constants.DatasetConstants
import com.atlas.data.local.dao.DatasetMetadataDao
import com.atlas.domain.repository.ApiKeyRepository
import com.atlas.domain.repository.BackupImportPreview
import com.atlas.domain.repository.BackupRepository
import com.atlas.domain.repository.CloudBackupPreferencesRepository
import com.atlas.domain.repository.CloudBackupScheduler
import com.atlas.domain.repository.CloudBackupWorkStatus
import java.io.File
import java.io.OutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DatasetVersionInfo(val name: String, val version: String)

data class CloudBackupUiState(
    val isConfigured: Boolean = false,
    val isEnabled: Boolean = false,
    val folderName: String = "",
    val statusText: String = "Encara no s'ha creat cap còpia al núvol.",
    val errorText: String? = null,
    val workStatus: CloudBackupWorkStatus = CloudBackupWorkStatus.IDLE,
    val lastSuccessfulBackupAt: String? = null,
)

data class SettingsUiState(
    val isBusy: Boolean = false,
    val message: String? = null,
    val pendingImportPreview: BackupImportPreview? = null,
    val datasetVersions: List<DatasetVersionInfo> = emptyList(),
    val cloudBackup: CloudBackupUiState = CloudBackupUiState(),
)

class SettingsViewModel(
    private val backupRepository: BackupRepository,
    private val cloudBackupPreferencesRepository: CloudBackupPreferencesRepository,
    private val cloudBackupScheduler: CloudBackupScheduler,
    private val apiKeyRepository: ApiKeyRepository,
    private val datasetMetadataDao: DatasetMetadataDao,
) : ViewModel() {

    val rapidApiKey: StateFlow<String> = apiKeyRepository.observeRapidApiKey()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val unsplashKey: StateFlow<String> = apiKeyRepository.observeUnsplashKey()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val mutableUiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = mutableUiState.asStateFlow()

    private var pendingImportFile: File? = null

    init {
        viewModelScope.launch {
            val rows = datasetMetadataDao.getAll()
            val versions = rows.map { DatasetVersionInfo(it.key.toDatasetDisplayName(), it.version) }
            mutableUiState.update { it.copy(datasetVersions = versions) }
        }
        viewModelScope.launch {
            cloudBackupPreferencesRepository.observeSettings().collect { settings ->
                mutableUiState.update { state ->
                    state.copy(
                        cloudBackup = CloudBackupUiState(
                            isConfigured = !settings.folderUri.isNullOrBlank(),
                            isEnabled = settings.enabled,
                            folderName = settings.folderName.orEmpty(),
                            statusText = settings.lastSuccessfulBackupAt.toCloudBackupStatus(
                                state.cloudBackup.workStatus,
                            ),
                            errorText = settings.lastError,
                            workStatus = state.cloudBackup.workStatus,
                            lastSuccessfulBackupAt = settings.lastSuccessfulBackupAt,
                        ),
                    )
                }
            }
        }
        viewModelScope.launch {
            cloudBackupScheduler.observeWorkStatus().collect { workStatus ->
                mutableUiState.update { state ->
                    state.copy(
                        cloudBackup = state.cloudBackup.copy(
                            workStatus = workStatus,
                            statusText = state.cloudBackup.lastSuccessfulBackupAt
                                .toCloudBackupStatus(workStatus),
                        ),
                    )
                }
            }
        }
    }

    fun saveRapidApiKey(key: String) {
        viewModelScope.launch { apiKeyRepository.saveRapidApiKey(key) }
    }

    fun saveUnsplashKey(key: String) {
        viewModelScope.launch { apiKeyRepository.saveUnsplashKey(key) }
    }

    fun configureCloudBackup(folderUri: String, folderName: String) {
        viewModelScope.launch {
            runCatching {
                cloudBackupPreferencesRepository.configureFolder(folderUri, folderName)
                cloudBackupScheduler.scheduleMonthly()
                cloudBackupScheduler.runNow()
            }.onSuccess {
                mutableUiState.update {
                    it.copy(message = "Carpeta configurada. S'ha programat la primera còpia.")
                }
            }.onFailure { error ->
                onOperationFailed(error.message ?: "No s'ha pogut configurar la carpeta.")
            }
        }
    }

    fun setCloudBackupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            cloudBackupPreferencesRepository.setEnabled(enabled)
            if (enabled) {
                cloudBackupScheduler.scheduleMonthly()
            } else {
                cloudBackupScheduler.cancel()
            }
        }
    }

    fun runCloudBackupNow() {
        cloudBackupScheduler.runNow()
        mutableUiState.update {
            it.copy(
                cloudBackup = it.cloudBackup.copy(
                    workStatus = CloudBackupWorkStatus.QUEUED,
                    statusText = QUEUED_CLOUD_BACKUP_STATUS,
                ),
            )
        }
    }

    fun disconnectCloudBackup() {
        viewModelScope.launch {
            cloudBackupScheduler.cancel()
            cloudBackupPreferencesRepository.clear()
            mutableUiState.update { it.copy(message = "Còpia al núvol desconnectada.") }
        }
    }

    suspend fun exportBackup(output: OutputStream) {
        backupRepository.exportBackup(output)
    }

    fun onOperationStarted() {
        mutableUiState.update { it.copy(isBusy = true, message = null) }
    }

    fun onExportFinished() {
        mutableUiState.update { it.copy(isBusy = false, message = "Còpia exportada correctament.") }
    }

    fun onOperationFailed(message: String) {
        clearPendingImportFile()
        mutableUiState.update { it.copy(isBusy = false, message = message, pendingImportPreview = null) }
    }

    fun previewImport(file: File) {
        clearPendingImportFile()
        mutableUiState.update { it.copy(isBusy = true, message = null) }
        viewModelScope.launch {
            runCatching {
                backupRepository.previewImport(file)
            }.onSuccess { preview ->
                pendingImportFile = file
                mutableUiState.update { it.copy(isBusy = false, pendingImportPreview = preview) }
            }.onFailure { error ->
                file.delete()
                onOperationFailed(error.message ?: "La còpia no es pot importar.")
            }
        }
    }

    fun confirmImport() {
        val file = pendingImportFile ?: return
        mutableUiState.update { it.copy(isBusy = true, message = null) }
        viewModelScope.launch {
            runCatching {
                backupRepository.importBackup(file)
            }.onSuccess {
                clearPendingImportFile()
                mutableUiState.update {
                    it.copy(isBusy = false, message = "Còpia importada correctament.", pendingImportPreview = null)
                }
            }.onFailure { error ->
                onOperationFailed(error.message ?: "La còpia no es pot importar.")
            }
        }
    }

    fun dismissImportPreview() {
        clearPendingImportFile()
        mutableUiState.update { it.copy(pendingImportPreview = null) }
    }

    fun clearMessage() {
        mutableUiState.update { it.copy(message = null) }
    }

    override fun onCleared() {
        clearPendingImportFile()
        super.onCleared()
    }

    private fun clearPendingImportFile() {
        pendingImportFile?.delete()
        pendingImportFile = null
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val backupRepository: BackupRepository,
        private val cloudBackupPreferencesRepository: CloudBackupPreferencesRepository,
        private val cloudBackupScheduler: CloudBackupScheduler,
        private val apiKeyRepository: ApiKeyRepository,
        private val datasetMetadataDao: DatasetMetadataDao,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(
                backupRepository = backupRepository,
                cloudBackupPreferencesRepository = cloudBackupPreferencesRepository,
                cloudBackupScheduler = cloudBackupScheduler,
                apiKeyRepository = apiKeyRepository,
                datasetMetadataDao = datasetMetadataDao,
            ) as T
    }
}

private fun String?.toCloudBackupStatus(workStatus: CloudBackupWorkStatus): String {
    when (workStatus) {
        CloudBackupWorkStatus.QUEUED -> return QUEUED_CLOUD_BACKUP_STATUS
        CloudBackupWorkStatus.RUNNING -> return RUNNING_CLOUD_BACKUP_STATUS
        CloudBackupWorkStatus.IDLE -> Unit
    }
    if (this.isNullOrBlank()) return "Encara no s'ha creat cap còpia al núvol."
    return runCatching {
        val formatted = CLOUD_BACKUP_DATE_FORMATTER.format(Instant.parse(this))
        "Última còpia: $formatted"
    }.getOrElse {
        "Última còpia completada."
    }
}

private const val QUEUED_CLOUD_BACKUP_STATUS =
    "Còpia pendent. Començarà quan hi hagi connexió."
private const val RUNNING_CLOUD_BACKUP_STATUS =
    "Creant i pujant la còpia… Pot trigar uns minuts."

private val CLOUD_BACKUP_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.forLanguageTag("ca"))
        .withZone(ZoneId.systemDefault())

private fun String.toDatasetDisplayName(): String = when (this) {
    DatasetConstants.COUNTRIES_KEY -> "Països i territoris"
    DatasetConstants.AIRPORTS_KEY -> "Aeroports"
    DatasetConstants.AIRLINES_KEY -> "Aerolínies"
    DatasetConstants.AIRCRAFT_TYPES_KEY -> "Aeronaus"
    else -> this
}
