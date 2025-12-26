package com.agropay

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform