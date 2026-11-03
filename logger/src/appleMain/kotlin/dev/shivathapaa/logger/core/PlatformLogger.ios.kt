package dev.shivathapaa.logger.core

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSLog
import platform.Foundation.NSString
import platform.Foundation.create

internal actual class PlatformLogger actual constructor(private val tag: String) {
    actual fun v(message: String, tag: String, throwable: Throwable?) {
        log("VERBOSE", tag, message, throwable)
    }

    actual fun d(message: String, tag: String, throwable: Throwable?) {
        log("DEBUG", tag, message, throwable)
    }

    actual fun i(message: String, tag: String, throwable: Throwable?) {
        log("INFO", tag, message, throwable)
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

    @OptIn(BetaInteropApi::class)
    private fun log(
        level: String,
        tag: String,
        message: String,
        throwable: Throwable?
    ) {
        val resolvedTag = tag.ifEmpty { this.tag }
        val line = "$level $resolvedTag: $message"
        val text = if (throwable == null) line else "$line\n${throwable.stackTraceToString().trimEnd()}"
        // %@ with an NSString keeps the text intact; %s decodes it with the system C-string
        // encoding, which garbles anything outside ASCII (emoji, accents, non-Latin scripts).
        NSLog("%@", NSString.create(string = text))
    }
}