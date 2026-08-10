package com.osmand.myapplication

import android.app.Application
import com.osmand.myapplication.di.initKoin
import org.koin.android.ext.koin.androidContext

class OsmAndApp : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@OsmAndApp)
        }
    }
}
