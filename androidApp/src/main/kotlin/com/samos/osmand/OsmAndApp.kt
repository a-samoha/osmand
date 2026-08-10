package com.samos.osmand

import android.app.Application
import com.samos.osmand.di.initKoin
import org.koin.android.ext.koin.androidContext

class OsmAndApp : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@OsmAndApp)
        }
    }
}
