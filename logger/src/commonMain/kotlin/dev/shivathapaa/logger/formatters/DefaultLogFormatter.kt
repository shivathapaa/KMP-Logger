package dev.shivathapaa.logger.formatters

import dev.shivathapaa.logger.core.LogEvent

/**
 * A standard formatter that provides a balance between readability and information.
 *
 * It includes the log level, logger name, message, and throwable stack trace if present.
 *
 * @property showEmoji Whether to include the log level emoji in the output.
 */
internal class DefaultLogFormatter(private val showEmoji: Boolean) : LogEventFormatter {
    override fun format(event: LogEvent): String = buildString {
        val emoji = if (showEmoji) "${event.level.emoji} " else ""
        append(emoji)
        append("[${event.level.name}] ")
        append(event.loggerName)

        event.message?.takeIf { it.isNotBlank() }?.let {
            append(": ")
            append(it)
        }

        event.throwable?.let {
            append('\n')
            append(it.stackTraceToString())
        }
    }
}