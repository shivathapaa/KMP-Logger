package dev.shivathapaa.logger.api

import dev.shivathapaa.logger.core.AttrBuilder

class Logger internal constructor(
    private val delegate: StructuredLogger
) {
    fun verbose(
        throwable: Throwable? = null,
        attrs: AttrBuilder.() -> Unit = {},
        message: () -> String
    ) = delegate.log(LogLevel.VERBOSE, throwable, attrs, message)

    fun debug(
        throwable: Throwable? = null,
        attrs: AttrBuilder.() -> Unit = {},
        message: () -> String
    ) = delegate.log(LogLevel.DEBUG, throwable, attrs, message)

    fun info(
        throwable: Throwable? = null,
        attrs: AttrBuilder.() -> Unit = {},
        message: () -> String
    ) = delegate.log(LogLevel.INFO, throwable, attrs, message)

    fun warn(
        throwable: Throwable? = null,
        attrs: AttrBuilder.() -> Unit = {},
        message: () -> String
    ) = delegate.log(LogLevel.WARN, throwable, attrs, message)

    fun error(
        throwable: Throwable? = null,
        attrs: AttrBuilder.() -> Unit = {},
        message: () -> String
    ) = delegate.log(LogLevel.ERROR, throwable, attrs, message)

    fun fatal(
        throwable: Throwable? = null,
        attrs: AttrBuilder.() -> Unit = {},
        message: () -> String
    ) = delegate.log(LogLevel.FATAL, throwable, attrs, message)
}
