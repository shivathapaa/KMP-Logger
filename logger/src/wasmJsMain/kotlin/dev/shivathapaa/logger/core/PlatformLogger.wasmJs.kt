package dev.shivathapaa.logger.core

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

    private fun log(
        level: String,
        tag: String,
        message: String,
        throwable: Throwable?
    ) {
        val resolvedTag = tag.ifEmpty { this.tag }
        val line = "$level $resolvedTag: $message"
        if (throwable == null) {
            println(line)
        } else {
            println("$line\n${throwable.stackTraceToString().trimEnd()}")
        }
    }
}