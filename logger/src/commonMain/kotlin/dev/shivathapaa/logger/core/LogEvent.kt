package dev.shivathapaa.logger.core

import dev.shivathapaa.logger.api.LogLevel

/**
 * Represents a single log event.
 *
 * @property level The log level of the event.
 * @property loggerName The name of the logger that created the event.
 * @property message The log message.
 * @property throwable The throwable associated with the log event, if any.
 * @property attributes A map of attributes associated with the log event.
 * @property context The log context at the time the event was created.
 * @property thread The name of the thread on which the event was created. (Currently not used)
 * @property timestamp The timestamp of the event, in milliseconds. (Currently not used)
 */
data class LogEvent(
    val level: LogLevel,
    val loggerName: String,
    val message: String?,
    val throwable: Throwable?,
    val attributes: Map<String, Any?>,
    val context: LogContext,
    val thread: String,
    val timestamp: Long? = null
)