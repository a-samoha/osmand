package com.samos.osmand.logger

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

fun initIosLogger() {
    Napier.base(DebugAntilog())
}

const val LOGGER_TAG = "OsmAnd"