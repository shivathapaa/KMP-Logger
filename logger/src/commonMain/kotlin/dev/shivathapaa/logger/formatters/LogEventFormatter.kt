package dev.shivathapaa.logger.formatters

import dev.shivathapaa.logger.core.LogEvent

/**
 * Interface for formatting a [LogEvent] into a [String].
 *
 * Implementations of this interface define how log events are represented in the output
 * (e.g., as JSON, plain text, or pretty-printed text).
 *
 * Example of a custom formatter:
 * ```kotlin
 * val myFormatter = LogEventFormatter { event ->
 *     "[\${event.level}] \${event.loggerName}: \${event.message}"
 * }
 * ```
 */
fun interface LogEventFormatter {
    /**
     * Formats the given [event] into a string representation.
     *
     * @param event The log event to format.
     * @return A string representation of the log event.
     */
    fun format(event: LogEvent): String
}