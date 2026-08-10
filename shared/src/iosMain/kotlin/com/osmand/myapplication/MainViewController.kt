package com.osmand.myapplication

import androidx.compose.ui.window.ComposeUIViewController
import com.osmand.myapplication.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = { initKoin() }
) { SharedCompose() }