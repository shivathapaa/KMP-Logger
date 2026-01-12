package dev.shivathapaa.logger.core

internal actual class PlatformLogger actual constructor(private val tag: String) {
    actual fun v(message: String, tag: String) {
        console.log(format("VERBOSE", tag, message))
    }

    actual fun d(message: String, tag: String) {
        console.log(format("DEBUG", tag, message))
    }

    actual fun i(message: String, tag: String) {
        console.info(format("INFO", tag, message))
    }

    actual fun w(message: String, tag: String, throwable: Throwable?) {
        if (throwable != null) {
            console.warn(format("WARN", tag, message), throwable)
        } else {
            console.warn(format("WARN", tag, message))
        }
    }

    actual fun e(message: String, tag: String, throwable: Throwable?) {
        if (throwable != null) {
            console.error(format("ERROR", tag, message), throwable)
        } else {
            console.error(format("ERROR", tag, message))
        }
    }

    actual fun wtf(message: String, tag: String, throwable: Throwable?) {
        if (throwable != null) {
            console.error(format("FATAL", tag, message), throwable)
        } else {
            console.error(format("FATAL", tag, message))
        }
    }

    private fun format(level: String, tag: String, message: String): String {
        val resolvedTag = tag.ifEmpty { this.tag }
        return "$level $resolvedTag: $message"
    }
}
