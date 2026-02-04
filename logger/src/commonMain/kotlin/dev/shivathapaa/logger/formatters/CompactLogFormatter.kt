package dev.shivathapaa.logger.formatters

import dev.shivathapaa.logger.core.LogEvent

/**
 * A compact formatter that produces single-line log messages (except for stack traces).
 * Useful for console output where space is limited.
 *
 * @property showEmoji Whether to include the log level emoji in the output.
 */
internal class CompactLogFormatter(private val showEmoji: Boolean) : LogEventFormatter {
    override fun format(event: LogEvent): String = buildString {
        val emoji = if (showEmoji) "${event.level.emoji} " else ""
        append(emoji)
        append(event.level.name)
        append(" | ")
        append(event.loggerName)

        event.message?.let {
            append(" - ")
            append(it)
        }

        if (event.attributes.isNotEmpty()) {
            append(" ")
            append(formatAttributes(event.attributes))
        }

        event.throwable?.let {
            append("\n")
            append(it.stackTraceToString())
        }
    }

    private fun formatAttributes(attrs: Map<String, Any?>): String =
        attrs.entries.joinToString(
            prefix = "[",
            postfix = "]",
            separator = " "
        ) { (k, v) -> "$k=$v" }
}