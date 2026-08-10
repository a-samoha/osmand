package com.samos.osmand

import androidx.compose.ui.window.ComposeUIViewController
import com.samos.osmand.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = { initKoin() }
) { SharedCompose() }