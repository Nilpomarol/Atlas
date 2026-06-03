package com.atlas.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

data class SettingsUiState(
    val isBusy: Boolean = false,
    val message: String? = null,
    val pendingImportPreview: BackupImportPreview? = null,
)

class SettingsViewModel(
    private val backupRepository: BackupRepository,
    private val apiKeyRepository: ApiKeyRepository,
) : ViewModel() {

    val rapidApiKey: StateFlow<String> = apiKeyRepository.observeRapidApiKey()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun saveRapidApiKey(key: String) {
        viewModelScope.launch { apiKeyRepository.saveRapidApiKey(key) }
    }
    private val mutableUiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = mutableUiState.asStateFlow()

    private var pendingImportJson: String? = null

    suspend fun buildBackupJson(): String =
        backupRepository.exportBackupJson()

    fun onExportFinished() {
        mutableUiState.update {
            it.copy(
                isBusy = false,
                message = "Còpia exportada correctament.",
            )
        }
    }

    fun onOperationFailed(message: String) {
        pendingImportJson = null
        mutableUiState.update {
            it.copy(
                isBusy = false,
                message = message,
                pendingImportPreview = null,
            )
        }
    }

    fun previewImport(json: String) {
        mutableUiState.update { it.copy(isBusy = true, message = null) }
        viewModelScope.launch {
            runCatching {
                backupRepository.previewImport(json)
            }.onSuccess { preview ->
                pendingImportJson = json
                mutableUiState.update {
                    it.copy(
                        isBusy = false,
                        pendingImportPreview = preview,
                    )
                }
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
                    it.copy(
                        isBusy = false,
                        message = "Còpia importada correctament.",
                        pendingImportPreview = null,
                    )
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
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(
                backupRepository = backupRepository,
                apiKeyRepository = apiKeyRepository,
            ) as T
    }
}
