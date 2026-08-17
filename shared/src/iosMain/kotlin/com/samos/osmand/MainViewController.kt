package com.samos.osmand

import androidx.compose.ui.window.ComposeUIViewController
import com.samos.osmand.di.initKoin
import com.samos.osmand.logger.initIosLogger

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
        initIosLogger()
    }
) { SharedCompose() }
