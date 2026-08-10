package com.samos.osmand

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform