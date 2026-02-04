package dev.shivathapaa.logger.api

import dev.shivathapaa.logger.core.AttrBuilder

/**
 * A logger providing structured logging capabilities.
 *
 * It allows logging messages with different [LogLevel]s, optional exceptions,
 * and structured attributes.
 */
class Logger internal constructor(
    private val delegate: StructuredLogger
) {
    /**
     * Logs a verbose message.
     *
     * @param throwable An optional exception.
     * @param attrs A builder for adding structured attributes.
     * @param message A lambda that returns the log message.
     */
    fun verbose(
        throwable: Throwable? = null,
        attrs: AttrBuilder.() -> Unit = {},
        message: () -> String
    ) = delegate.log(LogLevel.VERBOSE, throwable, attrs, message)

    /**
     * Logs a debug message.
     *
     * @param throwable An optional exception.
     * @param attrs A builder for adding structured attributes.
     * @param message A lambda that returns the log message.
     */
    fun debug(
        throwable: Throwable? = null,
        attrs: AttrBuilder.() -> Unit = {},
        message: () -> String
    ) = delegate.log(LogLevel.DEBUG, throwable, attrs, message)

    /**
     * Logs an info message.
     *
     * @param throwable An optional exception.
     * @param attrs A builder for adding structured attributes.
     * @param message A lambda that returns the log message.
     */
    fun info(
        throwable: Throwable? = null,
        attrs: AttrBuilder.() -> Unit = {},
        message: () -> String
    ) = delegate.log(LogLevel.INFO, throwable, attrs, message)

    /**
     * Logs a warning message.
     *
     * @param throwable An optional exception.
     * @param attrs A builder for adding structured attributes.
     * @param message A lambda that returns the log message.
     */
    fun warn(
        throwable: Throwable? = null,
        attrs: AttrBuilder.() -> Unit = {},
        message: () -> String
    ) = delegate.log(LogLevel.WARN, throwable, attrs, message)

    /**
     * Logs an error message.
     *
     * @param throwable An optional exception.
     * @param attrs A builder for adding structured attributes.
     * @param message A lambda that returns the log message.
     */
    fun error(
        throwable: Throwable? = null,
        attrs: AttrBuilder.() -> Unit = {},
        message: () -> String
    ) = delegate.log(LogLevel.ERROR, throwable, attrs, message)

    /**
     * Logs a fatal message. This will likely cause the application to terminate after processing.
     *
     * @param throwable An optional exception.
     * @param attrs A builder for adding structured attributes.
     * @param message A lambda that returns the log message.
     */
    fun fatal(
        throwable: Throwable? = null,
        attrs: AttrBuilder.() -> Unit = {},
        message: () -> String
    ) = delegate.log(LogLevel.FATAL, throwable, attrs, message)
}
