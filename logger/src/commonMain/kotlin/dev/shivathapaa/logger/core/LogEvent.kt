package dev.shivathapaa.logger.core

import dev.shivathapaa.logger.api.LogLevel

/**
 * Represents a single log event captured at a point in time.
 *
 * A [LogEvent] is an immutable snapshot assembled by [DefaultLogger] and
 * passed through the [LogPipeline] to each configured [dev.shivathapaa.logger.sink.LogSink].
 * Sinks and formatters should treat all fields as read-only.
 *
 * @property level The severity level of the event.
 * @property loggerName The name of the logger that created the event, used
 *   as the tag in formatted output.
 * @property message The log message. May be `null` if only structured
 *   attributes were provided.
 * @property throwable An optional exception associated with the event.
 * @property attributes Structured key-value pairs attached at the call site
 *   via the [AttrBuilder] DSL.
 * @property context The ambient [LogContext] active at the time the event
 *   was created.
 * @property thread The name of the thread on which the event was created.
 * @property timestamp The wall-clock time at which the event was created,
 *   as milliseconds since the Unix epoch.
 */
data class LogEvent(
    val level: LogLevel,
    val loggerName: String,
    val message: String?,
    val throwable: Throwable?,
    val attributes: Map<String, Any?>,
    val context: LogContext,
    val thread: String,
    val timestamp: Long
)