package com.samos.osmand

import android.app.Application
import com.samos.osmand.di.androidModule
import com.samos.osmand.di.sharedModule
import com.samos.osmand.logger.initLogger
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class OsmAndApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@OsmAndApp)
            modules(sharedModule, androidModule)
        }

        initLogger()
    }
}
