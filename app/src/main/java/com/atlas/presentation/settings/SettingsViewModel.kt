package com.atlas.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.atlas.core.constants.DatasetConstants
import com.atlas.data.local.dao.DatasetMetadataDao
import com.atlas.domain.repository.ApiKeyRepository
import com.atlas.domain.repository.BackupImportPreview
import com.atlas.domain.repository.BackupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DatasetVersionInfo(val name: String, val version: String)

data class SettingsUiState(
    val isBusy: Boolean = false,
    val message: String? = null,
    val pendingImportPreview: BackupImportPreview? = null,
    val datasetVersions: List<DatasetVersionInfo> = emptyList(),
)

class SettingsViewModel(
    private val backupRepository: BackupRepository,
    private val apiKeyRepository: ApiKeyRepository,
    private val datasetMetadataDao: DatasetMetadataDao,
) : ViewModel() {

    val rapidApiKey: StateFlow<String> = apiKeyRepository.observeRapidApiKey()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val unsplashKey: StateFlow<String> = apiKeyRepository.observeUnsplashKey()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val mutableUiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = mutableUiState.asStateFlow()

    private var pendingImportJson: String? = null

    init {
        viewModelScope.launch {
            val rows = datasetMetadataDao.getAll()
            val versions = rows.map { DatasetVersionInfo(it.key.toDatasetDisplayName(), it.version) }
            mutableUiState.update { it.copy(datasetVersions = versions) }
        }
    }

    fun saveRapidApiKey(key: String) {
        viewModelScope.launch { apiKeyRepository.saveRapidApiKey(key) }
    }

    fun saveUnsplashKey(key: String) {
        viewModelScope.launch { apiKeyRepository.saveUnsplashKey(key) }
    }

    suspend fun buildBackupJson(): String =
        backupRepository.exportBackupJson()

    fun onExportFinished() {
        mutableUiState.update { it.copy(isBusy = false, message = "Còpia exportada correctament.") }
    }

    fun onOperationFailed(message: String) {
        pendingImportJson = null
        mutableUiState.update { it.copy(isBusy = false, message = message, pendingImportPreview = null) }
    }

    fun previewImport(json: String) {
        mutableUiState.update { it.copy(isBusy = true, message = null) }
        viewModelScope.launch {
            runCatching {
                backupRepository.previewImport(json)
            }.onSuccess { preview ->
                pendingImportJson = json
                mutableUiState.update { it.copy(isBusy = false, pendingImportPreview = preview) }
            }.onFailure { error ->
                onOperationFailed(error.message ?: "La còpia no es pot importar.")
            }
        }
    }

    fun confirmImport() {
        val json = pendingImportJson ?: return
        mutableUiState.update { it.copy(isBusy = true, message = null) }
        viewModelScope.launch {
            runCatching {
                backupRepository.importBackupJson(json)
            }.onSuccess {
                pendingImportJson = null
                mutableUiState.update {
                    it.copy(isBusy = false, message = "Còpia importada correctament.", pendingImportPreview = null)
                }
            }.onFailure { error ->
                onOperationFailed(error.message ?: "La còpia no es pot importar.")
            }
        }
    }

    fun dismissImportPreview() {
        pendingImportJson = null
        mutableUiState.update { it.copy(pendingImportPreview = null) }
    }

    fun clearMessage() {
        mutableUiState.update { it.copy(message = null) }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val backupRepository: BackupRepository,
        private val apiKeyRepository: ApiKeyRepository,
        private val datasetMetadataDao: DatasetMetadataDao,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(
                backupRepository = backupRepository,
                apiKeyRepository = apiKeyRepository,
                datasetMetadataDao = datasetMetadataDao,
            ) as T
    }
}

private fun String.toDatasetDisplayName(): String = when (this) {
    DatasetConstants.COUNTRIES_KEY -> "Països i territoris"
    DatasetConstants.AIRPORTS_KEY -> "Aeroports"
    DatasetConstants.AIRLINES_KEY -> "Aerolínies"
    DatasetConstants.AIRCRAFT_TYPES_KEY -> "Aeronaus"
    else -> this
}
