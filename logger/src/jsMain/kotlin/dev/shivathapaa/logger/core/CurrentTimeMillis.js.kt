package dev.shivathapaa.logger.core

import kotlin.js.Date

internal actual fun currentTimeMillis(): Long = Date().getTime().toLong()