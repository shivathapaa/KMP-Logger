package dev.shivathapaa.logger.core

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.LOG_CONS
import platform.posix.LOG_CRIT
import platform.posix.LOG_DEBUG
import platform.posix.LOG_ERR
import platform.posix.LOG_INFO
import platform.posix.LOG_PID
import platform.posix.LOG_USER
import platform.posix.LOG_WARNING
import platform.posix.openlog
import platform.posix.syslog

@OptIn(ExperimentalForeignApi::class)
internal actual class PlatformLogger actual constructor(private val tag: String) {
    companion object {
        init {
            openlog("KMPLogger", LOG_PID or LOG_CONS, LOG_USER)
        }
    }

    actual fun v(message: String, tag: String) {
        syslog(LOG_DEBUG, "VERBOSE $tag: $message")
    }

    actual fun d(message: String, tag: String) {
        syslog(LOG_DEBUG, "DEBUG $tag: $message")
    }

    actual fun i(message: String, tag: String) {
        syslog(LOG_INFO, "INFO $tag: $message")
    }

    actual fun w(message: String, tag: String, throwable: Throwable?) {
        syslog(
            LOG_WARNING,
            buildMessage("WARN", tag, message, throwable)
        )
    }

    actual fun e(message: String, tag: String, throwable: Throwable?) {
        syslog(
            LOG_ERR,
            buildMessage("ERROR", tag, message, throwable)
        )
    }

    actual fun wtf(message: String, tag: String, throwable: Throwable?) {
        syslog(
            LOG_CRIT,
            buildMessage("FATAL", tag, message, throwable)
        )
    }

    private fun buildMessage(
        level: String,
        tag: String,
        message: String,
        throwable: Throwable?
    ): String {
        val resolvedTag = tag.ifEmpty { this.tag }
        return if (throwable != null) {
            "$level $resolvedTag: $message\n${throwable.stackTraceToString()}"
        } else {
            "$level $resolvedTag: $message"
        }
    }
}
