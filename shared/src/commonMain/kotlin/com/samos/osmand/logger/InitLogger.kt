package com.samos.osmand.logger

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

fun initLogger() {
    Napier.base(DebugAntilog())
}

const val LOGGER_TAG = "OsmAnd"