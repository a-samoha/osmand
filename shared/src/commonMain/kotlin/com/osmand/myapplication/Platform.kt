package com.osmand.myapplication

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform