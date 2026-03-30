package dev.shivathapaa.logger.core

import platform.Foundation.NSThread

internal actual fun currentThreadName(): String =
    NSThread.currentThread.name ?: "unknown"