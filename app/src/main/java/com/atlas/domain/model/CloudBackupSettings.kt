package com.atlas.domain.model

data class CloudBackupSettings(
    val folderUri: String? = null,
    val folderName: String? = null,
    val enabled: Boolean = false,
    val lastSuccessfulBackupAt: String? = null,
    val lastError: String? = null,
)
