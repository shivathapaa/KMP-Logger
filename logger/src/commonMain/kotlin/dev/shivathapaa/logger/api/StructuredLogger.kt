package dev.shivathapaa.logger.api

import dev.shivathapaa.logger.core.AttrBuilder

interface StructuredLogger {
    fun log(
        level: LogLevel,
        throwable: Throwable? = null,
        attrs: AttrBuilder.() -> Unit = {},
        message: () -> String
    )
}
