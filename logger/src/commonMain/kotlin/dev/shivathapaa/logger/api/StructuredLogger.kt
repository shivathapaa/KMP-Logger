package dev.shivathapaa.logger.api

import dev.shivathapaa.logger.core.AttrBuilder

/**
 * Interface defining the core logging capability for structured loggers.
 */
interface StructuredLogger {
    /**
     * Logs a message with a specific level and metadata.
     *
     * @param level The log level.
     * @param throwable An optional exception.
     * @param attrs A builder for adding structured attributes.
     * @param message A lambda providing the log message.
     */
    fun log(
        level: LogLevel,
        throwable: Throwable? = null,
        attrs: AttrBuilder.() -> Unit = {},
        message: () -> String
    )
}
