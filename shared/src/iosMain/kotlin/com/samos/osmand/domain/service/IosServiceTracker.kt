package com.samos.osmand.domain.service

class IosServiceTracker : ServiceTracker {
    override fun startDownloadService() { /* iOS implements with Background Fetch/Tasks */ }
    override fun stopDownloadService() { /* todo */ }
}
