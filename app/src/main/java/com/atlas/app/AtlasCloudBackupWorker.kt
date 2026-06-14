package com.atlas.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.atlas.data.backup.CloudBackupExportResult

class AtlasCloudBackupWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val application = applicationContext as? AtlasApplication ?: return Result.failure()
        setForeground(createForegroundInfo())
        return when (val result = application.container.cloudBackupExporter.export()) {
            CloudBackupExportResult.Success -> {
                showResultNotification(success = true)
                Result.success()
            }
            CloudBackupExportResult.Disabled -> Result.success()
            is CloudBackupExportResult.Failure -> {
                if (result.retryable && runAttemptCount < MAX_RETRY_ATTEMPTS) {
                    Result.retry()
                } else {
                    showResultNotification(success = false)
                    Result.failure()
                }
            }
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        createNotificationChannels()
        val cancelIntent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentTitle("Atlas")
            .setContentText("Desant una còpia de seguretat al núvol…")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Cancel·la",
                cancelIntent,
            )
            .build()
        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        } else {
            0
        }
        return ForegroundInfo(NOTIFICATION_ID, notification, serviceType)
    }

    private fun showResultNotification(success: Boolean) {
        runCatching {
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@runCatching
            }
            createNotificationChannels()
            val openAppIntent = Intent(applicationContext, com.atlas.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val contentIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val notification = NotificationCompat.Builder(applicationContext, RESULT_CHANNEL_ID)
                .setSmallIcon(
                    if (success) {
                        android.R.drawable.stat_sys_upload_done
                    } else {
                        android.R.drawable.stat_notify_error
                    },
                )
                .setContentTitle(
                    if (success) {
                        "Còpia al núvol completada"
                    } else {
                        "No s'ha pogut crear la còpia"
                    },
                )
                .setContentText(
                    if (success) {
                        "La còpia de seguretat d'Atlas s'ha desat correctament."
                    } else {
                        "Obre Atlas per revisar l'accés a la carpeta o tornar-ho a provar."
                    },
                )
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            applicationContext.getSystemService(NotificationManager::class.java)
                .notify(RESULT_NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Còpies de seguretat",
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
        manager.createNotificationChannel(
            NotificationChannel(
                RESULT_CHANNEL_ID,
                "Resultat de les còpies",
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
    }

    private companion object {
        const val MAX_RETRY_ATTEMPTS = 2
        const val NOTIFICATION_CHANNEL_ID = "atlas_cloud_backup"
        const val RESULT_CHANNEL_ID = "atlas_cloud_backup_results"
        const val NOTIFICATION_ID = 2401
        const val RESULT_NOTIFICATION_ID = 2402
    }
}
