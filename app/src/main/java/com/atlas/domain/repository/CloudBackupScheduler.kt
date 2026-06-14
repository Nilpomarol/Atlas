package com.atlas.domain.repository

import kotlinx.coroutines.flow.Flow

enum class CloudBackupWorkStatus {
    IDLE,
    QUEUED,
    RUNNING,
}

interface CloudBackupScheduler {
    fun observeWorkStatus(): Flow<CloudBackupWorkStatus>
    fun scheduleMonthly()
    fun cancel()
    fun runNow()
}
