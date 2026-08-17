package com.samos.osmand.domain.service

import android.content.Context
import android.content.Intent
import com.samos.osmand.data.service.MapDownloadService

class AndroidServiceTracker(private val context: Context) : ServiceTracker {

    override fun startDownloadService() {
        val intent = Intent(context, MapDownloadService::class.java).apply {
            action = MapDownloadService.ACTION_START
        }
        context.startForegroundService(intent)
    }

    override fun stopDownloadService() {
        val intent = Intent(context, MapDownloadService::class.java).apply {
            action = MapDownloadService.ACTION_STOP
        }
        context.startService(intent)
    }
}
