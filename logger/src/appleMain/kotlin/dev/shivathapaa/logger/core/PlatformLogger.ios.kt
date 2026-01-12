package dev.shivathapaa.logger.core

import platform.Foundation.NSLog

internal actual class PlatformLogger actual constructor(private val tag: String) {
    actual fun v(message: String, tag: String) {
        log("VERBOSE", tag, message, null)
    }

    actual fun d(message: String, tag: String) {
        log("DEBUG", tag, message, null)
    }

    actual fun i(message: String, tag: String) {
        log("INFO", tag, message, null)
    }

    actual fun w(message: String, tag: String, throwable: Throwable?) {
        log("WARN", tag, message, throwable)
    }

    actual fun e(message: String, tag: String, throwable: Throwable?) {
        log("ERROR", tag, message, throwable)
    }

    actual fun wtf(message: String, tag: String, throwable: Throwable?) {
        log("FATAL", tag, message, throwable)
    }

    private fun log(
        level: String,
        tag: String,
        message: String,
        throwable: Throwable?
    ) {
        val resolvedTag = tag.ifEmpty { this.tag }

        if (throwable != null) {
            NSLog(
                format = "%s %s: %s\n%s",
                level,
                resolvedTag,
                message,
                throwable.stackTraceToString()
            )
        } else {
            NSLog("%s %s: %s", level, resolvedTag, message)
        }
    }
}