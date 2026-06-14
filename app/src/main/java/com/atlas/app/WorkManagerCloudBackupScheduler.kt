package com.atlas.app

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.atlas.domain.repository.CloudBackupScheduler
import com.atlas.domain.repository.CloudBackupWorkStatus
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class WorkManagerCloudBackupScheduler(
    context: Context,
) : CloudBackupScheduler {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    override fun observeWorkStatus(): Flow<CloudBackupWorkStatus> {
        val manualStatus = workManager.getWorkInfosForUniqueWorkFlow(MANUAL_WORK_NAME)
            .map { workInfos -> workInfos.toCloudBackupWorkStatus(includeQueued = true) }
        val automaticStatus = workManager.getWorkInfosForUniqueWorkFlow(PERIODIC_WORK_NAME)
            .map { workInfos -> workInfos.toCloudBackupWorkStatus(includeQueued = false) }
        return combine(manualStatus, automaticStatus) { manual, automatic ->
            when {
                manual == CloudBackupWorkStatus.RUNNING ||
                    automatic == CloudBackupWorkStatus.RUNNING -> CloudBackupWorkStatus.RUNNING
                manual == CloudBackupWorkStatus.QUEUED -> CloudBackupWorkStatus.QUEUED
                else -> CloudBackupWorkStatus.IDLE
            }
        }.distinctUntilChanged()
    }

    override fun scheduleMonthly() {
        val request = PeriodicWorkRequestBuilder<AtlasCloudBackupWorker>(
            30,
            TimeUnit.DAYS,
        )
            .setConstraints(AUTOMATIC_BACKUP_CONSTRAINTS)
            .setInitialDelay(30, TimeUnit.DAYS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    override fun cancel() {
        workManager.cancelUniqueWork(PERIODIC_WORK_NAME)
        workManager.cancelUniqueWork(MANUAL_WORK_NAME)
    }

    override fun runNow() {
        val request = OneTimeWorkRequestBuilder<AtlasCloudBackupWorker>()
            .setConstraints(MANUAL_BACKUP_CONSTRAINTS)
            .build()
        workManager.enqueueUniqueWork(
            MANUAL_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    private companion object {
        const val PERIODIC_WORK_NAME = "atlas-cloud-backup-monthly"
        const val MANUAL_WORK_NAME = "atlas-cloud-backup-manual"
        val AUTOMATIC_BACKUP_CONSTRAINTS: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            .build()
        val MANUAL_BACKUP_CONSTRAINTS: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build()
    }
}

private fun List<WorkInfo>.toCloudBackupWorkStatus(
    includeQueued: Boolean,
): CloudBackupWorkStatus = when {
    any { it.state == WorkInfo.State.RUNNING } -> CloudBackupWorkStatus.RUNNING
    includeQueued && any {
        it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.BLOCKED
    } -> CloudBackupWorkStatus.QUEUED
    else -> CloudBackupWorkStatus.IDLE
}
