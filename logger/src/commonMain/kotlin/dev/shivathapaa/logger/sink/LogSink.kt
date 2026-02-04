package dev.shivathapaa.logger.sink

import dev.shivathapaa.logger.core.LogEvent

/**
 * Interface representing a destination for log events.
 *
 * Sinks are responsible for taking a [LogEvent] and writing it to a specific output,
 * such as the console, a file, a remote server, or an analytics service.
 *
 * ### Example: Custom File Sink
 * ```kotlin
 * class FileSink(private val filePath: String) : LogSink {
 *     override fun emit(event: LogEvent) {
 *         val line = "[\${event.level}] \${event.loggerName}: \${event.message}\n"
 *         // Write 'line' to file at 'filePath'
 *     }
 *
 *     override fun flush() {
 *         // Ensure all buffered data is written to disk
 *     }
 * }
 * ```
 *
 * ### Example: Sanitizing Sink (Middleware)
 * ```kotlin
 * class SanitizingSink(private val delegate: LogSink) : LogSink {
 *     private val sensitiveKeys = setOf("password", "apiKey", "token")
 *
 *     override fun emit(event: LogEvent) {
 *         val sanitizedAttrs = event.attributes.mapValues { (key, value) ->
 *             if (key in sensitiveKeys) "***REDACTED***" else value
 *         }
 *         val sanitizedEvent = event.copy(attributes = sanitizedAttrs)
 *         delegate.emit(sanitizedEvent)
 *     }
 *
 *     override fun flush() = delegate.flush()
 * }
 * ```
 */
interface LogSink {
    /**
     * Emits a log event to the output destination.
     *
     * @param event The log event to emit.
     */
    fun emit(event: LogEvent)

    /**
     * Flushes any buffered log events.
     * Called automatically when a FATAL log occurs or during shutdown.
     */
    fun flush() {}
}