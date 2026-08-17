package com.samos.osmand.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.samos.osmand.domain.model.DownloadStatus
import com.samos.osmand.domain.network.MapDownloadManager
import com.samos.osmand.logger.LOGGER_TAG
import com.samos.osmand.shared.R
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject

class MapDownloadService : Service() {

    private val downloadManager: MapDownloadManager by inject()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val notificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onCreate() {
        super.onCreate()
        Napier.d(tag = LOGGER_TAG) { "MapDownloadService: onCreate" }
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Napier.d(tag = LOGGER_TAG) { "MapDownloadService: onStartCommand" }
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        val initialNotification =
            buildNotification("Підготовка до завантаження...", progress = 0, isIndeterminate = true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Napier.d(tag = LOGGER_TAG) { "MapDownloadService: startForeground Android 10+" }
            startForeground(
                NOTIFICATION_ID,
                initialNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            Napier.d(tag = LOGGER_TAG) { "MapDownloadService: startForeground" }
            startForeground(NOTIFICATION_ID, initialNotification)
        }
        observeKmrManager()

        return START_STICKY
    }

    private fun observeKmrManager() {
        downloadManager.downloadStates
            .onEach { states ->
                // Check if there is a map loading right now.
                val activeDownload =
                    states.entries.firstOrNull { it.value is DownloadStatus.Downloading }

                if (activeDownload != null) {
                    val regionNode = activeDownload.key
                    val status = activeDownload.value as DownloadStatus.Downloading

                    updateNotification(
                        contentText = getString(R.string.downloading, regionNode.name),
                        progress = status.progress,
                        isIndeterminate = false
                    )
                } else {
                    val hasItemsInQueue = states.values.any { it is DownloadStatus.InQueue }
                    if (hasItemsInQueue) {
                        updateNotification(
                            contentText = getString(R.string.waiting_for_downloading),
                            progress = 0,
                            isIndeterminate = true
                        )
                    }
                }
            }
            .launchIn(serviceScope)
    }

    private fun updateNotification(contentText: String, progress: Int, isIndeterminate: Boolean) {
        val updatedNotification = buildNotification(contentText, progress, isIndeterminate)
        notificationManager.notify(NOTIFICATION_ID, updatedNotification)
    }

    private fun buildNotification(
        contentText: String,
        progress: Int,
        isIndeterminate: Boolean
    ): Notification {
        val rootIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, rootIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.downloading_maps))
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, isIndeterminate)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.kmr_map_download_notifications),
            NotificationManager.IMPORTANCE_LOW // Without sound
        ).apply {
            description =
                getString(R.string.displays_the_current_progress_of_map_file_downloads_in_the_background)
        }
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent): IBinder? {
        Napier.d(tag = LOGGER_TAG) { "MapDownloadService: OnBind intent $intent" }
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        private const val NOTIFICATION_ID = 2026
        private const val CHANNEL_ID = "map_downloads_channel_id"
    }
}
