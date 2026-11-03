package dev.shivathapaa.logger.core

import platform.Foundation.NSThread

internal actual fun currentThreadName(): String {
    val thread = NSThread.currentThread
    if (thread.isMainThread) return "main"
    // Most GCD and coroutine worker threads are unnamed; label them rather than claim "main".
    return thread.name?.takeIf { it.isNotEmpty() } ?: "background"
}